package com.cnepay.android.pos2;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class APIDemoActivity extends UIBaseActivity implements View.OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		btnSubmit.setOnClickListener(this);

	}
	
	
	@Override
	public void onPlugin() {
		super.onPlugin();
		this.showTitleSubmit();
	}
	
	@Override
	public void onPlugout() {
		super.onPlugout();
		this.hideTitleSubmit();
	}

	@Override
	public void onClick(View v) {
		this.startSwipe();
		this.hideTitleSubmit();
	}
	
	@Override
	public void onComplete(String cn) {
		Toast.makeText(this, ci.toString(), Toast.LENGTH_SHORT).show();
		this.showTitleSubmit();
	}
	
	@Override
	public void onError(int err) {
		Toast.makeText(this, "Error = " + err, Toast.LENGTH_SHORT).show();
		// 刷卡时，插拔卡可能会引起decodeError
		if (this.isPlugged()) {
			this.showTitleSubmit();
		}
	}
	
	@Override
	public void onTimeout() {
		Toast.makeText(this, "timeout", Toast.LENGTH_SHORT).show();
		this.showTitleSubmit();
	}
	
}
