package com.tangye.android.iso8583.protocol;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import com.tangye.android.iso8583.*;
import com.tangye.android.utils.CardInfo;
import com.tangye.android.utils.GBKBase64;

public class TransferMessage extends BaseMessageAbstract {

	private String TraceType_60_1;

	@Override
	protected void onCreateRequestIsoMessage(IsoMessage requestMsg) {
		requestMsg.setType(0x0200);
        requestMsg.setBinary(true);
        requestMsg.setValue(3, "080000", IsoType.NUMERIC, 6); // 交易处理码
		setIsPinNeed_22(true); // default need Pin input
		requestMsg.setValue(25, "00", IsoType.NUMERIC, 2); // 服务点条件码
		setMaxPinLength_26(10); // Pin Max length default 10, 可选
		requestMsg.setValue(49, "156", IsoType.ALPHA, 3); // 货币代码目前只支持RMB
		setSecurityControl_53("2000000000000000"); // 空全控制码默认为ANSI9.8+DES		
		TraceType_60_1 = "80"; // 卡卡转账
	}

	@Override
	protected void onCreateResponseIsoTemplate(IsoTemplate respTemp) {
		respTemp.setType(0x0210);
		respTemp.setValue(2, IsoType.LLVARBCD);
		respTemp.setValue(3, IsoType.NUMERIC, 6);
		respTemp.setValue(4, IsoType.AMOUNT);
		respTemp.setValue(6, IsoType.AMOUNT);
		respTemp.setValue(11, IsoType.NUMERIC, 6);
		respTemp.setValue(12, IsoType.TIME);
		respTemp.setValue(13, IsoType.DATE4);
		respTemp.setValue(14, IsoType.DATE_EXP); // 接收时可选域，卡有效期
		respTemp.setValue(15, IsoType.DATE4); // 清算日期
		respTemp.setValue(25, IsoType.NUMERIC, 2);
		respTemp.setValue(32, IsoType.LLVARBCD);
		respTemp.setValue(37, IsoType.ALPHA, 12);
		respTemp.setValue(38, IsoType.ALPHA, 6); // 授权码，可选6位，发卡行决定
		respTemp.setValue(39, IsoType.ALPHA, 2);
		respTemp.setValue(41, IsoType.ALPHA, 8);
		respTemp.setValue(42, IsoType.ALPHA, 15);
		respTemp.setValue(44, IsoType.LLVAR); // 收单/接收机构，附加码
		respTemp.setValue(49, IsoType.ALPHA, 3);
		respTemp.setValue(53, IsoType.NUMERIC, 16); // 2000000000000000
		respTemp.setValue(55, IsoType.LLLVAR);
		respTemp.setValue(59, IsoType.LLLVAR);
		respTemp.setValue(60, IsoType.LLLVARBCD);
		respTemp.setValue(63, IsoType.LLLVAR); // 自定义域
		respTemp.setValue(64, IsoType.ALPHA, 8); // MAC
	}

	@Override
	public boolean isBitmapValid() {
		int[] in = {2,3,4,6,11,12,13,14,15,25,32,37,38,39,41,42,44,49,53,55,59,60,63,64};
		int[] out = {3,4,5,6,11,22,25,26,35,41,42,46,47,48,49,52,53,56,58,60,64};
		return isBitmapValid(in, out);
	}
	
	public TransferMessage setAmountTotal_4(BigDecimal amount) {
		req.setValue(4, amount, IsoType.AMOUNT);
		return this;
	}
	
	public TransferMessage setReceiverBankId_5(String bankID) {
        req.setValue(5, bankID, IsoType.NUMERIC, 12);
        return this;
    }
	
	public TransferMessage setAmount_6(BigDecimal amount) {
        req.setValue(6, amount, IsoType.AMOUNT);
        return this;
    } 
	
    public TransferMessage setCardTracerNumber_11(String traceNumber) {
        req.setValue(11, traceNumber, IsoType.NUMERIC, 6);
        return this;
    }

    /*******
     * d is constructed as a exp date with YYMM format, 刷卡时不需要
    public ConsumeMessage setCardInvalidDate_14(java.util.Date d) {
    	req.setValue(14, d, IsoType.DATE_EXP);
    	return this;
    }
    *******/
    
    public TransferMessage setIsPinNeed_22(boolean is) {
    	if(is) {
    		req.setValue(22, "021", IsoType.NUMERIC, 3);
    	} else {
    		req.setValue(22, "022", IsoType.NUMERIC, 3);
    	}
    	return this;
    }
    
    public TransferMessage setMaxPinLength_26(int len) {
    	// length should be between 4-12, default 10;
    	if(len >=4 || len <= 12) {
    		req.setValue(26, len, IsoType.NUMERIC, 2);
    	}
    	return this;
    }
    
    public TransferMessage setTerminalMark_41(String terminalMark) {
        req.setValue(41, terminalMark, IsoType.ALPHA, 8);
        return this;
    }
    
    public TransferMessage setUserMark_42(String userMark) {
        req.setValue(42, userMark, IsoType.ALPHA, 15);
        return this;
    }
    
    public TransferMessage setUserName_46(String userName) {
        try {
            req.setValue(46, GBKBase64.encode(userName), IsoType.LLLVAR);
        } catch (UnsupportedEncodingException e) {
            req.setValue(46, userName, IsoType.LLLVAR);
        }
        return this;
    }
    
    public TransferMessage setReceiverBankName_47(String bankName) {
        try {
            req.setValue(47, GBKBase64.encode(bankName), IsoType.LLLVAR);
        } catch (UnsupportedEncodingException e) {
            req.setValue(47, bankName, IsoType.LLLVAR);
        }
        return this;
    }
    
    public TransferMessage setReceiverCardNumber_48(String cardNumber) {
        req.setValue(48, cardNumber, IsoType.LLLVARBCD);
        return this;
    }
    
    public TransferMessage setUserPin_52(String pin) {
    	req.setValue(52, pin, IsoType.ALPHA, 8); // 加密后的64位Binary,8个字节
    	return this;
    }
    
    public TransferMessage setSecurityControl_53(String sec) {
    	// Default 2000000000000000, ANSIX9.8Format DES
    	req.setValue(53, sec, IsoType.NUMERIC, 16); // 安全控制码16位BCD
    	return this;
    }
    
    public TransferMessage setReceiverUserName_56(String userName) {
        try {
            req.setValue(56, GBKBase64.encode(userName), IsoType.LLLVAR);
        } catch (UnsupportedEncodingException e) {
            req.setValue(56, userName, IsoType.LLLVAR);
        }
        return this;
    }
    
    public TransferMessage setSetNumber_60(String setNumber) {
    	String tmp = TraceType_60_1 + setNumber; // setNumber length should be 6
        req.setValue(60, tmp, IsoType.LLLVARBCD);
        return this;
    }
    
    public TransferMessage setUseMac_64() {
        req.setUseMac64(true);
    	return this;
    }
    
    public TransferMessage setCardInfo(CardInfo ci) {
		ci.loadMessage(req, true);
		return this;
	}
}
