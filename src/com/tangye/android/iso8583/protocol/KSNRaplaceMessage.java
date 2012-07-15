package com.tangye.android.iso8583.protocol;

import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.IsoTemplate;
import com.tangye.android.iso8583.IsoType;

public class KSNRaplaceMessage extends BaseMessageAbstract{
    
	@Override
	protected void onCreateRequestIsoMessage(IsoMessage requestMsg) {
		requestMsg.setType(0x0900);
		requestMsg.setBinary(true);
		requestMsg.setValue(60, "09", IsoType.LLLVARBCD);
	}

	@Override
	protected void onCreateResponseIsoTemplate(IsoTemplate respTemp) {
		respTemp.setType(0x0910);
		respTemp.setValue(39, IsoType.ALPHA, 2);
		respTemp.setValue(41, IsoType.ALPHA, 8);
		respTemp.setValue(42, IsoType.ALPHA, 15);
		respTemp.setValue(58, IsoType.LLLVAR); 
		respTemp.setValue(60, IsoType.LLLVARBCD);
	}

	@Override
	public boolean isBitmapValid() {
		int[] in = {39, 41, 42, 58,60};
		int[] out = {41, 42, 58, 60};
		return isBitmapValid(in, out);
	}
	
	public KSNRaplaceMessage setCardNumber_2(String cardNumber) {
        req.setValue(2, cardNumber, IsoType.LLVARBCD);
        return this;
    }
	
	public KSNRaplaceMessage setTerminalMark_41(String terminalMark){
		req.setValue(41, terminalMark, IsoType.ALPHA, 8);
        return this;
	}
	
	public KSNRaplaceMessage setUserMark_42(String userMark) {
        req.setValue(42, userMark, IsoType.ALPHA, 15);
        return this;
    }
    
	public KSNRaplaceMessage setKSN_58(String ksn){
		req.setValue(58, ksn, IsoType.LLLVAR);
		return this;
	}
	
}

