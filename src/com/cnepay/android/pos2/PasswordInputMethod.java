package com.cnepay.android.pos2;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;

public class PasswordInputMethod implements View.OnClickListener, OnKeyListener {

	private Runnable repeatRunnable;
	private final int NUMOFBUTTON = 10;

	private Button[] numButtons;
	private Button fnButton;
	private View delButton;
	private EditText passwordInput;
	private int[] tag;

	private StringBuilder passwordStr;

	private PasswordInputMethodListener passwordListener;

	public PasswordInputMethod(Button[] numberButtons, Button fnctionButton,
			View deleteButton, EditText passwordInputEditText,
			PasswordInputMethodListener passwordInputMethodListener) {
		numButtons = numberButtons;
		fnButton = fnctionButton;
		delButton = deleteButton;
		passwordInput = passwordInputEditText;
		tag = new int[NUMOFBUTTON];
		for (int i = 0; i < NUMOFBUTTON; i++) {
			tag[i] = i;
		}
		passwordStr = new StringBuilder();
		passwordListener = passwordInputMethodListener;
	}

	public void init() {
		passwordInput.setLongClickable(false);
		// passwordInput.setKeyListener(null);
		passwordInput.setOnKeyListener(this);
		passwordInput.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				v.requestFocus();
				return true;
			}
		});
		passwordInput.requestFocus();

		if (numButtons.length != NUMOFBUTTON) {
			throw new IllegalArgumentException("the length of numberButtons "
					+ "do not match the require");
		}
		for (int i = 0; i < numButtons.length; i++) {
			int tmp = randNumber(i, NUMOFBUTTON - 1);
			int tmpp = tag[tmp];
			tag[tmp] = tag[i];
			tag[i] = tmpp;
			numButtons[i].setTag(tag[i]);
			numButtons[i].setText("" + tag[i]);
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
		fnButton.setText("确定");
		fnButton.setOnClickListener(this);
	}

	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				// TODO callback cancel()
				passwordListener.onPasswordCancel();
				return true;
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int value = ((Integer) v.getTag()).intValue();
		switch (value) {
		case 10:
			passwordInput.setText("");
			String password = passwordStr.toString();
			passwordStr.delete(0, passwordStr.length());
			passwordListener.onSubmit(password);
			break;
		default:
			if (passwordStr.length() < 6) {
				passwordStr.append(String.valueOf(value));
				passwordInput.append("*");
			}
		}

	}

	private void performDelete() {
		String xxx = passwordInput.getText().toString();
		if (xxx.length() > 0) {
			xxx = xxx.substring(1);
			passwordStr.deleteCharAt(passwordStr.length() - 1);
		}
		passwordInput.setText("");
		passwordInput.append(xxx);
	}

	private int randNumber(int begin, int end) {
		return (int) (Math.random() * (end - begin + 1) + begin);
	}

	public interface PasswordInputMethodListener {
		public void onPasswordCancel();
		public void onSubmit(String password);
	}
}
