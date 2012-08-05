package com.tangye.android.utils;

import java.util.Random;

import com.cnepay.android.pos2.R;
import com.tangye.android.dialog.AlertDialogBuilderWrapper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.os.Build.VERSION;
import android.os.Environment;

public class PublicHelper {
	
	public static boolean isDebug = true; // true for test route, false for real environment
    
    public static String getError(String code, Context ctx) {
        int i = ctx.getResources().getIdentifier("c" + code, "string", ctx.getPackageName());
        if(i != 0) {
            return ctx.getString(i) + " (" + code + ")";
        }
        return code;
    }
    
    public static String getCashString(String length12) {
        if(length12 != null & length12.length() == 12) {
            try {
                int a = Integer.valueOf(length12.substring(0, 10));
                String b = String.valueOf(a) + "." + length12.substring(10, 12);
                return b;
            } catch (Exception e) {}
        }
        return "0.00";
    }
    
    public static AlertDialogBuilderWrapper getAlertDialogBuilder(Context ctx) {
		/*
		Class<?> b;
		b = Class.forName("android.app.AlertDialog$Builder");
		Constructor<?> c = b.getConstructor(Context.class, int.class);
		builder = (Builder) c.newInstance(ctx, 2);
		*/
    	return new AlertDialogBuilderWrapper(ctx, R.style.MyAlertDialog);
    }
    
    public static ProgressDialog getProgressDialog(Context context, CharSequence title, CharSequence message,
    		boolean indeterminate, boolean cancelable, OnCancelListener cancelListener) {
    	if(VERSION.SDK_INT > 10) {
	    	ProgressDialog dialog = new ProgressDialog(context, 2);
	    	dialog.setTitle(title);
	    	dialog.setMessage(message);
	    	dialog.setIndeterminate(indeterminate);
	    	dialog.setCancelable(cancelable);
	    	dialog.setOnCancelListener(cancelListener);
	    	dialog.show();
	    	return dialog;
    	} else {
    		return ProgressDialog.show(context, title, message, indeterminate, cancelable, cancelListener);
    	}
    }
    
    public static ProgressDialog getProgressDialog(Context context, CharSequence title, CharSequence message,
    		boolean indeterminate, boolean cancelable) {
    	if(VERSION.SDK_INT > 10) {
	    	ProgressDialog dialog = new ProgressDialog(context, 2);
	    	dialog.setTitle(title);
	    	dialog.setMessage(message);
	    	dialog.setIndeterminate(indeterminate);
	    	dialog.setCancelable(cancelable);
	    	dialog.show();
	    	return dialog;
    	} else {
    		return ProgressDialog.show(context, title, message, indeterminate, cancelable);
    	}
    }
    
    private static final char validRandomCode[] = {
    	'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'a', 'b', 'c', 'd', 'e', 'f' ,
    	'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v' ,
    	'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L' ,
    	'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '~', '!' ,
    	'@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+', '-', '=', '[', ']', '{' ,
    	'}', ';', ':', ',', '<', '.', '>', '?' 
    };
    
    private static final int lengthOfRandCode = validRandomCode.length;

    public static String getRandomCode(){
    	final int LEN = 3;
    	String result = "";
    	Random random = new Random(System.currentTimeMillis());
    	for(int i = 0; i < LEN; i++){
    		result =result + validRandomCode[Math.abs(random.nextInt()) % lengthOfRandCode];
    	}
    	return result;
    }
    
    public static String getMaskedString(String orig, int start, int end, char mask) {
    	StringBuilder c = new StringBuilder(orig);
    	try {
			for (int i = start; i < orig.length() - end; i++) {
				c = c.replace(i, i+1, String.valueOf(mask));
			}
    	} catch (Exception e) {
    		return orig;
    	}
		return c.toString();
    }
    
    public static String getSDPath(){ 
		boolean sdCardExist = Environment.getExternalStorageState()   
				.equals(Environment.MEDIA_MOUNTED); 
		if(sdCardExist) {     
			return (Environment.getExternalStorageDirectory()).toString(); 
		}
		return null; 
	}
    
    public static boolean isEmptyString(String str){
    	return str == null || str.equals("");
    }
    
    public static boolean isChineseStr(String str){
    	if(PublicHelper.isEmptyString(str)){
    		return false;
    	}
    	char[] ch = str.toCharArray(); 
    	for (int i = 0; i < ch.length; i++) { 
    		char c = ch[i]; 
    		if (!isChineseWord(c)) { 
    			return false; 
    		} 
    	} 
    	return true; 
    }
    
    private static boolean isChineseWord(char c) { 
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c); 
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || 
        	ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || 
        	ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || 
        	ub == Character.UnicodeBlock.GENERAL_PUNCTUATION || 
        	ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || 
        	ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) { 
            return true; 
        } 
        return false; 
    }
    
    public static int dp2px(Context context, float dpValue) {
    	final float scale = context.getResources().getDisplayMetrics().density;
    	return (int) (dpValue * scale + 0.5f);
	}
}
