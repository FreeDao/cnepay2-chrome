package com.cnepay.android.pos2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tangye.android.utils.MyDrawView;
import com.tangye.android.utils.PicDrawTransTransfer;

public class SignNameTransferActivity extends UIBaseActivity implements View.OnClickListener {
	private static final String TAG = "SignNameTransferActivity";
	private static final String UPLOAD_URL = "http://203.81.23.4:18080/tompms/transCurrent/uploadVoucher";
	private static final int TIMEOUTSOCKET = 10000;
    private static final int TIMEOUTCONNECTION = 10000;
    private static final int SIGN_SUCCESS = 0;
    private static final int SIGN_FAILURE = 1;
	
	Button cleanSignName;
	Button finish;
	MyDrawView signature;
	ProgressDialog progressDialog;
	String[] allString;
	private String SavePath;
	private String TransferHistory;
	Handler mHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sign);
		setTitle("签名凭据");
		setActivityPara(true, true);
		cleanSignName = (Button)findViewById(R.id.clean_sign);
		finish = (Button)findViewById(R.id.upload_ticket);
		signature = (MyDrawView)findViewById(R.id.signature_draw);
		
		Intent intent = getIntent();
		allString = intent.getStringArrayExtra("allString");
		if(allString == null || allString.length != 12) {
			makeNoitce("数据丢失，交易成功");
			finish();
		}
		
		mHandler = new Handler() {
			
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case SIGN_SUCCESS:
					String deleteP1 = SavePath + "/Screen_1.png";
					File deleteFile1 = new File(deleteP1);
					if(deleteFile1.exists()) {
        				deleteFile1.delete();
        			}
					if(progressDialog != null) {
			        	progressDialog.cancel();
			        }
					makeNoitce("上传成功");
					finish();
					break;
				case SIGN_FAILURE:
					if(progressDialog != null) {
        				progressDialog.cancel();
        				String temp = (String)msg.obj;
        				if(temp != null) {
        					makeNoitce(temp);
        				} else {
        					makeNoitce("上传失败");
        				}
        			}
					break;
				}
			}
		};
		
		finish.setOnClickListener(this);
		cleanSignName.setOnClickListener(this);
	}
	
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.upload_ticket:
			progressDialog = ProgressDialog.show(SignNameTransferActivity.this, // context 
					"", // title 
					"转账凭证上传中...",  // message 
					true, //进度是否是不确定的，这只和创建进度条有关 
					false);
			String pathSD = getSDPath();
			if(null == pathSD)
			{
				Toast.makeText(SignNameTransferActivity.this, "没有SD卡无法保存", Toast.LENGTH_LONG).show();
				progressDialog.cancel();
				return;
			}
			View signname = signature;
			signname.setDrawingCacheEnabled(true);
			Bitmap Bmp = signname.getDrawingCache();
			SavePath = pathSD + "/personInfo"; 
			TransferHistory = SavePath + "/TransferHistory";
			FileOutputStream fos = null;
			try {  
		        File path = new File(SavePath);
		        File createPath = new File(TransferHistory);
		        //文件  
		        String filepath = SavePath + "/Screen_1.png";  
		        File file = new File(filepath);  
		        if(!path.exists()){  
		            path.mkdirs();  
		        }
		        if(!createPath.exists()){  
		        	createPath.mkdirs();  
		        }
		        if (!file.exists()) {  
		            file.createNewFile();  
		        }  
		        fos = new FileOutputStream(file);  
		        if (null != fos) {  
		            Bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
		        }  
		    } catch (Exception e) { 
		        e.printStackTrace();  
		    } finally {
		    	try{
		    		if(fos != null) {
		    			fos.flush();
			    		fos.close();
			    	}
		    		signname.setDrawingCacheEnabled(false);
		    	} catch (Exception e) {
		    		e.printStackTrace();
		    	}
		    	
		    }
			PicDrawTransTransfer pic = new PicDrawTransTransfer(SignNameTransferActivity.this, allString);
			if (pic.isDrawingCacheEnabled() == false) {
				pic.setDrawingCacheEnabled(true);
			}
			Bitmap drawingCache = Bitmap.createBitmap(290, 505, Bitmap.Config.RGB_565);
			Canvas cvs = new Canvas();
			cvs.setBitmap(drawingCache);
			pic.draw(cvs);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			drawingCache.compress(CompressFormat.PNG, 0, bos);
			File file = null;
			FileOutputStream stream = null;
			try { 
				if (bos.size() > 0) {
					file = new File(TransferHistory + "/" + allString[10] + ".png");
					if (!file.exists()) {  
			            file.createNewFile();
			        }
					stream = new FileOutputStream(file);
					bos.writeTo(stream);
				}
			} catch (Exception e) { 
		        e.printStackTrace();  
		    } finally {
		    	try{
		    		if(bos != null) {
		    			bos.flush();
		    			bos.close();
		    		}
		    		if(stream != null) {
		    			stream.flush();
		    			stream.close();
			    	}
		    		pic.setDrawingCacheEnabled(false);
		    	} catch (Exception e) {
		    		e.printStackTrace();
		    	}
		    }
			new Thread(TAG) {
				public void run() {
					String err = "";
					File upfile = null;
					upfile = new File(TransferHistory + "/" + allString[10] + ".png");
					HttpParams httpParameters;
					httpParameters = new BasicHttpParams();
			    	HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUTCONNECTION);
			    	HttpConnectionParams.setSoTimeout(httpParameters, TIMEOUTSOCKET); 
			    	HttpClient client = new DefaultHttpClient(httpParameters);
					HttpPost httppost = new HttpPost(UPLOAD_URL);
					try {
			    		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			    		reqEntity.addPart("transId", new StringBody(allString[9]));
			        	reqEntity.addPart("img", new FileBody(upfile));
			        	
			        	httppost.setEntity(reqEntity);
				    	HttpResponse response = client.execute(httppost);
				    	if(response.getStatusLine().getStatusCode()==HttpStatus.SC_OK) {
				    		Message msg = mHandler.obtainMessage(SIGN_SUCCESS);
		                    mHandler.sendMessage(msg);
				    	} else {
				    		Message msg = mHandler.obtainMessage(SIGN_FAILURE);
							err = "服务器错误";
	                        msg.obj = err;
	                        mHandler.sendMessage(msg);
				    	}
					} catch (ClientProtocolException e) { 
						Message msg = mHandler.obtainMessage(SIGN_FAILURE);
						err = "协议错误，请重新上传";
                        msg.obj = err;
                        mHandler.sendMessage(msg);
			    	} catch (IOException e) {
			    		Message msg = mHandler.obtainMessage(SIGN_FAILURE);
						err = "连接错误，请重新上传";
                        msg.obj = err;
                        mHandler.sendMessage(msg);
			    	} catch (Exception e) {
			    		Message msg = mHandler.obtainMessage(SIGN_FAILURE);
						err = "未知错误";
                        msg.obj = err;
                        mHandler.sendMessage(msg);
			    	}
				}
			}.start();
			break;
		case R.id.clean_sign:
			signature.clear();
			break;
		}
	}
	
	private void makeNoitce(String err) {
		Toast t = Toast.makeText(this, err, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}
}
