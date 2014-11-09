package com.fwest98.fingify.Helpers;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.fwest98.fingify.Settings.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class HelperFunctions {
    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static boolean checkPlayServices(boolean showError, Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if(resultCode != ConnectionResult.SUCCESS && showError) {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity, Constants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("PlayServices", "No pushmessages");
            }
            return false;
        }
        return true;
    }

    public static boolean isScreenLocked(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.isKeyguardLocked();
    }
}
