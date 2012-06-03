package com.cnepay.android.pos2;

import com.bbpos.cswiper.CSwiperController;
import com.bbpos.cswiper.CSwiperController.CSwiperControllerState;
import com.bbpos.cswiper.CSwiperController.CSwiperStateChangedListener;
import com.bbpos.cswiper.CSwiperController.DecodeResult;
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

public class BaseActivity extends Activity implements CardReaderListener {

	private final static String TAG = "BaseActivity";
	private final static int T_SUCCESS = 150;
	//private final static int T_FAILURE = 50;

	// BEGIN API2
	private boolean testAPI = false;
	private CSwiperController cswiperController = null;
	private CSwiperStateChangedListener stateChangedListener2;

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
		if (cswiperController.getCSwiperState() == CSwiperControllerState.STATE_IDLE) {
			Log.v(TAG, "startCSwiper API2");
			try {
				cswiperController.startCSwiper();
			} catch (IllegalStateException e) {}
		}
	}
	
	public void stopSwipe() {
		if (cswiperController != null) {
			try {
				cswiperController.stopCSwiper();
			} catch (IllegalStateException e) {}
		}
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
			cswiperController.getCSwiperKsn();
		} else {
			onPlugout();
		}
	}

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
	}

	@Override
	public void onPlugin() {
		// TODO Auto-generated method stub
		Log.v(TAG, "plugin");
	}

	@Override
	public void onPlugout() {
		// TODO Auto-generated method stub
		Log.v(TAG, "plugout");
	}

	@Override
	public void onWaitForSwipe() {
		// TODO Auto-generated method stub
		Log.v(TAG, "wait for swipe");
	}

	@Override
	public void onTimeout() {
		// TODO Auto-generated method stub
		Log.v(TAG, "swipe timeout");
	}

	@Override
	public void onDecoding() {
		// TODO Auto-generated method stub
		Log.v(TAG, "decoding...");
	}

	@Override
	public void onComplete(String maskedCardNumber) {
		// TODO Auto-generated method stub
		Log.v(TAG, "card = " + maskedCardNumber);
	}

	@Override
	public void onError(int error) {
		// TODO Auto-generated method stub
		Log.i(TAG, "error = " + error);
	}

	class API2_CallbackListener implements CSwiperStateChangedListener {

		public void onCardSwipeDetected() {
			// TODO Auto-generated method stub
			Log.v(TAG, "onCardSwipeDetected");
		}

		public void onDecodeCompleted(String formatID, String ksn,
				String encTracks, int track1Length, int track2Length,
				int track3Length, String randomNumber, String maskedPAN,
				String expiryDate, String cardHolderName) {
			// TODO Auto-generated method stub
			Log.v(TAG, "onDecodeCompleted");
			/*String track2 = decodeTrack2API2(formatID, ksn,
					encTracks, track1Length, track2Length, track3Length, randomNumber,
					maskedPAN, expiryDate, cardHolderName);
			ci = new CardInfo(track2);
			*/
			vibrate(T_SUCCESS);
			BaseActivity.this.onComplete(maskedPAN);
		}

		public void onDecodeError(DecodeResult err) {
			// TODO Auto-generated method stub
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
			// TODO track2 clear
			BaseActivity.this.onError(errout);
		}

		public void onDecodingStart() {
			// TODO Auto-generated method stub
			Log.v(TAG, "onDecodingStart");
			BaseActivity.this.onDecoding();
		}

		public void onDevicePlugged() {
			// TODO Auto-generated method stub
			Log.v(TAG, "onDevicePlugged");
			if (cswiperController.isDevicePresent()) {
				testAPI = true;
				cswiperController.getCSwiperKsn();
			} else {
				// FIXME need ?
				onPlugout();
			}
		}

		public void onDeviceUnplugged() {
			// TODO Auto-generated method stub
			Log.v(TAG, "onDeviceUnplugged");
			// FIXME
			// IF ksn failure and plugged, we should not call onPlugout
			BaseActivity.this.onPlugout();
		}

		public void onError(int error, String message) {
			// TODO Auto-generated method stub
			Log.v(TAG, "API2 onError:" + message);
			if (testAPI && cswiperController != null
					&& error == CSwiperController.ERROR_FAIL_TO_GET_KSN) {
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
		}

		public void onGetKsnCompleted(String arg0) {
			Log.v(TAG, "onGetKsnCompleted");
			if (testAPI) {
				BaseActivity.this.onPlugin();
				testAPI = false;
			}
			// TODO
			// if we need ksn recorded here
		}

		public void onInterrupted() {
			// TODO Auto-generated method stub
			Log.v(TAG, "onInterrupted");
			BaseActivity.this.onError(E_API2_INTERRUPT);
		}

		public void onNoDeviceDetected() {
			// TODO Auto-generated method stub
			Log.v(TAG, "onNoDeviceDetected");
			BaseActivity.this.onError(E_API2_NO_DEVICE_ERROR);
		}

		public void onTimeout() {
			// TODO Auto-generated method stub
			Log.v(TAG, "onTimeout");
			BaseActivity.this.onTimeout();
		}

		public void onWaitingForCardSwipe() {
			// TODO Auto-generated method stub
			Log.v(TAG, "onWaitingForCardSwipe");
			BaseActivity.this.onWaitForSwipe();
		}

		public void onWaitingForDevice() {
			Log.v(TAG, "onWaitingForDevice");
			// TODO Auto-generated method stub

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
