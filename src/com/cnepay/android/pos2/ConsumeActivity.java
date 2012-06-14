package com.cnepay.android.pos2;

import com.tangye.android.iso8583.POSHelper;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConsumeActivity extends UIBaseActivity implements View.OnClickListener{
	
	private static final String TAG = "ConsumeActivity";
	
	
    private String[] all;
	//Button btnClear;
	Button btnFinish;
	String lastPaymentDes;
	Handler mHandler;
	volatile boolean isProcessing;
	TextView merchantNumber, terminalNumber, cardNo, iBankName, batchNumber,
	voucherNumber, authNumber, referNumber, dealDate, dealTime, dealAmount, merchantN;
	LinearLayout result;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.consume);
        this.showTitleSubmit();
        setTitle("消费结果");
        setRequireLogon();
        btnSubmit.setOnClickListener(this);
        //btnClear = (Button)findViewById(R.id.clear_signature);
        btnFinish = (Button)findViewById(R.id.charge_finish);
        
        merchantNumber = (TextView)findViewById(R.id.merchantno_ticket);
        terminalNumber = (TextView)findViewById(R.id.terminano_ticket);
        cardNo = (TextView)findViewById(R.id.cardno_ticket);
        iBankName = (TextView)findViewById(R.id.issuingname_ticket);
        voucherNumber = (TextView)findViewById(R.id.voucherno_ticket);
        authNumber = (TextView)findViewById(R.id.authno_ticket);
        referNumber = (TextView)findViewById(R.id.referno_ticket);
        dealDate = (TextView)findViewById(R.id.transactiondate_ticket);
        dealTime = (TextView)findViewById(R.id.transactiontime_ticket);
        dealAmount = (TextView)findViewById(R.id.amountticket);
        result = (LinearLayout)findViewById(R.id.recharge_result);
        batchNumber = (TextView)findViewById(R.id.batchno_ticket);
        merchantN = (TextView)findViewById(R.id.merchantname_ticket);
        
        String extra = POSHelper.getSessionString();
        if (extra == null) {
        	finish();
        }
        all = (String[]) getIntent().getStringArrayExtra(extra);
		if(all != null && all.length != 14) {
			//TODO below
		}
		merchantNumber.setText(all[0]);
		terminalNumber.setText(all[1]);
		cardNo.setText(all[2]);
		iBankName.setText(all[3]);
		batchNumber.setText(all[4]);
		voucherNumber.setText(all[5]);
		authNumber.setText(all[6]);
		referNumber.setText(all[7]);
		dealDate.setText(all[8]);
		dealTime.setText(all[9]);
		dealAmount.setText(all[10]);
		merchantN.setText(all[12]);
    	findViewById(R.id.recharge_loading).setVisibility(View.GONE);
    	findViewById(R.id.recharge_result).setVisibility(View.VISIBLE);
    	//* TODO delete TEST
    	// IsoMessage m = (IsoMessage)msg.obj;
    	// Toast.makeText(ConsumeActivity.this, m.getField(44).toString(), Toast.LENGTH_SHORT).show();
    	/*if(lastPaymentDes != null) {
    	    Toast.makeText(ConsumeActivity.this, getString(R.string.desc_chg) + ":" 
    	            + lastPaymentDes, Toast.LENGTH_SHORT).show();
    	}*/
    	//* END TEST
    	// TODO record history payment
    	// msg.obj is the IsoMessage for this consumption

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
