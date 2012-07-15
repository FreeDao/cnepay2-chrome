package com.tangye.android.iso8583;

import java.io.UnsupportedEncodingException;

public class IsoUtil {
    /**
     * 将每个字节变成两个16进制字符，例如“1234” =》 "31323334"(0x31 0x32 0x33 0x34)
     * 长度增加一倍
     * @param b
     * @return 字符中只含有ASCII
     */
    public static String hexString(byte[] b) {
        StringBuffer d = new StringBuffer(b.length * 2);
        for (int i=0; i<b.length; i++) {
            char hi = Character.forDigit ((b[i] >> 4) & 0x0F, 16);
            char lo = Character.forDigit (b[i] & 0x0F, 16);
            d.append(Character.toUpperCase(hi));
            d.append(Character.toUpperCase(lo));
        }
        return d.toString();
    }
    
    /**
     * 将每两个bcd字节按照hex组成一个2进制字节，例如“123D” =》 {0x12, 0x3D}两个字节，右边不足补0
     * 或者“123” =》 右补零 {0x12, 0x30}, 长度减少一半
     * @param BCD码字符串，范围，‘0’ - ‘？’共有16个字符组成的字符串
     * @return ISO-8859-1 String, 只含8BIT数据，通过getBytes("ISO-8859-1")获得原始字节数据
     * @throws UnsupportedEncodingException 
     */
    public static String bcdString(String s) throws UnsupportedEncodingException {
        s = s.toUpperCase();
        // 右补0，成偶数位字符
        if(s.length() % 2 == 1) {
            s += "0";
        }
        int charpos = 0;
        int bufpos = 0;
        byte[] buf = new byte[s.length() / 2];
        while (charpos < s.length()) {
            buf[bufpos] = (byte)(((s.charAt(charpos) - 48) << 4)
                    | (s.charAt(charpos + 1) - 48));
            charpos += 2;
            bufpos++;
        }
        return new String(buf, "ISO-8859-1");
    }
    
    /**
     * 将字节组扩充或裁剪，由length确定，扩充时，右补0，裁剪时丢弃右边字节
     * @param array
     * @param length
     * @return 处理后的的字节
     */
    public static byte[] trim (byte[] array, int length) {
        byte[] trimmedArray = new byte[length];
        System.arraycopy(array, 0, trimmedArray, 0, Math.min(length, array.length));
        return  trimmedArray;
    }
    
    /**
     * 按照两个字节组异或，得到一个新的字节组，长度由小的字节组决定，异或从最高位对齐开始
     * @param op1
     * @param op2
     * @return 新的字节组
     */
    public static byte[] xor (byte[] op1, byte[] op2) {
        byte[] result = null;
        // Use the smallest array
        if (op2.length > op1.length) {
            result = new byte[op1.length];
        } else {
            result = new byte[op2.length];
        }
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte)(op1[i] ^ op2[i]);
        }
        return result;
    }
    
    /**
     * 将字节组的每个字节用16进制表示，并以连续字符串输出，例如<br> 
     * 2  48 -16 -121 27 127 119 42<br>
     * 02 30  F0  87  1B 7F  77  2A =》"0230F0871B7F772A"<br>
     * 字符串长度增加一倍
     * @param 要处理的字节组
     * @return 处理后的字符串
     */
    public static String byte2hex(byte[] b, int offset, int len) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < len; n++) {
            stmp = (Integer.toHexString(b[offset + n] & 0XFF));
            if (stmp.length() == 1)
                hs = hs + "0" + stmp;
            else
                hs = hs + stmp;
        }
        return hs.toUpperCase();
    }
    
    public static String byte2hex(byte[] b) {
        return byte2hex(b, 0, b.length);
    }
    
    public static byte[] hex2byte(byte[] b, int offset, int len) {
        byte[] d = new byte[len];
        for (int i = 0; i < len * 2; i++) {
            int shift = i % 2 == 1 ? 0 : 4;
            d[i >> 1] |= Character.digit((char) b[offset + i], 16) << shift;
        }
        return d;
    }

    /**
     * 将一个HexString转换为原始的byte组，例如<br>
     * “230368F0C363B668” =》 23 03 68 F0 C3 63 B6 68<br>
     * =》byte[] {35 3 104 -16 -61 99 -74 104}<br>
     * 长度缩小一半 
     * @param hexString字符串
     * @return 原始byte组
     */
    public static byte[] hex2byte(String s) {
        if (s.length() % 2 == 0) {
            return hex2byte(s.getBytes(), 0, s.length() >> 1);
        } else {
            // Padding left zero to make it even size #Bug raised by tommy
            return hex2byte("0" + s);
        }
    }

    public static String byte2string(byte[] b64) {
        StringBuilder sb = new StringBuilder(64);
        for(int i = 0; i < b64.length; i++) {
            byte b = b64[i];
            for(int j = 0; j < 8; j++) {
                sb.append(String.valueOf((b & 0x80) >> 7));
                b <<= 1;
            }
        }
        return sb.toString();
    }
    
    public static byte[] string2byte(String bin) {
        if(bin.length() % 8 != 0 ) throw new IllegalArgumentException("string must be times of 8");
        int len = bin.length() / 8;
        byte[] bs = new byte[len];
        for(int i = 0; i < len; i++) {
            byte b = 0;
            for(int j = 0; j < 8; j++) {
                b <<= 1;
                if(bin.charAt( i * 8 + j) == '0') {
                } else if(bin.charAt( i * 8 + j) == '1') {
                    b |= 0x01;
                } else {
                    throw new IllegalArgumentException("char must only be '1' or '0'");
                }
            }
            bs[i] = b;
        }
        return bs;
    }
}
