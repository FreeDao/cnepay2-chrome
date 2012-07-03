package com.cnepay.android.pos2;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tangye.android.dialog.SwipeDialogController;
import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.POSEncrypt;
import com.tangye.android.iso8583.POSHelper;
import com.tangye.android.iso8583.POSSession;
import com.tangye.android.iso8583.protocol.ResetPasswdMessage;
import com.tangye.android.utils.CardInfo;
import com.tangye.android.utils.PublicHelper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ResetPasswdActivity extends UIBaseActivity implements
		View.OnClickListener, OnCancelListener {

	private SwipeDialogController dialog;
	private TextView card;
	private EditText txtPhone;
	private Button btnReset;
	private ResetPasswdMessage s;
	private Handler mHandler;
	private ProgressDialog progressDialog;

	private static final String TAG = "ResetPasswdActivity";
	private static final int ENABLE_TIMEOUT = 1000;
	private static final int SUCCESS = 0;
    private static final int FAILURE = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resetpswd);
		setTitle("重置密码");
		btnSubmit.setOnClickListener(this);
		int[] attrs = new int[] { R.layout.swipe_dialog, R.style.dialog,
				R.id.dialog_anim, R.id.dialog_note };
		dialog = new SwipeDialogController(this, attrs);
		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				stopSwipe();
			}
		});

		card = (TextView) this.findViewById(R.id.resetpd_account_number);
		card.setOnClickListener(this);

		txtPhone = (EditText) findViewById(R.id.resetpd_phone);

		btnReset = (Button) findViewById(R.id.resetpd_submit);
		btnReset.setOnClickListener(this);
		mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                case SUCCESS:
                    if(progressDialog != null && msg.obj != null) {
                    	// register successfully
                        progressDialog.dismiss();
                        progressDialog = null; // For not fade card number
                        // TODO alert dialog
                        AlertDialog.Builder builder = PublicHelper.getAlertDialogBuilder(ResetPasswdActivity.this);
        		        builder.setTitle("密码重置成功")
        		        .setIcon(android.R.drawable.ic_dialog_info)
        		        .setMessage("您的新密码为您的身份证后6位，请尽快登录账户并修改您的密码，以保障您的账户安全。")
        		        .setOnCancelListener(new OnCancelListener() {
							public void onCancel(DialogInterface dialog) {
								finish();
							}
        		        })
        		        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        		            public void onClick(DialogInterface dialog, int which) {
        		               finish();
        		            }
        		        })
        		        .show();
                    }
                    break;
                case FAILURE:
                    if(progressDialog != null) {
                        progressDialog.cancel();
                        errText((String)msg.obj);
                    }
                    break;
                }
                btnReset.setEnabled(true);
            }
        };
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        if(progressDialog != null) {
            progressDialog.cancel();
        }
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.resetpd_account_number:
			if (this.isPlugged()) {
				dialog.setText("请刷卡...");
				dialog.show();
				this.startSwipe();
			} else {
				errText("请插入读卡器");
			}
			break;
		case R.id.resetpd_submit:
			submit();
			break;
		}
	}

	@Override
	public void onComplete(String cn) {
		dialog.dismiss();
		card.setText(cn);
	}

	@Override
	public void onError(int err) {
		// 刷卡时，插拔卡可能会引起decodeError
		boolean isDecodeError = true;
		boolean isInterrupt = false;
		if (this.isPlugged()) {
			switch (err) {
			case E_API2_UNRESOLVED:
				errText("无法解析，请重试或者使用其他Android手机");
				break;
			case E_API2_FASTORSLOW:
				errText("刷卡过快或过慢，请重试");
				break;
			case E_API2_UNSTABLE:
				errText("刷卡不稳定，请重试");
				break;
			case E_API2_INVALID_DEVICE:
				errText("非法读卡器，请使用正规读卡器");
				break;
			case E_API2_INTERRUPT:
				isInterrupt = true;
			default:
				isDecodeError = false;
			}
		} else {
			switch (err) {
			case E_API2_INVALID_DEVICE:
				errText("无法识别该读卡器，请选择新的读卡器或者重新拔插");
				card.setHint("请拔掉此读卡器，重新插入");
				break;
			case E_API2_INTERRUPT:
				isInterrupt = true;
				break;
			}
		}
		if (isDecodeError || isInterrupt) {
			dialog.dismiss();
		}
	}
	
	@Override
	public void onSwipe() {
		dialog.setText("接受刷卡数据...");
	}

	@Override
	public void onDecoding() {
		dialog.setText("正在解码...");
	}

	@Override
	public void onPlugin() {
		super.onPlugin(); // UIBASE action
		card.setHint("请点击此处准备刷卡");
	}

	@Override
	public void onPlugout() {
		super.onPlugout(); // UIBASE action
		card.setHint("请插入读卡器");
	}

	@Override
	public void onTimeout() {
		errText("未检测到刷卡动作");
		dialog.dismiss();
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		progressDialog = null;
        if(s != null) {
            s.stop();
            s = null;
        }
	}

	/************ private function *************/

	private void verify_failure(View v, String err) {
		if (v != null) {
			v.requestFocus();
		}
		btnReset.postDelayed(new Runnable() {
			public void run() {
				btnReset.setEnabled(true);
			}
		}, ENABLE_TIMEOUT);
		errText(err);
		return;
	}

	private void errText(String txt) {
		Toast t = Toast.makeText(this, txt, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}

	// test cell phone number, and verify
	// http://www.v2000.net/hao.htm to check the begin number
	private boolean testPhone(String phone) {
		int[] begin = { 13, 14, 15, 18 };
		// only for cellphone, so length should be 11
		if (phone.length() != 11)
			return false;
		for (int i = 0; i < begin.length; i++) {
			if (phone.startsWith(String.valueOf(begin[i]))) {
				return true;
			}
		}
		return false;
	}
	
	private void submit() {
		InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(getCurrentFocus()
				.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		btnReset.setEnabled(false);
		final String phone = txtPhone.getText().toString();

		if (phone.length() == 0) {
			verify_failure(txtPhone, "请输入手机号");
			return;
		}
		if (!testPhone(phone)) {
			verify_failure(txtPhone, "手机号输入有误，请检查是否为手机号");
			return;
		}
		final POSEncrypt POS = POSHelper.getPOSEncrypt(this, phone);
		if(POS == null || !POS.isInitializedRanCode()){
			verify_failure(txtPhone, "并未绑定该手机信息，请使用无法登陆功能");
			return;
		}
		if (card.getText().length() == 0 || ci == null) {
			verify_failure(null, "卡号信息无效，请刷注册卡号");
			return;
		}
		// FIXME TODO cardInfo
		final CardInfo cardInfo = ci;
		final String account = cardInfo.getCard(false);
		
		progressDialog = ProgressDialog.show(this, // context 
				"", // title 
				"密码重置中...", // message 
				true, //进度是否是不确定的，这只和创建进度条有关 
				true,
				this);
		(new Thread() {
			public void run() {
			    s = new ResetPasswdMessage();
	            s.setCardNumber_2(account)
	             .setSource_16(getSource())
	             .setTerminalMark_41(POS.getPOSDecrypt(POS.TERMINAL))
	             .setUserMark_42(POS.getPOSDecrypt(POS.USERMARK))
	             .setOtherInfo_63(phone, POS.getPOSDecrypt(POS.RANDOMCODE));
	            POS.close();
	            boolean isOK = false;
	            String error = "";
	            try {
	                if(!s.isBitmapValid()) throw new RuntimeException("BitmapError");
	                IsoMessage resp = s.request();
	                if(resp != null) {
	                	String statusCode = resp.getField(39).toString();
	                	if (statusCode.equals("00")) {
                			Log.i(TAG, "应答码：" + resp.getField(39).toString());
							Log.i(TAG, "终端标识码：" + resp.getField(41).toString());
							Log.i(TAG, "受卡方标识码：" + resp.getField(42).toString());
		                    POSEncrypt POS = POSHelper.getPOSEncrypt(ResetPasswdActivity.this, phone);
		                    //TODO POS reset passwd;
	        				POS.close();
	        				error = account; // differ with stop by user
		                    isOK = true;
	                	} else {
	                		error = getError(statusCode);
	                	}
	                } else {
	                	// Manually stop client from user's aspect
	                    error = null;
	                    isOK = true;
	                }
	            } catch (SocketTimeoutException e) {
                    error = "连接超时，请确保网络稳定性";
                    e.printStackTrace();
                } catch (IllegalStateException e) {
	                error = "严重错误，请咨询售后";
	                e.printStackTrace();
	            } catch (UnknownHostException e) {
	                error = "无法连接主机，请检查连接性";
	                e.printStackTrace();
	            } catch (IOException e) {
	                error = "请检查手机的网络信号";
	                e.printStackTrace();
	            } catch (Exception e) {
	            	error = "报文错误，请联系客服";
                    Log.i(TAG, "Parse Error: " + e);
				}
	            if(!isOK) {
                    mHandler.obtainMessage(FAILURE, error).sendToTarget();
                } else {
                    mHandler.obtainMessage(SUCCESS, error).sendToTarget();
                }
			}
		}).start();
		
	}
	/************* end private function **************/

}
