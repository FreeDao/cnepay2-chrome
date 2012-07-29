package com.cnepay.android.pos2;

import android.content.Intent;
import android.os.Bundle;


public class AboutActivity extends UIBaseActivity{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(UpdateService.needUpgrade()){
			Intent intent = new Intent(this, UpdateActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
		}
		
		
		setContentView(R.layout.activity_about);
		setTitleSubmitText("About");
	}
}
