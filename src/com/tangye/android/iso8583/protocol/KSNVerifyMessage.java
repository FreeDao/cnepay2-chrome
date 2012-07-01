package com.tangye.android.iso8583.protocol;

import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.IsoTemplate;
import com.tangye.android.iso8583.IsoType;

public class KSNVerifyMessage extends BaseMessageAbstract{

	@Override
	protected void onCreateRequestIsoMessage(IsoMessage requestMsg) {
		requestMsg.setType(0x0900);
		requestMsg.setBinary(true);
		requestMsg.setValue(60, "01", IsoType.LLLVARBCD);
	}

	@Override
	protected void onCreateResponseIsoTemplate(IsoTemplate respTemp) {
		respTemp.setType(0x0910);
		respTemp.setValue(39, IsoType.ALPHA, 2);
		respTemp.setValue(60, IsoType.LLLVARBCD);
	}

	@Override
	public boolean isBitmapValid() {
		int[] in = {39, 60};
		int[] out = {58, 60};
		return isBitmapValid(in, out);
	}
	   
	public KSNVerifyMessage setKSN_58(String ksn){
		req.setValue(58, ksn, IsoType.LLLVAR);
		return this;
	}
	

}
