package com.tangye.android.iso8583;

public class Template {
    public IsoType type;
    public int length;
    public Template(IsoType t) {
        this(t, t.getLength());
    }
    public Template(IsoType t, int len) {
        type = t;
        length = len;
    }
}