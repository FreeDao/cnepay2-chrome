package com.cnepay.android.pos2;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.POSEncrypt;
import com.tangye.android.iso8583.POSHelper;
import com.tangye.android.iso8583.POSSession;
import com.tangye.android.iso8583.protocol.SignInMessage;
import com.tangye.android.utils.AES;
import com.tangye.android.utils.PublicHelper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends UIBaseActivity 
				implements View.OnClickListener, OnCancelListener {

	private ProgressDialog progressDialog;
	private Button btnLogin;
	private EditText txtPhone, txtPasswd;
	private CheckBox checkRemember;
	private SignInMessage s;
	private Handler mHandler;
	
	private static final String TAG = "LoginActivity";
	private static final int ENABLE_TIMEOUT = 1000;
	private static final int SUCCESS = 0;
    private static final int FAILURE = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(UpdateService.needUpgrade()){
			Intent intent = new Intent(this, UpdateActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
		}
		
		if(POSHelper.getSessionID() > 0) {
            Intent intent = new Intent(LoginActivity.this, ManagerActivity.class);
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
		checkRemember = (CheckBox) findViewById(R.id.log_remember);
		findViewById(R.id.log_resetpd).setOnClickListener(this);
		findViewById(R.id.log_rebind).setOnClickListener(this);
		SharedPreferences sp = getSharedPreferences("rem_info", 0);
		String n1 = sp.getString("name", "");
		if(n1.length() > 0) {
			checkRemember.setChecked(true);
			txtPhone.setText(n1);
			String p1 = AES.decryptTrack(sp.getString("passwd", ""), n1);
			if (p1 != null && p1.length() >= 6) {
				txtPasswd.setText(p1.substring(0, 6));
			}
		} else {
			checkRemember.setChecked(false);
			txtPasswd.setText("");
		}
		
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
                        Intent intent = new Intent(LoginActivity.this, ManagerActivity.class);
                        if (info.length() == 0) {
                        	Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        } else {
                        	Toast.makeText(LoginActivity.this, info, Toast.LENGTH_SHORT).show();
                        }
	        			startActivity(intent);
	        			String nam = txtPhone.getText().toString();
	        			String pas = txtPasswd.getText().toString();
	        			if (checkRemember.isChecked()) {
	        				if (nam.length() > 0) {
	        					Editor edit = getSharedPreferences("rem_info", 0).edit();
	        					edit.putString("name", nam);
	        					String pwd = AES.encryptTrack(pas + "0000000000", nam);
	        					edit.putString("passwd", pwd);
	        					edit.commit();
	        				}
	        			} else {
	        				Editor edit = getSharedPreferences("rem_info", 0).edit();
	        				edit.clear().commit();
	        			}
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
    	        		        .setNegativeButton(android.R.string.cancel, null)
    	        		        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
    	        		            public void onClick(DialogInterface dialog, int which) {
    	        		               String num = inputServer.getText().toString().toUpperCase().trim();
    	        		               InputMethodManager imm = (InputMethodManager)getSystemService(LoginActivity.INPUT_METHOD_SERVICE);
    	        		               imm.hideSoftInputFromWindow(inputServer.getWindowToken(), 0);
    	        		               sendSerial(num);
    	        		             }
    	        		        })
    	        		        .show();
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
    	if (s != null) {
    		s.stop();
    		s = null;
    	}
	}

	@Override
	public void onClick(final View v) {
		Intent intent;
		switch(v.getId()) {
		case R.id.log_rebind:
			intent = new Intent(LoginActivity.this, ReBindActivity.class);
			startActivity(intent);
			break;
		case R.id.log_resetpd:
			intent = new Intent(LoginActivity.this, ResetPasswdActivity.class);
			startActivity(intent);
			break;
		case R.id.title_submit:
			v.setEnabled(false);
			v.postDelayed(new Runnable() {
				public void run() {
					v.setEnabled(true);
				}
			}, ENABLE_TIMEOUT);
			AlertDialog.Builder builder = PublicHelper.getAlertDialogBuilder(this);
            builder.setMessage("请认真填写银行卡号、序列号、手机号码，一经注册成功，实名认证通过将不得更改");
            builder.setTitle("提示");
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        			startActivity(intent);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
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
		progressDialog.setCanceledOnTouchOutside(false);
		(new Thread() {
			public void run() {
			    POSEncrypt POS = POSHelper.getPOSEncrypt(LoginActivity.this, account);
			    if(!POS.isInitializedExceptRanCode()) {
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
	             .setSetNumber_60(POS.getPOSDecrypt(POS.SETNUMBER));
	            String ranCode = "";
	            if(POS.isInitializedRanCode()){
	            	s.setPhoneNumber_63(account, POS.getPOSDecrypt(POS.RANDOMCODE));
	            }else{
	            	ranCode = PublicHelper.getRandomCode();
	            	s.setPhoneNumber_63(account, ranCode);
	            }
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
	                    } else if ( statusCode.equals("X9")
	                    		 || statusCode.equals("X8")
	                    		 || statusCode.equals("X7")
	                    		 || statusCode.equals("X6")
	                    		 || statusCode.equals("X5")){
	                    	//??这个地方的逻辑，我有点迷糊，有空告诉我一下
	                    	msg = getError(statusCode);
	                    }
	                	if (msg != null) {
	                		POSSession SESSION = POSHelper.getPOSSession(LoginActivity.this, true);
		                    SESSION.initWK(resp.getField(62).toString(),
		                    			   account,
		                    			   passwd,
		                    			   resp.getField(2).toString(),
		                    			   resp.getField(58).toString(),
		                    			   msg.length() == 0).close();
		                    String setn = resp.getField(60).toString().substring(2, 8);
		                    POS = POSHelper.getPOSEncrypt(LoginActivity.this, account);
		                    if(POS.isInitializedExceptRanCode() && !POS.isInitializedRanCode()){
		                    	POS.setRandomCode(ranCode);
		                    }
		                    POS.setSetNumber(setn).close();
		                    Log.i(TAG, "Log Set Number: " + setn);
	        				error = msg; // differ with stop by user
		                    isOK = true;
	                	} else {
	                		if(statusCode.equals("Z0")){
	                			error = "error";
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
                    Log.i(TAG, "Exception Error: " + e);
                    e.printStackTrace();
				}
	            if(!isOK) {
                    mHandler.obtainMessage(FAILURE, error).sendToTarget();
                } else {
                    mHandler.obtainMessage(SUCCESS, error).sendToTarget();
                }
	            s = null;
			}
		}).start();
	}

	//发送序列号绑定报文
	private void sendSerial(String num) { }
	
}
