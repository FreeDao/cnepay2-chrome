package com.cnepay.android.pos2;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.POSEncrypt;
import com.tangye.android.iso8583.POSHelper;
import com.tangye.android.iso8583.POSSession;
import com.tangye.android.iso8583.protocol.ConsumeMessage;
import com.tangye.android.utils.GBKBase64;

public class ConsumeActivity extends UIBaseActivity implements View.OnClickListener{
	
	private static final String TAG = "ConsumeActivity";
	
	private static final int SUCCESS = 0;
    private static final int FAILURE = 1;
	
    private String[] all;
	//Button btnClear;
	Button btnFinish;
	String lastPaymentDes;
	Handler mHandler;
	volatile boolean isProcessing;
	TextView merchantNumber, terminalNumber, cardNo, iBankName, batchNumber,
	voucherNumber, authNumber, referNumber, dealDate, dealTime, dealAmount, merchantN;
	LinearLayout result;
	
	
	private ConsumeMessage cm;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.consume);
        this.showTitleSubmit();
        setTitle("消费结果");
        this.btnSubmit.setOnClickListener(this);
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
        
        
        mHandler = new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
        		switch(msg.what) {
        		case SUCCESS:
	        		setResult(RESULT_OK);
	        		all = (String[])msg.obj;
	        		if(all != null && all.length != 14) {
	        			//TODO
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
		        	break;
        		case FAILURE:
        			setResult(RESULT_CANCELED, (Intent)msg.obj);
        			findViewById(R.id.recharge_loading).setVisibility(View.GONE);
        			finish();
        			// TODO need to 冲正
        			break;
        		}
        	}
        };
        //startconsume();
		
	}
	
	private void startconsume() {
	    isProcessing = true;
		final String key = POSHelper.getSessionString();
		if(key == null) {
			finish(); // TODO session GONE;
			return;
		}
		(new Thread(TAG) {
				
			public void run() {
				String[] allMessage = new String[]{};
				Intent intent = getIntent();
				String[] tmp = intent.getStringArrayExtra(key);
				String amount = tmp[0];
				String descri = tmp[1];
				String track2 = tmp[2];
				String passwd = tmp[3];
				POSSession SESSION = POSHelper.getPOSSession();
				if(SESSION == null){
					finish();
					return;
				}
				String name = SESSION.getSessionAccount();
				SESSION.close();
				if(name == null) {
					finish();
					return; // TODO session invalid
				}
				POSEncrypt POS = POSHelper.getPOSEncrypt(ConsumeActivity.this, name);
				POS.addTraceNumber();
				cm = new ConsumeMessage();
                cm.setAmount_4(new BigDecimal(amount))
                .setCardTracerNumber_11(POS.getPOSDecrypt(POS.TRACENUMBER))
                .setIsPinNeed_22(true)
                .setMaxPinLength_26(10) // TODO hard-code here
                .setTrack2Info_35(track2)
                .setTerminalMark_41(POS.getPOSDecrypt(POS.TERMINAL))
                .setUserMark_42(POS.getPOSDecrypt(POS.USERMARK))
                .setUserPin_52(passwd)
                .setSetNumber_60(POS.getPOSDecrypt(POS.SETNUMBER))
                .setUseMac_64();
				POS.close();
				boolean isOk = false;
	            String error = "";
	            try {
	                if(!cm.isBitmapValid()) throw new RuntimeException("BitmapError");
	                IsoMessage resp = cm.request();
	                if(resp != null) {
	                	String statusCode = resp.getField(39).toString();
	                	if (statusCode.equals("00")) {
	                		String merchantNo = resp.getField(42).toString();
	                		String terminalNo = resp.getField(41).toString();
	                		String cardNumber = resp.getField(2).toString();
	                		String batchNo = ConsumeActivity.this.getBatchNum(resp.getField(60).toString());
	                		String voucherNo = resp.getField(11).toString();
	                		String authNo = null;
	                		if(resp.getField(42).toString().length() == 0 || resp.getField(42).toString() == null) {
	                			authNo = "000000";
	                		} else {
	                			authNo = resp.getField(42).toString();
	                		}
	                		String referNo = resp.getField(37).toString();
	                		String transactionDate = ConsumeActivity.this.getTransactionDate(resp.getField(13).toString());
	                		String transactionTime = ConsumeActivity.this.getTransactionTime(resp.getField(12).toString());
	                		String transactionAmount = amount;
	                		String traceId = resp.getField(59).toString();
	                		String merchantName = GBKBase64.decode(resp.getField(55).toString());
	                		long time=System.currentTimeMillis();
	                		Calendar mCalendar=Calendar.getInstance();
	                		mCalendar.setTimeInMillis(time);
	                		int TransactionYear = mCalendar.get(Calendar.YEAR);
	                		String FileName =TransactionYear + resp.getField(13).toString() + resp.getField(12).toString();
	                		allMessage = new String[]{merchantNo, terminalNo, cardNumber,
	                				batchNo, voucherNo, authNo, referNo, transactionDate,
	                				transactionTime, transactionAmount, traceId, merchantName, FileName};
		                    isOk = true;
	                	} else {
	                		/*
	                		if(statusCode.equals("Z3")){
	                			error = "序列号已被使用";
	                		} else if (statusCode.equals("R5")){
	                			error = "卡已被注册过";
	                		} else if (statusCode.equals("R6")){
			                			error = "信用卡不能注册";
	                		} else if (statusCode.equals("Z4")){
	                			error = "您已绑定过序列号";
	                		} else {
	                			error = getError(statusCode);
	                		}
	                		*/
	                		error = getError(statusCode);
	                	}
	                } else {
	                	// Manually stop client from user's aspect
	                    error = null;
	                    isOk = false;
	                }
	            } catch (SocketTimeoutException e) {
	                error = "连接超时，请确保网络稳定性";
	                e.printStackTrace();
	            } catch (IllegalStateException e) {
	                error = "严重错误，请咨询售后";
	                e.printStackTrace();
	            } catch (UnknownHostException e) {
	                error = "无法连接主机，请检查连接性";
	                e.printStackTrace();
	            } catch (IOException e) {
	                error = "请检查手机的网络信号";
	                e.printStackTrace();
	            } catch (Exception e) {
	            	error = "报文错误，请联系客服";
	                Log.i(TAG, "Parse Error: " + e);
				}  
	            if(!isOk) {
	                mHandler.obtainMessage(FAILURE, error).sendToTarget();
	            } else {	            	 
	                setDescription(descri);
	                mHandler.obtainMessage(SUCCESS, allMessage).sendToTarget();
	            }
	            isProcessing = false;
			}
		}).start();
	}
	
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && isProcessing) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
	
	@Override
    protected void onPause() {
        super.onPause();
       
    }

	@Override
	public void onClick(View v) {
		
	}

	@Override
	public void onComplete(String cn) {
		super.onComplete(cn);
		
	}

	@Override
	public void onError(int err) {
		super.onError(err);
	}

	@Override
	public void onDecoding() {
		super.onDecoding();
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
	}
	
	private void setDescription(String des) {
		// TODO display the description on the screen
		lastPaymentDes = des;
	}
	
	private String getTransactionDate(String code) {
		long time=System.currentTimeMillis();
		Calendar mCalendar=Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		int TransactionYear = mCalendar.get(Calendar.YEAR);
		StringBuffer mouthDay = new StringBuffer(code);
		String transactionDate = TransactionYear + "/" + mouthDay.insert(2, "/");
		return transactionDate;
	}
	
	private String getTransactionTime(String code) {
		StringBuffer sb2 = new StringBuffer(code);
		sb2.insert(2, ":");
		sb2.insert(5, ":");
		String transactionTime = sb2.toString();
		return transactionTime;
	}
	
	private String getBatchNum(String code){
		String batchNum = code.substring(2);
		return batchNum;
	}
}
