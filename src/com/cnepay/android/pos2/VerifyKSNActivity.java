package com.cnepay.android.pos2;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.protocol.KSNVerifyMessage;
import com.tangye.android.utils.PublicHelper;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class VerifyKSNActivity extends UIBaseActivity implements
		View.OnClickListener{
	
	private TextView hintPlugin;
	private String myKSN;
	private Handler mHandler;
	private ProgressDialog progressDialog;

	private static final String TAG = "VerifySerialNumberActivity";
	private static final int SUCCESS = 0;
    private static final int FAILURE = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_verify_ksn);
		setTitle("刷卡器激活");
		setTitleSubmitText("激活");
		hideTitleSubmit();
		btnSubmit.setOnClickListener(this);
		setActivityPara(false, false, new KsnTestListener() {
			@Override
			public boolean test(String ksn) {
				if(PublicHelper.isEmptyString(ksn)){
					errText("读取刷卡器错误");
				}
				myKSN = ksn;
				showTitleSubmit();
				return true;
			}
		});

		hintPlugin = (TextView)findViewById(R.id.verify_ksn_hint);

		
		mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                case SUCCESS:
                	String all = (String)msg.obj;
                    if(progressDialog != null && all != null) {
                        progressDialog.cancel();
                        progressDialog = null; // For not fade card number
                        errText("刷卡器激活成功");
                        Intent intent = new Intent(VerifyKSNActivity.this, VerifySerialNumberActivity.class);
                        intent.putExtra("ksn", all);
                        startActivity(intent);
                        finish();
                    }
                    break;
                case FAILURE:
                    if(progressDialog != null) {
                        progressDialog.cancel();
                        errText((String)msg.obj);
                        showTitleSubmit();
            			finish();
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

	@Override
	public void onError(int err) {
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
				errText("非法刷卡器，请使用正规刷卡器");
				break;
			case E_API2_INTERRUPT:
				break;
			default:
			}
		} else {
			switch (err) {
			case E_API2_INVALID_DEVICE:
				errText("无法识别该刷卡器，请选择新的刷卡器或者重新拔插");
				break;
			case E_API2_INVALID_KSN:
				errText("非法刷卡器，请使用已注册绑定的刷卡器");
				break;
			case E_API2_INTERRUPT:
				break;
			}
		}
	}
	
	@Override
	public void onPlugin() {
		super.onPlugin(); // UIBASE action
		this.showTitleSubmit();
		hintPlugin.setText("请点击【激活】，激活刷卡器");
	}

	@Override
	public void onPlugout() {
		super.onPlugout(); // UIBASE action
		hintPlugin.setHint("请插入刷卡器");
	}
	

	/************ private function *************/

	private void errText(String txt) {
		Toast t = Toast.makeText(this, txt, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}

	private void submit() {
		hideTitleSubmit();
		progressDialog = PublicHelper.getProgressDialog(this, // context 
				"",	// title 
				"刷卡器激活中...", // message 
				true, //进度是否是不确定的，这只和创建进度条有关 
				false);
		(new Thread() {
			public void run() {
			    KSNVerifyMessage s = new KSNVerifyMessage();
	            s.setKSN_58(myKSN);
	            
	            boolean isOK = false;
	            String error = "";
	            try {
	            	
	                if(!s.isBitmapValid()) throw new RuntimeException("BitmapError");
	                IsoMessage resp = s.request();
	                if(resp == null) {
	                	isOK = false;
	                	error = "未知错误";
	                }else{ 
		                String statusCode = resp.getField(39).toString();
	                	if (statusCode.equals("00")) {
	                		isOK = true;
	                		error = myKSN;
	                	}else{
	                		error = getError(statusCode);
	                		isOK = false;
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
                    mHandler.obtainMessage(SUCCESS, error).sendToTarget();
                }
			}
		}).start();
		
	}
	
	/************* end private function **************/


}
