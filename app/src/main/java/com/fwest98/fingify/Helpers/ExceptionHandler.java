package com.fwest98.fingify.Helpers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;

public class ExceptionHandler {
    public static void handleException(Exception exception, Context context, boolean log) {
        Toast.makeText(context, exception.getMessage() + ": " + Arrays.toString(exception.getStackTrace()), Toast.LENGTH_SHORT).show();
        if(log) Log.e("ERROR", exception.getMessage(), exception);
    }
}