package com.tangye.android.iso8583.protocol;

import com.tangye.android.iso8583.*;
import com.tangye.android.utils.CardInfo;

public class BalanceEnquiryMessage extends BaseMessageAbstract {

	private String TraceType_60_1;

	@Override
	protected void onCreateRequestIsoMessage(IsoMessage requestMsg) {
		requestMsg.setType(0x0200);
        requestMsg.setBinary(true);
        requestMsg.setValue(3, "310000", IsoType.NUMERIC, 6); // 交易处理码
		requestMsg.setValue(25, "00", IsoType.NUMERIC, 2); // 服务点条件码
		requestMsg.setValue(49, "156", IsoType.ALPHA, 3); // 货币代码目前只支持RMB
		setSecurityControl_53("2000000000000000"); // 空全控制码默认为ANSI9.8+DES		
		TraceType_60_1 = "01"; // 查询
	}

	@Override
	protected void onCreateResponseIsoTemplate(IsoTemplate respTemp) {
		respTemp.setType(0x0210);
		respTemp.setValue(2, IsoType.LLVARBCD);
		respTemp.setValue(3, IsoType.NUMERIC, 6);
		respTemp.setValue(11, IsoType.NUMERIC, 6);
		respTemp.setValue(12, IsoType.TIME);
		respTemp.setValue(13, IsoType.DATE4);
		respTemp.setValue(14, IsoType.DATE_EXP); // 接收时可选域，卡有效期
		respTemp.setValue(25, IsoType.NUMERIC, 2);
		respTemp.setValue(32, IsoType.LLVARBCD);
		respTemp.setValue(37, IsoType.ALPHA, 12);
		respTemp.setValue(39, IsoType.ALPHA, 2);
		respTemp.setValue(41, IsoType.ALPHA, 8);
		respTemp.setValue(42, IsoType.ALPHA, 15);
		respTemp.setValue(44, IsoType.LLVAR); // 收单/接收机构，附加码
		respTemp.setValue(49, IsoType.ALPHA, 3);
		respTemp.setValue(53, IsoType.NUMERIC, 16); // 2000000000000000
		respTemp.setValue(54, IsoType.LLLVAR); // 附加金额
		respTemp.setValue(58, IsoType.LLLVAR);
		respTemp.setValue(60, IsoType.LLLVARBCD);
		respTemp.setValue(64, IsoType.ALPHA, 8); // MAC
	}

	@Override
	public boolean isBitmapValid() {
		int[] in = {2,3,11,12,13,14,25,32,37,39,41,42,44,49,53,54,58,60,64};
		int[] out = {3,11,22,25,26,35,41,42,49,52,53,58,60,64};
		return isBitmapValid(in, out);
	}
		
    public BalanceEnquiryMessage setCardTracerNumber_11(String traceNumber) {
        req.setValue(11, traceNumber, IsoType.NUMERIC, 6);
        return this;
    }

    
    public BalanceEnquiryMessage setIsPinNeed_22(boolean is) {
    	if(is) {
    		req.setValue(22, "021", IsoType.NUMERIC, 3);
    	} else {
    		req.setValue(22, "022", IsoType.NUMERIC, 3);
    	}
    	return this;
    }
    
    public BalanceEnquiryMessage setMaxPinLength_26(int len) {
    	// length should be between 4-12, default 10;
    	if(len >=4 || len <= 12) {
    		req.setValue(26, len, IsoType.NUMERIC, 2);
    	}
    	return this;
    }
    
    public BalanceEnquiryMessage setTerminalMark_41(String terminalMark) {
        req.setValue(41, terminalMark, IsoType.ALPHA, 8);
        return this;
    }
    
    public BalanceEnquiryMessage setUserMark_42(String userMark) {
        req.setValue(42, userMark, IsoType.ALPHA, 15);
        return this;
    }
    
    public BalanceEnquiryMessage setUserPin_52(String pin) {
    	req.setValue(52, pin, IsoType.ALPHA, 8); // 加密后的64位Binary,8个字节
    	return this;
    }
    
    public BalanceEnquiryMessage setSecurityControl_53(String sec) {
    	// Default 2000000000000000, ANSIX9.8Format DES
    	req.setValue(53, sec, IsoType.NUMERIC, 16); // 安全控制码16位BCD
    	return this;
    }
    
    public BalanceEnquiryMessage setSetNumber_60(String setNumber) {
    	String tmp = TraceType_60_1 + setNumber; // setNumber length should be 6
        req.setValue(60, tmp, IsoType.LLLVARBCD);
        return this;
    }
    
    public BalanceEnquiryMessage setUseMac_64() {
        req.setUseMac64(true);
    	return this;
    }

    public BalanceEnquiryMessage setCardInfo(CardInfo ci) {
		ci.loadMessage(req, true);
		return this;
	}
}
