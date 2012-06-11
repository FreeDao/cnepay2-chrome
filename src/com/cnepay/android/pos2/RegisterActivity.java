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
import com.tangye.android.iso8583.protocol.SignUpMessage;
import com.tangye.android.utils.GernateSNumber;
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

public class RegisterActivity extends UIBaseActivity implements
		View.OnClickListener, OnCNAPSResultListener, OnCancelListener {

	private SwipeDialogController dialog;
	private TextView card, bank;
	private EditText txtSerial, txtPname, txtPid, txtPhone, txtPwd, txtRePwd;
	private String bankid;
	private Button btnCreate;
	private SignUpMessage s;
	private Handler mHandler;
	private ProgressDialog progressDialog;

	private static final String TAG = "RegisterActivity";
	private static final int ENABLE_TIMEOUT = 1000;
	private static final int SUCCESS = 0;
    private static final int FAILURE = 1;
	private final GernateSNumber gn = new GernateSNumber();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reg);
		setTitle("账户注册");
		setOnCNAPSResultListener(this); // 增加选择开户银行功能
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

		card = (TextView) this.findViewById(R.id.reg_account_number);
		card.setOnClickListener(this);

		txtSerial = (EditText) findViewById(R.id.reg_serial);
		txtPname = (EditText) findViewById(R.id.reg_username);
		txtPid = (EditText) findViewById(R.id.reg_idnumber);
		txtPhone = (EditText) findViewById(R.id.reg_phone);
		txtPwd = (EditText) findViewById(R.id.first_pwd_reg);
		txtRePwd = (EditText) findViewById(R.id.second_pwd_reg);

		bank = (TextView) findViewById(R.id.reg_bankname);
		bank.setOnClickListener(this);

		btnCreate = (Button) findViewById(R.id.reg_submit);
		btnCreate.setOnClickListener(this);
		mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                case SUCCESS:
                    if(progressDialog != null && msg.obj != null) {
                    	// register successfully
                        progressDialog.dismiss();
                        progressDialog = null; // For not fade card number
                        errText("注册成功");
                        finish();
                    }
                    break;
                case FAILURE:
                    if(progressDialog != null) {
                        progressDialog.cancel();
                        errText((String)msg.obj);
                    }
                    break;
                }
                btnCreate.setEnabled(true);
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
		case R.id.reg_account_number:
			if (this.isPlugged()) {
				dialog.setText("请刷卡...");
				dialog.show();
				this.startSwipe();
			} else {
				errText("请插入读卡器");
			}
			break;
		case R.id.reg_bankname:
			if (bankid != null && bankid.length() > 0) {
				AlertDialog.Builder builder = PublicHelper.getAlertDialogBuilder(RegisterActivity.this);
		        builder.setTitle("开户银行信息")
		        .setIcon(android.R.drawable.ic_dialog_info)
		        .setMessage(bank.getText().toString() + "\n联行号：" + bankid + "\n\n是否修改？")
		        .setNegativeButton(android.R.string.cancel, null)
		        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		        	public void onClick(DialogInterface dialog, int which) {
		            	chooseBank();
		        	}
		        });
		        builder.create().show();
			} else {
				chooseBank();
			}
			break;
		case R.id.reg_submit:
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
	public void onCNAPSResult(String BankName, String BankID) {
		bank.setText(BankName);
		bankid = BankID;
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
		btnCreate.postDelayed(new Runnable() {
			public void run() {
				btnCreate.setEnabled(true);
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

	// test id number, and verify
	private boolean testID(String id) {
		int[] co = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };
		id = id.toUpperCase();
		if (id.length() == 18) {
			int sum = 0;
			for (int i = 0; i < 17; i++) {
				char c = id.charAt(i);
				int x = c - 48;
				if (x < 0 || x > 9)
					return false;
				sum += co[i] * x;
			}
			sum %= 11;
			sum = (12 - sum) % 11;
			char c = id.charAt(17);
			if (c == 'X')
				c = 58;
			if (c - 48 == sum)
				return true;
		}
		return false;
	}

	// test password
	private boolean testPwd(String id) {
		Pattern pattern = Pattern.compile("^[a-zA-Z0-9]+$");
		Matcher matcher = pattern.matcher(id);
		boolean b = matcher.matches();
		return b;
	}

	// test serialNum
	private boolean testSerial(String id) {
		Pattern pattern = Pattern.compile("^[A-Z0-9]{16}$");
		Matcher matcher = pattern.matcher(id);
		boolean b = matcher.matches();
		return b;
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
		btnCreate.setEnabled(false);
		if (card.getText().length() == 0) {
			verify_failure(null, "卡号信息无效");
			return;
		}
		final String account = "6222020200068682125";
		final String pname = txtPname.getText().toString();
		final String pid = txtPid.getText().toString().toUpperCase();
		final String phone = txtPhone.getText().toString();
		final String bname = bank.getText().toString();
		final String bid = bankid;
		final String serialNum = txtSerial.getText().toString()
				.toUpperCase();
		final String password = txtPwd.getText().toString();
		final String repassword = txtRePwd.getText().toString();
		if (!testSerial(serialNum)) {
			verify_failure(txtSerial, "序列号必须是16位数字字母");
			return;
		}
		if (!gn.Verify(serialNum)) {
			verify_failure(txtSerial, "序列号输入不正确，请仔细核对");
			return;
		}
		if (pname.length() > 10) {
			verify_failure(txtPname, "姓名输入不能超过十位");
			return;
		}
		if (pname.length() == 0 || pid.length() == 0 || phone.length() == 0) {
			verify_failure(txtPname, "个人信息不完整");
			return;
		}
		if (pid.length() != 15 && pid.length() != 18) {
			verify_failure(txtPid, "身份证号码长度应该是15位或者18位");
			return;
		} else if (pid.length() == 18) {
			if (!testID(pid)) {
				verify_failure(txtPid, "非法身份证号码，请检查您的输入");
				return;
			}
		}
		if (!testPhone(phone)) {
			verify_failure(txtPhone, "手机号输入有误，请检查是否为手机号");
			return;
		}
		if (bname.length() == 0 || bid.length() == 0) {
			verify_failure(bank, "银行信息不完整");
			return;
		}
		if (bid.length() != 12) {
			verify_failure(bank, "联行号通过您的发卡银行客服获取");
			return;
		}
		if (password.length() == 0 || password == null
				|| password.length() != 6) {
			verify_failure(txtPwd, "请输入6位密码");
			return;
		} else if (password.length() == 6) {
			if (!testPwd(password)) {
				verify_failure(txtPwd, "密码必须由字母和数字组成");
				txtPwd.setText("");
				return;
			}
		}
		if (!password.equals(repassword)) {
			verify_failure(txtRePwd, "两次输入密码不一致");
			txtRePwd.setText("");
			return;
		}
		
		progressDialog = ProgressDialog.show(this, // context 
				"", // title 
				"注册中...", // message 
				true, //进度是否是不确定的，这只和创建进度条有关 
				true,
				this);
		(new Thread() {
			public void run() {
			    s = new SignUpMessage();
	            s.setCardNumber_2(account)
	             .setSource_16(getSource())
	             .setSerialNum_54(serialNum)
	             .setUserNameGBK_55(pname)
	             .setBankInfoGBK_47(bname)
	             .setUserPassword_57(password)
	             .setOtherInfo_63(pid, bid, phone);
	            boolean isOK = false;
	            String error = "";
	            try {
	                if(!s.isBitmapValid()) throw new RuntimeException("BitmapError");
	                IsoMessage resp = s.request();
	                if(resp != null) {
	                	String statusCode = resp.getField(39).toString();
	                	if (statusCode.equals("00")) {
	                		if(resp.getField(2).toString().equals(account)) {
	                			Log.i(TAG, "应答码：" + resp.getField(39).toString());
								Log.i(TAG, "终端标识码：" + resp.getField(41).toString());
								Log.i(TAG, "受卡方标识码：" + resp.getField(42).toString());
			                    POSEncrypt POS = POSHelper.getPOSEncrypt(RegisterActivity.this, phone);
		        				POS.init(resp, password);
		        				POS.close();
		        				error = account; // differ with stop by user
			                    isOK = true;
	                		} else {
	                			error = "Fatal error, please contact with us";
	                			Log.e(TAG, "Fatal Card Error");
	                		}
	                	} else {
	                		if(statusCode.equals("Z3")){
	                			error = "序列号已被使用";
	                		} else if (statusCode.equals("R5")){
	                			error = "卡已被注册过";
	                		} else if (statusCode.equals("R6")){
			                			error = "信用卡不能注册";
	                		} else if (statusCode.equals("Z4")){
	                			error = "您已绑定过序列号";
	                		} else {
	                			error = getError(statusCode);
	                		}
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
