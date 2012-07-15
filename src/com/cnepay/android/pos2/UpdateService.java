package com.cnepay.android.pos2;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


public class UpdateService extends Service {

    public static final int UNCHECKED = -1;
    public static final int CHECKED_NO_NEED = 0;
    public static final int CHECKED_NEED_UPGRADE = 1;
    
    private static final String TAG = "UpdateService";
    private static int checkstate = UNCHECKED; 
    private Thread updatethread = null;

    @Override
    public void onCreate() {
    	super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	super.onStartCommand(intent, flags, startId);
        startCheck();
        return START_STICKY;
    }
    
    public static boolean needUpgrade() {
        return checkstate == CHECKED_NEED_UPGRADE;
    }
    
    /**
     * 检测版本信息
     */
    private void startCheck() {
        if(updatethread == null && checkstate == UNCHECKED) {
            updatethread = new Thread() {
                public void run() {
                    UpdateModel um = new UpdateModel(UpdateService.this);
                    checkstate = um.update(getPackageName());
                    //for test
                    //checkstate = um.update("com.cnepay.android.pos");
                    Log.i(TAG, "checking result: " + String.valueOf(checkstate));
                    updatethread = null;
                }
            };
            updatethread.start();
        } else {
            Log.i(TAG, "No need to start thread, checkstate: " + String.valueOf(checkstate));
        }
    }
    
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}

