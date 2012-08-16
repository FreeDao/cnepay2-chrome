package com.tangye.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.View;
import android.view.animation.TranslateAnimation;
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
	private TextView note;
	private ImageView img;
	private String txt;
	private OnCancelListener listener;
	
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
		if (txt != null) {
			note.setText(txt);
		}
		dlg.setOnDismissListener(this);
		if (listener != null) dlg.setOnCancelListener(listener);
		dlg.show();
		
		TranslateAnimation ta = new TranslateAnimation(-260, 1000, 0, 0);
		ta.setDuration(3000);
		ta.setRepeatCount(-1);
		img.startAnimation(ta);
		
	}
	
	public void dismiss() {
		if(dlg != null && dlg.isShowing()) {
			dlg.dismiss();
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		note.setVisibility(View.INVISIBLE);
	}

	public Dialog getDialog() {
		return dlg;
	}
	
	public void setText(int resId) {
		if (note != null) {
			note.setText(resId);
		}
		txt = mContext.getString(resId);
	}
	
	public void setText(String text) {
		if (note != null) {
			note.setText(text);
		}
		txt = text;
	}
	
	public void setOnCancelListener(OnCancelListener l) {
		listener = l;
	}
	
}
