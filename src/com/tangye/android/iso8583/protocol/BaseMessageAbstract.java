package com.tangye.android.iso8583.protocol;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import android.util.Log;
import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.IsoTemplate;
import com.tangye.android.iso8583.IsoUtil;

public abstract class BaseMessageAbstract {

	private static final String TAG = "BaseMessageAbstract";
	private static final String ISOHEADER = "6000060000603000000000";
	public static final String TPDU_BCD = new String(IsoUtil.hex2byte(ISOHEADER));
	private static final String DEFAULT_ADDR = "203.81.23.13";//"119.97.180.133";
	private static final int DEFAULT_PORT = 59002;
	
	//203.81.23.23：29002

	protected IsoMessage req;
    protected IsoTemplate res;
    protected int SOCKET_TIMEOUT = 1000 * 30;
    protected BaseClient client;
    protected boolean ignoreAnyError = false;
    
    public BaseMessageAbstract() {
        req = new IsoMessage();
        onCreateRequestIsoMessage(req);
        res = new IsoTemplate();
        onCreateResponseIsoTemplate(res);
    }
    
    public void setSoTimeout(int timeout) {
    	SOCKET_TIMEOUT = timeout;
    }
    
    protected abstract void onCreateRequestIsoMessage(IsoMessage requestMsg);
    protected abstract void onCreateResponseIsoTemplate(IsoTemplate respTemp);
    public abstract boolean isBitmapValid();
    
    protected boolean isBitmapValid(int[] inbitmap, int[] outbitmap) {
    	int[] b1 = res.getBitmapArray();
    	int[] b2 = req.getBitmapArray();
    	if(inbitmap.length != b1.length || outbitmap.length != b2.length) return false;
    	for(int i = 0; i < b1.length; i++) {
    		if(b1[i] != inbitmap[i]) return false;
    	}
    	for(int i = 0; i < b2.length; i++) {
    		if(b2[i] != outbitmap[i]) return false;
    	}
        return true;
    }
    
    public IsoMessage request() throws UnknownHostException, IOException, Exception {
    	return request(DEFAULT_ADDR, DEFAULT_PORT);
    }
    
    public IsoMessage request(String addr, int port, int ... headerlen) throws UnknownHostException, IOException, Exception {
        Socket socket = new Socket(addr, port);
        socket.setSoTimeout(SOCKET_TIMEOUT);
        client = new BaseClient(socket, res);
        Log.i(TAG, String.format("Sending Request to %s:%d", addr, port));
        req.setIsoHeader(TPDU_BCD); // 设置HEADER
        res.setIsoHeader(TPDU_BCD);
        if(headerlen.length > 0)
        	req.write(socket.getOutputStream(), headerlen[0]);
        else {
        	req.write(socket.getOutputStream(), 2); // 默认两个字节长度
            // Log.i(TAG, String.format("Sending data: %s", IsoUtil.byte2hex(req.writeData())));
        }
        if(client.doRequest()) {
            return client.getResponseMessage();
        } else {
            if(ignoreAnyError) {
                return null;
            }
            if(client.getException() != null) {
                throw client.getException();
            }
            throw new Exception("Fatal Error When Requesting");
        }
    }
    
    public void stop() {
    	if(client != null) {
    		try {
    			client.close();
    		} catch(IOException e) {}
    		client = null;
    	}
    	ignoreAnyError = true;
    }

    protected static String extAlpha(String value, int length) {
    	if (value == null) {
            value = "";
        }
        if (value.length() > length) {
            return value.substring(0, length);
        }
        char[] c = new char[length];
        System.arraycopy(value.toCharArray(), 0, c, length - value.length(), value.length());
        for (int i = 0; i < length - value.length(); i++) {
            c[i] = ' ';
        }
        return new String(c);
    }
    
    protected static String toGBK(String s) {
    	if (s == null) {
    		return "";
    	}
    	String bankStr = "";
    	byte[] bankByte;
        try {
            bankByte = s.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return s;
        }
    	for(int i =0 ;i < bankByte.length;i++){
    	    bankStr += String.valueOf(bankByte[i])+" ";
    	}
    	return bankStr.trim();
    }
}
