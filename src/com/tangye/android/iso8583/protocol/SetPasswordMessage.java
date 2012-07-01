package com.tangye.android.iso8583.protocol;

import java.io.UnsupportedEncodingException;

import android.util.Log;

import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.IsoTemplate;
import com.tangye.android.iso8583.IsoType;
import com.tangye.android.utils.GBKBase64;

public class SetPasswordMessage extends BaseMessageAbstract {
	
	@Override
	protected void onCreateRequestIsoMessage(IsoMessage requestMsg) {
		requestMsg.setType(0x0900);
		requestMsg.setBinary(true);
		req.setValue(60, "06", IsoType.LLLVARBCD);
	}

	@Override
	protected void onCreateResponseIsoTemplate(IsoTemplate respTemp) {
		respTemp.setType(0x0910);
		respTemp.setValue(2, IsoType.LLVARBCD);
		respTemp.setValue(16, IsoType.NUMERIC, 4);
		respTemp.setValue(39, IsoType.ALPHA, 2);
		respTemp.setValue(56, IsoType.LLLVAR);
		respTemp.setValue(57, IsoType.LLLVAR);
		respTemp.setValue(60, IsoType.LLLVARBCD);
		respTemp.setValue(64, IsoType.ALPHA, 8);
	}

	@Override
	public boolean isBitmapValid() {
		int[] in = {2,16,39,56,57,60,64};
		int[] out = {16,41,42,56,57,60,64};
		return isBitmapValid(in, out);
	}
	
	public SetPasswordMessage setSource_16(String source){
		req.setValue(16, source, IsoType.NUMERIC, 4);
		return this;
	}
	
	public SetPasswordMessage setTerminalMark_41(String terminalMark) {
        req.setValue(41, terminalMark, IsoType.ALPHA, 8);
        return this;
    }
    
    public SetPasswordMessage setUserMark_42(String userMark) {
        req.setValue(42, userMark, IsoType.ALPHA, 15);
        return this;
    }
    
    public SetPasswordMessage setOldPassword_56(String oldPwd){
    	try {
			req.setValue(56, GBKBase64.encode(oldPwd), IsoType.LLLVAR);
		} catch (UnsupportedEncodingException e) {
			Log.e("aboutPassword", "56's setValue");
		}
    	return this;
    }
    
    public SetPasswordMessage setNewPassword_57(String newPassword){
    	try {
			req.setValue(57, GBKBase64.encode(newPassword), IsoType.LLLVAR);
		} catch (UnsupportedEncodingException e) {
			Log.e("aboutPassword", "57's setValue");
		}
    	return this;
    }
    
    public SetPasswordMessage setUseMac_64() {
        req.setUseMac64(true);
    	return this;
    }

}
