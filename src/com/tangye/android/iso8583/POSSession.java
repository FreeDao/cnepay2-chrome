package com.tangye.android.iso8583;

import java.io.UnsupportedEncodingException;

import com.bbpos.cswiper.encrypt.DES;
import android.content.Context;

public class POSSession {
	
	private String pik;
	private String mak;
	private String account;
    private Context ctx;
		
	public POSSession(Context context) {
		ctx = context.getApplicationContext();
	}
	
    public String getSessionAccount() {
        return account;
    }	
	
	public POSSession initWK(String wkey, String track2) throws UnsupportedEncodingException {
		if(wkey == null || wkey.length() != 24) throw new IllegalArgumentException("WK should be 24 bytes long");
		// android.util.Log.e("WK", IsoUtil.byte2hex(wkey.getBytes("ISO-8859-1")));
		pik = wkey.substring(0, 8);
		mak = wkey.substring(12, 20);
		byte[] bpik = pik.getBytes("ISO-8859-1");
		byte[] bmak = mak.getBytes("ISO-8859-1");
		pik = IsoUtil.byte2hex(bpik);
		mak = IsoUtil.byte2hex(bmak);
		if(track2 != null) {
			String cn = track2.substring(0, track2.indexOf("="));
			POSEncrypt pos = POSHelper.getPOSEncrypt(ctx, cn);
			String kek = pos.dAES(pos.getPOSDecrypt(pos.KEKENCODED), track2);
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
	        account = cn;
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
    
    public String getPIN(String pin, String track2) {        
        byte[] pinBlock = (byte[]) null;
        if (pin.length() > 10)
            throw new IllegalArgumentException("Invalid PIN length: " + pin.length());
        if (track2.length() < 13)
            throw new IllegalArgumentException("Invalid Account Number");
        int len = track2.length();
        String accountNumber = track2.substring(len - 13, len - 1);
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
	
	public void close() {
		ctx = null; // 防止造成内存泄漏
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
}
