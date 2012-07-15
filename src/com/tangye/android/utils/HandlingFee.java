package com.tangye.android.utils;

import java.math.BigDecimal;

public class HandlingFee {
    long raw;
    long fee;
    long all;
    
    public HandlingFee(BigDecimal fee) {
        this(fee.toString());
    }
    
    public HandlingFee(String x) {
        int pos = x.indexOf(".");
        long a = Long.valueOf(x.substring(0, pos));
        long b = Long.valueOf(x.substring(pos+1));
        init(a * 100 + b);
    }
    
    public HandlingFee(long orig) {
        init(orig);
    }
    
    private void init(long orig) {
        raw = orig;
        long ad = Math.round(raw * 0.01d);
        if(ad < 100) ad = 100;
        else if(ad > 6000) ad = 6000;
        fee = ad;
        all = raw + fee;
    }
    
    public BigDecimal getRaw() {
        return getBD(raw);
    }
    
    public BigDecimal getFee() {
        return getBD(fee);
    }
    
    public BigDecimal getAll() {
        return getBD(all);
    }
    
    private BigDecimal getBD(long v) {
        String x1 = String.valueOf(v / 100);
        String x2 = String.valueOf(v % 100);
        if(x2.length()<2)
            x2 = "0" + x2;
        return new BigDecimal(x1 + "." + x2);
    }
}
