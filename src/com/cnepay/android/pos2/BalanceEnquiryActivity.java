package com.cnepay.android.pos2;


import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Calendar;

import com.cnepay.android.pos2.PasswordInputMethod.PasswordInputMethodListener;
import com.tangye.android.dialog.SwipeDialogController;
import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.POSEncrypt;
import com.tangye.android.iso8583.POSHelper;
import com.tangye.android.iso8583.POSSession;
import com.tangye.android.iso8583.protocol.BalanceEnquiryMessage;
import com.tangye.android.utils.CardInfo;
import com.tangye.android.utils.PublicHelper;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class BalanceEnquiryActivity extends UIBaseActivity implements OnClickListener
		,PasswordInputMethodListener, OnCancelListener{
	private EditText txtPassword;
	private TextView txtAmount;
	private Button[] btns;
	private Button fnButton;
	private View delButton;
	private TextView noteSwipe, card;
	private SwipeDialogController dialog;
	private PasswordInputMethod passwdIM;
	private ViewGroup framePass, layoutMask;
	private ViewGroup numPad;
	private ImageView imgCardType, imgCardReader;
	private BalanceEnquiryMessage s;
	private Handler mHandler;
	private ProgressDialog progressDialog;
	
	private final String TAG = "BalanceEnquiryActivity";
	private final static int SUCCESS = 0;
	private final static int FAILURE = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_balance_enquiry);
		setTitle("手机充值");
		setActivityPara(true, true);
		btnSubmit.setOnClickListener(this);
		
		initUI();		
		passwdIM = new PasswordInputMethod(btns, fnButton, delButton, txtPassword, this);

		mHandler = new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
        		switch(msg.what) {
        		case SUCCESS:
        			String all = (String) msg.obj;
	    			if(progressDialog != null && all != null) {
	    				progressDialog.dismiss();
	    				progressDialog = null; // For not fade card number	
	        		}
	    			txtAmount.setText(all);
	    			framePass.setVisibility(View.GONE);
        			numPad.setVisibility(View.GONE);
        			noteSwipe.setVisibility(View.VISIBLE);
        			ScaleAnimation sa = new ScaleAnimation(1, 0, 1, 0, 
        	                Animation.RELATIVE_TO_SELF, 0.5f, 
        	                Animation.RELATIVE_TO_SELF, 0.5f);
        	        sa.setDuration(200);
        	        imgCardType.startAnimation(sa);
        	        imgCardType.postDelayed(new Runnable() {
        	            public void run() {
        	            	imgCardType.setVisibility(View.GONE);
        	            }
        	        }, 200);
	    			if(isPlugged()) {
        				showTitleSubmit();
        			}
	    			
		        	break;
        		case FAILURE:
        			String e = (String) msg.obj;
        			if(progressDialog != null) {
                         progressDialog.cancel();
                    }
        			card.setText("");
        			framePass.setVisibility(View.GONE);
        			numPad.setVisibility(View.GONE);
        			noteSwipe.setVisibility(View.VISIBLE);
        			ScaleAnimation sas = new ScaleAnimation(1, 0, 1, 0, 
        	                Animation.RELATIVE_TO_SELF, 0.5f, 
        	                Animation.RELATIVE_TO_SELF, 0.5f);
        			sas.setDuration(200);
        	        imgCardType.startAnimation(sas);
        	        imgCardType.postDelayed(new Runnable() {
        	            public void run() {
        	            	imgCardType.setVisibility(View.GONE);
        	            }
        	        }, 200);
        			if(isPlugged()) {
        				showTitleSubmit();
        			}
        			if (e != null) {
        				makeError(e);
        			}
        			break;
        		}
        	}
		};
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_submit:
			hideTitleSubmit();
			dialog.setText("请刷卡...");
			dialog.show();
			startSwipe();
			break;
		}
	}
	
	private void initUI() {
		initNumPad();
		setTitleSubmitText("确认刷卡");
		noteSwipe = (TextView) findViewById(R.id.balance_enquiry_notation);
		noteSwipe.setText("正在检测刷卡器...");
		imgCardType = (ImageView)findViewById(R.id.card_type);
        imgCardReader = (ImageView)findViewById(R.id.card_indicator);
		framePass = (ViewGroup) findViewById(R.id.password_frame);
		layoutMask = (ViewGroup) findViewById(R.id.layout_mask);
		txtPassword = (EditText) findViewById(R.id.card_password);
		card = (TextView) findViewById(R.id.card_text);
		framePass.setVisibility(View.GONE);
		layoutMask.setClickable(true);
		int[] attrs = new int[] { R.layout.swipe_dialog, R.style.dialog,
				R.id.dialog_anim, R.id.dialog_note };
		
		dialog = new SwipeDialogController(this, attrs);
		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				stopSwipe();
			}
		});
		
	}

	private void initNumPad() {
		txtAmount = (TextView) findViewById(R.id.balance_enquiry_amount);
		btns = new Button[10];
		numPad = (ViewGroup) findViewById(R.id.num_pad);
		for (int i = 0; i < 9; i++) {
			ViewGroup vgtmp = ((ViewGroup) numPad.getChildAt(i / 3));
			btns[i + 1] = (Button) vgtmp.getChildAt(i % 3);
		}
		ViewGroup vg1 = ((ViewGroup) numPad.getChildAt(3));
		btns[0] = (Button) vg1.getChildAt(1);
		TextView t1 = (TextView) vg1.getChildAt(1);
		Log.e(TAG, t1.getText().toString());
		fnButton = (Button) vg1.getChildAt(0);
		delButton = vg1.getChildAt(2);
		
		numPad.setVisibility(View.GONE);
	}

	@Override
	public void onPasswordCancel() {
		Log.v(TAG, "onPasswordCancel");
		card.setText("");
		framePass.setVisibility(View.GONE);
		numPad.setVisibility(View.GONE);
		noteSwipe.setVisibility(View.VISIBLE);
		ScaleAnimation sa = new ScaleAnimation(1, 0, 1, 0, 
                Animation.RELATIVE_TO_SELF, 0.5f, 
                Animation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(200);
        imgCardType.startAnimation(sa);
        imgCardType.postDelayed(new Runnable() {
            public void run() {
            	imgCardType.setVisibility(View.GONE);
            }
        }, 200);
		if(isPlugged()) {
			showTitleSubmit();
		}
	}

	@Override
	public void onSubmit(String password) {
		Log.v(TAG, "onSubmit");
		doRequest(password);
	}
	
	@Override
	public void onComplete(String cn) {
		dialog.dismiss();
		card.setText(cn);
		framePass.setVisibility(View.VISIBLE);
		numPad.setVisibility(View.VISIBLE);
		noteSwipe.setVisibility(View.GONE);
		imgCardType.setVisibility(View.VISIBLE);
    	ScaleAnimation sa = new ScaleAnimation(0, 1, 0, 1, 
    	        Animation.RELATIVE_TO_SELF, 0.5f, 
    	        Animation.RELATIVE_TO_SELF, 0.5f);
    	sa.setDuration(200);
    	imgCardType.startAnimation(sa);
    	
    	passwdIM.init();
    	
	}
	
	@Override
	public void onDecoding() {
		dialog.setText("正在解码...");
	}
	
	@Override
	public void onSwipe() {
		dialog.setText("接受刷卡数据...");
	}
	
	@Override
	public void onPlugin() {
		super.onPlugin(); // UIBASE action
		if(imgCardReader.getVisibility() != View.VISIBLE) {
            imgCardReader.setVisibility(View.VISIBLE);
            ScaleAnimation sa = new ScaleAnimation(0, 1, 0, 1, 
                    Animation.RELATIVE_TO_SELF, 0.5f, 
                    Animation.RELATIVE_TO_SELF, 0.5f);
            sa.setDuration(200);
            imgCardReader.startAnimation(sa);
        }
		if (framePass.getVisibility() == View.GONE) {
        	showTitleSubmit();
        }
		noteSwipe.setText("点击【确认刷卡】");
	}
	
	@Override
	public void onPlugout() {
		super.onPlugout(); // UIBASE action
		if(imgCardReader.getVisibility() == View.VISIBLE) {
            ScaleAnimation sa = new ScaleAnimation(1, 0, 1, 0, 
                    Animation.RELATIVE_TO_SELF, 0.5f, 
                    Animation.RELATIVE_TO_SELF, 0.5f);
            sa.setDuration(200);
            imgCardReader.startAnimation(sa);
            imgCardReader.postDelayed(new Runnable() {
                public void run() {
                    imgCardReader.setVisibility(View.GONE);
                }
            }, 200);
        }
		hideTitleSubmit();
		noteSwipe.setText("请插入刷卡器");
	}
	
	@Override
	public void onTimeout() {
		super.onTimeout();
		makeError("未检测到刷卡动作");
		dialog.dismiss();
		if(isPlugged()) {
			showTitleSubmit();
		}
	}
	
	
	private void makeError(String err) {
		Toast t = Toast.makeText(this, err, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}
	
	
	@Override
	public void onError(int err) {
		Log.i(TAG, "error = " + err);
		dialog.dismiss();
		// 刷卡时，插拔卡可能会引起decodeError
		boolean isDecodeError = true;
		boolean isInterrupt = false;
		if (this.isPlugged()) {
			switch (err) {
			case E_API2_UNRESOLVED:
				makeError("无法解析，请重试或者使用其他Android手机");
				break;
			case E_API2_FASTORSLOW:
				makeError("刷卡过快或过慢，请重试");
				break;
			case E_API2_UNSTABLE:
				makeError("刷卡不稳定，请重试");
				break;
			case E_API2_INVALID_DEVICE:
				makeError("非法刷卡器，请使用正规刷卡器");
				break;
			case E_API2_INTERRUPT:
				isInterrupt = true;
			default:
				isDecodeError = false;
			}
		} else {
			switch (err) {
			case E_API2_INVALID_DEVICE:
				makeError("无法识别该刷卡器，请选择新的刷卡器或者重新拔插");
				noteSwipe.setText("请拔掉此刷卡器，重新插入");
				break;
			case E_API2_INVALID_KSN:
				makeError("非法刷卡器，请使用已注册绑定的刷卡器");
				noteSwipe.setText("请拔掉此刷卡器，重新插入");
				break;
			case E_API2_INTERRUPT:
				isInterrupt = true;
				break;
			}
		}
		if (isDecodeError || isInterrupt) {
			dialog.dismiss();
			if (isPlugged()) {
				showTitleSubmit();
			}
		}
	}
	
	private boolean doRequest(String password) {
	    POSSession session = POSHelper.getPOSSession();
		if (ci == null || session == null) {
			makeError("POS机出错！");
			return false;
		}
		final CardInfo cardInfo = ci;
		final String name = session.getSessionAccount();
		if (name == null || name.length() == 0) {
			makeError("POS机出错！");
			return false;
		}
		try {
			password = session.getPIN(password, cardInfo.getCard(false));
		} catch(Exception e) {
			makeError("请输入4到6位银行卡密码！");
			return false;
		}
		final String passwd = password;
		hideTitleSubmit();
		
		progressDialog = PublicHelper.getProgressDialog(this, // context 
				"",	// title 
				"查询中...", // message 
				true, //进度是否是不确定的，这只和创建进度条有关 
				true,
				this);
		(new Thread(TAG) {
			public void run() {
				POSEncrypt POS = POSHelper.getPOSEncrypt(BalanceEnquiryActivity.this, name);
				POS.addTraceNumber();
				s = new BalanceEnquiryMessage();
                s.setCardTracerNumber_11(POS.getPOSDecrypt(POS.TRACENUMBER))
                .setIsPinNeed_22(true)
                .setMaxPinLength_26(10) // TODO hard-code here
                .setCardInfo(cardInfo)
                .setTerminalMark_41(POS.getPOSDecrypt(POS.TERMINAL))
                .setUserMark_42(POS.getPOSDecrypt(POS.USERMARK))
                .setUserPin_52(passwd)
                .setSetNumber_60(POS.getPOSDecrypt(POS.SETNUMBER))
                .setUseMac_64();
				POS.close();
				boolean isOk = false;
	            String error = "";
	            try {
	                if(!s.isBitmapValid()) throw new RuntimeException("BitmapError");
	                IsoMessage resp = s.request();
	                if(resp != null) {
	                	String statusCode = resp.getField(39).toString();
	                	if (statusCode.equals("00")) {
	                		error = resp.getField(54).toString();
		                    isOk = true;
	                	} else {
	                		error = getError(statusCode);
	                	}
	                } else {
	                	// Manually stop client from user's aspect
	                	error = null;
	                    isOk = false;
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
	            if(!isOk) {
	                mHandler.obtainMessage(FAILURE, error).sendToTarget();
	            } else {	            	 
	                mHandler.obtainMessage(SUCCESS, error).sendToTarget();
	            }
			}
		}).start();
		return true;
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		progressDialog = null;
        if(s != null) {
            s.stop();
            s = null;
        }
	}

}

