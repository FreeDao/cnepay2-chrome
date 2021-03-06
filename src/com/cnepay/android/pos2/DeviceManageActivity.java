package com.cnepay.android.pos2;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.tangye.android.dialog.AlertDialogBuilderWrapper;
import com.tangye.android.dialog.CustomProgressDialog;
import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.POSEncrypt;
import com.tangye.android.iso8583.POSHelper;
import com.tangye.android.iso8583.POSSession;
import com.tangye.android.iso8583.protocol.KSNRaplaceMessage;
import com.tangye.android.utils.PublicHelper;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceManageActivity extends UIBaseActivity implements OnClickListener {
	
	private TextView boundKSN;
	private TextView userHint;
	private String currentKsn = null;
	private Handler mHandler;
	private boolean isProcessing;
	private CustomProgressDialog progressDialog;
	
	private final static int SUCCESS = 0;
	private final static int FAILURE = 1;
	
	private final String TAG = "DeviceManageActivity";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_manage);
		setTitle("设备管理");
		initUI();
		setActivityPara(true, false, new KsnTestListener() {
			@Override
			public boolean test(String ksn) {
				POSSession session = POSHelper.getPOSSession();
				if (session == null) {
					userHint.setText("请重新登录");
					hideTitleSubmit();
					currentKsn = null;
					return false;
				}
				if(session != null && session.testKsn(ksn)) {
					userHint.setText("请插入新刷卡器");
					hideTitleSubmit();
					currentKsn = null;
				} else {
					// 获得新的ksn
					if(!isProcessing){
						showTitleSubmit();
						userHint.setText("请点击右上角按钮替换刷卡器");
					}
					currentKsn = ksn;
				}
				return true;
			}
		});

		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				AlertDialogBuilderWrapper builder;
				switch(msg.what) {
				case SUCCESS:
					if(progressDialog != null){
						progressDialog.dismiss();
					}
					builder = PublicHelper.getAlertDialogBuilder(DeviceManageActivity.this);
				    builder.setTitle("刷卡器替换")
				    .setIcon(android.R.drawable.ic_dialog_info)
				    .setMessage("刷卡器替换成功！新的刷卡器号码为\n" + (String)msg.obj)
				    .setCancelable(true)
				    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				    	public void onClick(DialogInterface dialog, int which) {
				    		finish();
				    	}
				    });
				    builder.show();
				    //清理现场
					boundKSN.setText((String)msg.obj);
					userHint.setText("绑定成功。\n您可以继续绑定新的刷卡器。");
					break;
				case FAILURE:
					if(progressDialog != null){
						progressDialog.cancel();
					}
					String error = (String)msg.obj;
					builder = PublicHelper.getAlertDialogBuilder(DeviceManageActivity.this);
				    builder.setTitle("刷卡器替换")
				    .setIcon(android.R.drawable.ic_dialog_alert)
				    .setMessage(error)
				    .setCancelable(true)
				    .setPositiveButton(android.R.string.ok, null);
				    builder.show();
					userHint.setText("请插入其他刷卡器");
					break;
				}
				if(isPlugged()) {
					showTitleSubmit();
				}
			}
		};
		
	}
	
	private void initUI(){
		setTitleSubmitText("刷卡器替换");
		boundKSN = (TextView)findViewById(R.id.dev_manage_current_ksn);
		userHint = (TextView)findViewById(R.id.dev_manage_user_hint);
		POSSession posSession = POSHelper.getPOSSession();
		final String ksn = posSession.getKsn();
		if(ksn == null || ksn.equals("")) {
			finish();
			return;
		}
		boundKSN.setText(ksn);
		btnSubmit.setOnClickListener(this);
	}
	
	public void onPlugout(){
		super.onPlugout();
		hideTitleSubmit();
		userHint.setText("请插入刷卡器");
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
		
		progressDialog = PublicHelper.getProgressDialog(DeviceManageActivity.this, // context 
				"",	// title 
				"刷卡器替换中...", // message 
				true, 
				false);
		
		final String newKsn = currentKsn;
		(new Thread(TAG) {
			public void run() {
				KSNRaplaceMessage s = new KSNRaplaceMessage();
				s.setTerminalMark_41(POS.getPOSDecrypt(POS.TERMINAL))
				.setUserMark_42(POS.getPOSDecrypt(POS.USERMARK))
				.setKSN_58(newKsn);
				POS.close();
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
	                		session.setKSN(newKsn);
	                		error = session.getKsn();
	                		isOk = true;
	                	} else {
                    		error = getError(statusCode);
	                	}
	                } else {
	                	// Manually stop client from user's aspect
	                	// not going to happen
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
	            isProcessing = false;
			}
		}).start();
		return true;
	}

	@Override
	public void onClick(View v) {
		hideTitleSubmit();
		AlertDialogBuilderWrapper builder = PublicHelper.getAlertDialogBuilder(DeviceManageActivity.this);
	    builder.setTitle("刷卡器替换")
	    .setIcon(android.R.drawable.ic_dialog_alert)
	    .setMessage("是否替换刷卡器？" + "\n原刷卡器将不能二次绑定")
	    .setCancelable(false)
	    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	    		showTitleSubmit();
	    	}
	    })
	    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
				if(!processKSNReplace()){
					isProcessing = false;
					showTitleSubmit();
				}
	    	}
	    });
	    builder.show();
	}
	
}
