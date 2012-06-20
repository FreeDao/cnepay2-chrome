package com.tangye.android.iso8583.protocol;

import java.io.UnsupportedEncodingException;

import android.util.Log;

import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.IsoTemplate;
import com.tangye.android.iso8583.IsoType;
import com.tangye.android.utils.GBKBase64;

public class ReBindMessage extends BaseMessageAbstract {

    private String FixedValue_63_1;
    private String FixedValue_63_3;
    
	@Override
	protected void onCreateRequestIsoMessage(IsoMessage requestMsg) {
		requestMsg.setType(0x0900);
		requestMsg.setBinary(true);
		FixedValue_63_1 = "000";
		FixedValue_63_3 = "00000000000000000000";
		requestMsg.setValue(60, "05", IsoType.LLLVARBCD);
	}

	@Override
	protected void onCreateResponseIsoTemplate(IsoTemplate respTemp) {
		respTemp.setType(0x0910);
		respTemp.setValue(2, IsoType.LLVARBCD);
		respTemp.setValue(16, IsoType.NUMERIC, 4);
		respTemp.setValue(39, IsoType.ALPHA, 2);
		respTemp.setValue(41, IsoType.ALPHA, 8);
		respTemp.setValue(42, IsoType.ALPHA, 15);
		respTemp.setValue(46, IsoType.LLLVAR); // 密钥
		respTemp.setValue(60, IsoType.LLLVARBCD);
	}

	@Override
	public boolean isBitmapValid() {
		int[] in = {2,16,39,41,42,46,60};
		int[] out = {2,16,57,60,63};
		return isBitmapValid(in, out);
	}
	
	public ReBindMessage setCardNumber_2(String cardNumber) {
        req.setValue(2, cardNumber, IsoType.LLVARBCD);
        return this;
    }
	
	public ReBindMessage setSource_16(String source){
		req.setValue(16, source, IsoType.NUMERIC, 4);
		return this;
	}
	
	public ReBindMessage setUserPassword_57(String password){
		try {
			req.setValue(57, GBKBase64.encode(password), IsoType.LLLVAR);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Log.e("aboutPassword", "57's setValue");
		}
		return this;
	}

	public ReBindMessage setOtherInfo_63(String id, String phone) {
		String x = FixedValue_63_1;
		x += extAlpha(id, 20); // 身份证
		x += FixedValue_63_3;
		x += extAlpha(phone, 20); //手机号
        req.setValue(63, x, IsoType.LLLVAR);
        return this;
    }
}
