package com.capitec.interfaceapp.runtime;

import android.content.Context;
import android.util.Log;

public final class InterfaceLogcatLogger {
    private static final String TAG = "CapitecInterface_Impl";

    private InterfaceLogcatLogger() { }

    public static void input(Context context, String method, Object... args) {
        if (isDebuggable(context)) {
            Log.i(TAG, method + " input=" + java.util.Arrays.deepToString(args == null ? new Object[0] : args));
        }
    }

    public static void returnValue(Context context, String method, Object value) {
        if (isDebuggable(context)) {
            Log.i(TAG, method + " return=" + valueText(value));
        }
    }

    public static void returnVoid(Context context, String method) {
        if (isDebuggable(context)) {
            Log.i(TAG, method + " return=void");
        }
    }

    public static void failure(Context context, String method, Throwable throwable) {
        if (isDebuggable(context)) {
            Log.e(TAG, method + " error=" + throwable.getClass().getSimpleName()
                    + ": " + safe(throwable.getMessage()), throwable);
        }
    }

    private static boolean isDebuggable(Context context) {
        return true;
    }

    private static String valueText(Object value) {
        return ValueFormatter.format(value);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
