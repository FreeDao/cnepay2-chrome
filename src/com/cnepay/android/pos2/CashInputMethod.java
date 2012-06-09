package com.cnepay.android.pos2;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;

public class CashInputMethod implements View.OnClickListener {

	private Runnable repeatRunnable;
	private final String TAG = "CashInputMethod";

	private Button[] numButtons;
	private Button fnButton;
	private View delButton;
	private EditText cashInput;

	public CashInputMethod(Button[] numberButtons, Button fnctionButton,
			View deleteButton, EditText cashInputEditText) {
		numButtons = numberButtons;
		fnButton = fnctionButton;
		delButton = deleteButton;
		cashInput = cashInputEditText;

	}

	public void init() {
		cashInput.setHint("￥0.00");
		cashInput.setLongClickable(false);
		cashInput.setKeyListener(null);
		cashInput.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		cashInput.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				v.requestFocus();
				return true;
			}
		});

		if (numButtons.length != 10) {
			throw new IllegalArgumentException("the length of numberButtons "
					+ "do not match the require");
		}
		for (int i = 0; i < numButtons.length; i++) {
			numButtons[i].setTag(i);
			numButtons[i].setText("" + i);
			numButtons[i].setOnClickListener(this);

		}

		delButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					performDelete();
					final View fv = v;
					repeatRunnable = new Runnable() {
						public void run() {
							if (fv.isPressed()) {
								performDelete();
								fv.postDelayed(this, 50);
							}
						}
					};
					v.postDelayed(repeatRunnable, 500);
				} else if (action == MotionEvent.ACTION_UP) {
					if (repeatRunnable != null) {
						v.removeCallbacks(repeatRunnable);
					}
					repeatRunnable = null;
				}
				return false;
			}
		});
		fnButton.setTag(10);
		fnButton.setText("00");
		fnButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int value = ((Integer) v.getTag()).intValue();
		Log.v(TAG, "value = " + value);
		if (value / 10 == 0) {
			setCashText(value);
		} else {
			if (value == 10) {
				setCashText(0);
				setCashText(0);
			}
		}
	}

	private void setCashText(int value) {
		long newvalue = getCashInCents();
		if (value >= 0 && value <= 9) {
			newvalue = newvalue * 10 + value;
		} else if (value == KeyEvent.KEYCODE_DEL - KeyEvent.KEYCODE_0) {
			newvalue /= 10;
		}
		Log.v(TAG, "newvalue = " + newvalue);
		Log.v(TAG, "cashInputinput.isFoucsed = " + cashInput.isFocused());
		if (!cashInput.isFocused() || newvalue >= 1000000000000d) {
			return;
		} else if (newvalue == 0) {
			cashInput.setText("");
			return;
		}
		String x1 = String.valueOf(newvalue / 100);
		String x2 = String.valueOf(newvalue % 100);
		Log.v(TAG, "x1 = " + x1 + ",x2 = " + x2);
		if (x2.length() < 2)
			x2 = "0" + x2;
		cashInput.setText("");
		cashInput.append("￥" + x1 + "." + x2);
	}

	private long getCashInCents() {
		long ca;
		try {
			ca = Math.round((100 * Double.valueOf(cashInput.getText()
					.toString().substring(1))));
		} catch (Exception e) {
			ca = 0;
		}
		return ca;
	}

	private void performDelete() {
		setCashText(KeyEvent.KEYCODE_DEL - KeyEvent.KEYCODE_0);
	}
}
