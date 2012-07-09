package com.cnepay.android.pos2;

import com.tangye.android.utils.PublicHelper;
import com.tangye.android.utils.VoucherDraw;
import com.tangye.android.utils.VoucherDraw.Item;

import android.graphics.BitmapFactory;
import android.os.Bundle;

public class CashTransferActivity extends ConsumeBaseActivity {
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("转账成功");
		setTraceId(m[11]);
		setFilePath("TransferHistory", m[12]);
	}
	
	@Override
	protected void setSource(VoucherDraw view, String signaturePath) {
		Item source[] = {
			new Item(BitmapFactory.decodeResource(getResources(), R.drawable.unionpay), 40, 20, 20, -55),
			new Item(getString(R.string.app_name) + "手机客户端支付", 35, 0.8f, true, 0, 20, true),
			new Item("终端号(TERMINAL NO): " + m[0], 30, 0.6f, 20, 0, 0, false),
			new Item("转出卡号(CARD NO): " + PublicHelper.getMaskedString(m[1], 6, 4, '*') +" S", 30, 0.6f, 20, 0, 0, true),
			new Item("转入卡号(CARD NO): " + m[2], 30, 0.6f, 20, 0, 0, true),
			new Item("交易类型(TRANS TYPE):", 30, 0.6f, 20, 0, 0, false),
				new Item("消费/SALE(S)", 30, 0.6f, 40, 0, 0, false),
			new Item("授权码(AUTH NO): " + m[3], 30, 0.6f, 20, 0, 0, false),
			new Item("参考号(REFER NO): " + m[4], 30, 0.6f, 20, 0, 0, false),
			new Item("批次号(BATCH NO): " + m[5], 20, 1, 20, 0, 0, false),
			new Item("凭证号(VOUCHER NO): " + m[6], 20, 1, 20, 0, 0, false),
			new Item("交易日期(DATE): " + m[7], 20, 1, 20, 0, 0, false),
			new Item("交易时间(TIME): " + m[8], 20, 1, 20, 0, 0, false),
			new Item("汇款人(REMITTER): " + m[9], 30, 0.6f, 20, 0, 0, false),
			new Item("金额(AMOUNT):", 30, 0.6f, 20, 0, 0, false),
				new Item("RMB: " + m[10], 30, 0.9f, 40, 0, 0, true),
			new Item("备注(REFERENCE): 转账", 30, 0.6f, 20, 0, 0, false),
			new Item("持卡人签名(CARDHOLDER SIGNATURE):", 30, 0.6f, 20, 0, 120, false),
			new Item(BitmapFactory.decodeFile(signaturePath), 120, true, -120, 0),
			new Item("本人确认以上交易", 25, 0.6f, 20, 0, 0, false),
			new Item("同意将其计入本卡账户", 25, 0.6f, 20, 0, 0, false),
			new Item("I ACKNOWLEDGE SATISFACTORY", 25, 0.6f, 20, 0, 0, false),
			new Item("RECEIPT OF RELATIVE GOODS/SERVICE", 25, 0.6f, 20, 0, 30, false),
			new Item("商户存根(MERCHANT COPY)", 30, 0.6f, 20, 0, 20, false)
		};
		view.setResource(source);
	}

}
