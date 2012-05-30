package com.tangye.cardreader;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * The main card reader class to perform a bank card listening action and audio resolution
 * @author tangye
 *
 */
public class CardReader {

	/**
	 * Card reader state
	 * @author tangye
	 */
    public static enum STATE {
    	/**
    	 * unset state, means it is in a initial state
    	 */
        UNSET, 
        
        /**
         * the card reader is plugged in, preparing for reading data
         */
        PREPARED, 
        
        /**
         * the card reader is not prepared, maybe plugged out
         */
        UNPREPARED, 
        
        /**
         * the audio recorder is started for listening to card reader plug in-out
         */
        STARTED, 
        
        /**
         * the audio recorder is stopped
         */
        STOPPED
    }
    
    /**
     * Error types, feedback when swiping card
     * @author tangye
     */
    public static enum ERROR {
    	/**
    	 * swipe too slow
    	 */
        TOO_SLOW, 
        
        /**
         * swipe too fast
         */
        TOO_FAST, 
        
        /**
         * cannot decode for unknown reason, maybe swipe unstable
         */
        UN_RESOLVED, 
        
        /**
         * internal error, just for debug usage
         */
        NOT_DEFINE
    }

    private final static String TAG = "CardReader";
    private final static int SAMPLE_RATE_IN_HZ = 44100;
    private final static int STAT_POWER_COUNTER = 10; // related to BlockSize, so need to fix
    private final static int MAX_SIZE_BUFFER = 20000;
    private final static float[] MID_FILTER_FACTOR = {0.3f, 0.6f, 0.85f, 1, 1, 1, 0.85f, 0.6f, 0.3f};
    private final static float[] FREQENCY_FACTOR = {0.25f, 2.8f};

    private volatile boolean isRun = false;
    private int BLOCK_SIZE = 1024;
    private AudioRecord ar;
    private int bs;
    private CardReaderObserver mObserver;
    private STATE mStatus;
    private Thread recordThread;

    /**
     * default constructor to prepare the card reader resources
     * @throws IllegalStateException when audio recorder cannot be setup
     */
    public CardReader() throws IllegalStateException {
        mStatus = STATE.UNSET;
        bs = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        /*while (BLOCK_SIZE < bs)
            BLOCK_SIZE <<= 1;*/
        if(bs < 0) {
            throw new IllegalStateException("This Device cannot support Audio Input");
        }
        BLOCK_SIZE = bs;
        recreate();
        if(ar == null) {
        	throw new IllegalStateException("This Device cannot support Audio Recorder");
        }
    }

    /**
     * constructor
     * @param observer the init callback observer
     * @throws IllegalStateException when audio recorder cannot be setup
     * @link #setCardReaderObserver(CardReaderObserver)
     */
    public CardReader(CardReaderObserver observer) throws IllegalStateException {
        this();
        mObserver = observer;
    }

    private void recreate() {
    	if(ar == null) {
    		ar = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, BLOCK_SIZE);
    	}
    }

    /**
     * release all audio recorder resource and object<br>
     * if called, the object must be set to null
     * @see android.media.AudioRecord#release()
     */
    public void release() {
        if(ar != null && ar.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
            ar.release();
        }
        ar = null;
    }

    class RecordThread extends Thread {
        private short[] x = new short[MAX_SIZE_BUFFER];
        private short[] xfilter = new short[MAX_SIZE_BUFFER];

        @Override
        public void run() {
            // Log.i(TAG, "Thread running successfully");
        	if(ar == null) return;
        	try {
                ar.startRecording();
        	} catch (IllegalStateException e) {
        		onErrorFeedback(ERROR.NOT_DEFINE);
        		e.printStackTrace();
        		return;
        	}
            onStatusChange(STATE.STARTED);
            short[] buffer = new short[BLOCK_SIZE];
            int pos = 0;
            int powerStat = 0;
            int powerStatCount = 0;
            int xpos = 0;
            boolean isOverFlow = false;
            while (isRun) {
                /* read raw signal into buffer */
            	if(ar == null) {
            		onStatusChange(STATE.STOPPED);
            		break;
            	}
                int r = ar.read(buffer, pos, BLOCK_SIZE - pos);
                if (r < 0 ) {
                    Log.e(TAG, "Wrong input");
                    continue;
                } else if (r < BLOCK_SIZE - pos) {
                    pos += r;
                    continue;
                }
                pos = 0;
                /* finish read raw signal */
                /*************************************************************/
                /*
                 * get the power of signal determine whether the card reader is
                 * plugged in a not while started, or check SwipeService to find
                 * the headset state
                 */
                int headsetState = getHeadsetState();
                if(headsetState != -1) {
                    onStatusChange(headsetState == 0?STATE.UNPREPARED:STATE.PREPARED);
                    powerStat = 0;
                    powerStatCount = 0;
                } else if(mStatus == STATE.STARTED) {
                    powerStat += (int) MathHelper.calculatePowerDb(buffer, 0,
                            BLOCK_SIZE);
                    powerStatCount++;
                    if (powerStatCount == STAT_POWER_COUNTER) {
                        powerStat /= STAT_POWER_COUNTER;
                        Log.i(TAG, "POWER = "+String.valueOf(powerStat) + "dB");
                        if (powerStat < MathHelper.NoiseThresholdDB) {
                            onStatusChange(STATE.PREPARED);
                        } else {
                            onStatusChange(STATE.UNPREPARED);
                        }
                        powerStat = 0;
                        powerStatCount = 0;
                    }
                }
                /* finish judge the card reader state */
                /*************************************************************/
                if(mStatus == STATE.PREPARED) {
                    short max = 0;
                    for(int i = 0; i < BLOCK_SIZE; i++) {
                        short newvalue = (short)Math.abs(buffer[i]);
                        if(newvalue > max)
                            max = newvalue;
                    }
                    if(max > MathHelper.SignalThreshold) {
                        if(xpos + BLOCK_SIZE < MAX_SIZE_BUFFER) {
                            System.arraycopy(buffer, 0, x, xpos, BLOCK_SIZE);
                            xpos += BLOCK_SIZE;
                        } else {
                            isOverFlow = true;
                            // Log.i(TAG, "Too slow");
                        }
                    } else if(xpos > 0) {
                        // get an input buffer x which is interesting to us
                        // MathHelper.output(x, xpos); // intend to use Matlab for analysis
                        /*************************** start middle filter ***************************/
                        int MID_FILTER_RANGE = MID_FILTER_FACTOR.length;
                        int starter = (MID_FILTER_RANGE - 1) / 2;
                        for(int j = 0; j < starter; j++) {
                            xfilter[j] = x[j];
                            xfilter[xpos - j] = x[xpos - j];
                        }
                        for(int j = starter; j < xpos - starter; j++) {
                            float all = (float)x[j+starter] * MID_FILTER_FACTOR[MID_FILTER_RANGE - 1];
                            for(int p = -starter; p<starter; p++) {
                                all += (float)x[j+p] * (float)MID_FILTER_FACTOR[p + starter];
                            }
                            xfilter[j] = (short)(all/MID_FILTER_RANGE);
                        }
                        short[] xtmp = xfilter;
                        xfilter = x;
                        x = xtmp; // swap x and xfilter
                        /*************************** end middle filter *****************************/
                        // start to analyze the data from the magnetic strip *****(audio input)*****
                        short lastx = x[0];
                        int cc = 1; // sample count counter
                        int T0 = 1; // base half periodic count
                        StringBuilder out = new StringBuilder(MathHelper.SignalBitMinLength);
                        int just = 0; // assisted fact
                        int Ttmp = 0; // temporary half periodic count
                        int maxFailureBitLength = 0;
                        int i;
                        for(i = 1; i < xpos; i++) {
                            short y = x[i];
                            if((lastx ^ y) < 0) {
                                lastx = y;
                                int T = cc; // record current half periodic count
                                cc = 0; // clear sample counter
                                float rate = T0 / (float)T;
                                // Log.i(TAG, "new rate: " + String.valueOf(rate) + " T=" +String.valueOf(T));
                                boolean stop = false;
                                if(rate >= FREQENCY_FACTOR[0] && rate <= FREQENCY_FACTOR[1]) {
                                    if(rate < 1.5f && just == 0) {
                                        out.append("0");
                                        T0 = T;
                                    } else if(rate >= 1.5f) {
                                        just++;
                                        if(just == 2) {
                                            just = 0;
                                            out.append("1");
                                            T0 = T + Ttmp;
                                        } else {
                                            Ttmp = T;
                                        }
                                    } else {
                                        stop = true;
                                    }
                                } else {
                                    // Log.i(TAG, String.valueOf(rate));
                                    stop = true;
                                }
                                if(stop) {
                                    if(out.length() > MathHelper.SignalBitMinLength) {
                                        onDataReturned(out);
                                        i = xpos + 1; // break the for loop, xpos + 1 means success
                                        // Log.i(TAG, "success "+String.valueOf(xpos));
                                    } else {
                                        maxFailureBitLength = Math.max(maxFailureBitLength, out.length());
                                    }
                                    out = new StringBuilder(MathHelper.SignalBitMinLength);
                                    T0 = T;
                                    just = 0;
                                }
                            }
                            cc++; // sample counter ++
                        }
                        if(i == xpos) {
                            if(isOverFlow) {
                                onErrorFeedback(ERROR.TOO_SLOW);
                                Log.i(TAG, "error "+String.valueOf(xpos));
                            } else if(maxFailureBitLength > MathHelper.SignalBitMinLength / 8) {
                                if(xpos < MathHelper.SignalSampleMinLength) {
                                    onErrorFeedback(ERROR.TOO_FAST);
                                } else {
                                    onErrorFeedback(ERROR.UN_RESOLVED);
                                }
                            }
                        }
                        xpos = 0;
                        isOverFlow = false;
                    }
                } else if(mStatus == STATE.UNPREPARED) {
                    xpos = 0;
                    isOverFlow = false;
                    if(ar == null) {
                        onStatusChange(STATE.STOPPED);
                        break;
                    }
                    ar.stop();
                    release();
                    onStatusChange(STATE.STOPPED);
                    break;
                }
            }
            // Log.i(TAG, "Thread end");
            // here is the loop just inner the while
        } // end the while(isRun)
    }

    /**
     * set the callback observer to receive the feedbacks
     * @param cro card reader observer to set
     */
    public void setCardReaderObserver(CardReaderObserver cro) {
        mObserver = cro;
    }

    /**
     * pause the listening thread to audio recorder and stop reading card
     */
    public void pause() {
        isRun = false;
        if(recordThread != null) {
            recordThread.interrupt();
        }
        if(ar != null)
            ar.stop();
        onStatusChange(STATE.STOPPED);
    }
    
    /**
     * start listening to the audio magnetic strip signals
     * @return true if start successfully<br>
     *         false if failed
     */
    public boolean start() {
        if(!isRun) {
            if(recordThread != null && recordThread.isAlive()) {
                // Log.i(TAG, "start failed, recordthread alive");
                return false;
            }
            recordThread = new RecordThread();
            recordThread.start();
            isRun = true;
            // Log.i(TAG, "new recordthread");
            return true;
        }
        // Log.i(TAG, "already running recordthread");
        return false;
    }

    private void onStatusChange(STATE status) {
        if (mObserver != null && mStatus != status) {
            mStatus = status;
            mObserver.onStatusChange(status);
            Log.i(TAG, String.valueOf(status));
        }
    }
    
    private void onDataReturned(StringBuilder out) {
        if (mObserver != null) {
            mObserver.onDataReturned(out);
        }
    }

    private void onErrorFeedback(ERROR err) {
        if (mObserver != null) {
            mObserver.onErrorFeedback(err);
        }
    }

    /**
     * Interface CardReaderObserver
     * @author tangye
     */
    public interface CardReaderObserver {
    	/**
    	 * once the listener state changed
    	 * @param status
    	 */
        public void onStatusChange(STATE status);
        
        /**
         * once identified the read-in data
         * @param data
         */
        public void onDataReturned(StringBuilder data);
        
        /**
         * once data cannot be resolved for some reason
         * @param err
         */
        public void onErrorFeedback(ERROR err);
        
        /**
         * override this method to get the headset state
         * @return headset state
         */
        public int getHeadsetState();
    }

    /**
     * private function
     * Variety for each activity
     * @return head state
     */
    private int getHeadsetState() {
    	return mObserver != null?mObserver.getHeadsetState() : 0;
    }
}
