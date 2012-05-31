
package com.cnepay.android.pos2;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UIBaseActivity extends BaseActivity {

	private Toast mToast;
	private boolean notNotify = false;
	private static long lastLeaveTime = 0;
	private boolean mPlugged = false;

	protected ImageView imgIndicator = null;
	protected TextView txtTitle = null;
	protected ViewGroup btnSubmit = null;

	@Override
	public void setTitle(CharSequence title) {
		txtTitle.setText(title);
	}

	@Override
	public void setTitle(int resid) {
		txtTitle.setText(resid);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mToast = new Toast(this);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		// setContentView(R.layout.main);
	}

	@Override
	public void onPlugin() {
		imgIndicator.setImageResource(R.drawable.signup_reader);
		mPlugged = true;
		if (notNotify) {
			notNotify = false;
			return;
		}
		mToast.cancel();
		mToast = new Toast(this);
		mToast.setDuration(Toast.LENGTH_SHORT);
		mToast.setGravity(Gravity.CENTER, 0, 0);
		View view = View.inflate(this, R.layout.toast_indicator, null);
		ImageView im = (ImageView)view.findViewById(R.id.toast_indi);
		im.setImageResource(R.drawable.hud_square_connected);
		mToast.setView(view);
		mToast.show();
	}

	@Override
	public void onPlugout() {
		imgIndicator.setImageResource(R.drawable.signup_reader_off);
		mPlugged = false;
		if (notNotify) {
			notNotify = false;
			return;
		}
		mToast.cancel();
		mToast = new Toast(this);
		mToast.setDuration(Toast.LENGTH_SHORT);
		mToast.setGravity(Gravity.CENTER, 0, 0);
		View view = View.inflate(this, R.layout.toast_indicator, null);
		ImageView im = (ImageView)view.findViewById(R.id.toast_indi);
		im.setImageResource(R.drawable.hud_square_disconnected);
		mToast.setView(view);
		mToast.show();
	}

	@Override
	public void setContentView(int view) {
		super.setContentView(view);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title); // title为自己标题栏的布局
		imgIndicator = (ImageView)getWindow().findViewById(R.id.icon_indicator);
		imgIndicator.setClickable(true);
		/*
		 * if (SwipeService.getHeadsetState() == 0) {
		 * imgIndicator.setImageResource(R.drawable.signup_reader_off); } else
		 * if (SwipeService.getHeadsetState() == 1) {
		 * imgIndicator.setImageResource(R.drawable.signup_reader); }
		 */
		txtTitle = (TextView)getWindow().findViewById(R.id.title_name);
		btnSubmit = (ViewGroup)getWindow().findViewById(R.id.title_submit);
		// init();
	}

	@Override
	protected void onResume() {
		if (SystemClock.elapsedRealtime() - lastLeaveTime < 10000) {
			notNotify = true;
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		lastLeaveTime = SystemClock.elapsedRealtime();
	}

	/********** user function **************/
	/**
	 * 重新计算leave时间
	 */
	public static void updateLastLeaveTime() {
		lastLeaveTime = SystemClock.elapsedRealtime();
	}

	/**
	 * 显示actionbar的button
	 */
	public void showTitleSubmit() {
		if (btnSubmit != null && btnSubmit.getVisibility() != View.VISIBLE) {
			TranslateAnimation ta = new TranslateAnimation(200.0f, 0.0f, 0.0f, 0.0f);
			ta.setDuration(200);
			ta.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					btnSubmit.setEnabled(true);
					getWindow().findViewById(R.id.title_seprater).setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationStart(Animation animation) {
				}

			});
			btnSubmit.setVisibility(View.VISIBLE);
			btnSubmit.startAnimation(ta);
		}
	}

	/**
	 * 隐藏actionbar的button
	 */
	public void hideTitleSubmit() {
		if (btnSubmit != null && btnSubmit.getVisibility() == View.VISIBLE) {
			TranslateAnimation ta = new TranslateAnimation(0.0f, 200.0f, 0.0f, 0.0f);
			ta.setDuration(200);
			ta.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					btnSubmit.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationStart(Animation animation) {
				}

			});
			getWindow().findViewById(R.id.title_seprater).setVisibility(View.GONE);
			btnSubmit.setEnabled(false);
			btnSubmit.startAnimation(ta);
		}
	}

	/**
	 * 检查刷卡器的插入拔出状态
	 * 
	 * @return 如果拔出或者未准备妥当，则返回false，否则返回true
	 */
	public boolean isPlugged() {
		return mPlugged;
	}
	/*********** end user function **************/
}
