package com.cnepay.android.pos2;

import com.tangye.android.utils.PublicHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

public class UpdateActivity extends Activity implements OnCancelListener{
	
	private final static String TAG = "UpdateActivity";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		SharedPreferences sp = getSharedPreferences("settings", 0);
        String ver = sp.getString("ver", null);
        final String src = sp.getString("src", null);
        String ext = sp.getString("ext", null);
        
        Log.v(TAG, "ver = " + ver);
        //Log.v(TAG, "src = " + src);
        //Log.v(TAG, "ext = " + ext);
        
        if(ver == null || src == null) {
            finish();
            return;
        }
        
        if(ext != null) {
            // TODO extra contains many user-defined domains, we treat the first to be
            // the size of this apk
            String[] extras = ext.split("\\s+");
            if(extras.length > 0) {
                ver += "\n大小：" + extras[0];
                //Log.v(TAG, "ver = " + ver);
            }
            if(extras.length > 1) {
            	ver += "日志：\n" + extras[1];
            }
        }
        
		AlertDialog.Builder builder = PublicHelper.getAlertDialogBuilder(this);
        builder.setTitle("更新提示")
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setMessage("检测到程序更新，必须更新后才能继续使用\n\n版本：" + ver)
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
		         finish();
			}
		})
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int which) {
        		Uri uri = Uri.parse(src);
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i);
                finish();
        	}
        })
        .setOnCancelListener(this);
        AlertDialog d = builder.create();
        d.setCanceledOnTouchOutside(false);
        d.show();
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		finish();
	}
    
}
