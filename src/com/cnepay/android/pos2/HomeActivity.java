package com.cnepay.android.pos2;

import com.tangye.android.iso8583.POSHelper;

import android.content.Intent;
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
			intent = new Intent(HomeActivity.this, VerifySerialNumberActivity.class);
        	startActivity(intent);
			break;
		case R.id.splash_rebind:
			intent = new Intent(this, ReBindActivity.class);
			startActivity(intent);
			break;
		case R.id.splash_about:
			intent = new Intent(this, AboutUsActivity.class);
			startActivity(intent);
			break;
		}
	}
	
	
}
