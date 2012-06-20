
package com.cnepay.android.pos2;

import com.tangye.android.iso8583.POSHelper;
import com.tangye.android.iso8583.POSSession;
import com.tangye.android.utils.PublicHelper;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
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
	private boolean isNeedSession = false;
	private OnCNAPSResultListener cnapsListener = null;

	protected ImageView imgIndicator = null;
	protected TextView txtTitle = null;
	protected ViewGroup btnSubmit = null;
	
	private final String TAG = "UIBaseActivity";
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
		Log.v(TAG, "start service");
		startService(new Intent(this, UpdateService.class));
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
		if (notNotify) {
			notNotify = false;
			mPlugged = false;
			return;
		}
		if (!mPlugged) return;
		mPlugged = false;
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
	public void onError(int error) {
		// This can be override totally outside session
		// Otherwise it must make sure to handle 
		// #E_API2_INVALID_DEVICE# error
		if (isNeedSession) {
			if (error == E_API2_INVALID_DEVICE) {
				mToast.cancel();
				mToast = Toast.makeText(this, "非法读卡器！请使用注册时绑定的读卡器!", Toast.LENGTH_SHORT);
				mToast.setGravity(Gravity.CENTER, 0, 0);
				mToast.show();
			}
		}
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
		mPlugged = false; // wait for KSN test result
		
		/**** login session control ****/
		if(isNeedSession && POSHelper.getSession() <= 0) {
			finish();
        }
		/******login session control end*****/
		
		super.onResume();
		
		if(UpdateService.needUpgrade()) {
				signoff();
				finish();
        }
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		lastLeaveTime = SystemClock.elapsedRealtime();
	}
	
	private static final int ABOUT_MENU_ID = Menu.FIRST;
    private static final int QUIT_MENU_ID = Menu.FIRST + 1;
    
    private void signoff() {
    	POSHelper.deleteSession();
    	finish();
    }
    
    private void showAboutDialog() {
    	// TODO show about dialog
    	Toast.makeText(this, "关与菜单", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, ABOUT_MENU_ID, 0, "关于")
        .setShortcut('2', 'a')
        .setIcon(android.R.drawable.ic_menu_info_details);
        if(isNeedSession) {
            menu.add(0, QUIT_MENU_ID, 0, "注销")
            .setShortcut('3', 'q')
            .setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        }
        //menu.add(0, EMBOSS_MENU_ID, 0, "Emboss").setShortcut('4', 's');
        //menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'z');
        //menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');
        //menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop").setShortcut('5', 'z');

        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case QUIT_MENU_ID:
                signoff();
                return true;
            case ABOUT_MENU_ID:
                showAboutDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CNAPS_REQUEST) {
            if (resultCode == RESULT_OK) {
                if(data != null) {
                    String[] extra = data.getStringArrayExtra(CNAPSHttpActivity.INTENT_EXTRA);
                    if(extra.length == 2 && cnapsListener != null) {
                    	cnapsListener.onCNAPSResult(extra[0], extra[1]);
                    	return;
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
	
	

	/********** user function **************/
	public static int CNAPS_REQUEST = 0x1000;
	
	/**
	 * 若需要选择开户银行信息，则使用该方法
	 * @param l
	 */
	public void setOnCNAPSResultListener(OnCNAPSResultListener l) {
		cnapsListener = l;
	}
	
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
	
	/**
	 * 设置右上角按钮
	 * @param text
	 */
	public void setTitleSubmitText(String text) {
		if(btnSubmit != null) {
			TextView tv = (TextView) btnSubmit.getChildAt(1);
			tv.setText(text);
		}
	}
	
	/**
	 * 选择开户银行
	 */
	public void chooseBank() {
		Intent i = new Intent(this, CNAPSHttpActivity.class);
	    startActivityForResult(i, CNAPS_REQUEST);
	}
	
	/**
	 * 获取软件代理商代码
	 * @return 区分代理客户端的代码
	 */
	public String getSource() {
		return getString(R.string.source);
	}
	
	/**
	 * 获取公共错误代码信息
	 * @param code 39域
	 * @return 错误信息
	 */
	public String getError(String code) {
        return PublicHelper.getError(code, this);
    }
	
	/**
	 * onCreate时调用，如果调用，则表示访问该activity必须登录
	 */
	public void setRequireLogon() {
		isNeedSession = true;
		setKsnTestListener(new KsnTestListener() {
			@Override
			public boolean test(String ksn) {
				POSSession session = POSHelper.getPOSSession();
				return session != null && session.testKsn(ksn);
			}
		});
	}
	
	/**
	 * 获取SD卡路径
	 * @return 如果mount了SD卡，返回路径，否则返回null
	 */
	public String getSDPath(){ 
		boolean sdCardExist = Environment.getExternalStorageState()   
				.equals(Environment.MEDIA_MOUNTED); 
		if(sdCardExist)
		{     
			return (Environment.getExternalStorageDirectory()).toString(); 
		}
		return null; 
	}
	/*********** end user function **************/
}
