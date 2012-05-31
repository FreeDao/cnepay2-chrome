package com.cnepay.android.pos2;

import com.bbpos.cswiper.CSwiperController;
import com.bbpos.cswiper.CSwiperController.CSwiperControllerState;
import com.bbpos.cswiper.CSwiperController.CSwiperStateChangedListener;
import com.bbpos.cswiper.CSwiperController.DecodeResult;
import com.bbpos.cswiper.encrypt.Rambler;
import com.tangye.cardreader.CardInfo;
import com.tangye.cardreader.CardReader;
import com.tangye.cardreader.MathHelper;
import com.tangye.cardreader.CardReader.CardReaderObserver;
import com.tangye.cardreader.CardReader.ERROR;
import com.tangye.cardreader.CardReader.STATE;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;

public class BaseActivity extends Activity implements CardReaderListener {

	private final static String TAG = "BaseActivity";
	private final static int T_SUCCESS = 150;
	//private final static int T_FAILURE = 50;

	protected int version = 2; // 2 means API2, 1 means API1
	private int choseVersion = 2; //0 means do not force API,
	                                           //1 means only API1,2 means only API2
	protected CardInfo ci = null;

	private static int headsetState = -1; // 0 unplug, 1 plug, for API1
	private boolean testAPI = false;
	private volatile boolean allowSwipe = false;

	/**************************************************/
	// BEGIN API1

	private CardReader rt = null;
	private CardReaderObserver stateChangedListener1;
	private Handler mAPI1Handler;

	private static final int CARDREADER_CARD_NUMBER = 0;
	private static final int CARDREADER_SWITCH_INDICATOR = 1;
	private final BroadcastReceiver brc = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			int state = intent.getIntExtra("state", 0); // 0 unplug, 1 plug
			int hasMic = intent.getIntExtra("microphone", 0); // 0 none, 1 has
																// microphone
			if (state == 0) {
				headsetState = 0;
			} else if (hasMic == 1) {
				headsetState = 1;
			}
		}
	};
	
	//private Timer timer;
	//private TimerTask task;
	private Runnable timer = new Runnable() {
		
		public void run() {
			allowSwipe = false;
			onTimeout();
			Log.v(TAG, "API1 start timeout");
		}
		
	};

	// END API1

	/************** API1 & API2 Separate **************/

	// BEGIN API2

	private CSwiperController cswiperController = null;
	private CSwiperStateChangedListener stateChangedListener2;

	private static final String BDK = "0123456789ABCDEFFEDCBA9876543210";

	// END API2

	// BEGIN ERROR INFO
	
	public final static int E_API1_BASE 			= 100;
	
	public final static int E_API1_UNRESOLVED 		= E_API1_BASE + 1;
	public final static int E_API1_TOOSLOW 			= E_API1_BASE + 2;
	public final static int E_API1_TOOFAST 			= E_API1_BASE + 3;
	public final static int E_API1_UNSTABLE 		= E_API1_BASE + 4;
	public final static int E_API1_FATAL 			= E_API1_BASE + 5;
	
	public final static int E_API1_INTERRUPT 		= E_API1_BASE + 6;
	public final static int E_API1_INIT 			= E_API1_BASE + 7;

	public final static int E_API2_BASE 			= 1000;
	public final static int E_API2_UNRESOLVED	 	= E_API2_BASE + 1;
	public final static int E_API2_FASTORSLOW 		= E_API2_BASE + 2;
	public final static int E_API2_UNSTABLE 		= E_API2_BASE + 3;
	public final static int E_API2_FATAL			= E_API2_BASE + 4;
	
	public final static int E_API2_NO_DEVICE_ERROR 	= E_API2_BASE + 5;
	public final static int E_API2_INTERRUPT 		= E_API2_BASE + 6;
	public final static int E_API2_INIT 			= E_API2_BASE + 7;
	public final static int E_API2_KSN 				= E_API2_BASE + 8;

	// END ERROR INFO
	/**************************************************/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// API1 INITIALIZE PATIAL
		mAPI1Handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case CARDREADER_CARD_NUMBER:
					vibrate(T_SUCCESS);
					onComplete((String) msg.obj);
					break;
				case CARDREADER_SWITCH_INDICATOR:
					switch ((STATE) msg.obj) {
					case PREPARED:
						onPlugin();
						break;
					case UNPREPARED:
						onPlugout();
						break;
					case STOPPED:
						if(rt != null && version == 1) {
							deleteAPI1();
							initAPI2();
						}
						break;
					}
					break;
				}
			}
		};
		stateChangedListener1 = new API1_CallbackListener();
		stateChangedListener2 = new API2_CallbackListener();

		// END API1 INTIALIZE PARTIAL

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
		/*
		if(forceVersion != 0){
			version = forceVersion;
		}
		*/
		if (version == 1) {
			if(allowSwipe) {
				Log.v(TAG, "API1 start illegalstate");
				return;
			}
			allowSwipe = true;
			// TODO new timer 30s
			/*
			task = new TimerTask(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					allowSwipe = false;
					onTimeout();
				}};
			timer.schedule(task, 30000);
			*/
			Log.v(TAG, "start API1 timer");
			mAPI1Handler.postDelayed(timer, 30000);
		} else if (version == 2) {
			if (cswiperController.getCSwiperState() == CSwiperControllerState.STATE_IDLE) {
				Log.v(TAG, "startCSwiper API2");
				try {
					cswiperController.startCSwiper();
				} catch (IllegalStateException e) {}
			}
		}
	}
	
	public void stopSwipe() {
		// TODO stop
		/*
		if(forceVersion != 0){
			version = forceVersion;
		}
		*/
		if (version == 2) {
			if (cswiperController != null) {
				try {
					cswiperController.stopCSwiper();
				} catch (IllegalStateException e) {}
			}
		} else if (version == 1) {
			if(allowSwipe){
				onError(E_API1_INTERRUPT);
				allowSwipe = false;
			}
			stopTimer();
			
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
		if (version == 1) {
			deleteAPI1();
		} else if (version == 2) {
			deleteAPI2();
		}
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
		
		// test only
		startSwipe();
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
		Log.v(TAG, "track2 = " + ci.toString());
	}

	@Override
	public void onError(int error) {
		// TODO Auto-generated method stub
		Log.i(TAG, "error = " + error);
	}

	class API1_CallbackListener implements CardReaderObserver {
		public void onStatusChange(final STATE status) {
			Message msg = mAPI1Handler.obtainMessage(
					CARDREADER_SWITCH_INDICATOR, status);
			msg.sendToTarget();
		}

		public void onDataReturned(StringBuilder data) {
			try {
				stopTimer();
				if (!allowSwipe) return;
				allowSwipe = false;
				final String out = MathHelper.decodeCardInformation(data);
				ci = new CardInfo(out);
				String maskcn = MathHelper.StringMask(ci.getCardNumber(), 'X',
						6, 4);
				Message msg = mAPI1Handler.obtainMessage(
						CARDREADER_CARD_NUMBER, maskcn);
				msg.sendToTarget();
			} catch (final IllegalStateException e) {
				ci = null;
				/*
				 * makeToast("未识别，请重新刷卡", ERROR.UN_RESOLVED);
				 * appendLog(e.getMessage());
				 */
				onAPI1Error(E_API1_UNRESOLVED);
			}
		}

		public void onErrorFeedback(ERROR err) {
			// FIXME need to set ci = null？
			stopTimer();
			if (!allowSwipe) return;
			allowSwipe = false;
			ci = null;
			switch (err) {
			case TOO_SLOW:
				onAPI1Error(E_API1_TOOSLOW);
				break;
			case TOO_FAST:
				onAPI1Error(E_API1_TOOFAST);
				break;
			case UN_RESOLVED:
				onAPI1Error(E_API1_UNSTABLE);
				break;
			default:
				onAPI1Error(E_API1_FATAL);
				// FIXME sometimes we just finish the activity
				// when meeting such unexpected error
				// finish();
			}
		}

		public int getHeadsetState() {
			return headsetState;
		}
	}

	class API2_CallbackListener implements CSwiperStateChangedListener {

		public void onCardSwipeDetected() {
			// TODO Auto-generated method stub
			Log.v(TAG, "onCardSwipeDetected");
		}

		public void onDecodeCompleted(String arg0, String arg1, String arg2,
				int arg3, int arg4, int arg5, String arg6, String arg7,
				String arg8, String arg9) {
			// TODO Auto-generated method stub
			Log.v(TAG, "onDecodeCompleted");
			String track2 = decodeTrack2API2(arg0, arg1,
					arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
			ci = new CardInfo(track2);
			vibrate(T_SUCCESS);
			BaseActivity.this.onComplete(arg7);
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
			ci = null;
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
			// BaseActivity.this.onPlugin();
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
				if (choseVersion == 0) {
					// TODO once we only use API2, we can control this constancy
					deleteAPI2();
					initAPI1();
				}else{
					BaseActivity.this.onError(error);
				}
			} else {
				// TODO translate inner API error code to our own API error code
				int errout;
				switch(error) {
				case CSwiperController.ERROR_FAIL_TO_START:
					allowSwipe = false;
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
			// TODO Auto-generated method stub
			Log.v(TAG, "onGetKsnCompleted");
			
			version = 2;
			if (testAPI) {
				BaseActivity.this.onPlugin();
				testAPI = false;
			}

		}

		public void onInterrupted() {
			// TODO Auto-generated method stub
			Log.v(TAG, "onInterrupted");
			allowSwipe = false;
			BaseActivity.this.onError(E_API2_INTERRUPT);
		}

		public void onNoDeviceDetected() {
			// TODO Auto-generated method stub
			Log.v(TAG, "onNoDeviceDetected");
			allowSwipe = false;
			BaseActivity.this.onError(E_API2_NO_DEVICE_ERROR);
		}

		public void onTimeout() {
			// TODO Auto-generated method stub
			Log.v(TAG, "onTimeout");
			allowSwipe = false;
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
	 *      API1 & API2 private function        *
	 ********************************************/
	// BEGIN API1 Private Function
	private void onAPI1Error(final int error) {
		mAPI1Handler.post(new Runnable() {
			public void run() {
				onError(error);
			}
		});
	}

	private void initAPI1() {
		try {
			Log.v(TAG, "intiAPI1");
			headsetState = 1; // FIXME need?
			version = 1;
			registerReceiver(brc, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
			rt = new CardReader();
			rt.setCardReaderObserver(stateChangedListener1);
			if (!rt.start()) {
				onError(E_API1_INIT);
			}
		} catch (IllegalStateException e) {
			finish();
		}
	}

	private void deleteAPI1() {
		if (true) {
			Log.v(TAG, "deleteAPI1");
			unregisterReceiver(brc);
			if (rt != null) {
				stopSwipe();
				rt.pause();
				rt.release();
				rt = null;
			}
		}
	}
	
	private void stopTimer(){
		//timer.purge();
		//timer.cancel();
		if (mAPI1Handler != null) {
			mAPI1Handler.removeCallbacks(timer);
		}
	}
	
	// END API1 Private Function
	/********************************************/
	// BEGIN API2 Private Function
	private void initAPI2() {
		if (cswiperController == null) {
			Log.v(TAG, "initAPI2");
			cswiperController = new CSwiperController(getApplicationContext(),
					stateChangedListener2);
			cswiperController.setDetectDeviceChange(true);
			version = 2;
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
	// END API2 Private Function
	/********************************************/
	
}
