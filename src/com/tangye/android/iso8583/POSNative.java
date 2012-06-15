
package com.tangye.android.iso8583;

import android.content.Context;

public class POSNative {
	private Context ct;

	public POSNative(Context context) {
		ct = context.getApplicationContext();
	}

	public Context getContext() {
		return ct;
	}
	
	public void releaseNative() {
		ct = null;
	}

	static {
		System.loadLibrary("POSNative");
	}

	public native String getNativeK(String pin, String card);
}
