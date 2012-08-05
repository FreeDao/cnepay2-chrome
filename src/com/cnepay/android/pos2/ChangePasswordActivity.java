package com.cnepay.android.pos2;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tangye.android.dialog.AlertDialogBuilderWrapper;
import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.POSEncrypt;
import com.tangye.android.iso8583.POSHelper;
import com.tangye.android.iso8583.POSSession;
import com.tangye.android.iso8583.protocol.SetPasswordMessage;
import com.tangye.android.utils.PublicHelper;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePasswordActivity extends UIBaseActivity implements OnClickListener{
	private EditText oldPwd;
	private EditText newPwd;
	private EditText newPwdRepeat;
	private Button submit;
	
	private static final int ENABLE_TIMEOUT = 1000;
	private final static int SUCCESS = 0;
	private final static int FAILURE = 1;
	private ProgressDialog progressDialog;
	
	private final String TAG = "ChangePasswordActivity";
	
	private boolean isProcessing;
	private Handler mHandler;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);
		setTitle("修改密码");
		setActivityPara(true, true);
		initUI();
		
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what){
				case SUCCESS:
					if(progressDialog != null){
						progressDialog.dismiss();
					}
					AlertDialogBuilderWrapper builder = PublicHelper.getAlertDialogBuilder(ChangePasswordActivity.this);
				    builder.setTitle("修改密码")
				    .setIcon(android.R.drawable.ic_dialog_info)
				    .setMessage("密码修改成功")
				    .setCancelable(true)
				    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				    	public void onClick(DialogInterface dialog, int which) {
				    		finish();
				    	}
				    });
				    builder.show();
 				    oldPwd.setText("");
				    newPwd.setText("");
				    newPwdRepeat.setText("");
					break;
				case FAILURE:
					if(progressDialog != null){
						progressDialog.dismiss();
					}
					submit.setEnabled(true);
					String error = (String)msg.obj;
					makeError(error);
					break;
					
				}
			}
		};
	}
	
	private void initUI(){
		oldPwd = (EditText)findViewById(R.id.change_pwd_old);
		newPwd = (EditText)findViewById(R.id.change_pwd_new);
		newPwdRepeat = (EditText)findViewById(R.id.change_pwd_new_repeat);
		submit = (Button)findViewById(R.id.change_pwd_submit);
		submit.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.change_pwd_submit:
			InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(getCurrentFocus()
					.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			submit.setEnabled(false);
			final String oldPwdStr = oldPwd.getText().toString();
			final String newPwdStr = newPwd.getText().toString();
			String newPwdRepeatStr = newPwdRepeat.getText().toString();
			if ( oldPwdStr == null || oldPwdStr.length() != 6) {
				verify_failure(oldPwd, "请输入六位原密码");
				return;
			} else if (oldPwdStr.length() == 6) {
				if (!testPwd(oldPwdStr)) {
					verify_failure(oldPwd, "原密码必须由字母和数字组成");
					oldPwd.setText("");
					return;
				}
			}
			if (newPwdStr == null || newPwdStr.length() != 6) {
				verify_failure(newPwd, "请输入六位新密码");
				return;
			} else if (newPwdStr.length() == 6) {
				if (!testPwd(oldPwdStr)) {
					verify_failure(newPwd, "新密码必须由字母和数字组成");
					newPwd.setText("");
					return;
				}
			}
			if (newPwdRepeatStr == null || !newPwdStr.equals(newPwdRepeatStr)) {
				verify_failure(newPwdRepeat, "两次输入密码不一致");
				newPwdRepeat.setText("");
				return;
			}
			
			AlertDialogBuilderWrapper builder = PublicHelper.getAlertDialogBuilder(ChangePasswordActivity.this);
		    builder.setTitle("修改密码")
		    .setIcon(android.R.drawable.ic_dialog_alert)
		    .setMessage("确定修改密码？")
		    .setCancelable(false)
		    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int which) {
						submit.setEnabled(true);
		    	}
		    })
		    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int which) {
		    		if(!setPassword(oldPwdStr, newPwdStr)){
		    			isProcessing = false;
						submit.setEnabled(true);
						finish(); // should not happen
					}
		    	}
		    });
		    builder.show();			
		}
	}
	
	// test password
	private boolean testPwd(String id) {
		Pattern pattern = Pattern.compile("^[a-zA-Z0-9]+$");
		Matcher matcher = pattern.matcher(id);
		boolean b = matcher.matches();
		return b;
	}
	
	private void makeError(String err) {
		Toast t = Toast.makeText(this, err, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}
	
	private void verify_failure(View v, String err) {
		if (v != null) {
			v.requestFocus();
		}
		submit.postDelayed(new Runnable() {
			public void run() {
				submit.setEnabled(true);
			}
		}, ENABLE_TIMEOUT);
		makeError(err);
		return;
	}
	
	private boolean setPassword(final String oldPwd, final String newPwd){
		isProcessing = true;
	    final POSSession session = POSHelper.getPOSSession();
		if (session == null) {
			makeError("POS机出错！");
			return false;
		}
		
		final String name = session.getSessionAccount();
		if (name == null || name.length() == 0) {
			makeError("POS机出错！");
			return false;
		}
		final POSEncrypt POS = POSHelper.getPOSEncrypt(this, name);
		if(POS == null){
			makeError("POS机出错！");
			return false;
		}
		
		progressDialog = PublicHelper.getProgressDialog(ChangePasswordActivity.this, // context 
				"",	// title 

			"正在更新密码...", // message 
			true, 
				false);
		(new Thread() {
			public void run() {
			    SetPasswordMessage s = new SetPasswordMessage();
			    s.setSource_16(getSource())
			    .setTerminalMark_41(POS.getPOSDecrypt(POS.TERMINAL))
			    .setUserMark_42(POS.getPOSDecrypt(POS.USERMARK))
			    .setOldPassword_56(oldPwd)
			    .setNewPassword_57(newPwd)
			    .setUseMac_64();
	            POS.close();
	            boolean isOK = false;
	            String error = "";
	            try {
	                if(!s.isBitmapValid()) throw new RuntimeException("BitmapError");
	                IsoMessage resp = s.request();
	                if(resp != null) {
	                	String statusCode = resp.getField(39).toString();
	                	if (statusCode.equals("00")) {
	                		POSEncrypt POS =  POSHelper.getPOSEncrypt(ChangePasswordActivity.this, name);
	                		if(POS.setPwdChange(oldPwd, newPwd, resp)) {
	                			Editor edit = getSharedPreferences("rem_info", 0).edit();
	        					edit.remove("passwd");
	        					edit.commit();
	                			isOK = true;
	                		} else {
	                			isOK = false;
	                			error = "Fatal error with info data"; // should not happen
	                		}
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
	            isProcessing = false;
	            POS.close();
			}
		}).start();
		
		return true;
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && isProcessing) {
        	makeError("不能中止交易");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
	
}
