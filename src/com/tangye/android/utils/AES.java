package com.tangye.android.utils;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.tangye.android.iso8583.IsoUtil;


public class AES {
	
	// private static final String CRYPT_KEY = "ECRTIFJKDddfafad";
	
    public static byte[] encrypt(byte[] data, byte[] key)
    {
    	SecretKey sk = new SecretKeySpec(key, "AES");
    	try {
    		Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
    		cipher.init(Cipher.ENCRYPT_MODE, sk);
			byte[] enc = cipher.doFinal(data);
			return enc;
        } catch (javax.crypto.NoSuchPaddingException e) {
        } catch (java.security.NoSuchAlgorithmException e) {
        } catch (java.security.InvalidKeyException e) {
        } catch (javax.crypto.BadPaddingException e) {
		} catch (IllegalBlockSizeException e) {
		} 
    	
    	return null;
    }
    
    public static byte[] decrypt(byte[] data, byte[] key)
    {
    	SecretKey sk = new SecretKeySpec(key, "AES");
    	try {
    		Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
    		cipher.init(Cipher.DECRYPT_MODE, sk);
			byte[] enc = cipher.doFinal(data);
			return enc;
        } catch (javax.crypto.NoSuchPaddingException e) {
        } catch (java.security.NoSuchAlgorithmException e) {
        } catch (java.security.InvalidKeyException e) {
        } catch (javax.crypto.BadPaddingException e) {
		} catch (IllegalBlockSizeException e) {
		} 
    	
    	return null;
    }
    
    public static String encrypt(String data, String key)
    {
    	byte[] bData, bKey, bOutput;
    	String result;
    	
    	bData = String2Hex(data);
    	bKey = String2Hex(key);
    	bOutput = encrypt(bData, bKey);
    	result = Hex2String(bOutput);
    	
    	return result;
    }

    public static String decrypt(String data, String key)
    {
    	byte[] bData, bKey, bOutput;
    	String result;
    	
    	bData = String2Hex(data);
    	bKey = String2Hex(key);
    	bOutput = decrypt(bData, bKey);
    	result = Hex2String(bOutput);
    	
    	return result;
    }

    public static String Hex2String(byte[] data)
    {
		String result = "";
		for (int i=0; i<data.length; i++)
		{
			int tmp = (data[i] >> 4);
			result += Integer.toString((tmp & 0x0F), 16);
			tmp = (data[i] & 0x0F);
			result += Integer.toString((tmp & 0x0F), 16);
		}
	
		return result;
    }
    
	public static byte[] String2Hex(String data)
	{
		byte[] result;
		
		result = new byte[data.length()/2];
		for (int i=0; i<data.length(); i+=2)
			result[i/2] = (byte)(Integer.parseInt(data.substring(i, i+2), 16));
		
		return result;
	}
	
	
	/**
	 * begin private
	 * @author tangye
	 *
     * 加密
     * @param data
     * @param key only ascii 16 Byte
     * @return
     * @throws Exception
     */
    public final static String encryptTrack(String data, String key) {
        try {
            return IsoUtil.byte2hex(encrypt(data.getBytes(), generateKey(key)));
        } catch (Exception e) {
        }
        return null;
    }
    
    /**
     * @author tangye
     * 
     * 解密
     * @param data
     * @param key only ascii 16 Byte
     * @return
     * @throws Exception
     */
    public final static String decryptTrack(String data, String key) {
        try {
            return new String(decrypt(IsoUtil.hex2byte(data), generateKey(key)));
        } catch (Exception e) {
        }
        return null;
    }
    /*
    public final static String decrypt(String data) {
        return decrypt(data, CRYPT_KEY);
    }
    
    public final static String encrypt(String data) {
        return encrypt(data, CRYPT_KEY);
    }*/

    private final static byte[] generateKey(String key) {
        StringBuilder xb = new StringBuilder(key);
        xb.reverse();
        try {
			return xb.toString().substring(0, 16).getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			return xb.toString().substring(0, 16).getBytes();
		}
    }
}
