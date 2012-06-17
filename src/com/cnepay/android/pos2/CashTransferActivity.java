package com.cnepay.android.pos2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CashTransferActivity extends UIBaseActivity implements View.OnClickListener{
	
	private String[] all;
	private Button btnFinish;
	
	private TextView terminalNumber, inCardNo, batchNumber, outCardNo,
	voucherNumber, authNumber, referNumber, dealDate, dealTime, dealAmount;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cashtransfer);
        this.hideTitleSubmit();
        setTitle("转账结果");
        setRequireLogon();

        btnFinish = (Button)findViewById(R.id.charge_finish_transfer);
        btnFinish.setOnClickListener(this);
        
        terminalNumber = (TextView)findViewById(R.id.terminano_ticket_transfer);
        outCardNo = (TextView)findViewById(R.id.out_ticket_transfer);
        voucherNumber = (TextView)findViewById(R.id.voucherno_ticket_transfer);
        authNumber = (TextView)findViewById(R.id.authno_ticket_transfer);
        referNumber = (TextView)findViewById(R.id.referno_ticket_transfer);
        dealDate = (TextView)findViewById(R.id.transactiondate_ticket_transfer);
        dealTime = (TextView)findViewById(R.id.transactiontime_ticket_transfer);
        dealAmount = (TextView)findViewById(R.id.amountticket_transfer);
        batchNumber = (TextView)findViewById(R.id.batchno_ticket_transfer);
        inCardNo = (TextView)findViewById(R.id.in_ticket_transfer);
        
        all = (String[]) getIntent().getStringArrayExtra("allstring");
		if(all == null || all.length != 12) {
			finish();
		}
		terminalNumber.setText(all[0]);
		outCardNo.setText(all[1]);
		batchNumber.setText(all[2]);
		voucherNumber.setText(all[3]);
		authNumber.setText(all[4]);
		referNumber.setText(all[5]);
		dealDate.setText(all[6]);
		dealTime.setText(all[7]);
		dealAmount.setText("RMB￥" + all[8]);
		inCardNo.setText(all[11]);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.charge_finish_transfer:
			v.setEnabled(false);
			Intent intent = new Intent(CashTransferActivity.this, SignNameTransferActivity.class);
			intent.putExtra("allString", all);
			startActivity(intent);
			finish();
			break;
		}
	}
}
