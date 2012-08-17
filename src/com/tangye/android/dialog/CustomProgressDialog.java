package com.tangye.android.dialog;

import com.cnepay.android.pos2.R;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CustomProgressDialog extends Dialog {

	private Context mContext;
	private Window mWindow;
	private TextView mMessageView;
	private ProgressBar mProgress;
	private ImageView mImgView;
	private CharSequence mMessage;
	private CharSequence mTitle;
	
	private boolean mIndeterminate;
	private final static int TYPE_BLUE = 1;
	private final static int TYPE_ORANGE = 0;
	private final static String TAG = "CustomProgressDialog";

	public CustomProgressDialog(int type, Context context) {
		super(context);
		mContext = context;
		setupView();
	}

	public CustomProgressDialog(int type, Context context, int theme) {
		super(context, theme);
		mContext = context;
		switch(type){
			case TYPE_ORANGE:
				setupView();
				break;
			case TYPE_BLUE:
				setupBlueView();
				break;
		}
	}
	
	private void setupBlueView(){
		mWindow = getWindow();
		View view = View.inflate(mContext, R.layout.progress_dialog_blue, null);
		mWindow.setContentView(view);
		WindowManager.LayoutParams winlp = mWindow.getAttributes();
		winlp.width = WindowManager.LayoutParams.MATCH_PARENT;
		mWindow.setAttributes(winlp);
		mMessageView = (TextView) mWindow.findViewById(R.id.dialog_progresstext_blue);
		mImgView = (ImageView) mWindow.findViewById(R.id.loading_blue_dot);
		if (mTitle != null) {
			setTitle(mTitle);
		}
		if (mMessage != null) {
			setMessage(mMessage);
		}
		ImageView blueBg = (ImageView)mWindow.findViewById(R.id.loading_bg_blue);
		
		DisplayMetrics dpm = mContext.getResources().getDisplayMetrics();
        
		/*
		int[] location = new int[2];
		blueBg.getLocationOnScreen(location);
	    int x = location[0];
	    int y = location[1];
	    int w = blueBg.getWidth();
	    int h = mImgView.getHeight();
	    float startX = 0;
	    float startY = dpm.heightPixels/2;
	    float endX = dpm.widthPixels;
	    float endY = dpm.heightPixels/2;
	    
	    Log.v(TAG, "x = " + dpm.widthPixels + ",y = " + dpm.heightPixels + ",w = " + w + ",h = " + h );
	    Log.v(TAG, "l:" + mImgView.getScrollX() + "t:" + mImgView.getScrollY());
		TranslateAnimation mTranAnimation = new TranslateAnimation(startX, endX, startY, endY);
		mTranAnimation.setDuration(2000);
		mTranAnimation.setRepeatCount(30);
		mImgView.startAnimation(mTranAnimation);
		*/
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
