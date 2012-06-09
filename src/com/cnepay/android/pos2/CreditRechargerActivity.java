package com.cnepay.android.pos2;





import com.cnepay.android.pos2.PasswordInputMethod.PasswordInputMethodListener;

import com.tangye.android.iso8583.POSHelper;
import com.tangye.swipedialog.SwipeDialogController;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CreditRechargerActivity extends UIBaseActivity implements View.OnClickListener{
	private EditText txtInput, txtPassword, txtDescribe;
	private Button[] btns;
	private Button fnButton;
	private View delButton;
	private TextView noteSwipe,card;
	private static final int CONSUME_REQUEST = 0;
	
	private SwipeDialogController dialog;
	private  PasswordInputMethodListener passListener;
	private ViewGroup framePass, card_detect_box, layoutMask;
	
	private final String TAG = "CreditRechargerActivity";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recharger);
		setTitle("账户注册");
		this.showTitleSubmit();
		this.btnSubmit.setOnClickListener(this);
		
		initUI();
		
		new CashInputMethod(btns, fnButton, delButton, txtInput).init();
		
		
		int[] attrs = new int[] { R.layout.swipe_dialog, R.style.dialog,
				R.id.dialog_anim, R.id.dialog_note };
		dialog = new SwipeDialogController(this, attrs);
		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				stopSwipe();
			}
		});
		
		
		 passListener = new PasswordInputMethodListener(){

			@Override
			public void onPasswordCancel() {
				// TODO Auto-generated method stub
				Log.v(TAG, "onPasswordCancel");
			}

			@Override
			public void onSubmit(String password) {
				// TODO Auto-generated method stub
				Log.v(TAG, "onSubmit");
				Intent intent = new Intent(CreditRechargerActivity.this, ConsumeActivity.class);
				String amount = txtInput.getText().toString().substring(1);
				String descri = txtDescribe.getText().toString();
				String cardNumber = card.getText().toString();
				String passwd = password.toString();
				/*
				try {
				    passwd = POSHelper.getPOSSession().getPIN(passwd, cardNumber);
				} catch(Exception e) {
				    makeError("Please input 4 to 6 length password");
				    return;
				}
				*/
				String[] tmp = {amount, descri, cardNumber, passwd};
				String key = POSHelper.getSessionString();
				/*
				if(key == null) {
					finish(); // TODO session gone;
					return;
				}
				*/
				intent.putExtra(key, tmp);
				startActivity(intent);
			}};
	}
	
	@Override
    protected void onPause() {
        super.onPause();
       
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		//这个地方我们必须知道layout的信息，不合适
		case R.id.title_submit:
			if(!this.isPlugged()){
				makeError("请插入读卡器");
				break;
			}
			if(getCashInCents() == 0){
				makeError("请输入交易金额");
				break;
			}
			InputMethodManager inputManager = (InputMethodManager)            
        		getSystemService(INPUT_METHOD_SERVICE); 
			inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),      
					InputMethodManager.HIDE_NOT_ALWAYS);
			
			
			txtInput.clearFocus();
			this.hideTitleSubmit();
			//btnSubmit.setEnabled(false);
			dialog.setText("请刷卡...");
			dialog.show();
			this.startSwipe();
			
			break;
		}
	}

	
	@Override
	public void onComplete(String cn) {
		super.onComplete(cn);
		
		dialog.dismiss();
		txtInput.clearFocus();
		this.hideTitleSubmit();
		//btnSubmit.setEnabled(false);
		
		card_detect_box.setVisibility(View.VISIBLE);
		card.setText(cn);
		framePass.setVisibility(View.VISIBLE);
		noteSwipe.setText("请输入密码，完成后点击确定");
		
		new PasswordInputMethod(btns, fnButton, delButton, txtPassword, passListener).init();
	}
	
	@Override
	public void onDecoding() {
		super.onDecoding();
		if (dialog != null) {
			dialog.setText("正在解码...");
		}
	}

	@Override
	public void onPlugin() {
		super.onPlugin(); // UIBASE action
	}

	@Override
	public void onPlugout() {
		super.onPlugout(); // UIBASE action
	}

	@Override
	public void onTimeout() {
		super.onTimeout();
		makeError("未检测到刷卡动作");
		dialog.dismiss();
		txtInput.requestFocus();
		this.showTitleSubmit();
		//btnSubmit.setEnabled(true);
	}

	@Override
	public void onError(int error) {
		super.onError(error);
		Log.i(TAG, "error = " + error);
		
		makeError("有错误");
		dialog.dismiss();
		txtInput.requestFocus();
		this.showTitleSubmit();
		//btnSubmit.setEnabled(true);
		
	}
	
	private void initUI(){
		initNumPad();
		noteSwipe = (TextView)findViewById(R.id.notation_swipe);
		framePass = (ViewGroup)findViewById(R.id.password_frame);
		layoutMask = (ViewGroup)findViewById(R.id.layout_mask);
		card_detect_box = (ViewGroup)findViewById(R.id.card_detect_box);
		txtPassword = (EditText)findViewById(R.id.card_password);
		txtDescribe = (EditText)findViewById(R.id.description_input);
		card = (TextView)findViewById(R.id.card_text);
        framePass.setVisibility(View.GONE);
        layoutMask.setClickable(true);
	}
	
	private void initNumPad(){
    	txtInput = (EditText)findViewById(R.id.cash_input);
		btns = new Button[10];
        ViewGroup vg = (ViewGroup)findViewById(R.id.num_pad);
        for(int i = 0; i < 9; i++) {
        	ViewGroup vgtmp = ((ViewGroup)vg.getChildAt(i / 3));
        	btns[i + 1] = (Button)vgtmp.getChildAt(i % 3);
        }
        ViewGroup vg1 = ((ViewGroup)vg.getChildAt(3));
        btns[0] = (Button)vg1.getChildAt(1);
        TextView t1 = (TextView)vg1.getChildAt(1);
        Log.e("showViewgroup",t1.getText().toString());
        fnButton = (Button)vg1.getChildAt(0);
        delButton = vg1.getChildAt(2);
        
	}
	
	private long getCashInCents() {
		long ca;
		try {
			ca = Math.round((100 * Double.valueOf(txtInput.getText().toString().substring(1))));
		} catch(Exception e) {
			ca = 0;
		}
		return ca;
	}
	
	public void makeError(String err) {
		Toast t = Toast.makeText(this, err, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}
}

