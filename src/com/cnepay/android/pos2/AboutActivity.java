package com.cnepay.android.pos2;

import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends UIBaseActivity{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_about);
		setTitle("关于我刷");
		
		TextView tv = (TextView) findViewById(R.id.about_version);
		tv.setText("我刷版本：V" + getVersion());
	}
}
