package com.tangye.cardreader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * A helper which provides some functions and arguments used when decoding
 * @author tangye
 *
 */
public class MathHelper {
	    
	/**
	 * average power threshold to filter out useful data
	 */
    public static final int NoiseThresholdDB = -60;
    
    /**
     * sample attitude threshold to filter out userful raw data
     */
    public static final int SignalThreshold = 200;
    
    /**
     * min length buffer of binary code decoded from magnetic strip
     */
    public static final int SignalBitMinLength = 200;
    
    /**
     * min length of samples which is used to be decoded
     */
    public static final int SignalSampleMinLength = 7100;
    
    /**
     * Calculate the power of the given input signal.
     * 
     * @param   sdata       Buffer containing the input samples to process.
     * @param   off         Offset in sdata of the data of interest.
     * @param   samples     Number of data samples to process.
     * @return              The calculated power in dB relative to the maximum
     *                      input level; hence 0dB represents maximum power,
     *                      and minimum power is about -95dB.  Particular
     *                      cases of interest:
     *                      <ul>
     *                      <li>A non-clipping full-range sine wave input is
     *                          about -2.41dB.
     *                      <li>Saturated input (heavily clipped) approaches
     *                          0dB.
     *                      <li>A low-frequency fully saturated input can
     *                          get above 0dB, but this would be pretty
     *                          artificial.
     *                      <li>A really tiny signal, which only occasionally
     *                          deviates from zero, can get below -100dB.
     *                      <li>A completely zero input will produce an
     *                          output of -Infinity.
     *                      </ul>
     *                      <b>You must be prepared to handle this infinite
     *                      result and results greater than zero,</b> although
     *                      clipping them off would be quite acceptable in
     *                      most cases.
     */
    public final static double calculatePowerDb(short[] sdata, int off, int samples) {
        // Calculate the sum of the values, and the sum of the squared values.
        // We need longs to avoid running out of bits.
        double sum = 0;
        double sqsum = 0;
        for (int i = 0; i < samples; i++) {
            final long v = sdata[off + i];
            sum += v;
            sqsum += v * v;
        }
        double power = (sqsum - sum * sum / samples) / samples;
        // Scale to the range 0 - 1.
        power /= MAX_16_BIT * MAX_16_BIT;
        // Convert to dB, with 0 being max power.  Add a fudge factor to make
        // a "real" fully saturated input come to 0 dB.
        return Math.log10(power) * 10f + FUDGE;
    }

    private static final float MAX_16_BIT = 32768;
    private static final float FUDGE = 0.6f;

    /**
     * magnetic strip bcd(ascii) encoded character
     */
    public static final String[] BCD_DATA_VALUE = {
        "0",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        ":", // Control
        ";", // Start Sentinel
        "<", // Control
        "=", // Field Separator
        ">", // Control
        "?"  // End Sentinel
    };
    
    /**
     * magnetic strip binary charset which compounds to encoded characters
     */
    public static final String[] BCD_DATA_BITS = {
        "00001",
        "10000",
        "01000",
        "11001",
        "00100",
        "10101",
        "01101",
        "11100",
        "00010",
        "10011",
        "01011", // Control
        "11010", // Start Sentinel
        "00111", // Control
        "10110", // Field Separator
        "01110", // Control
        "11111"  // End Sentinel
    };

    private static int getIndex(String[] a, String s) {
        for(int i = 0; i<a.length; i ++) {
            if(a[i].equals(s))
                return i;
        }
        return -1;
    }

    /**
     * Decode magnetic strip audio digit bit signals codes from the bit-data
     * @param data the raw source binary data on the second magnetic strip track 2 
     * @return a string decoded from the track, without first and the last character
     * @throws IllegalStateException cannot resolve the data
     */
    public final static String decodeCardInformation(StringBuilder data) throws IllegalStateException {
        String out = "";
        if(data == null) {
            throw new IllegalArgumentException("Null data string");
        }
        // android.util.Log.e("MATH", data.toString());
        int start = data.indexOf("011010");
        int end = data.lastIndexOf("111110");
        boolean isReverse = false;
        if(start < 0 || end <= start) {
            data.reverse();
            isReverse = true;
            start = data.indexOf("011010");
            end = data.lastIndexOf("111110");
            if(start < 0 || end <= start) {
                throw new IllegalStateException("Cannot resolve the data");
            }
        }
        boolean trySecond;
        do {
            trySecond = false;
            for(int i = start+6; i <= end - 5; i += 5) {
                String bits = data.substring(i, i+5);
                int chart = getIndex(BCD_DATA_BITS, bits);
                if(chart < 0) {
                    if(isReverse) {
                        throw new IllegalStateException("Cannot decode the data: " + bits);
                    } else {
                        data.reverse();
                        isReverse = true;
                        start = data.indexOf("011010");
                        end = data.lastIndexOf("111110");
                        if(start < 0 || end <= start) {
                            throw new IllegalStateException("Cannot resolve the data");
                        }
                        out = "";
                        trySecond = true;
                        break;
                    }
                }
                out += BCD_DATA_VALUE[chart];
            }
        }while(trySecond);
        return out;
    }
    
    /**
     * Debug the raw source data samples, please use this function
     * A data.txt file will be created under external partition
     * 
     * @param signal the input audio sampled signals
     * @param length the length of the array to be recorded
     * <b>please add external sdcard permission into android manifest xml file</b>
     */
    public static void output(short[] signal, int length) {
        File f = new File("/mnt/sdcard/data.txt");
        BufferedWriter output;
        try {
            output = new BufferedWriter(new FileWriter(f));
            for(int i = 0; i<length; i++) {
                output.write(String.valueOf(signal[i] + " "));
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * a function to hide some digits from exposure
     * 
     * @param input the raw digit-string of which some bits need to be masked  
     * @param mask which character used to fill the mask bits
     * @param pre the length of prefix which is not in mask
     * @param suf the length of suffix which is not in mask
     * @return masked string
     */
    public static String StringMask(String input, char mask, int pre, int suf) {
    	int len = input.length();
    	if(len < pre + suf) {
    		return input;
    	}
    	String prefix = input.substring(0, pre);
    	String suffix = input.substring(len - suf, len);
    	char[] ch = new char[len - pre - suf];
    	Arrays.fill(ch, mask);
    	String out = new String(ch);
    	out = prefix + out + suffix;
    	return out;
    }
}
