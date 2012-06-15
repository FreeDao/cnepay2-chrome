package com.tangye.android.iso8583;

//import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tangye.android.utils.MD5;

//import android.app.AlarmManager;
import android.content.Context;
import android.util.Log;

public class POSHelper {
	
	private static final String TAG = "POSHelper";
	private static final String PREFIX = "SESSION_";
	private static long sessionId = -1;	
	private static Map<String, POSSession> session = new ConcurrentHashMap<String, POSSession>();
		
	/**
	 * Get a POS Terminal info box, store any POS related fixed info after register
	 * @param ctx Context
	 * @param phone The phone number registered by the card holder
	 * @return the POS terminal handler which is targeting on process info of it
	 */
	public static POSEncrypt getPOSEncrypt(Context ctx, String phone) {
		String md5 = MD5.getMD5ofStr(phone);
		POSEncrypt pe = new POSEncrypt(ctx, md5);
		return pe;
	}
	
	/**
	 * Get a Session during POS online
	 * @param ctx Context
	 * @param isNew whether to get a new session
	 * @return an existed session or a new session. return null when the created session is lost
	 */
	public static POSSession getPOSSession(Context ctx, boolean isNew) {
		if (ctx == null) return null;
		String sid;
		POSSession ses;
		if(isNew) {
			resetSession();
			sid = Long.toString(newSession());
			ses = new POSSession(ctx);
			session.put(PREFIX + sid, ses);
		} else {
			sid = getSessionString();
			if(sid == null) return null;
			ses = session.get(PREFIX + sid);
			if(ses == null) return null;
		}
		return ses;
	}
	
	/**
	 * Delete all existing session
	 */
	public static void deleteSession() {
	    POSSession SESSION = getPOSSession();
	    if(SESSION != null) {
	        SESSION.release();
	        SESSION = null;
	    }
	    // TODO stop expired the session
	    resetSession();
	}
	
	public static POSSession getPOSSession() {
	    String sid = getSessionString();
        if(sid == null) return null;
        POSSession ses = session.get(PREFIX + sid);
        if(ses == null) return null;
        return ses;
	}
	
	/* TODO expired the session
	private void setAlarm(Context ctx, long millis) {
	    Intent i = new Intent(this, ActiveSyncPushReceiver.class);
        i.putExtra(EXTRA_ACCOUNT_ID, accountId);
        i.putExtra(EXTRA_ALARM_REASON, reasonCode);
        i.setData(key.toUri());
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmMap.put(key, pi);
        final long triggerTime = System.currentTimeMillis() + millis;
        AlarmManager alarmManager = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, triggerTime, pi);
	}*/
	
	private static void resetSession() {
	    session.clear();
	    sessionId = -1;
	    Log.d(TAG, "Session reset already");
	    // TODO stop all session expired mode
	}
	
	private static long newSession() {
    	sessionId = new Date().getTime();
    	// TODO expired the session
    	return sessionId;
    }
    
	/**
	 * get the current sessionID
	 * @return the current sessionID
	 */
    public static long getSession() {
    	// TODO expired the session
    	return sessionId;
    }
    
    /**
     * get the current sessionID String
     * @return the current sessionID string, if session is smaller than 0, then return null
     */
    public static String getSessionString() {
    	if(sessionId > 0)
    		return String.valueOf(getSession());
    	return null;
    }
    
    /**
     * Delete account which is bind on this POS phone
     * @param phone is the phone number registered by the card holder
     * @return true for delete success, otherwise false
     */
    /*
    public static boolean deleteAccount(String phone) {
    	java.io.File f = new java.io.File(PREFS_PATH + phone + ".xml");
    	if(f.exists()) {
    		Log.i(TAG, "Delete successfully");
    		return f.delete();
    	}
    	return false;
    }
    */
    
}
