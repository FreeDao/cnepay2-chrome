package com.tangye.android.iso8583;

import java.util.Date;

import com.tangye.android.utils.AES;
import com.tangye.android.utils.DES;
import com.tangye.android.utils.MD5;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class POSEncrypt extends POSNative {
	
	public final String TRACENUMBER = "t";
	public final String TERMINAL = "tn";
	public final String USERMARK = "u";
	public final String SETNUMBER = "sn";
	public final String KEKENCODED = "kr";
	
	private SharedPreferences sp;
	
	public POSEncrypt(Context ctx, String md5) {
		super(ctx);
		sp = ctx.getApplicationContext().getSharedPreferences(md5, 0);
	}
	
	public void init(IsoMessage msg, String passwd) throws IllegalStateException {
		long x = new Date().getTime() % 999999 + 1;
		String traceNumber = String.format("%06d", x);
		// android.util.Log.e("KEKEncoded", msg.getField(46).toString());
		byte[] KEK = IsoUtil.hex2byte(msg.getField(46).toString());
        KEK = DES.decrypt(KEK);
        if(KEK == null || KEK.length == 0) throw new IllegalStateException("KEK cannot read");
        String ming = IsoUtil.byte2hex(KEK); // HEXSTRING KEK
        // android.util.Log.e("KEKDecoded", ming);
		new ready(sp.edit())
		.put(TRACENUMBER, traceNumber) // 流水号
		.put(TERMINAL, msg.getField(41).toString()) // 终端号
		.put(USERMARK, msg.getField(42).toString()) // 商户号
		.put(SETNUMBER, "000001") // 批次号
		.put(KEKENCODED, pAES(ming, getNativeK(passwd, msg.getField(2).toString())))
		.commit(); // store permanently
		ming = null;
	}
	
	public boolean isInitialized() {
		return has(TRACENUMBER) && has(TERMINAL) && has(USERMARK) && has(KEKENCODED);
	}
	
	public String getPOSDecrypt(String key) {
		if(!has(key)) throw new IllegalStateException("Yet not init POS");
		String str = get(key);
		return str;
	}
	
	public POSEncrypt addTraceNumber() {
		if(!has(TRACENUMBER)) throw new IllegalStateException("Yet not init POS");
		String traceNumber = get(TRACENUMBER, "000000");
		int x = Integer.valueOf(traceNumber) % 999999 + 1;
		traceNumber = String.format("%06d", x);
		new ready(sp.edit())
		.put(TRACENUMBER, traceNumber)
		.commit();
		return this;
	}
	
	public POSEncrypt setSetNumber(String setNumber) {
        // 如果长度不是6位或者与以前的值相同，则采用自己+1的方案
        if(setNumber.length() != 6 || setNumber.equals(get(SETNUMBER, "000100"))) {
            return this;
        }
        new ready(sp.edit())
        .put(SETNUMBER, setNumber)
        .commit();
        return this;
    }
	
	public POSEncrypt addSetNumber(String setNumber) {
	    // 如果长度不是6位或者与以前的值相同，则采用自己+1的方案
	    if(setNumber.length() != 6 || setNumber.equals(get(SETNUMBER, "000100"))) {
	        addSetNumber();
	        return this;
	    }
	    new ready(sp.edit())
        .put(SETNUMBER, setNumber)
        .commit();
        return this;
	}
	
	public POSEncrypt addSetNumber() {
        if(!has(SETNUMBER)) throw new IllegalStateException("Yet not init POS");
        String setNumber = get(SETNUMBER, "000100");
        int x = Integer.valueOf(setNumber);
        if(x > 999990) x = 100;
        else x++;
        setNumber = String.format("%06d", x);
        new ready(sp.edit())
        .put(SETNUMBER, setNumber)
        .commit();
        return this;
    }
	
	public void close() {
		sp = null;
		releaseNative();
	}
	
	private String pAES(String b64, String k1) {
		return AES.encryptTrack(b64, k1);
	}
	
	public String dAES(String s, String k1) {
		return AES.decryptTrack(s, k1);
	}
	
	private String pMD5(String k1) {
		return MD5.getMD5ofStr(k1);
	}
	
	private boolean has(String key) {
		return sp.contains(pMD5(key));
	}
	
	private String get(String key) {
		return sp.getString(pMD5(key), null);
	}
	
	private String get(String key, String def) {
		return sp.getString(pMD5(key), def);
	}
	
	class ready {
		private Editor edit;
		public ready(Editor ed) {
			edit = ed;
		}
		public ready put(String key, String val) {
			edit.putString(pMD5(key), val);
			return this;
		}
		public void commit() {
			edit.commit();
			edit = null;
		}
	}
}
