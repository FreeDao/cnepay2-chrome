package com.cnepay.android.pos2;


import com.tangye.android.iso8583.POSHelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ConsumeActivity extends UIBaseActivity implements View.OnClickListener{
	
	//private static final String TAG = "ConsumeActivity";
	
    private String[] all;
	private Button btnFinish;
	private TextView merchantNumber, terminalNumber, cardNo, batchNumber,
					voucherNumber, authNumber, referNumber, dealDate, dealTime, 
					dealAmount, merchantN, reference;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.consume);
        this.hideTitleSubmit();
        setTitle("消费结果");
        setRequireLogon();
        //btnSubmit.setOnClickListener(this);
        btnFinish = (Button)findViewById(R.id.charge_finish);
        btnFinish.setOnClickListener(this);
        
        merchantNumber = (TextView)findViewById(R.id.merchantno_ticket);
        terminalNumber = (TextView)findViewById(R.id.terminano_ticket);
        cardNo = (TextView)findViewById(R.id.cardno_ticket);
        voucherNumber = (TextView)findViewById(R.id.voucherno_ticket);
        authNumber = (TextView)findViewById(R.id.authno_ticket);
        referNumber = (TextView)findViewById(R.id.referno_ticket);
        dealDate = (TextView)findViewById(R.id.transactiondate_ticket);
        dealTime = (TextView)findViewById(R.id.transactiontime_ticket);
        dealAmount = (TextView)findViewById(R.id.amountticket);
        reference = (TextView)findViewById(R.id.reference);
        batchNumber = (TextView)findViewById(R.id.batchno_ticket);
        merchantN = (TextView)findViewById(R.id.merchantname_ticket);
        
        String extra = POSHelper.getSessionString();
        if (extra == null) {
        	finish();
        }
        all = (String[]) getIntent().getStringArrayExtra(extra);
		if(all == null || all.length != 14) {
			finish();
		}
		merchantNumber.setText(all[0]);
		terminalNumber.setText(all[1]);
		cardNo.setText(all[2]);
		batchNumber.setText(all[3]);
		voucherNumber.setText(all[4]);
		authNumber.setText(all[5]);
		referNumber.setText(all[6]);
		dealDate.setText(all[7]);
		dealTime.setText(all[8]);
		dealAmount.setText("RMB￥" + all[9]);
		merchantN.setText(all[11]);
		reference.setText(all[13]);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.charge_finish:
			v.setEnabled(false);
			Intent intent = new Intent(ConsumeActivity.this, SignNameActivity.class);
			intent.putExtra("allString", all);
			startActivity(intent);
			finish();
			break;
		}
	}

}
