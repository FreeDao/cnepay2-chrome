package com.cnepay.android.pos2;

public interface CardReaderListener {

	public void onPlugin();
	public void onPlugout();
	public void onWaitForSwipe();
	public void onTimeout();
	public void onDecoding();
	public void onComplete(String maskedCardNumber);
	public void onError(int error);

}
