package com.cnepay.android.pos2;


import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.POSEncrypt;
import com.tangye.android.iso8583.POSHelper;
import com.tangye.android.iso8583.POSNative;
import com.tangye.android.iso8583.POSSession;
import com.tangye.android.iso8583.protocol.SignInMessage;
import com.tangye.android.utils.PublicHelper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends UIBaseActivity 
				implements View.OnClickListener, OnCancelListener {

	private ProgressDialog progressDialog;
	private Button btnLogin;
	private EditText txtPhone, txtPasswd;
	private SignInMessage s;
	private Handler mHandler;
	
	private static final String TAG = "LoginActivity";
	private static final int ENABLE_TIMEOUT = 1000;
	private static final int SUCCESS = 0;
    private static final int FAILURE = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(POSHelper.getSession() > 0) {
            Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
		setContentView(R.layout.login);
		
		setTitleSubmitText("注册");
		
		btnLogin = (Button) findViewById(R.id.login);
		btnLogin.setOnClickListener(this);
		txtPhone = (EditText) findViewById(R.id.log_id);
		txtPasswd = (EditText) findViewById(R.id.log_password);
		
		showTitleSubmit();
		btnSubmit.setOnClickListener(this);
		
		mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                case SUCCESS:
                    if(progressDialog != null && msg.obj != null) {
                    	// register successfully
                        progressDialog.dismiss();
                        progressDialog = null; // For not fade card number
                        String info = (String) msg.obj;
                        Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                        if (info.length() == 0) {
                        	makeError("登录成功");
                        } else {
                        	intent.putExtra("juage", 1);
                        	makeError(info);
                        }
	        			startActivity(intent);
	        			finish();
                    }
                    break;
                case FAILURE:
                    if(progressDialog != null) {
                        progressDialog.cancel();
                        String info = (String) msg.obj;
                        if (info != null) {
                        	if (info.equals("error")) {
                        		final EditText inputServer = new EditText(LoginActivity.this);
    	        		        AlertDialog.Builder builder = PublicHelper.getAlertDialogBuilder(LoginActivity.this);
    	        		        builder.setTitle("序列号已被使用")
    	        		        .setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
    	        		                .setNegativeButton(android.R.string.cancel, null);
    	        		        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
    	        		            public void onClick(DialogInterface dialog, int which) {
    	        		               String num = inputServer.getText().toString().toUpperCase().trim();
    	        		               InputMethodManager imm = (InputMethodManager)getSystemService(LoginActivity.INPUT_METHOD_SERVICE);
    	        		               imm.hideSoftInputFromWindow(inputServer.getWindowToken(), 0);
    	        		               sendSerial(num);
    	        		             }
    	        		        });
    	        		        builder.create().show();
                        	} else {
                        		makeError(info);
                        	}
                        }
                    }
                    break;
                }
                btnLogin.setEnabled(true);
            }
        };

        // TEST JNI
		this.setTitleSubmitText(POSNative.getNativeK("d", "pp"));
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        if(progressDialog != null) {
        	progressDialog.cancel();
        }
    }
    
    @Override
	public void onCancel(DialogInterface dialog) {
    	progressDialog = null;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.title_submit:
			AlertDialog.Builder builder = PublicHelper.getAlertDialogBuilder(this);
            builder.setMessage("请认真填写银行卡号、序列号、手机号码，一经注册成功，实名认证通过将不得更改");
            builder.setTitle("提示");
            builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        			startActivity(intent);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.create().show();
			break;
		case R.id.login:
			if(progressDialog == null) {
				doRequest();
	        }
			break;
		}
		
	}
	
	private void verify_failure(View v, String err) {
		if (v != null) {
			v.requestFocus();
		}
		btnLogin.postDelayed(new Runnable() {
			public void run() {
				btnLogin.setEnabled(true);
			}
		}, ENABLE_TIMEOUT);
		makeError(err);
		return;
	}
	
	private void makeError(String txt) {
		Toast t = Toast.makeText(this, txt, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}
	
	private void doRequest() {
		InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(getCurrentFocus()
				.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		btnLogin.setEnabled(false);

		final String account = txtPhone.getText().toString();
		final String passwd = txtPasswd.getText().toString();
		if (account.length() == 0) {
			verify_failure(txtPhone, "账号不能为空");
			return;
		}
		if (passwd.length() == 0) {
			verify_failure(txtPasswd, "密码不能为空");
			return;
		}
		
		progressDialog = ProgressDialog.show(this, // context 
				"", // title 
				"登录中...", // message 
				true, //进度是否是不确定的，这只和创建进度条有关 
				true,
				this);
		(new Thread() {
			public void run() {
			    POSEncrypt POS = POSHelper.getPOSEncrypt(LoginActivity.this, account);
			    if(!POS.isInitialized()) {
                    POS.close();
                    mHandler.obtainMessage(FAILURE, "该手机未绑定此账号").sendToTarget();
                    return;
			    }
			    s = new SignInMessage();
	            s.setCardTracerNumber_11(POS.getPOSDecrypt(POS.TRACENUMBER))
	             .setSource_16(getSource())
	             .setTerminalMark_41(POS.getPOSDecrypt(POS.TERMINAL))
	             .setUserMark_42(POS.getPOSDecrypt(POS.USERMARK))
	             .setUserPassword_57(passwd)
	             .setSetNumber_60(POS.getPOSDecrypt(POS.SETNUMBER))
	             .setPhoneNumber_63(account);
	            boolean isOK = false;
	            String error = "";
	            try {
	                if(!s.isBitmapValid()) throw new RuntimeException("BitmapError");
	                IsoMessage resp = s.request();
	                if(resp != null) {
	                	String statusCode = resp.getField(39).toString();
	                	String msg = null;
	                	if (statusCode.equals("00")) {
	                    	msg = ""; // 登录成功
	                    } else if (statusCode.equals("X9")) {
	                    	msg = "账户认证未通过";
	                    } else if (statusCode.equals("X8")) {
	                    	msg = "实名认证已审核未通过";
	                    } else if (statusCode.equals("X7")) {
	                    	msg = "实名认证通过";
	                    } else if (statusCode.equals("X6")) {
	                    	msg = "实名认证审核中";
	                    } else if (statusCode.equals("X5")) {
	                    	msg = "未进行实名认证";
	                    }
	                	if (msg != null) {
	                		POSSession SESSION = POSHelper.getPOSSession(LoginActivity.this, true);
		                    SESSION.initWK(resp.getField(62).toString(),
		                    			   account,
		                    			   passwd,
		                    			   resp.getField(2).toString()).close();
		                    String setn = resp.getField(60).toString().substring(2, 8);
		                    POS = POSHelper.getPOSEncrypt(LoginActivity.this, account);
		                    POS.setSetNumber(setn).close();
		                    Log.i(TAG, "Log Set Number: " + setn);
		                    
	        				error = msg; // differ with stop by user
		                    isOK = true;
	                	} else {
	                		if(statusCode.equals("Z0")){
	                			error = "error";
	                		} else if (statusCode.equals("03")){
	                			error = "未在POS中心上初始化该POS终端";
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

	//发送序列号绑定报文
	private void sendSerial(String num) { }
	
}
