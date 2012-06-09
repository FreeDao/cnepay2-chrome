package com.tangye.android.iso8583;

public class POSNative {
	static {
		System.loadLibrary("POSNative");
	}
	
	public static native String getNativeK(String pin, String card);
}
