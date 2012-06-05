package com.tangye.android.iso8583.protocol;

import java.io.*;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;

import android.util.Log;

import com.tangye.android.iso8583.*;

public class BaseClient {

    private static final String TAG = "BaseClient";
	protected Socket socket;
    protected BufferedInputStream in;
    protected IsoTemplate temp;
    protected ArrayList<Integer> bitmapArr;
    protected IsoMessage msg;
    
    private Exception exception;
    
    public BaseClient(Socket sock, IsoTemplate template) {
        socket = sock;
        temp = template;
        msg = new IsoMessage();
    }
    
    public boolean doRequest() {
        boolean result = false;
        try {
            in = new BufferedInputStream(socket.getInputStream());
            if(socket != null && socket.isConnected()) {
                result = IsoMessage.parseMessageFrom(socket, msg, temp, in);
            }
            in.close();
            in = null;
            return result;
        } catch(IOException e) {
        	e.printStackTrace();
        	exception = e;
            return result;
        } catch (ParseException e) {
			e.printStackTrace();
			exception = e;
			return result;
		} finally {
            if(socket != null) {
                try {
                    socket.close();
                } catch(IOException e) {}
                socket = null;
            }
        }
    }
    
    public Exception getException() {
        return exception;
    }
    
    public IsoMessage getResponseMessage() {
        return msg;
    }
    
    public void close() throws IOException {
    	// Log.i(TAG, "Try to close the socket");
    	if(socket != null) {
    		try {
    			if(in != null) {
    				in.close();
    			}
    		} catch(IOException e) {}
    		in = null;
    		Log.i(TAG, "Socket is closing");
    		socket.close();
    		socket = null;
    	}
    	// Log.i(TAG, "Socket is closed");
    }
}
