package com.tangye.android.iso8583;

import java.io.UnsupportedEncodingException;

import com.tangye.android.utils.DES;
import com.tangye.android.utils.PublicHelper;

import android.content.Context;

public class POSSession extends POSNative {
	
	private String pik;
	private String mak;
	private String account;
	private String card;
	private String ksn;
	private boolean ready;
    private Context ctx;
		
	public POSSession(Context context) {
		super(context);
		ctx = context.getApplicationContext();
	}
	
    public String getSessionAccount() {
        return account;  // phone number
    }
    
    public String getCardNumber() {
    	// FIXME to use privately
    	return card;  // card number
    }
	
	public POSSession initWK(String wkey, 
			String mAccount, String mPasswd, 
			String mCard, String mKsn, boolean mReady) throws UnsupportedEncodingException {
		if(wkey == null || wkey.length() != 24) throw new IllegalArgumentException("WK should be 24 bytes long");
		// android.util.Log.e("WK", IsoUtil.byte2hex(wkey.getBytes("ISO-8859-1")));
		pik = wkey.substring(0, 8);
		mak = wkey.substring(12, 20);
		byte[] bpik = pik.getBytes("ISO-8859-1");
		byte[] bmak = mak.getBytes("ISO-8859-1");
		pik = IsoUtil.byte2hex(bpik);
		mak = IsoUtil.byte2hex(bmak);
		if(mAccount != null && mPasswd != null) {
			POSEncrypt pos = POSHelper.getPOSEncrypt(ctx, mAccount);
			//android.util.Log.v("POS", IsoUtil.byte2hex(s.getBytes("ISO-8859-1")));			
			String mi = pos.getPOSDecrypt(pos.KEKENCODED);
			String s = "";
			if (mi.length() == 0) {
				mi = pos.getPOSDecrypt(pos.RESETPWD);
				if(mi.length() == 0) throw new IllegalStateException("no kek found!!!");
				s = getSimpleK(mCard);
			} else {
				s = getNativeK(mPasswd, mCard);
			}
			String kek = pos.dAES(mi, s);
			releaseNative();
			byte[] bkek = IsoUtil.hex2byte(kek);
			pos.close();
			// android.util.Log.e("PIKEncoded", pik);
			// android.util.Log.e("MAKEncoded", mak);
			try {
                bpik = DES.decrypt(bpik, bkek);
                bmak = DES.decrypt(bmak, bkek);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
			pik = IsoUtil.byte2hex(bpik);
			mak = IsoUtil.byte2hex(bmak);
			// android.util.Log.e("PIKDecoded", pik);
			// android.util.Log.e("MAKDecoded", mak);
			// android.util.Log.e("KEKDecoded", kek);
	        account = mAccount;
	        card = mCard;
	        setKSN(mKsn); // TODO need to test boolean return?
	        ready = mReady;
		} else {
		    throw new IllegalArgumentException("Fatal error when get WK");
		}
		return this;
	}
	
	/**
     * Get MAC bytes before 64 field is set
     * @param key must be 8 bytes
     * @return the 8 bytes MAC
     * @throws Exception when DES encrypt fails
     */
    public byte[] getMac(byte[] data) throws Exception {
        int len = data.length - data.length % 8;
        if (data.length != len) data = IsoUtil.trim(data, len + 8);
        byte[] mac = new byte[8];
        byte[] tmp = new byte[8];
        byte[] block = new byte[8];
        for (int i = 0; i < data.length / 8; i++) {
            System.arraycopy(data, i * 8, tmp, 0, 8);
            block = IsoUtil.xor(block, tmp);
        }
        block = IsoUtil.hexString(block).getBytes();
        System.arraycopy(block, 0, tmp, 0, 8);
        mac = DES.encrypt(tmp, IsoUtil.hex2byte(mak));
        System.arraycopy(block, 8, tmp, 0, 8);
        tmp = IsoUtil.xor(mac, tmp);
        mac = DES.encrypt(tmp, IsoUtil.hex2byte(mak));
        block = IsoUtil.hexString(mac).getBytes();
        System.arraycopy(block, 0, mac, 0, 8);
        return mac;
    }
    
    public String getPIN(String pin, String cardNumber) {        
        byte[] pinBlock = (byte[]) null;
        if (pin.length() > 10)
            throw new IllegalArgumentException("Invalid PIN length: " + pin.length());
        if (cardNumber.length() < 13)
            throw new IllegalArgumentException("Invalid Account Number");
        int len = cardNumber.length();
        String accountNumber = cardNumber.substring(len - 13, len - 1);
        String block1 = null;
        switch (pin.length()) {
        case 4:
            block1 = "04" + pin + "FFFFFFFFFF";
            break;
        case 5:
            block1 = "05" + pin + "FFFFFFFFF";
            break;
        case 6:
            block1 = "06" + pin + "FFFFFFFF";
            break;
        case 7:
            block1 = "07" + pin + "FFFFFFF";
            break;
        case 8:
            block1 = "08" + pin + "FFFFFF";
            break;
        default:
            throw new IllegalArgumentException("Unsupported PIN Length: " + pin.length());
        }
        byte[] block1ByteArray = IsoUtil.hex2byte(block1);
        byte[] block2ByteArray = (byte[]) null;
        String block2 = "0000" + accountNumber;
        block2ByteArray = IsoUtil.hex2byte(block2);
        pinBlock = IsoUtil.xor(block1ByteArray, block2ByteArray);
        try {
            // pik is ming wen 8 bytes here, generate 64 bytes binary mode
            return new String(DES.encrypt(pinBlock, IsoUtil.hex2byte(pik)), "ISO-8859-1");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new String(pinBlock);
    }
    
    public boolean testKsn(String KSN) {
		return KSN.substring(0, 14).equalsIgnoreCase(ksn);
	}
    
    public String getKsn(){
    	return PublicHelper.getMaskedString(ksn, 3, 4, 'X');
    }
    
    public boolean isAuthenticated() {
    	return ready;
    }
	
	public void close() {
		ctx = null; // 防止造成内存泄漏
		releaseNative();
	}
	
	/**
	 * should be release of content of it, and should set to null
	 */
	public void release() {
	    close();
	    pik = null;
	    mak = null;
	    account = null;
	    // TODO stop expired the session
	}
	
	public boolean setKSN(final String newKSN){
		if(newKSN == null || newKSN.length() < 14){
			return false;
		}
		ksn = newKSN.substring(0, 14);
		return true;
	}
}
