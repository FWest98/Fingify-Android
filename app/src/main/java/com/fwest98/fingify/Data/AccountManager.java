package com.fwest98.fingify.Data;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.VolleyError;
import com.fwest98.fingify.CustomUI.ProgressDialog;
import com.fwest98.fingify.Helpers.ApiRequest;
import com.fwest98.fingify.Helpers.AsyncActionCallback;
import com.fwest98.fingify.Helpers.ExceptionHandler;
import com.fwest98.fingify.Helpers.HelperFunctions;
import com.fwest98.fingify.Helpers.HmacApiRequest;
import com.fwest98.fingify.Helpers.RequestQueue;
import com.fwest98.fingify.R;
import com.fwest98.fingify.Receivers.InternetConnectionManager;
import com.fwest98.fingify.Settings.Constants;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fj.data.Either;
import lombok.Getter;

public class AccountManager {
    @Getter private static boolean isSet;
    @Getter private static boolean isHandlingNewVersion;
    @Getter private static String username;
    @Getter private static String publicKey;
    @Getter private static String privateKey;

    private Context context;
    private static int currentVersion = 1;
    private static boolean isInitialized = false;

    private static AccountManager instance;

    private static final String loginInternetListenerName = "loginDialog";
    private static final String registerInternetListenerName = "registerDialog";

    //region Initialize

    /**
     * Initialize account class, load the values from the storage to make sure everything is up-to-date
     * @param context The context
     */
    public static void initialize(Context context) {
        initialize(context, true);
    }
    public static void initialize(Context context, boolean showUI) {
        initialize(context, showUI, false);
    }
    public static void initialize(Context context, boolean showUI, boolean force) {
        if(isInitialized && !force) return;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        currentVersion = 0;
        int oldVersion;

        try {
            currentVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Account", e.getMessage(), e);
        }

        if((publicKey = pref.getString("publicKey", null)) == null) {
            isSet = false;
        } else {
            isSet = true;
            username = pref.getString("username", null);
            privateKey = pref.getString("privateKey", null);
        }

        /**
         * New breaking releases todo:
         * - Increase AndroidManifest version number
         * - Define new version below
         * - Mark new version number in arrays.xml as breaking(!!!!)
         * - Implement a pref change for the version number
         */

        // Check versions. Default to current version if no previous version is found, in that case it is a fresh install
        if((oldVersion = pref.getInt("oldVersion", currentVersion)) != currentVersion && !isHandlingNewVersion) {
            // Update happened
            int[] breakingVersions = context.getResources().getIntArray(R.array.breaking_account_versions);
            for(int version : breakingVersions) {
                if(currentVersion >= version && oldVersion < version) { // breaking changes
                    switch(version) {
                        case 1: {
                            isSet = false;
                            ExceptionHandler.handleException(new Exception(context.getString(R.string.account_initialize_breaking_change)), context, true);
                            return;
                        }
                        case 3: {
                            Version3Migration(pref, showUI, context);
                        }
                    }
                }
            }
            if(!isHandlingNewVersion) // no breaking change, but just a normal new version
                pref.edit().putInt("oldVersion", currentVersion).commit();
        }

        if(!isHandlingNewVersion) isInitialized = true;
    }

    /**
     * Process received JSON from the API when logging in (store everything and set right values), also upload applications to server
     * @param json The response from the server as JSONObject
     * @throws JSONException Sometimes something goes wrong
     */
    private void processJSON(JSONObject json, Activity activity) throws JSONException {
        if(isSet()) logout(a -> {}); // Remove all applications and APIKey of old account

        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();

        publicKey = json.getString("publicKey");
        pref.putString("publicKey", publicKey);

        privateKey = json.getString("privateKey");
        pref.putString("privateKey", privateKey);

        username = json.getString("username");
        pref.putString("username", username);

        isSet = true;

        pref.putInt("oldVersion", currentVersion);

        pref.apply();

        ApplicationManager.addApplicationsToServer(ApplicationManager.getApplications(context), result -> {}, context);
        registerGCM(activity);
    }

    //endregion
    //region Constructors

    private AccountManager(Context context) {
        initialize(context, true);
        this.context = context;
    }

    private AccountManager(Context context, boolean showUI) {
        initialize(context, showUI);
        this.context = context;
    }

    public static AccountManager getInstance(Context context) { return getInstance(context, true); }

    /**
     * Get an account instance for the current context, if no instance exists, a new one will be created and initialized
     * @param context The current context
     * @return An account instance for your context
     */
    public static AccountManager getInstance(Context context, boolean showUI) {
        if(instance == null || !context.equals(instance.context)) {
            instance = new AccountManager(context, showUI);
        }
        return instance;
    }

    //endregion

    //region Login

    /**
     * Log in with username and password. Creates a dialog where you can fill in the login data. Has a "Register" button as well.
     * @param callback The callback function to execute when it's finished
     */
    public void login(AsyncActionCallback<Either<Throwable, JSONObject>> callback) {
        login(null, callback);
    }

    /**
     * Log in with username and password. Creates a dialog where you can fill in the login data. Has a "Register" button as well
     * @param activity The current activity that calls the login function
     * @param callback The callback function to execute when it's finished
     */
    public void login(Activity activity, AsyncActionCallback<Either<Throwable, JSONObject>> callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_login, null);

        // Tabs
        TabLayout tabLayout = (TabLayout) dialogView.findViewById(R.id.tabs);
        FrameLayout tabs = (FrameLayout) dialogView.findViewById(R.id.tab_wrapper);
        final TabLayout.Tab[] currentTab = { tabLayout.newTab().setText(R.string.dialog_login_tabs_userpass).setTag(R.id.dialog_login_tabs_userpass) };

        tabLayout.addTab(currentTab[0]);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getTag() == null || !(tab.getTag() instanceof Integer)) return;

                for(int i = 0; i < tabs.getChildCount(); i++) {
                    View child = tabs.getChildAt(i);
                    child.setVisibility(View.GONE);
                }

                dialogView.findViewById((int) tab.getTag()).setVisibility(View.VISIBLE);
                currentTab[0] = tab;
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        /* Dialog buttons */
        builder.setView(dialogView)
                .setPositiveButton(R.string.common_login, (dialogInterface, id) -> {}) // No callbacks here because
                .setNeutralButton(R.string.dialog_login_buttons_register, (dialogInterface, id) -> {}) // this will automatically
                .setNegativeButton(R.string.common_cancel, (dialogInterface, id) -> {}); // dismiss dialog after execution

        AlertDialog loginDialog = builder.create();
        loginDialog.show();

        // The real buttoncallbacks (without dismiss)
        loginDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v ->
                register(result -> {
                    loginDialog.dismiss(); callback.onCallback(result);
                })
        );
        loginDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(v -> {
            loginDialog.dismiss();
            callback.onCallback(Either.right(null));
        });
        loginDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                AlertDialog progressDialog = ProgressDialog.create(context.getString(R.string.account_login_progress_tile), context.getString(R.string.account_login_progress_desc), context);

                switch((int) currentTab[0].getTag()) {
                    case R.id.dialog_login_tabs_userpass: {
                        String username = ((EditText) dialogView.findViewById(R.id.dialog_login_userpass_username)).getText().toString();
                        String password = ((EditText) dialogView.findViewById(R.id.dialog_login_userpass_password)).getText().toString();

                        if ("".equals(username))
                            throw new Exception(context.getString(R.string.account_login_usernamerequired));
                        if ("".equals(password))
                            throw new Exception(context.getString(R.string.account_login_passwordrequired));

                        login(activity, username, password, null, result -> {
                            if(result.isRight()) {
                                loginDialog.dismiss();
                            }
                            progressDialog.dismiss();
                            callback.onCallback(result);
                        });

                        break;
                    }
                    default:
                        throw new Exception(context.getString(R.string.account_login_wrongtab));
                }
            } catch (Exception e) {
                ExceptionHandler.handleException(e, context, false);
            }
        });

        InternetConnectionManager.registerListener(loginInternetListenerName, hasInternetConnection -> {
            loginDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(hasInternetConnection);
            loginDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(hasInternetConnection);
        });
        loginDialog.setOnDismissListener(dialog -> InternetConnectionManager.unregisterListener(loginInternetListenerName));
    }

    /**
     * Perform the actual web request(s) to log in
     * @param username The username included in the QR-code URL or entered by the user
     * @param password The password entered by the user
     * @param OTP The current OTP code
     * @param callback The callback function to execute when it's finished
     */
    private void login(Activity activity,
                       String username,
                       String password,
                       String OTP,
                       AsyncActionCallback<Either<Throwable, JSONObject>> callback) {

        ApiRequest<JSONObject> loginRequest = new ApiRequest<JSONObject>(Method.POST, Constants.HTTP_BASE + "account/login", response -> {
            // Receives response string. Parse to JSON
            return new JSONObject(response);
        }, response -> {
            try {
                processJSON(response, activity);
                callback.onCallback(Either.right(response));
            } catch (JSONException e) {
                ExceptionHandler.handleException(new Exception(context.getString(R.string.apirequests_invalid_response), e), context, true);
            }
        }, error -> {
            ExceptionHandler.handleException(new Exception(context.getString(R.string.account_login_error_login) + ": " + error.getMessage(), error.getCause()), context, true);
            callback.onCallback(Either.left(error.getCause()));
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> baseMap = new HashMap<>();
                baseMap.put("username", username);
                baseMap.put("password", password);
                baseMap.put("TOTPcode", OTP);
                baseMap.put("platform", "Android");
                baseMap.put("deviceName", Build.BRAND + Build.MODEL);
                return baseMap;
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                if(volleyError.networkResponse == null) return volleyError;
                switch(volleyError.networkResponse.statusCode) {
                    case 401:
                        return new VolleyError(context.getString(R.string.account_login_unauthorized), volleyError);
                    case 400:
                        return new VolleyError(context.getString(R.string.apirequests_missing_data), volleyError);
                    case 500:
                        return new VolleyError(context.getString(R.string.apirequests_servererror), volleyError);
                    default:
                        return new VolleyError(context.getString(R.string.apirequests_unknown_error), volleyError);
                }
            }
        };

        RequestQueue.addToRequestQueue(loginRequest, context);
    }

    //endregion
    //region Register

    /**
     * Register a new user. Creates a dialog with the form
     * @param callback The callback function to execute when it's finished
     */
    public void register(AsyncActionCallback<Either<Throwable, JSONObject>> callback) {
        register(null, callback);
    }

    /**
     * Register a new user. Creates a dialog with the form
     * @param activity The current activity that calls the login function
     * @param callback The callback function to execute when it's finished
     */
    public void register(Activity activity, AsyncActionCallback<Either<Throwable, JSONObject>> callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_register, null);

        builder.setTitle(R.string.dialog_register_title);

        builder.setView(dialogView)
                .setPositiveButton(R.string.dialog_register_buttons_register, (dialog, which) -> {})
                .setNegativeButton(R.string.common_cancel, (dialog, which) -> {
                    dialog.dismiss();
                    callback.onCallback(Either.right(null));
                });

        AlertDialog registerDialog = builder.create();
        registerDialog.show();

        registerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            String username = ((EditText) dialogView.findViewById(R.id.dialog_register_username)).getText().toString();
            String password = ((EditText) dialogView.findViewById(R.id.dialog_register_password)).getText().toString();
            String repass = ((EditText) dialogView.findViewById(R.id.dialog_register_password_repeat)).getText().toString();
            String email = ((EditText) dialogView.findViewById(R.id.dialog_register_email)).getText().toString();

            try {
                AlertDialog progressDialog = ProgressDialog.create(context.getString(R.string.account_registration_progress_title), context.getString(R.string.account_registration_progress_desc), context);

                if("".equals(username)) throw new Exception(context.getString(R.string.account_registration_username_required));
                if("".equals(password)) throw new Exception(context.getString(R.string.account_registration_password_required));
                if(!password.equals(repass)) throw new Exception(context.getString(R.string.account_registration_passwords_equal));

                register(activity, username, password, email, result -> {
                    if (result.isRight()) {
                        registerDialog.dismiss();
                    }
                    progressDialog.dismiss();
                    callback.onCallback(result);
                });
            } catch (Exception e) {
                ExceptionHandler.handleException(e, context, false);
            }
        });

        InternetConnectionManager.registerListener(registerInternetListenerName,
                hasInternetConnection -> registerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(hasInternetConnection));
        registerDialog.setOnDismissListener(dialog -> InternetConnectionManager.unregisterListener(registerInternetListenerName));
    }


    /**
     * Perform the actual webrequest(s) to register
     * @param username The username
     * @param password The password entered by the user
     * @param email The emailaddress given by the user
     * @param callback The callbak function to execute when it's done
     */
    private void register(Activity activity, String username, String password, String email, AsyncActionCallback<Either<Throwable, JSONObject>> callback) {

        ApiRequest<JSONObject> registerRequest = new ApiRequest<JSONObject>(Method.POST, Constants.HTTP_BASE + "account/register", JSONObject::new, response -> {
            try {
                processJSON(response, activity);
                callback.onCallback(Either.right(response));
            } catch(JSONException e) {
                ExceptionHandler.handleException(new Exception(context.getString(R.string.apirequests_invalid_response), e), context, true);
            }
        }, error -> {
            ExceptionHandler.handleException(new Exception(context.getString(R.string.account_registration_error) + ": " + error.getMessage(), error.getCause()), context, true);
            callback.onCallback(Either.left(error.getCause()));
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> baseMap = super.getParams();
                baseMap.put("username", username);
                baseMap.put("password", password);
                baseMap.put("email", email);
                baseMap.put("platform", "Android");
                baseMap.put("deviceName", Build.BRAND + Build.MODEL);
                return baseMap;
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                if(volleyError.networkResponse == null) return volleyError;
                switch(volleyError.networkResponse.statusCode) {
                    case 400:
                        return new VolleyError(context.getString(R.string.apirequests_missing_data), volleyError);
                    case 409:
                        return new VolleyError(context.getString(R.string.account_registration_conflict), volleyError);
                    case 500:
                        return new VolleyError(context.getString(R.string.apirequests_servererror), volleyError);
                    default:
                        return new VolleyError(context.getString(R.string.apirequests_unknown_error), volleyError);
                }
            }
        };

        RequestQueue.addToRequestQueue(registerRequest, context);
    }

    //endregion
    //region Logout

    private void logout(AsyncActionCallback<Either<Throwable, JSONObject>> callback) {
        HmacApiRequest<Either<Throwable, JSONObject>> logoutRequest = new HmacApiRequest<Either<Throwable, JSONObject>>(
                Method.POST,
                Constants.HTTP_BASE + "account/logout",
                a -> Either.right(null),
                either -> callback.onCallback(either),
                error -> {
                    ExceptionHandler.handleException(
                            new Exception(context.getString(R.string.account_logout_error) + ": " + error.getMessage(), error.getCause()), context, true
                    );
                    callback.onCallback(Either.left(error.getCause()));
                }, context, privateKey, publicKey)
        {
            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                //noinspection ThrowableResultOfMethodCallIgnored
                VolleyError baseHandling = super.parseNetworkError(volleyError);

                if(baseHandling != volleyError) { // error not handled
                    return baseHandling;
                }

                if(volleyError.networkResponse == null) return baseHandling;
                return new VolleyError(context.getString(R.string.apirequests_unknown_error), volleyError);
            }
        };

        RequestQueue.addToRequestQueue(logoutRequest, context);
    }

    //endregion
    //region GCM

    private void registerGCM() { registerGCM(null); }

    private void registerGCM(Activity activity) {
        if(activity == null) {
            if(!HelperFunctions.checkPlayServices(context)) return;
        } else {
            if(!HelperFunctions.checkPlayServices(activity)) return;
        }

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

        HmacApiRequest<Object> registerGCMRequest = new HmacApiRequest<Object>(
                Method.POST,
                Constants.HTTP_BASE + "account/gcm",
                a -> null,
                either -> {},
                error -> ExceptionHandler.handleException(
                        new Exception(context.getString(R.string.account_gcmreg_error) + ": " + error.getMessage(), error.getCause()), context, true
                ), context)
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> baseMap = super.getParams();
                try {
                    // Very weird but EMPTY NAME is required!!
                    baseMap.put("", gcm.register(Constants.PLAY_SENDER_ID));
                } catch (IOException e) {
                    deliverError(new VolleyError(context.getString(R.string.account_gcmreg_playservices), e));
                }
                return baseMap;
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                //noinspection ThrowableResultOfMethodCallIgnored
                VolleyError baseHandling = super.parseNetworkError(volleyError);

                if(baseHandling != volleyError) return baseHandling;
                if(volleyError.networkResponse == null) return baseHandling;

                switch(volleyError.networkResponse.statusCode) {
                    case 400:
                        return new VolleyError(context.getString(R.string.apirequests_missing_data), volleyError);
                    default:
                        return new VolleyError(context.getString(R.string.apirequests_unknown_error), volleyError);
                }
            }
        };

        RequestQueue.addToRequestQueue(registerGCMRequest, context);
    }

    //endregion
    //region Migrations

    private static void Version3Migration(SharedPreferences pref, boolean showUI, Context context) {
        if(pref.getString("key", null) == null) return; // then this is no breaking change

        isHandlingNewVersion = true;
        if(!HelperFunctions.hasInternetConnection(context)) {
            // Show error dialog if wanted
            if(!showUI) return;
            new AlertDialog.Builder(context)
                    .setTitle(R.string.account_migration_error)
                    .setMessage(context.getString(R.string.account_migration_error_detail) + ": " + context.getString(R.string.apirequests_no_internet_connection))
                    .show();
            return;
        }

        final AlertDialog progressDialog;
        if(showUI) {
            progressDialog = ProgressDialog.create(context.getString(R.string.account_migration_progress_title), context.getString(R.string.account_migration_progress_desc), context);
            progressDialog.show();
        } else progressDialog = null;

        class MigrationConflict extends VolleyError {
            public MigrationConflict(String exceptionMessage, Throwable reason) {
                super(exceptionMessage, reason);
            }
        }

        ApiRequest<JSONObject> migrateApiRequest = new ApiRequest<JSONObject>(
                Method.PATCH,
                Constants.HTTP_BASE + "account/upgrade",
                JSONObject::new,
                result -> {
                    if(showUI) progressDialog.dismiss();
                    try {
                        publicKey = result.getString("publicKey");
                        privateKey = result.getString("privateKey");
                        username = result.getString("username");
                        isSet = true;
                        isInitialized = true;

                        pref.edit()
                                .remove("apiKey")
                                .putString("publicKey", publicKey)
                                .putString("privateKey", privateKey)
                                .putString("username", username)
                                .putInt("oldVersion", currentVersion)
                                .apply();

                        isHandlingNewVersion = false;
                    } catch (JSONException e) {
                        if(showUI)
                            ExceptionHandler.handleException(new Exception(context.getString(R.string.account_migration_error_detail) + ": " + context.getString(R.string.apirequests_invalid_response), e), context, true);
                        else
                            Log.e("MIGRATION", e.getMessage(), e);
                    }
                },
                error -> {
                    if(showUI) {
                        if(error instanceof MigrationConflict) {
                            ExceptionHandler.handleException(new Exception(context.getString(R.string.account_migration_error_relogin), error.getCause()), context, true);
                        } else {
                            ExceptionHandler.handleException(new Exception(context.getString(R.string.account_migration_error_detail) + ": " + error.getMessage(), error.getCause()), context, true);
                        }
                        progressDialog.dismiss();
                    } else {
                        Log.e("MIGRATION", error.getMessage(), error);
                    }
                    if(error instanceof MigrationConflict) {
                        isSet = false;
                        isHandlingNewVersion = false;
                        isInitialized = true;

                        pref.edit()
                                .remove("apiKey")
                                .remove("username")
                                .putInt("oldVersion", currentVersion)
                                .apply();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> baseMap = super.getParams();
                baseMap.put("username", pref.getString("username", ""));
                baseMap.put("apiKey", pref.getString("key", ""));
                baseMap.put("platform", "Android");
                baseMap.put("deviceName", Build.BRAND + Build.MODEL);
                return baseMap;
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                if(volleyError.networkResponse == null) return volleyError;
                switch (volleyError.networkResponse.statusCode) {
                    case 401:
                        return new VolleyError(context.getString(R.string.apirequests_unauthorized), volleyError);
                    case 400:
                        return new VolleyError(context.getString(R.string.apirequests_missing_data), volleyError);
                    case 409:
                        return new MigrationConflict("Already migrated!", volleyError);
                    case 500:
                        return new VolleyError(context.getString(R.string.apirequests_servererror), volleyError);
                    default:
                        return new VolleyError(context.getString(R.string.apirequests_unknown_error), volleyError);
                }
            }
        };

        RequestQueue.addToRequestQueue(migrateApiRequest, context);
    }

    //endregion
}
