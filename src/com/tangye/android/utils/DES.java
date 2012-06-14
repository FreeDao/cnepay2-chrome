package com.tangye.android.utils;

import javax.crypto.*;
import javax.crypto.spec.*;

public class DES {
	
	private final static String CRYPT_KEY = "空中商城";
	
    public static byte[] encrypt(byte[] data, byte[] key)
    {
    	SecretKey sk = new SecretKeySpec(key, "DES");
    	try {
    		Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
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
    	SecretKey sk = new SecretKeySpec(key, "DES");
    	try {
    		Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
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
	 */
	public final static byte[] decrypt(byte[] data) {
        try {
            return decrypt(data, CRYPT_KEY.getBytes("GBK"));
        } catch (Exception e) {
            return null;
        }
    }
    
    public final static byte[] encrypt(byte[] data) {
        try {
            return encrypt(data, CRYPT_KEY.getBytes("GBK"));
        } catch (Exception e) {
            return null;
        }
    }
    // end private
}
