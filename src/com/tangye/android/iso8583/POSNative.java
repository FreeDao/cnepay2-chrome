
package com.tangye.android.iso8583;

import android.content.Context;

public class POSNative {
	private Context ct;
	static {
		System.loadLibrary("POSNative");
	}

	public POSNative(Context context) {
		ct = context.getApplicationContext();
	}

	public Context getContext() {
		return ct;
	}
	
	protected void releaseNative() {
		ct = null;
	}
	
	protected native String getNativeK(String pin, String card);

	protected String getSimpleK(String card) {
		String pin = "32904c";
		if(card.length() > 7) {
			pin = card.substring(1, 7);
		}
		return getNativeK(pin, card);
	}
}
