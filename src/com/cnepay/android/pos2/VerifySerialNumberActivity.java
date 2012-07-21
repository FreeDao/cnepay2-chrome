package com.cnepay.android.pos2;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.protocol.SerialNumberVerifyMessage;
import com.tangye.android.utils.GernateSNumber;
import com.tangye.android.utils.PublicHelper;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class VerifySerialNumberActivity extends UIBaseActivity implements
		View.OnClickListener{
	
	private EditText serialNumber;
	private String myKSN;
	private Handler mHandler;
	private ProgressDialog progressDialog;

	private static final String TAG = "VerifySerialNumberActivity";
	private static final int SUCCESS = 0;
    private static final int FAILURE = 1;
	private final GernateSNumber gn = new GernateSNumber();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_verify_serial_number);
		setTitle("序列号激活");
		setTitleSubmitText("激活");
		this.showTitleSubmit();
		btnSubmit.setOnClickListener(this);
		setActivityPara(false, false);
		myKSN = this.getIntent().getExtras().getString("ksn");
		
		serialNumber = (EditText)findViewById(R.id.serial_number);
		serialNumber.setHint("请输入序列号,点击【激活】");

		
		mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                case SUCCESS:
                	String[] all = (String[])msg.obj;
                    if(progressDialog != null && all != null) {
                        progressDialog.cancel();
                        progressDialog = null; // For not fade card number
                        errText("序列号激活成功");
                        Intent intent = new Intent(VerifySerialNumberActivity.this, RegisterActivity.class);
                        intent.putExtra("register", all);
                        startActivity(intent);
                        finish();
                    }
                    break;
                case FAILURE:
                    if(progressDialog != null) {
                        progressDialog.cancel();
                        errText((String)msg.obj);
            			//finish();
                    }
                    break;
                }
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
		submit();
	}
	

	/************ private function *************/

	private void errText(String txt) {
		Toast t = Toast.makeText(this, txt, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}

	// test serialNum
	private boolean testSerial(String id) {
		Pattern pattern = Pattern.compile("^[A-Z0-9]{16}$");
		Matcher matcher = pattern.matcher(id);
		boolean b = matcher.matches();
		return b;
	}
	
	private void submit() {
		InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(getCurrentFocus()
				.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		
		final String serialNum = serialNumber.getText().toString()
				.toUpperCase();
		if (!testSerial(serialNum)) {
			errText("序列号必须是16位数字字母");
			return;
		}
		if (!gn.Verify(serialNum)) {
			errText("序列号输入不正确，请仔细核对");
			return;
		}
		
		hideTitleSubmit();
		serialNumber.setEnabled(false);
		progressDialog = PublicHelper.getProgressDialog(this, // context 
				"",	// title 
				"校验中...", // message 
				true, //进度是否是不确定的，这只和创建进度条有关 
				false);
		(new Thread() {
			public void run() {
	            SerialNumberVerifyMessage ss = new SerialNumberVerifyMessage();
            	ss.setKSN_54(serialNum)
            	.setSource_16(getSource());
	            boolean isOK = false;
	            String error = "";
	            String[] verifyOk = new String[2];
	            try {
	            	if(!ss.isBitmapValid()) throw new RuntimeException("BitmapError");
	               	IsoMessage res = ss.request();
	               	if(res == null) {
	               		isOK = false;
	               		error = "未知错误";
	                }else{ 
		                String statusCode = res.getField(39).toString();
		                if (statusCode.equals("00")) {
		                	isOK = true;
			                verifyOk[0] = myKSN;
			                verifyOk[1] = serialNum;
		                }else{
		                	isOK = false;
		                	error = getError(statusCode);
		                }
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
                    mHandler.obtainMessage(SUCCESS, verifyOk).sendToTarget();
                }
			}
		}).start();
		
	}
	
	/************* end private function **************/


}
