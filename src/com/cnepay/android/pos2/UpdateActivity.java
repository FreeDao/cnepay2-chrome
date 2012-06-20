package com.cnepay.android.pos2;

import com.tangye.android.utils.PublicHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class UpdateActivity extends Activity{
	private final String TAG = "UpdateActivity";
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences sp = getSharedPreferences("settings", 0);
        String ver = sp.getString("ver", null);
        final String src = sp.getString("src", null);
        String ext = sp.getString("ext", null);
        
        Log.v(TAG, "ver = " + ver);
        Log.v(TAG, "src = " + src);
        Log.v(TAG, "ext = " + ext);
        
        if(ver == null || src == null) {
            finish();
            return;
        }
        
        if(ext != null) {
            // TODO extra contains many user-defined domains, we treat the first to be
            // the size of this apk
            String[] extras = ext.split("\\s+");
            if(extras.length > 0) {
                ver += " " + extras[0];
                Log.v(TAG, "ver = " + ver);
            }
        }
		AlertDialog.Builder builder = PublicHelper.getAlertDialogBuilder(UpdateActivity.this);
        builder.setTitle("更新")
        .setIcon(android.R.drawable.ic_dialog_info)
        .setCancelable(false)
        .setMessage("检测到程序更新，请更新后继续使用")
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
		         finish();
			}})
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int which) {
        		Uri uri = Uri.parse(src);
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i);
                finish();
        	}
        });
        builder.create().show();
	}
	
    
}
