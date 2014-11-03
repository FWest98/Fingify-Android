package com.fwest98.fingify.Helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.PreferenceManager;

import com.fwest98.fingify.R;
import com.fwest98.fingify.Settings.Constants;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

public class FingerprintManager {

    /**
     * Try to authenticate a user with fingerprints
     * @param context Application context
     * @param callback The callbacks
     */
    public static void authenticate(Context context, FingerprintCallbacks callback) {
        authenticate(context, callback, false);
    }

    /**
     * Try to authenticate a user with fingerprints
     * @param context Application context
     * @param callback The callbacks
     * @param force Force the authentication
     */
    public static void authenticate(Context context, FingerprintCallbacks callback, boolean force) {
        /* Check if fingerprint authentication is enabled */
        boolean fingerprintAuthenticationEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.FINGERPRINT_AUTHENTICATION_SETTING, false);
        if(!fingerprintAuthenticationEnabled && !force) {
            // That's bad, it's not enabled....

            // Return the error
            callback.onFinished(FingerprintResponses.DISABLED);
            return; // No further processing
        }

        if(!isFingerPrintSupported(context)) {
            // No support
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(Constants.FINGERPRINT_AUTHENTICATION_SETTING, false).commit();
            callback.onFinished(FingerprintResponses.NOT_SUPPORTED);
            return;
        }

        if (!hasFingerPrints(context)) {
            // No fingerprints found, register new one(s)
            register(context, callback, false);
            return;
        }

        Spass spass = new Spass();
        try {
            // Samsung device
            spass.initialize(context);
            SpassFingerprint spassFingerprint = new SpassFingerprint(context);

            // Setup
            if(spass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT_CUSTOMIZED_DIALOG)) {
                // Let's edit the UI
                spassFingerprint.setDialogTitle(context.getString(R.string.fingerprint_authentication_title), context.getResources().getColor(android.R.color.black));
            }

            // Identify
            spassFingerprint.startIdentifyWithDialog(context, new SpassFingerprint.IdentifyListener() {
                @Override
                public void onFinished(int i) {
                    // It finished. Let's check results
                    if (i == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS || i == SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS) {
                        callback.onFinished(FingerprintResponses.SUCCEEDED);
                    } else {
                        callback.onFinished(FingerprintResponses.FAILED);
                    }
                }

                @Override
                public void onReady() {
                }

                @Override
                public void onStarted() {
                }
            }, true);

            return; //End to prevent disabling fingerprints
        } catch(SsdkUnsupportedException e) {
            // Not supported, continue
        }

        // No fingerprint-enabled device...
        // Disable fingerprint authentication
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(Constants.FINGERPRINT_AUTHENTICATION_SETTING, false).commit();
        callback.onFinished(FingerprintResponses.NOT_SUPPORTED);
    }

    /**
     * Register a new fingerprint
     * @param context Application context
     * @param callback The callbacks
     */
    public static void register(Context context, FingerprintCallbacks callback) {
        register(context, callback, true);
    }

    private static void register(Context context, FingerprintCallbacks callback, boolean checkCompat) {
        if(checkCompat) {
            if (!isFingerPrintSupported(context)) {
                callback.onFinished(FingerprintResponses.NOT_SUPPORTED);
            }

            Spass spass = new Spass();
            try {
                // Samsung device
                spass.initialize(context);
                SpassFingerprint spassFingerprint = new SpassFingerprint(context);

                // Register
                spassFingerprint.registerFinger(context, () -> {
                    // Registration finihed
                    // Check for fingerprints again
                    if(!hasFingerPrints(context)) {
                        callback.onFinished(FingerprintResponses.DISABLED);
                    } else {
                        callback.onFinished(FingerprintResponses.SUCCEEDED);
                    }
                });
            } catch(SsdkUnsupportedException e) {}
        }
    }

    /**
     * Check if there is any supported fingerprint authentication possible on the device
     * @return if there is any fingerprint authentication possible
     */
    public static boolean isFingerPrintSupported(Context context) {
        /* Check for all supported SDK's if it's supported on the device */
        Spass spass = new Spass();
        try {
            spass.initialize(context);
            return true; // It worked
        } catch (SsdkUnsupportedException e) {
        }

        return false;
    }

    /**
     * Check if there are any registered fingerprints
     * @return if there are any registered fingerprints
     */
    public static boolean hasFingerPrints(Context context) {
        if(!isFingerPrintSupported(context)) return false; // No fingerprints supported, so no fingerprints registered

        Spass spass = new Spass();
        try {
            spass.initialize(context);
            return new SpassFingerprint(context).hasRegisteredFinger();
        } catch(SsdkUnsupportedException e) {}

        return false;
    }

    /**
     * Create a dialog which recommends the user to enable fingerprint authentication
     * @param context Application context
     */
    public static void createAdviceDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.fingerprint_authentication_dialog_title))
                .setMessage(context.getString(R.string.fingerprint_authentication_dialog_message))
                .setPositiveButton(context.getString(R.string.fingerprint_authentication_dialog_enable_button), (dialog, which) -> {
                    // The user wants to enable fingerprint recognition
                    // We have to check his fingerprint first
                    authenticate(context, (result) -> {
                        if(result == FingerprintResponses.SUCCEEDED) {
                            // It succeeded, enable fingerprints
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(Constants.FINGERPRINT_AUTHENTICATION_SETTING, true).commit();
                        } else {
                            // It failed, disable fingerprints
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(Constants.FINGERPRINT_AUTHENTICATION_SETTING, false).commit();
                        }
                    }, true);
                    dialog.dismiss();
                })
                .setNegativeButton(context.getString(R.string.fingerprint_authentication_dialog_cancel_button), (dialog, which) -> {
                    // Disable fingerprints
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(Constants.FINGERPRINT_AUTHENTICATION_SETTING, false).commit();
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static enum FingerprintResponses {
        SUCCEEDED, FAILED, DISABLED, NOT_SUPPORTED
    }

    public static interface FingerprintCallbacks {
        void onFinished(FingerprintResponses result);
    }
}
