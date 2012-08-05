package com.cnepay.android.pos2;

import com.tangye.android.dialog.AlertDialogBuilderWrapper;
import com.tangye.android.utils.PublicHelper;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MobileChargeActivity extends UIBaseActivity implements OnClickListener{
	
	private EditText mobileNumber;
	private EditText mobileNumberRepeat;
	private Button submit;
	private RadioGroup chargeAmount;
	
	private static final int ENABLE_TIMEOUT = 1000;
	private long amountToPay[] = {30000, 10000, 5000};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mobile_charge);
		setTitle("手机充值");
		hideTitleSubmit();
		setActivityPara(true, true);
		initUI();
	}
	
	private void initUI(){
		mobileNumber = (EditText)findViewById(R.id.mobile_charge_number);
		mobileNumberRepeat = (EditText)findViewById(R.id.mobile_charge_number_repeat);
		submit = (Button)findViewById(R.id.mobile_charge_submit);
		submit.setOnClickListener(this);
		chargeAmount = (RadioGroup)findViewById(R.id.mobile_charge_amount);
		chargeAmount.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
					
			}});
	}

	@Override
	public void onClick(View arg0) {
		submit.setEnabled(false);
		final String mobNumber = mobileNumber.getText().toString();
		if(!testPhone(mobNumber)){
			verify_failure(mobileNumber, "请输入正确手机号");
			mobileNumber.setText("");
			return;
		}
		
		String mobNumberRepeat = mobileNumberRepeat.getText().toString();
		if(!mobNumber.equals(mobNumberRepeat)){
			verify_failure(mobileNumberRepeat, "两次手机号码输入不一致");
			return;
		}
		
		
		RadioButton amount = (RadioButton) findViewById(chargeAmount.getCheckedRadioButtonId());
		if(amount == null){
			verify_failure(chargeAmount, "请选择充值金额");
			return;
		}
		StringBuilder info = new StringBuilder();
    	info.append("手机号码： " + mobNumber);
    	final int tmp = Integer.parseInt(amount.getTag().toString());
    	info.append("\n充值金额：￥" + amountToPay[tmp]/100);

    	AlertDialogBuilderWrapper builder = PublicHelper.getAlertDialogBuilder(MobileChargeActivity.this);
	    builder.setTitle("手机充值")
	    .setIcon(android.R.drawable.ic_dialog_info)
	    .setMessage(info.toString() + "\n\n是否继续充值？")
	    .setCancelable(false)
	    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
					submit.setEnabled(true);
	    	}
	    })
	    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent(MobileChargeActivity.this, MobileChargeConsumeActivity.class);
					i.putExtra("amount", amountToPay[tmp]);
					i.putExtra("mobileNumber", mobNumber);
        			startActivity(i);
					finish();
	    	}
	    });
	    builder.show();		
		
	}
	
	private void makeError(String err) {
		Toast t = Toast.makeText(this, err, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}
	
	private void verify_failure(View v, String err) {
		if (v != null) {
			v.requestFocus();
		}
		submit.postDelayed(new Runnable() {
			public void run() {
				submit.setEnabled(true);
			}
		}, ENABLE_TIMEOUT);
		makeError(err);
		return;
	}	
	
	private boolean testPhone(String phone) {
		int[] begin = { 13, 14, 15, 18 };
		// only for cellphone, so length should be 11
		if (phone != null && phone.length() != 11)
			return false;
		for (int i = 0; i < begin.length; i++) {
			if (phone.startsWith(String.valueOf(begin[i]))) {
				return true;
			}
		}
		return false;
	}
	
}
