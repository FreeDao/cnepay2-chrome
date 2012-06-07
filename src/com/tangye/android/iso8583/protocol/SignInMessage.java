package com.tangye.android.iso8583.protocol;

import java.io.UnsupportedEncodingException;

import android.util.Log;

import com.tangye.android.iso8583.*;
import com.tangye.android.utils.GBKBase64;

public class SignInMessage extends BaseMessageAbstract {

	private String TraceType_60_1;
	private String NetworkInfoManage_60_3;
	private String OperatorNumber_63_1;

	@Override
	protected void onCreateRequestIsoMessage(IsoMessage requestMsg) {
		requestMsg.setType(0x0800);
        requestMsg.setBinary(true);
        requestMsg.setValue(25, "01", IsoType.NUMERIC, 2); // 00 刷卡登录，01 手机号登录
		TraceType_60_1 = "00";
		NetworkInfoManage_60_3 = "001";
		OperatorNumber_63_1 = "000";    // 操作员代码
	}

	@Override
	protected void onCreateResponseIsoTemplate(IsoTemplate respTemp) {
		respTemp.setType(0x0810);
		respTemp.setValue(11, IsoType.NUMERIC, 6);
		respTemp.setValue(12, IsoType.TIME);
		respTemp.setValue(13, IsoType.DATE4);
		respTemp.setValue(16, IsoType.NUMERIC, 4);
		respTemp.setValue(32, IsoType.LLVARBCD);
		respTemp.setValue(37, IsoType.ALPHA, 12);
		respTemp.setValue(39, IsoType.ALPHA, 2);
		respTemp.setValue(41, IsoType.ALPHA, 8);
		respTemp.setValue(42, IsoType.ALPHA, 15);
		respTemp.setValue(55, IsoType.LLLVAR);
		respTemp.setValue(60, IsoType.LLLVARBCD);
		respTemp.setValue(62, IsoType.LLLVAR); // WK BINARY24字节，按照ASCII对待
	}

	@Override
	public boolean isBitmapValid() {
		int[] in = {11,12,13,16,32,37,39,41,42,55,60,62};
		int[] out = {11,16,25,41,42,57,60,63}; // TODO 是否需要57域 密码
		return isBitmapValid(in, out);
	}
	
    public SignInMessage setCardTracerNumber_11(String traceNumber) {
        req.setValue(11, traceNumber, IsoType.NUMERIC, 6);
        return this;
    }
    
    public SignInMessage setSource_16(String source){
		req.setValue(16, source, IsoType.NUMERIC, 4);
		return this;
	}
    
    /*public SignInMessage setLoginType_25(String num){
    	req.setValue(25, num, IsoType.NUMERIC, 2);
    	return this;
    }*/
    
    public SignInMessage setTerminalMark_41(String terminalMark) {
        req.setValue(41, terminalMark, IsoType.ALPHA, 8);
        return this;
    }
    
    public SignInMessage setUserMark_42(String userMark) {
        req.setValue(42, userMark, IsoType.ALPHA, 15);
        return this;
    }
    
    public SignInMessage setUserPassword_57(String password){
		try {
			req.setValue(57, GBKBase64.encode(password), IsoType.LLLVAR);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Log.e("aboutPassword", "57's setValue");
		}
		return this;
	}
    
    public SignInMessage setSetNumber_60(String setNumber) {
    	String tmp = TraceType_60_1 + setNumber + NetworkInfoManage_60_3;
        req.setValue(60, tmp, IsoType.LLLVARBCD);
        return this;
    }
    
    public SignInMessage setPhoneNumber_63(String phone) {
    	String x = OperatorNumber_63_1;
		x += extAlpha(phone, 20); //手机号
        req.setValue(63, x, IsoType.LLLVAR);
        return this;
    }
}
