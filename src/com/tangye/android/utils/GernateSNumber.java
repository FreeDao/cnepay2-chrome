package com.tangye.android.utils;



import java.security.*;
import javax.crypto.KeyGenerator;

import com.tangye.android.iso8583.IsoUtil;
/**
 * Created by IntelliJ IDEA.
 * User: yuanlin
 * Date: 12-1-11
 * Time: 下午5:08
 * To change this template use File | Settings | File Templates.
 */
public class GernateSNumber {

    final int[] wi = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1};
    // verify digit

    private int[] ai = new int[16];
   final String[]  b_15 = {
                             "0","1","2","3",
                             "4","5","6","7",
                             "8","9","A","B",
                             "C","D","E","F"
                            } ;
    //verify
    public boolean Verify(String sNb) {

        if (sNb.length() != 16) {
            return false;
        }
         sNb = sNb.toUpperCase();
        String verify = sNb.substring(15, 16);
        if (verify.equals(getVerify(sNb))) {
            return true;
        }
        return false;
    }

    //get verify
    public String getVerify(String sNbid) {
        int remaining = 0;

        if (sNbid.length() == 16) {
            sNbid = sNbid.substring(0, 15);
        }
        if (sNbid.length() == 15) {
            int sum = 0;
            for (int i = 0; i < 15; i++) {
                //获得其ASCII值
               ai[i] = (byte)sNbid.charAt(i);
            }
            for (int i = 0; i < 15; i++) {
                sum = sum + wi[i] * ai[i];
            }
            remaining = sum % 16;
        }
        return b_15[remaining];
    }

    public String getSnumber() throws NoSuchProviderException, NoSuchAlgorithmException {

           Provider provider  = Security.getProvider("SunJCE");
            KeyGenerator k1  = KeyGenerator.getInstance("DES",provider.getName());
            Key generatedClearKey = k1.generateKey();
            String sourceNumber = IsoUtil.hexString(generatedClearKey.getEncoded());
            sourceNumber = sourceNumber.substring(0,15)+this.getVerify(sourceNumber);

            return sourceNumber;
  }

}

