package com.cnepay.android.pos2;

import com.cnepay.android.pos2.PasswordInputMethod.PasswordInputMethodListener;

import com.tangye.android.dialog.SwipeDialogController;
import com.tangye.android.iso8583.POSHelper;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CreditRechargerActivity extends UIBaseActivity implements
		View.OnClickListener, PasswordInputMethodListener {
	private EditText txtInput, txtPassword, txtDescribe;
	private Button[] btns;
	private Button fnButton;
	private View delButton;
	private TextView noteSwipe, card;
	private SwipeDialogController dialog;
	private CashInputMethod cashIM;
	private PasswordInputMethod passwdIM;
	private ViewGroup framePass, layoutMask;
	private ImageView imgCardType, imgCardReader;

	private final static int CONSUME_REQ = 1;
	private final static String TAG = "CreditRechargerActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recharger);
		setTitle("账户注册");
		btnSubmit.setOnClickListener(this);
		initUI();
		cashIM = new CashInputMethod(btns, fnButton, delButton, txtInput);
		passwdIM = new PasswordInputMethod(btns, fnButton, delButton, txtPassword, this);
		cashIM.init();
	}

	@Override
	public void onPasswordCancel() {
		Log.v(TAG, "onPasswordCancel");
		card.setText("");
		framePass.setVisibility(View.GONE);
		noteSwipe.setVisibility(View.VISIBLE);
		ScaleAnimation sa = new ScaleAnimation(1, 0, 1, 0, 
                Animation.RELATIVE_TO_SELF, 0.5f, 
                Animation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(200);
        imgCardType.startAnimation(sa);
        imgCardType.postDelayed(new Runnable() {
            public void run() {
            	imgCardType.setVisibility(View.GONE);
            }
        }, 200);
		cashIM.init();
		if(isPlugged()) {
			showTitleSubmit();
		}
	}

	@Override
	public void onSubmit(String password) {
		// TODO Auto-generated method stub
		Log.v(TAG, "onSubmit");
		Intent intent = new Intent(this, ConsumeActivity.class);
		String amount = txtInput.getText().toString().substring(1);
		String descri = txtDescribe.getText().toString();
		String cardNumber = card.getText().toString();
		String passwd = password.toString();
		/*
		 * try { passwd = POSHelper.getPOSSession().getPIN(passwd,
		 * cardNumber); } catch(Exception e) {
		 * makeError("Please input 4 to 6 length password"); return; }
		 */
		String[] tmp = { amount, descri, cardNumber, passwd };
		String key = POSHelper.getSessionString();
		/*
		 * if(key == null) { finish(); // TODO session gone; return; }
		 */
		intent.putExtra(key, tmp);
		startActivityForResult(intent, CONSUME_REQ);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 这个地方我们必须知道layout的信息，不合适
		case R.id.title_submit:
			InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(getCurrentFocus()
					.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			if (cashIM.getCashInCents() == 0) {
				txtInput.requestFocus();
				makeError("请输入交易金额");
				return;
			}
			hideTitleSubmit();
			txtInput.clearFocus();
			dialog.setText("请刷卡...");
			dialog.show();
			startSwipe();
			// noteSwipe.setText("");
			break;
		}
	}

	@Override
	public void onComplete(String cn) {
		super.onComplete(cn);

		dialog.dismiss();
		card.setText(cn);
		framePass.setVisibility(View.VISIBLE);
		noteSwipe.setVisibility(View.GONE);
		imgCardType.setVisibility(View.VISIBLE);
    	ScaleAnimation sa = new ScaleAnimation(0, 1, 0, 1, 
    	        Animation.RELATIVE_TO_SELF, 0.5f, 
    	        Animation.RELATIVE_TO_SELF, 0.5f);
    	sa.setDuration(200);
    	imgCardType.startAnimation(sa);
		passwdIM.init();
	}

	@Override
	public void onDecoding() {
		dialog.setText("正在解码...");
	}
	
	@Override
	public void onSwipe() {
		dialog.setText("接受刷卡数据...");
	}

	@Override
	public void onPlugin() {
		super.onPlugin(); // UIBASE action
		if(imgCardReader.getVisibility() != View.VISIBLE) {
            imgCardReader.setVisibility(View.VISIBLE);
            ScaleAnimation sa = new ScaleAnimation(0, 1, 0, 1, 
                    Animation.RELATIVE_TO_SELF, 0.5f, 
                    Animation.RELATIVE_TO_SELF, 0.5f);
            sa.setDuration(200);
            imgCardReader.startAnimation(sa);
        }
		if (framePass.getVisibility() == View.GONE) {
        	showTitleSubmit();
        }
		noteSwipe.setText("输入金额，点击【确认刷卡】");
	}

	@Override
	public void onPlugout() {
		super.onPlugout(); // UIBASE action
		if(imgCardReader.getVisibility() == View.VISIBLE) {
            ScaleAnimation sa = new ScaleAnimation(1, 0, 1, 0, 
                    Animation.RELATIVE_TO_SELF, 0.5f, 
                    Animation.RELATIVE_TO_SELF, 0.5f);
            sa.setDuration(200);
            imgCardReader.startAnimation(sa);
            imgCardReader.postDelayed(new Runnable() {
                public void run() {
                    imgCardReader.setVisibility(View.GONE);
                }
            }, 200);
        }
		hideTitleSubmit();
		noteSwipe.setText("请插入读卡器");
	}

	@Override
	public void onTimeout() {
		super.onTimeout();
		makeError("未检测到刷卡动作");
		dialog.dismiss();
		txtInput.requestFocus();
		if(isPlugged()) {
			showTitleSubmit();
		}
	}

	@Override
	public void onError(int err) {
		Log.i(TAG, "error = " + err);
		dialog.dismiss();
		txtInput.requestFocus();
		// 刷卡时，插拔卡可能会引起decodeError
		boolean isDecodeError = true;
		boolean isInterrupt = false;
		if (this.isPlugged()) {
			switch (err) {
			case E_API2_UNRESOLVED:
				makeError("无法解析，请重试或者使用其他Android手机");
				break;
			case E_API2_FASTORSLOW:
				makeError("刷卡过快或过慢，请重试");
				break;
			case E_API2_UNSTABLE:
				makeError("刷卡不稳定，请重试");
				break;
			case E_API2_INTERRUPT:
				isInterrupt = true;
			default:
				isDecodeError = false;
			}
		} else {
			switch (err) {
			case E_API2_INVALID_DEVICE:
				makeError("无法识别该读卡器，请选择新的读卡器或者重新拔插");
				noteSwipe.setText("请拔掉此读卡器，重新插入");
				break;
			case E_API2_INTERRUPT:
				isInterrupt = true;
				break;
			}
		}
		if (isDecodeError || isInterrupt) {
			dialog.dismiss();
			if (isPlugged()) {
				showTitleSubmit();
			}
		}
	}

	private void initUI() {
		initNumPad();
		setTitleSubmitText("确认刷卡");
		noteSwipe = (TextView) findViewById(R.id.notation_swipe);
		imgCardType = (ImageView)findViewById(R.id.card_type);
        imgCardReader = (ImageView)findViewById(R.id.card_indicator);
		framePass = (ViewGroup) findViewById(R.id.password_frame);
		layoutMask = (ViewGroup) findViewById(R.id.layout_mask);
		txtPassword = (EditText) findViewById(R.id.card_password);
		txtDescribe = (EditText) findViewById(R.id.description_input);
		card = (TextView) findViewById(R.id.card_text);
		framePass.setVisibility(View.GONE);
		layoutMask.setClickable(true);
		int[] attrs = new int[] { R.layout.swipe_dialog, R.style.dialog,
				R.id.dialog_anim, R.id.dialog_note };
		dialog = new SwipeDialogController(this, attrs);
		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				stopSwipe();
			}
		});
	}

	private void initNumPad() {
		txtInput = (EditText) findViewById(R.id.cash_input);
		btns = new Button[10];
		ViewGroup vg = (ViewGroup) findViewById(R.id.num_pad);
		for (int i = 0; i < 9; i++) {
			ViewGroup vgtmp = ((ViewGroup) vg.getChildAt(i / 3));
			btns[i + 1] = (Button) vgtmp.getChildAt(i % 3);
		}
		ViewGroup vg1 = ((ViewGroup) vg.getChildAt(3));
		btns[0] = (Button) vg1.getChildAt(1);
		TextView t1 = (TextView) vg1.getChildAt(1);
		Log.e("showViewgroup", t1.getText().toString());
		fnButton = (Button) vg1.getChildAt(0);
		delButton = vg1.getChildAt(2);
	}

	private void makeError(String err) {
		Toast t = Toast.makeText(this, err, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}
}
