package com.fwest98.fingify.Data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fwest98.fingify.Helpers.ExceptionHandler;
import com.fwest98.fingify.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import lombok.Getter;

public class Account {
    @Getter private static boolean isSet;
    @Getter private static String username;
    @Getter private static String apiKey;

    private Context context;
    private WebRequestCallbacks callbacks;
    private static int currentVersion = 1;

    private static Account instance;

    //region Initialize

    public static void initialize(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        int oldVersion;
        try {
            currentVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Account", e.getMessage(), e);
        }

        // Check versions
        if((oldVersion = pref.getInt("oldVersion", currentVersion)) != currentVersion) {
            // Update happened
            int[] breakingVersions = context.getResources().getIntArray(R.array.breaking_account_versions);
            for(int version : breakingVersions) {
                if(currentVersion >= version && oldVersion < version) { // breaking changes
                    isSet = false;
                    ExceptionHandler.handleException(new Exception("Wijzigingen in de app vereisen opnieuw inloggen"), context, true);
                    return;
                }
            }
        }

        String key;
        if((key = pref.getString("key", null)) == null) {
            isSet = false;
            return;
        }
        isSet = true;
        username = pref.getString("username", null);
        apiKey = key;
    }

    private void processJSON(String JSON) throws JSONException {
        JSONObject base = new JSONObject(JSON);
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();

        apiKey = base.getString("key");
        pref.putString("key", apiKey);

        username = base.getString("username");
        pref.putString("username", username);

        isSet = true;

        pref.putInt("oldVersion", currentVersion);

        pref.commit();
    }

    //endregion
    //region Constructors

    private Account(Context context) {
        initialize(context);
        this.context = context;
    }

    public static Account getInstance(Context context) {
        if(instance == null || !context.equals(instance.context)) {
            instance = new Account(context);
        }
        return instance;
    }

    //endregion


    //region WebActions

    private class WebActions extends AsyncTask<Account.WebRequestCallbacks, Exception, Object> {

    }

    //endregion
    //region Interfaces

    public interface WebRequestCallbacks {
        public HttpResponse onRequestCreate(HttpClient client) throws Exception;

    }

    //endregion
}
