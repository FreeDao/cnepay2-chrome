package com.cnepay.android.pos2;

import com.cnepay.android.pos2.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;

public class LoaderActivity extends Activity implements Runnable {
	
	private final static String TAG = "LoaderActivity";
	private Handler mHandler;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService(new Intent(this, UpdateService.class));
		mHandler = new Handler();
		if (SystemClock.elapsedRealtime() - UIBaseActivity.getLastLeaveTime() < 10000) {
			run();
			return;
		}
		mHandler.postDelayed(this, 2500);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);
		findViewById(R.id.loader_splash).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mHandler.removeCallbacks(LoaderActivity.this);
					run();
				}
				return false;
			}
		});
	}

	@Override
	public void run() {
		Log.v(TAG, "start loading");
		startActivity(new Intent(this, LoginActivity.class));
		finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacks(this);
	}
    
}
