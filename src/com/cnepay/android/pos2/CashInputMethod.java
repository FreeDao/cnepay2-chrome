package com.cnepay.android.pos2;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;

public class CashInputMethod implements View.OnClickListener {
	
	private Runnable repeatRunnable;
	private Button[] numButtons;
	private Button fnButton;
	private View delButton;
	private EditText cashInput;

	/**
	 * 金额输入法
	 * @param numberButtons 所有数字键的Buttons引用
	 * @param fnctionButton 功能键的Button引用
	 * @param deleteButton 删除键的View引用
	 * @param cashInputEditText 金额输入框引用
	 */
	public CashInputMethod(Button[] numberButtons, Button fnctionButton,
			View deleteButton, EditText cashInputEditText) {
		numButtons = numberButtons;
		fnButton = fnctionButton;
		delButton = deleteButton;
		cashInput = cashInputEditText;

	}

	public void init() {
		cashInput.setHint("￥0.00");
		cashInput.setClickable(true);
		cashInput.setLongClickable(false);
		cashInput.setKeyListener(null);
		cashInput.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_UP) {
					setCashText(keyCode - KeyEvent.KEYCODE_0);
				}
				return false;
			}
		});
		cashInput.requestFocus();

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
		if (value / 10 == 0) {
			setCashText(value);
		} else {
			if (value == 10) {
				setCashText(0);
				setCashText(0);
			}
		}
	}
	
	public void setFixedCashText(long value){
		int[] index = new int[50];
		int len = 0;
		while(value != 0){
			index[len++] = (int) (value % 10);
			value = value / 10;
		}
		for(int i = len - 1; i >= 0; i--){
			setCashText(index[i]);
		}
		cashInput.setFocusable(false);
	}
	
	private void setCashText(int value) {
		long newvalue = getCashInCents();
		if (value >= 0 && value <= 9) {
			newvalue = newvalue * 10 + value;
		} else if (value == KeyEvent.KEYCODE_DEL - KeyEvent.KEYCODE_0) {
			newvalue /= 10;
		}
		if (!cashInput.isFocused() || newvalue >= 1000000000000d) {
			return;
		} else if (newvalue == 0) {
			cashInput.setText("");
			return;
		}
		String x1 = String.valueOf(newvalue / 100);
		String x2 = String.valueOf(newvalue % 100);
		if (x2.length() < 2)
			x2 = "0" + x2;
		cashInput.setText("");
		cashInput.append("￥" + x1 + "." + x2);
	}

	/**
	 * 获取金额函数
	 * @return 获取总的金额，以分为单位返回
	 */
	public long getCashInCents() {
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
