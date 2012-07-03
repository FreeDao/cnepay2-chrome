package com.tangye.android.iso8583.protocol;

import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.IsoTemplate;
import com.tangye.android.iso8583.IsoType;

public class ResetPasswdMessage extends BaseMessageAbstract {
	
	@Override
	protected void onCreateRequestIsoMessage(IsoMessage requestMsg) {
		requestMsg.setType(0x0900);
		requestMsg.setBinary(true);
		req.setValue(60, "07", IsoType.LLLVARBCD);
	}

	@Override
	protected void onCreateResponseIsoTemplate(IsoTemplate respTemp) {
		respTemp.setType(0x0910);
		respTemp.setValue(2, IsoType.LLVARBCD);
		respTemp.setValue(16, IsoType.NUMERIC, 4);
		respTemp.setValue(39, IsoType.ALPHA, 2);
		respTemp.setValue(46, IsoType.LLLVAR); // 密钥
		respTemp.setValue(60, IsoType.LLLVARBCD);
	}

	@Override
	public boolean isBitmapValid() {
		int[] in = {2,16,39,46,60};
		int[] out = {2,16,41,42,58,60,63};
		return isBitmapValid(in, out);
	}
	
	public ResetPasswdMessage setCardNumber_2(String cardNumber) {
        req.setValue(2, cardNumber, IsoType.LLVARBCD);
        return this;
    }
	
	public ResetPasswdMessage setSource_16(String source){
		req.setValue(16, source, IsoType.NUMERIC, 4);
		return this;
	}
	
	public ResetPasswdMessage setTerminalMark_41(String terminalMark) {
        req.setValue(41, terminalMark, IsoType.ALPHA, 8);
        return this;
    }
    
    public ResetPasswdMessage setUserMark_42(String userMark) {
        req.setValue(42, userMark, IsoType.ALPHA, 15);
        return this;
    }
    
	public ResetPasswdMessage setOtherInfo_63(String phone, String randCode) {
		String x = randCode;
		x += extAlpha(phone, 20); //手机号
        req.setValue(63, x, IsoType.LLLVAR);
        return this;
    }

}
