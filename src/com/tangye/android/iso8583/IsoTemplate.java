package com.tangye.android.iso8583;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IsoTemplate {
    
	private int type;
	private String isoHeader;
    private Map<Integer,Template> templates = new ConcurrentHashMap<Integer,Template>();
    
    public void setIsoHeader(String value) {
        isoHeader = value;
    }
    /** Returns the ISO header that this message was created with. */
    public String getIsoHeader() {
        return isoHeader;
    }
    
    public void setValue(int index, IsoType t) {
        setValue(index, t, 0);
    }
    
    public void setType(int value) {
        type = value;
    }

    public int getType() {
        return type;
    }
    
    public boolean hasValue(int idx) {
        return templates.get(idx) != null;
    }
    
    public void setValue(int index, IsoType t, int len) {
        if (index < 2 || index > 128) {
            throw new IndexOutOfBoundsException("Field index must be between 2 and 128");
        }
        if (len < 0) {
            templates.remove(index);
        } else {
            Template v = null;
            if (t.needsLength()) {
                v = new Template(t, len);
            } else {
                v = new Template(t);
            }
            templates.put(index, v);
        }
    }
    
    public int[] getBitmapArray() {
        ArrayList<Integer> keys = new ArrayList<Integer>();
        keys.addAll(templates.keySet());
        Collections.sort(keys);
        int[] bm = new int[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            bm[i] = keys.get(i);
        }
        return bm;
    }
    
    public ArrayList<Integer> testAndGetBitmap(byte[] bitmap) {
        ArrayList<Integer> keys = new ArrayList<Integer>();
        String BM = IsoUtil.byte2string(bitmap);
        for(int i = 0; i < 64; i++) {
        	char c = BM.charAt(i);
        	if(c == '1') {
        		if(hasValue(i+1)) {
        			keys.add(i+1);
        		} else {
        		    android.util.Log.i("RC BIMAP", "Field: " + String.valueOf(i+1) + " is invalid");
        			return null;
        		}
        	} else if(c != '0') {
        		return null;
        	}
        }
        return keys;
    }
    
    public Template getField(int idx) {
        if(hasValue(idx)) {
            return templates.get(idx);
        }
        return null;
    }

}
