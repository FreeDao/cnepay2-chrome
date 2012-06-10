package com.tangye.android.utils;

import java.lang.reflect.Constructor;

import android.app.AlertDialog.Builder;
import android.content.Context;

public class PublicHelper {
    
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
    
    public static Builder getAlertDialogBuilder(Context ctx) {
    	Builder builder;
    	try {
    		Class<?> b;
			b = Class.forName("android.app.AlertDialog$Builder");
			Constructor<?> c = b.getConstructor(Context.class, int.class);
			builder = (Builder) c.newInstance(ctx, 2);
		} catch (Exception e) {
			builder = new Builder(ctx);
			e.printStackTrace();
		}
    	return builder;
    }
}
