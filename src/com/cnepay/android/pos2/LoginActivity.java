package com.cnepay.android.pos2;


import com.tangye.android.iso8583.POSNative;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends UIBaseActivity implements View.OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		setTitleSubmitText("注册");
		showTitleSubmit();
		btnSubmit.setOnClickListener(this);
		this.setTitleSubmitText(POSNative.getNativeK("d", "pp"));
	}

	@Override
	public void onClick(View v) {
		Intent i = new Intent(this, RegisterActivity.class);
		startActivity(i);
	}
	
}
