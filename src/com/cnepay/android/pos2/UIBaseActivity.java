
package com.cnepay.android.pos2;

import com.tangye.android.iso8583.POSHelper;
import com.tangye.android.iso8583.POSSession;
import com.tangye.android.utils.PublicHelper;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
	private static int lastIndicatorResId = 0;
	private boolean mPlugged = false;
	private boolean isNeedSession = false;
	private OnCNAPSResultListener cnapsListener = null;

	protected ImageView imgIndicator = null;
	protected View viewDetect = null;
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
		lastIndicatorResId = R.drawable.signup_reader;
		imgIndicator.setImageResource(lastIndicatorResId);
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
		lastIndicatorResId = R.drawable.signup_reader_off;
		imgIndicator.setImageResource(lastIndicatorResId);
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
			if (error == E_API2_INVALID_KSN) {
				mToast.cancel();
				mToast = Toast.makeText(this, "非法刷卡器！请使用注册时绑定的刷卡器!", Toast.LENGTH_SHORT);
				mToast.setGravity(Gravity.CENTER, 0, 0);
				mToast.show();
			} else if (error == E_API2_INVALID_DEVICE) {
				mToast.cancel();
				mToast = Toast.makeText(this, "无法识别该刷卡器，请选择新的刷卡器或者重新拔插!", Toast.LENGTH_SHORT);
				mToast.setGravity(Gravity.CENTER, 0, 0);
				mToast.show();
			}
		}
	}

	@Override
	public void setContentView(int view) {
		super.setContentView(view);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title); // title为自己标题栏的布局
		imgIndicator = (ImageView) getWindow().findViewById(R.id.icon_indicator);
		imgIndicator.setClickable(true);
		viewDetect = getWindow().findViewById(R.id.icon_detecting);
		txtTitle = (TextView)getWindow().findViewById(R.id.title_name);
		btnSubmit = (ViewGroup)getWindow().findViewById(R.id.title_submit);
	}

	@Override
	protected void onResume() {		
		if (SystemClock.elapsedRealtime() - lastLeaveTime < 10000) {
			notNotify = true;
		}
		if (lastIndicatorResId != 0) {
			imgIndicator.setImageResource(lastIndicatorResId);
		}
		mPlugged = false; // wait for KSN test result
		
		/**** login session control ****/
		if(isNeedSession && POSHelper.getSessionID() <= 0) {
			finish();
        }
		/******login session control end*****/
		
		if(UpdateService.needUpgrade()) {
			signoff();
        }
		
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		lastLeaveTime = SystemClock.elapsedRealtime();
	}
	
	private final Runnable an1 = new Runnable() {
    	public void run() {
    		imgIndicator.setVisibility(View.GONE);
    		Log.i(TAG, "detect gone");
    	}
    };
    
    private final Runnable an2 = new Runnable() {
    	public void run() {
    		viewDetect.setVisibility(View.GONE);
    		Log.i(TAG, "un detect gone");
    	}
    };
	
	@Override
	protected void deviceDetecting(boolean detect) {
		if (viewDetect != null && !notNotify) {
			if (detect && imgIndicator.getVisibility() == View.VISIBLE) {
				Log.i(TAG, "detect");
				Animation am = new TranslateAnimation(0, 0, 0, -400);
			    am.setDuration(500);
			    imgIndicator.postDelayed(an1, 500);
			    imgIndicator.startAnimation(am);
			    
			    Animation am2 = new TranslateAnimation(0, 0, 400, 0);
			    am2.setDuration(500);
			    viewDetect.setVisibility(View.VISIBLE);
			    viewDetect.startAnimation(am2);
			    viewDetect.removeCallbacks(an2);
			} else if(!detect && viewDetect.getVisibility() == View.VISIBLE) {
				Log.i(TAG, "un detect");
				Animation am2 = new TranslateAnimation(0, 0, 400, 0);
			    am2. setDuration(500);
			    imgIndicator.setVisibility(View.VISIBLE);
			    imgIndicator.startAnimation(am2);
			    imgIndicator.removeCallbacks(an1);
				
				Animation am = new TranslateAnimation(0, 0, 0, -400);
			    am.setDuration(500);
			    viewDetect.postDelayed(an2, 500);
			    viewDetect.startAnimation(am);
			}
		}
	}
	
	private static final int ABOUT_MENU_ID = Menu.FIRST;
    private static final int QUIT_MENU_ID = Menu.FIRST + 1;
    
    private void signoff() {
    	POSHelper.deleteSession();
    	finish();
    }
    
    private void showAboutDialog() {
    	AlertDialog.Builder builder = PublicHelper.getAlertDialogBuilder(this);
        builder.setTitle("关于" + getString(R.string.app_name))
        .setIcon(android.R.drawable.ic_dialog_info)
        .setMessage("版本号: " + getVersion())
        .setPositiveButton(android.R.string.ok, null)
        .show();
    }
    
    private String getVersion() {
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0); 
            return packInfo.versionName;
        } catch(Exception e) {}
        return "Error Version";
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
	
	public static long getLastLeaveTime() {
		return lastLeaveTime;
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
	 * onCreate时调用,初始化Activity相关参数
	 * @param needSeesion 需要登录
	 * @param needDefaultKsnListener 是否检测默认ksn
	 * @param ksnTestListeners, 如果不需要默认检测，则使用自定检测函数
	 */
	public void setActivityPara(boolean needSeesion, boolean needDefaultKsnListener, KsnTestListener...ksnTestListeners ) {
		isNeedSession = needSeesion;
		if(needDefaultKsnListener){
			setKsnTestListener(new KsnTestListener() {
				@Override
				public boolean test(String ksn) {
					POSSession session = POSHelper.getPOSSession();
					return session != null && session.testKsn(ksn);
				}
			});
		} else if (ksnTestListeners == null || ksnTestListeners.length == 0) {
			setKsnTestListener(null);
		} else {
			setKsnTestListener(ksnTestListeners[0]);
		}
	}

	public String getSDPath(){ 
		return PublicHelper.getSDPath();
	}
	/*********** end user function **************/
}
