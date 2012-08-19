package com.tangye.android.dialog;

import com.cnepay.android.pos2.R;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CustomProgressDialog extends Dialog {

	private Context mContext;
	private Window mWindow;
	private TextView mMessageView;
	private ProgressBar mProgress;
	private ImageView mCursor;
	private CharSequence mMessage;
	private CharSequence mTitle;
	private boolean mIndeterminate;
	
	public static final int ORANGE = 0;
	public static final int BLUE = 1;

	public CustomProgressDialog(Context context) {
		super(context);
		mContext = context;
		setupView();
	}

	public CustomProgressDialog(Context context, int theme, int type) {
		super(context, theme);
		mContext = context;
		if (type == ORANGE) {
			setupView();
		} else {
			setupView2();
		}
	}
	
	private void setupView2() {
		mWindow = getWindow();
		View view = View.inflate(mContext, R.layout.progress_dialog_blue, null);
		mWindow.setContentView(view);
		WindowManager.LayoutParams winlp = mWindow.getAttributes();
		winlp.width = WindowManager.LayoutParams.MATCH_PARENT;
		mWindow.setAttributes(winlp);
		mMessageView = (TextView) mWindow.findViewById(R.id.dialog_blue_progresstext);
		mCursor = (ImageView) mWindow.findViewById(R.id.cross_blue_cursor);
		DisplayMetrics  dm = new DisplayMetrics();
		mWindow.getWindowManager().getDefaultDisplay().getMetrics(dm);
		TranslateAnimation ta = new TranslateAnimation(-100, dm.widthPixels, 0, 0);
		ta.setDuration(2000);
		ta.setRepeatCount(Animation.INFINITE);
		mCursor.startAnimation(ta);
		mProgress = null;
		if (mTitle != null) {
			setTitle(mTitle);
		}
		if (mMessage != null) {
			setMessage(mMessage);
		}
	}

	private void setupView() {
		mWindow = getWindow();
		View view = View.inflate(mContext, R.layout.progress_dialog, null);
		mWindow.setContentView(view);
		WindowManager.LayoutParams winlp = mWindow.getAttributes();
		winlp.width = WindowManager.LayoutParams.MATCH_PARENT;
		mWindow.setAttributes(winlp);
		mMessageView = (TextView) mWindow.findViewById(R.id.dialog_progresstext);
		mProgress = (ProgressBar) mWindow.findViewById(R.id.dialog_progressbar);
		mCursor = null;
		if (mTitle != null) {
			setTitle(mTitle);
		}
		if (mMessage != null) {
			setMessage(mMessage);
		}
		setIndeterminate(mIndeterminate);
	}

	public void setMessage(int resId) {
		setMessage(mContext.getString(resId));
	}

    public void setMessage(CharSequence message) {
		mMessage = message;
		if (mMessageView != null) {
			mMessageView.setText(message);
		}
    }

    @Override
    public void setTitle(CharSequence title) {
    	// TODO nothing currently
    	mTitle = title;
    }
   
    @Override
    public void setTitle(int resId) {
    	setTitle(mContext.getString(resId));
    }
    
    public void setIndeterminate(boolean indeterminate) {
    	mIndeterminate = indeterminate;
    	if (mProgress != null) {
    		mProgress.setIndeterminate(indeterminate);
    	}
    }

}
