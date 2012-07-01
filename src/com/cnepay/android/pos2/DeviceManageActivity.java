package com.cnepay.android.pos2;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Calendar;

import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.POSEncrypt;
import com.tangye.android.iso8583.POSHelper;
import com.tangye.android.iso8583.POSSession;
import com.tangye.android.iso8583.protocol.KSNRaplaceMessage;
import com.tangye.android.iso8583.protocol.TransferMessage;
import com.tangye.android.utils.PublicHelper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceManageActivity extends UIBaseActivity {
	
	private EditText ksnId;
	private Button replace;
	private TextView hint;
	private TextView currentKSN;
	private Handler mHandler;
	private boolean isProcessing;
	
	private final static int REPLACE_SUCCESS 	= 0;
	private final static int REPLACE_FAILURE 	= 1;
	
	
	private final String TAG = "DeviceManageActivity";
	
	private ProgressDialog noSwipeCardHint = null;
	private ProgressDialog progressDialog;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_manage);
		setTitle("设备管理");
		setActivityPara(true, false);
		btnSubmit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
			}});
		initUI();
		
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what){
				case REPLACE_SUCCESS:
					if(progressDialog != null){
						progressDialog.dismiss();
					}
					makeError("ksn替换成功");
					break;
				case REPLACE_FAILURE:
					if(progressDialog != null){
						progressDialog.dismiss();
					}
					String error = (String)msg.obj;
					makeError(error);
					break;
					
				}
			}
		};
		
	}
	
	private void initUI(){
		ksnId = (EditText)findViewById(R.id.edit_text_ksnId);
		replace = (Button)findViewById(R.id.button_replace);
		hint = (TextView)findViewById(R.id.text_view_hint);
		currentKSN = (TextView)findViewById(R.id.text_view_currentKsn);
		POSSession posSession = POSHelper.getPOSSession();
		final String ksn = posSession.getKsn();
		if(ksn == null || ksn.equals("")){
			//TODO process can not get ksn
		}
		ksnId.setText(ksn);
		
		replace.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v) {
				if(!DeviceManageActivity.this.isPlugged()){
					noSwipeCardHint = new ProgressDialog(DeviceManageActivity.this);
        			noSwipeCardHint.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        			noSwipeCardHint.setMessage("请插入刷卡器...");
        			noSwipeCardHint.show();
				}else{
					AlertDialog.Builder builder = PublicHelper.getAlertDialogBuilder(DeviceManageActivity.this);
			        builder.setTitle("刷卡器替换")
			        .setIcon(android.R.drawable.ic_dialog_info)
			        .setMessage("是否替换刷卡器？" + "\n原刷卡器将不能二次绑定")
			        .setCancelable(false)
			        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							
						}
					})
			        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			        	public void onClick(DialogInterface dialog, int which) {
		        			processKSNReplace();
			        	}
			        });
			        builder.create().show();
				}
			}
		});
	}
	
	@Override
	public void onPlugin() {
		super.onPlugin();
		if(noSwipeCardHint != null){
			noSwipeCardHint.dismiss();
			noSwipeCardHint = null;
		}
	}
	
	private void makeError(String err) {
		Toast t = Toast.makeText(this, err, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && isProcessing) {
        	makeError("不能中止交易");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
	
	private boolean processKSNReplace(){
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
		final POSEncrypt POS = POSHelper.getPOSEncrypt(DeviceManageActivity.this, name);
		if(POS == null){
			makeError("POS机出错！");
			return false;
		}
		
		progressDialog = ProgressDialog.show(DeviceManageActivity.this, // context 
				"",	// title 
				"刷卡器替换中...", // message 
				true, 
				false);
		
		(new Thread(TAG) {
			public void run() {
				KSNRaplaceMessage s = new KSNRaplaceMessage();
				s.setTerminalMark_41(POS.TERMINAL)
				.setUserMark_42(POS.USERMARK)
				.setKSN_58(session.getKsn());
				boolean isOk = false;
	            String error = "";
	            try {
	                if(!s.isBitmapValid()) throw new RuntimeException("BitmapError");
	                IsoMessage resp = s.request();
	                if(resp != null) {
	                	Log.v(TAG, "resp != null");
	                	String statusCode = resp.getField(39).toString();
	                	Log.v(TAG, "statusCode = " + statusCode);
	                	if (statusCode.equals("00")) {
	                		
		                    isOk = true;
	                	} else {
                    		error = getError(statusCode);
	                	}
	                } else {
	                	// Manually stop client from user's aspect
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
	            	//TODO failed
	            	Log.v(TAG, "failed");
	            	mHandler.obtainMessage(REPLACE_FAILURE, error).sendToTarget();
	            } else {
	            	//TODO success
	            	Log.v(TAG, "success");
	            	mHandler.obtainMessage(REPLACE_SUCCESS, error).sendToTarget();
	            }
	            isProcessing = false;
			}
		}).start();
		return true;
	}
	
}
