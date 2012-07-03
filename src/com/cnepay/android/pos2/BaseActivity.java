package com.cnepay.android.pos2;

import com.bbpos.cswiper.CSwiperController;
import com.bbpos.cswiper.CSwiperController.CSwiperControllerState;
import com.bbpos.cswiper.CSwiperController.CSwiperStateChangedListener;
import com.bbpos.cswiper.CSwiperController.DecodeResult;
import com.tangye.android.utils.CardInfo;
//import com.bbpos.cswiper.encrypt.Rambler;

import android.app.Activity;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
import android.os.Vibrator;
import android.util.Log;

public abstract class BaseActivity extends Activity implements CardReaderListener {

	private final static String TAG = "BaseActivity";
	private final static int T_SUCCESS = 150;
	private final static int MAX_DETECT_TIME = 3;
	//private final static int T_FAILURE = 50;

	// BEGIN API2
	private boolean testAPI = false;
	private int testTimes = 0;
	private CSwiperController cswiperController = null;
	private CSwiperStateChangedListener stateChangedListener2 = null;
	private KsnTestListener ksnTestListener = null;
	
	protected CardInfo ci;

	//private static final String BDK = "0123456789ABCDEFFEDCBA9876543210";

	// END API2

	// BEGIN ERROR INFO

	public final static int E_API2_BASE 			= 1000;
	public final static int E_API2_UNRESOLVED	 	= E_API2_BASE + 1;
	public final static int E_API2_FASTORSLOW 		= E_API2_BASE + 2;
	public final static int E_API2_UNSTABLE 		= E_API2_BASE + 3;
	public final static int E_API2_FATAL			= E_API2_BASE + 4;
	
	public final static int E_API2_NO_DEVICE_ERROR 	= E_API2_BASE + 5;
	public final static int E_API2_INTERRUPT 		= E_API2_BASE + 6;
	public final static int E_API2_INIT 			= E_API2_BASE + 7;
	public final static int E_API2_KSN 				= E_API2_BASE + 8;
	public final static int E_API2_INVALID_DEVICE	= E_API2_BASE + 9;

	// END ERROR INFO
	/**************************************************/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		stateChangedListener2 = new API2_CallbackListener();
	}
	
	private void vibrate(int time) {
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    	vibrator.vibrate(time);
	}
	
	/*************************************************
	 *   public function exposed to child activity   *
	 *************************************************/

	/**
	 * startSwipe the card
	 * startSwipe保证当调用该api时，允许刷卡
	 */
	public void startSwipe() {
		if (cswiperController != null && cswiperController.getCSwiperState()
				== CSwiperControllerState.STATE_IDLE) {
			Log.v(TAG, "startCSwiper API2");
			try {
				cswiperController.startCSwiper();
			} catch (IllegalStateException e) {}
		}
	}
	
	/**
	 * 关闭刷卡接口，必要时提前结束刷卡
	 */
	public void stopSwipe() {
		if (cswiperController != null) {
			try {
				cswiperController.stopCSwiper();
			} catch (IllegalStateException e) {}
		}
	}
	
	/**
	 * only call this function in onCreate(), if you want to test ksn
	 * @param l the listener to set for testing ksn, null for not test
	 */
	public void setKsnTestListener(KsnTestListener l) {
		ksnTestListener = l;
	}
	
	/************** end public function **************/
		

	@Override
	protected void onResume() {
		super.onResume();
		Log.v(TAG, "onResume");
		// init CardSipeAPI2
		initAPI2();
		
		if (cswiperController.isDevicePresent()) {
			testAPI = true;
			testTimes = 0;
			deviceDetecting(testAPI);
			cswiperController.getCSwiperKsn();
		} else {
			onPlugout();
		}
	}
	
	protected abstract void deviceDetecting(boolean detect);

	@Override
	protected void onPause() {
		super.onPause();
		deleteAPI2();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// FIXME is there anything to destroy
		// TODO somthing
		ci = null;
	}

	@Override
	public void onPlugin() {
		Log.v(TAG, "plugin");
	}

	@Override
	public void onPlugout() {
		Log.v(TAG, "plugout");
	}

	@Override
	public void onWaitForSwipe() {
		Log.v(TAG, "wait for swipe");
	}

	@Override
	public void onTimeout() {
		Log.v(TAG, "swipe timeout");
	}
	
	@Override
	public void onSwipe() {
		Log.v(TAG, "swiping...");
	}

	@Override
	public void onDecoding() {
		Log.v(TAG, "decoding...");
	}

	@Override
	public void onComplete(String maskedCardNumber) {
		Log.v(TAG, "card = " + maskedCardNumber);
	}

	@Override
	public void onError(int error) {
		Log.i(TAG, "error = " + error);
	}

	class API2_CallbackListener implements CSwiperStateChangedListener {

		public void onCardSwipeDetected() {
			BaseActivity.this.onSwipe();
			Log.v(TAG, "onCardSwipeDetected");
		}

		public void onDecodeCompleted(String formatID, String ksn,
				String encTracks, int track1Length, int track2Length,
				int track3Length, String randomNumber, String maskedPAN,
				String expiryDate, String cardHolderName) {
			Log.v(TAG, "onDecodeCompleted");
			try {
				ci = new CardInfo(formatID, ksn, encTracks, maskedPAN);
				vibrate(T_SUCCESS);
				BaseActivity.this.onComplete(ci.getCard(true));
			} catch (IllegalStateException e) {
				e.printStackTrace();
				ci = null;
				BaseActivity.this.onError(E_API2_INVALID_DEVICE);
			}
		}

		public void onDecodeError(DecodeResult err) {
			Log.v(TAG, "onDecodeError");
			int errout;
			switch(err) {
			case DECODE_SWIPE_FAIL:
				errout = E_API2_FASTORSLOW;
				break;
			case DECODE_CRC_ERROR:
				errout = E_API2_UNSTABLE;
				break;
			default:
				errout = E_API2_UNRESOLVED;
				break;
			}			
			ci = null;
			BaseActivity.this.onError(errout);
		}

		public void onDecodingStart() {
			Log.v(TAG, "onDecodingStart");
			BaseActivity.this.onDecoding();
		}

		public void onDevicePlugged() {
			Log.v(TAG, "onDevicePlugged");
			if (cswiperController.isDevicePresent()) {
				testAPI = true;
				testTimes = 0;
				deviceDetecting(testAPI);
				cswiperController.getCSwiperKsn();
			} else {
				// FIXME need ?
				onPlugout();
			}
		}

		public void onDeviceUnplugged() {
			Log.v(TAG, "onDeviceUnplugged");
			// FIXME
			// IF ksn failure and plugged, we should not call onPlugout
			testAPI = false;
			deviceDetecting(testAPI);
			BaseActivity.this.onPlugout();
		}

		public void onError(int error, String message) {
			Log.v(TAG, "API2 onError:" + message);
			if (testAPI && cswiperController != null
					&& error == CSwiperController.ERROR_FAIL_TO_GET_KSN) {
				// TODO FIXME， to enable 3 times verification, error should be non-device
				if (testTimes++ < MAX_DETECT_TIME) {
					cswiperController.getCSwiperKsn();
					return;
				}
				onPlugout();
				BaseActivity.this.onError(E_API2_INVALID_DEVICE);
			} else {
				// TODO translate inner API error code to our own API error code
				int errout;
				switch(error) {
				case CSwiperController.ERROR_FAIL_TO_START:
					errout = E_API2_INIT;
					break;
				case CSwiperController.ERROR_FAIL_TO_GET_KSN:
					errout = E_API2_KSN;
					break;
				default:
					errout = E_API2_FATAL;
				}
				
				BaseActivity.this.onError(errout);
			}
			testAPI = false;
			deviceDetecting(testAPI);
		}

		public void onGetKsnCompleted(String ksn) {
			Log.v(TAG, "onGetKsnCompleted");
			if (testAPI) {
				if (ksnTestListener == null || ksnTestListener.test(ksn)) {
					BaseActivity.this.onPlugin();
				} else {
					onPlugout();
					BaseActivity.this.onError(E_API2_INVALID_DEVICE);
				}
				testAPI = false;
				deviceDetecting(testAPI);
			}
			// TODO
			// if we need ksn recorded here
		}

		public void onInterrupted() {
			Log.v(TAG, "onInterrupted");
			BaseActivity.this.onError(E_API2_INTERRUPT);
		}

		public void onNoDeviceDetected() {
			Log.v(TAG, "onNoDeviceDetected");
			BaseActivity.this.onError(E_API2_NO_DEVICE_ERROR);
		}

		public void onTimeout() {
			Log.v(TAG, "onTimeout");
			BaseActivity.this.onTimeout();
		}

		public void onWaitingForCardSwipe() {
			Log.v(TAG, "onWaitingForCardSwipe");
			BaseActivity.this.onWaitForSwipe();
		}

		public void onWaitingForDevice() {
			Log.v(TAG, "onWaitingForDevice");
			// FIXME Nothing to do??
		}
	}

	/********************************************
	 *      API2 private function        *
	 ********************************************/
	// BEGIN API2 Private Function
	private void initAPI2() {
		if (cswiperController == null) {
			Log.v(TAG, "initAPI2");
			cswiperController = new CSwiperController(getApplicationContext(),
					stateChangedListener2);
			cswiperController.setDetectDeviceChange(true);
		}
	}

	private void deleteAPI2() {
		if (cswiperController != null) {
			Log.v(TAG, "deleteAPI2");
			if (cswiperController.getCSwiperState() != CSwiperControllerState.STATE_IDLE) {
				stopSwipe();
				onError(E_API2_INTERRUPT);
			}
			cswiperController.deleteCSwiper();
			cswiperController = null;
		}
	}

/* DO NOT decode track info on API2
	private String decodeTrack2API2(String formatID, String ksn,
			String encTracks, int track1Length, int track2Length,
			int track3Length, String randomNumber, String maskedPAN,
			String expiryDate, String cardHolderName) {
		// int id = IsoUtil.hex2byte(formatID)[0] & 0xff;
		int id = Integer.parseInt(formatID, 16);
		String[] Track = Rambler.decryptTrack(id, encTracks, track1Length,
				track2Length, track3Length, ksn, BDK);
		// Log.v(TAG, "track2=" + Track[1]);
		return Track[1];
	}
*/
	// END API2 Private Function
	/********************************************/
	
}
