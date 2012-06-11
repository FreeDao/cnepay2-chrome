package com.tangye.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.DialogInterface.OnCancelListener;

public class SwipeDialogController implements OnDismissListener {
	
	private Dialog dlg;
	private int dialogLayout;
	private int dialogStyle;
	private int dialogAnim;
	private int dialogNote;
	
	private Context mContext;
	private AnimationDrawable anim;
	private TextView note;
	private ImageView img;
	private String txt;
	private OnCancelListener listener;
	private boolean adjust = false;
	
	/**
	 * new Dialog controller
	 * @param context
	 * @param attr R.layout, R.style, R.id.animation, R.id.notice
	 */
	public SwipeDialogController(Context context, int[] attr) {
		if (attr.length != 4) {
			throw new IllegalArgumentException(
					"attr shuold be 4, including layout, style, animation, notice");
		}
		mContext = context;
		dialogLayout = attr[0];
		dialogStyle = attr[1];
		dialogAnim = attr[2];
		dialogNote = attr[3];
	}
	
	public void show() {
		if (dlg != null && dlg.isShowing()) {
			dlg.dismiss();
		}
		dlg = new Dialog(mContext, dialogStyle);
		dlg.setContentView(dialogLayout);
		img = (ImageView) dlg.findViewById(dialogAnim);
		note = (TextView) dlg.findViewById(dialogNote);
		note.setVisibility(View.INVISIBLE);
		adjust = false;
		if (txt != null) {
			note.setText(txt);
		}
		anim = (AnimationDrawable) img.getDrawable();
		dlg.setOnDismissListener(this);
		if (listener != null) dlg.setOnCancelListener(listener);
		img.postDelayed(new Runnable() {
			public void run() {
				note.setVisibility(View.VISIBLE);
				adjust();
				anim.start();
			}
		}, 500);
		dlg.show();
	}
	
	public void dismiss() {
		if(dlg != null && dlg.isShowing()) {
			dlg.dismiss();
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		anim.stop();
		note.setVisibility(View.INVISIBLE);
	}

	public Dialog getDialog() {
		return dlg;
	}
	
	public void setText(int resId) {
		if (note != null) {
			note.setText(resId);
			adjust();
		}
		txt = mContext.getString(resId);
	}
	
	public void setText(String text) {
		if (note != null) {
			note.setText(text);
			adjust();
		}
		txt = text;
	}
	
	public void setOnCancelListener(OnCancelListener l) {
		listener = l;
	}
	
	private void adjust() {
		int screenH = img.getMeasuredHeight();
		int screenW = img.getMeasuredWidth();
		if (screenH != 0 && !adjust) {
			//FIXME hardcode 540px of the picture
			int actualH = (int) (screenW / 540.0f * screenH);
			int fontH = note.getHeight();
			//android.util.Log.v("DIALOG", "s=" + img.getMeasuredWidth() + " f=" + fontH);
			int top = (int) (actualH * 0.23f - (fontH - screenH + actualH) / 2);
			note.setPadding(0, top, 0, 0);
			note.requestLayout();
			adjust = true;
		}
	}
	
}
