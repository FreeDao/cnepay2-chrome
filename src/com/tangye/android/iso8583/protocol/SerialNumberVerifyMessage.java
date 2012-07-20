package com.tangye.android.iso8583.protocol;


import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.IsoTemplate;
import com.tangye.android.iso8583.IsoType;

public class SerialNumberVerifyMessage extends BaseMessageAbstract{

	@Override
	protected void onCreateRequestIsoMessage(IsoMessage requestMsg) {
		requestMsg.setType(0x0900);
		requestMsg.setBinary(true);
		requestMsg.setValue(60, "02", IsoType.LLLVARBCD);
	}

	@Override
	protected void onCreateResponseIsoTemplate(IsoTemplate respTemp) {
		respTemp.setType(0x0910);
		respTemp.setValue(16, IsoType.NUMERIC, 4);
		respTemp.setValue(39, IsoType.ALPHA, 2);
		respTemp.setValue(54, IsoType.LLLVAR);
		respTemp.setValue(60, IsoType.LLLVARBCD);
	}

	@Override
	public boolean isBitmapValid() {
		int[] in = {16, 39, 54, 60};
		int[] out = {16, 54, 60};
		return isBitmapValid(in, out);
	}
	   
	public SerialNumberVerifyMessage setSource_16(String source){
		req.setValue(16, source, IsoType.NUMERIC, 4);
		return this;
	}
	
	public SerialNumberVerifyMessage setKSN_54(String serial){
		req.setValue(54, serial, IsoType.LLLVAR);
		return this;
	}
	

}

