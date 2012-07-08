package com.cnepay.android.pos2;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tangye.android.utils.MyDrawView;
import com.tangye.android.utils.VoucherDraw;

public class SignatureActivity extends UIBaseActivity implements View.OnClickListener {
	
	private static final String TAG = "SignatureActivity";
	
	private Button cleanSignName;
	private Button finish;
	private MyDrawView signature;
	private String SavePath;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sign);
		setTitle("签名凭据");
		setActivityPara(true, true);
		cleanSignName = (Button)findViewById(R.id.clean_sign);
		finish = (Button)findViewById(R.id.upload_ticket);
		signature = (MyDrawView)findViewById(R.id.signature_draw);
		
		finish.setOnClickListener(this);
		cleanSignName.setOnClickListener(this);
	}
	
	private void saveSignature() {
		String pathSD = getSDPath();
		String filepath = null;
		Log.v(TAG, "pathSD = " + pathSD);
		if(null == pathSD) {
			makeNoitce("没有SD卡无法保存");
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
		Bitmap Bmp = signature.getBitmap();
		SavePath = pathSD + "/personInfo";
		FileOutputStream fos = null;
		try {  
	        File path = new File(SavePath);
	        if(!path.exists()) {
	        	if(!path.mkdir()) {
	        		makeNoitce("目录错误！");
					setResult(RESULT_CANCELED);
					finish();
					return;
	        	}
	        }
	        //文件
	        filepath = SavePath + "/signature.png";
	        File file = new File(filepath);
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
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    	signature.clear();
	    }
		
		if (filepath != null) {
			Intent i = new Intent();
			i.putExtra(VoucherDraw.INTENT_EXTRA, filepath);
			setResult(RESULT_OK, i);
			finish();
		} else {
			setResult(RESULT_CANCELED);
			finish();
		}
	}
	
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.upload_ticket:
			saveSignature();
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
