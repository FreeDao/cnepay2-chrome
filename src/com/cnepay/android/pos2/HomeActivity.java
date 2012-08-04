package com.cnepay.android.pos2;

import com.tangye.android.iso8583.POSHelper;
import com.tangye.android.utils.PublicHelper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class HomeActivity extends UIBaseActivity 
		implements View.OnClickListener {

	private ImageView login;
	private ImageView register;
	private ImageView rebind;
	private ImageView about;
	
	private static final int ENABLE_TIMEOUT = 1000;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/**
		 * already checked in LoaderActivity
		 * 
		 * if(UpdateService.needUpgrade()) {
			Intent intent = new Intent(this, UpdateActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
		}*/

		if(POSHelper.getSessionID() > 0) {
            Intent intent = new Intent(HomeActivity.this, ManagerActivity.class);
            startActivity(intent);
            finish();
            return;
        }
		setContentView(R.layout.activity_home);
		
		login = (ImageView)findViewById(R.id.splash_login);
		register = (ImageView)findViewById(R.id.splash_register);
		rebind = (ImageView)findViewById(R.id.splash_rebind);
		about = (ImageView)findViewById(R.id.splash_about);
		
		login.setOnClickListener(this);
		register.setOnClickListener(this);
		rebind.setOnClickListener(this);
		about.setOnClickListener(this);
	}

	@Override
	public void onClick(final View v) {
		v.setEnabled(false);
		v.postDelayed(new Runnable() {
			public void run() {
				v.setEnabled(true);
			}
		}, ENABLE_TIMEOUT);
		Intent intent = null;
		switch(v.getId()) {
		case R.id.splash_login:
			intent = new Intent(this, LoginActivity.class);
			startCallbackActivity(intent);
			break;
		case R.id.splash_register:
			AlertDialog.Builder builder = PublicHelper.getAlertDialogBuilder(this);
            builder.setMessage("请认真填写银行卡号、序列号、手机号码，一经注册成功，实名认证通过将不得更改");
            builder.setTitle("提示");
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	Intent intent = new Intent(HomeActivity.this, VerifySerialNumberActivity.class);
        			startActivity(intent);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
			break;
		case R.id.splash_rebind:
			intent = new Intent(this, ReBindActivity.class);
			startActivity(intent);
			break;
		case R.id.splash_about:
			intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			break;
		}
	}
	
	
}
