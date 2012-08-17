package com.cnepay.android.pos2;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tangye.android.dialog.CustomProgressDialog;
import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.protocol.SerialNumberVerifyMessage;
import com.tangye.android.utils.GernateSNumber;
import com.tangye.android.utils.PublicHelper;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class VerifySerialNumberActivity extends UIBaseActivity implements
		View.OnClickListener, OnCancelListener{
	
	private EditText[] serialNumber;
	private final int LEN = 4;
	private final int TEXT_LEN = 4;
	private String myKSN;
	private Handler mHandler;
	private TextView serialHint;
	private CustomProgressDialog progressDialog;

	private static final String TAG = "VerifySerialNumberActivity";
	private static final int SUCCESS = 0;
    private static final int FAILURE = 1;
	private final GernateSNumber gn = new GernateSNumber();
	private SerialNumberVerifyMessage s;
	
	class SerialTextWatcher implements TextWatcher {
		
		private int index;
		
		public SerialTextWatcher(int index) {
			this.index = index;
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (s.length() == TEXT_LEN) {
				if (!testSerial(s.toString(), 4)) {
					serialNumber[index].selectAll();
					errText("输入有误！");
					return;
				}
				if (index < LEN - 1) {
					serialNumber[index + 1].setEnabled(true);
					Editable ea = serialNumber[index + 1].getText();
					Selection.setSelection(ea, ea.length());
					serialNumber[index + 1].requestFocus();
				}
			} else if (s.length() > TEXT_LEN) {
				serialNumber[index].setText(s.subSequence(0,  4));
				serialNumber[index].setSelection(4);
			}
			ensureSerial();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {}
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_verify_serial_number);
		setTitle("序列号激活");
		setTitleSubmitText("激活");
		btnSubmit.setOnClickListener(this);
		setActivityPara(false, false);
		myKSN = this.getIntent().getExtras().getString("ksn");
		//myKSN = "39920611000002";
		
		
		InputFilter mFilter = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				char[] v = new char[end - start];
				TextUtils.getChars(source, start, end, v, 0);
				String s = new String(v).toUpperCase();
				if (!testSerial(s, s.length())) {
					return dest.subSequence(dstart, dend);
				}
				if (source instanceof Spanned) {
					SpannableString sp = new SpannableString(s);
					TextUtils.copySpansFrom((Spanned) source, start,
							end, null, sp, 0);
					return sp;
				} else {
					return s;
				}
			}
		};
		
		
		serialNumber = new EditText[LEN];
		serialNumber[0] = (EditText)findViewById(R.id.serial_number1);
		serialNumber[1] = (EditText)findViewById(R.id.serial_number2);
		serialNumber[2] = (EditText)findViewById(R.id.serial_number3);
		serialNumber[3] = (EditText)findViewById(R.id.serial_number4);
		for(int i = 0; i < LEN; i++ ) {
			serialNumber[i].setEnabled(false);
			serialNumber[i].addTextChangedListener(new SerialTextWatcher(i));
			serialNumber[i].setFilters(new InputFilter[] {mFilter});
		}
		serialNumber[0].setEnabled(true);
		
		serialHint = (TextView)findViewById(R.id.verify_serial_number_hint);
		serialHint.setText("请输入序列号，然后点击【激活】");
		
		mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                case SUCCESS:
                	String[] all = (String[])msg.obj;
                    if(progressDialog != null && all != null) {
                        progressDialog.dismiss();
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
                    }
                    String err = (String)msg.obj;
                    if(err != null){
                    	errText(err);
                    } else {
                    	errText("未知错误");
                    }
                    break;
                }
                for(int i = 0; i < LEN; i++){
                	serialNumber[i].setEnabled(true);                    	
                }
                ensureSerial();
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
	
	@Override
	public void onCancel(DialogInterface dialog) {
		progressDialog = null;
        if(s != null) {
            s.stop();
            s = null;
        }
	}

	/************ private function *************/

	private void errText(String txt) {
		Toast t = Toast.makeText(this, txt, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}

	// test serialNum
	private boolean testSerial(String id, int len) {
		Pattern pattern = Pattern.compile("^[A-F0-9]{" + len + "}$");
		Matcher matcher = pattern.matcher(id);
		boolean b = matcher.matches();
		return b;
	}
	
	private String getSerialNumber() {
		StringBuilder tmp = new StringBuilder(16);
		for(int i = 0; i < LEN; i++){
			tmp.append(serialNumber[i].getText().toString()); 
		}
		return tmp.toString();
	}
	
	private void ensureSerial() {
		String s = getSerialNumber();
		if(s.length() != 16){
			serialHint.setText("请输入十六位序列号");
			hideTitleSubmit();
			return;
		}
		if (gn.Verify(s)) {
			serialHint.setText("请点击【激活】，激活序列号");
			showTitleSubmit();
		} else {
			hideTitleSubmit();
			serialHint.setText("十六位序列号有误，请检查");
		}
	}
	
	private void submit() {
		InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(getCurrentFocus()
				.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		final String serialNum = getSerialNumber().toUpperCase();
		if (!testSerial(serialNum, 16)) {
			errText("序列号必须是16位数字字母");
			return;
		}
		if (!gn.Verify(serialNum)) {
			errText("序列号输入不正确，请仔细核对");
			return;
		}
		hideTitleSubmit();
		serialHint.setText("正在验证序列号...");
		for(int i = 0; i < LEN; i++){
			serialNumber[i].setEnabled(false);
		}
		progressDialog = PublicHelper.getProgressDialog(0,
				this, // context 
				"",	// title 
				"激活中...", // message 
				true, //进度是否是不确定的，这只和创建进度条有关 
				true,
				this);
		(new Thread() {
			public void run() {
	            s = new SerialNumberVerifyMessage();
            	s.setKSN_54(serialNum)
            	.setSource_16(getSource());
	            boolean isOK = false;
	            String error = "";
	            String[] verifyOk = new String[2];
	            try {
	            	if(!s.isBitmapValid()) throw new RuntimeException("BitmapError");
	               	IsoMessage res = s.request();
	               	if(res == null) {
	               		isOK = true;
	               		verifyOk = null;
	                } else { 
		                String statusCode = res.getField(39).toString();
		                if (statusCode.equals("00")) {
		                	isOK = true;
			                verifyOk[0] = myKSN;
			                verifyOk[1] = serialNum;
		                } else {
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
	            	e.printStackTrace();
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
