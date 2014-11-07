package com.fwest98.fingify.Data;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TabHost;

import com.fwest98.fingify.Helpers.ExceptionHandler;
import com.fwest98.fingify.Helpers.ExtendedClock;
import com.fwest98.fingify.Helpers.ExtendedTotp;
import com.fwest98.fingify.Helpers.HelperFunctions;
import com.fwest98.fingify.R;
import com.fwest98.fingify.Settings.Constants;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import fj.data.HashMap;
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

    /**
     * Initialize account class, load the values from the storage to make sure everything is up-to-date
     * @param context The context
     */
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
                    ExceptionHandler.handleException(new Exception(context.getString(R.string.account_initialize_breaking_change)), context, true);
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

    /**
     * Process received JSON from the API when logging in (store everything and set right values), also upload applications to server
     * @param JSON The response from the server as string
     * @throws JSONException Sometimes something goes wrong
     */
    private void processLogin(String JSON) throws JSONException {
        JSONObject base = new JSONObject(JSON);
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();

        apiKey = base.getString("key");
        pref.putString("key", apiKey);

        username = base.getString("username");
        pref.putString("username", username);

        isSet = true;

        pref.putInt("oldVersion", currentVersion);

        pref.apply();

        setApplications(Application.getApplications(context), result -> {});
    }

    //endregion
    //region Constructors

    private Account(Context context) {
        initialize(context);
        this.context = context;
    }

    /**
     * Get an account instance for the current context, if no instance exists, a new one will be created and initialized
     * @param context The current context
     * @return An account instance for your context
     */
    public static Account getInstance(Context context) {
        if(instance == null || !context.equals(instance.context)) {
            instance = new Account(context);
        }
        return instance;
    }

    //endregion

    //region Login

    /**
     * Log in with username and password. Creates a dialog where you can fill in the login data. Has a "Register" button as well.
     * @param successCallback The callback function to execute when it's finished
     */
    public void login(AsyncActionCallback successCallback) {
        login(successCallback, ex -> {});
    }

    /**
     * Log in with username and password. Creates a dialog where you can fill in the login data. Has a "Register" button as well
     * @param successCallback The callback function to execute when it's finished
     * @param errorCallback The callback function to execute when something's gone horribly wrong or the user just cancelled
     */
    public void login(AsyncActionCallback successCallback, AsyncActionCallback errorCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_login, null);
        TabHost tabHost = (TabHost) dialogView.findViewById(R.id.dialog_login_tabhost);

        // Setup the TabHost
        tabHost.setup();

        TabHost.TabSpec userpassTab = tabHost.newTabSpec("userpassTab");
        userpassTab.setContent(R.id.dialog_login_tabs_userpass);
        userpassTab.setIndicator(context.getString(R.string.dialog_login_tabs_userpass));
        tabHost.addTab(userpassTab);

        //TabHost.TabSpec socialTab = tabHost.newTabSpec("socialTab");
        //socialTab.setContent(R.id.dialog_login_tabs_social);
        //socialTab.setIndicator(context.getString(R.string.dialog_login_tabs_social));
        //tabHost.addTab(socialTab);

        // Create dialog
        builder.setView(dialogView)
                .setPositiveButton(R.string.dialog_login_buttons_login, (dialogInterface, id) -> {}) // No callbacks here because
                .setNeutralButton(R.string.dialog_login_buttons_register, (dialogInterface, id) -> {}) // this will automatically
                .setNegativeButton(R.string.common_cancel, (dialogInterface, id) -> {}); // dismiss dialog after execution

        AlertDialog loginDialog = builder.create();
        loginDialog.show();

        // The real buttoncallbacks (without dismiss)
        loginDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v -> register(result -> { loginDialog.dismiss(); successCallback.onFinished(result); })); //TODO Add register
        loginDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(v -> {
            loginDialog.dismiss();
            try {
                errorCallback.onFinished(null);
            } catch(Exception ignored) {}
        });
        loginDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Check for tab
            if(tabHost.getCurrentTab() == 1) { // social tab
                ExceptionHandler.handleException(new Exception(context.getString(R.string.account_login_wrongtab)), context, false);
            }

            String username = ((EditText) dialogView.findViewById(R.id.dialog_login_userpass_username)).getText().toString();
            String password = ((EditText) dialogView.findViewById(R.id.dialog_login_userpass_password)).getText().toString();

            try {
                if("".equals(username)) throw new Exception(context.getString(R.string.account_login_usernamerequired));
                if("".equals(password)) throw new Exception(context.getString(R.string.account_login_passwordrequired));

                login(username, password, null, result -> {
                    loginDialog.dismiss();
                    successCallback.onFinished(result);
                });
            } catch (Exception e) {
                ExceptionHandler.handleException(e, context, false);
            }
        });
    }

    /**
     * Log in with a processed QR-code from the fingify website
     * @param OTP The current OTP code
     * @param username The username included in the QR-code URL
     * @param successCallback The callback function to execute when it's finished
     */
    public void login(String OTP, String username, AsyncActionCallback successCallback) {
        login(OTP, username, null, successCallback);
    }

    /**
     * Perform the actual web request(s) to log in
     * @param username The username included in the QR-code URL or entered by the user
     * @param password The password entered by the user
     * @param OTP The current OTP code
     * @param successCallback The callback function to execute when it's finished
     */
    private void login(String username, String password, String OTP, AsyncActionCallback successCallback) {
        WebRequestCallbacks webRequestCallbacks = new WebRequestCallbacks() {
            @Override
            public HttpURLConnection onCreateConnection() throws Exception {
                List<NameValuePair> postParameters = new ArrayList<>();
                postParameters.add(new BasicNameValuePair("username", username));
                postParameters.add(new BasicNameValuePair("password", password));
                postParameters.add(new BasicNameValuePair("TOTPcode", OTP));
                UrlEncodedFormEntity data = new UrlEncodedFormEntity(postParameters);

                URL url = new URL(Constants.HTTP_BASE + "account/login");
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode((int) data.getContentLength());

                data.writeTo(connection.getOutputStream());

                return connection;
            }

            @Override
            public String onValidateResponse(String content, int statusCode) throws Exception {
                switch(statusCode) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        throw new Exception(context.getString(R.string.account_login_unauthorized));
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        throw new Exception(context.getString(R.string.account_webactions_missing_data));
                    case HttpURLConnection.HTTP_INTERNAL_ERROR:
                        throw new Exception(context.getString(R.string.account_webactions_servererror));
                    case HttpURLConnection.HTTP_OK:
                        if("".equals(content)) throw new Exception(context.getString(R.string.account_webactions_unknown_error));
                        return content;
                    default:
                        throw new Exception(context.getString(R.string.account_webactions_unknown_error));
                }
            }

            @Override
            public void onProcessData(String data) {
                try {
                    processLogin(data);
                    successCallback.onFinished(data);
                } catch (JSONException e) {
                    ExceptionHandler.handleException(new Exception(context.getString(R.string.account_login_error_login), e), context, true);
                }
            }

            @Override
            public void onError(Exception exception) {
                ExceptionHandler.handleException(new Exception(context.getString(R.string.account_login_error_login) + ": " + exception.getMessage(), exception), context, true);
            }
        };

        new WebActions().execute(webRequestCallbacks);
    }

    //endregion
    //region Register

    /**
     * Register a new user. Creates a dialog with the form
     * @param successCallback The callback function to execute when it's finished
     */
    public void register(AsyncActionCallback successCallback) {
        register(successCallback, result -> {});
    }

    /**
     * Register a new user. Creates a dialog with the form
     * @param successCallback The callback function to execute when it's finished
     * @param errorCallback The callback function to execute when something's gone horribly wrong or the user just cancelled
     */
    public void register(AsyncActionCallback successCallback, AsyncActionCallback errorCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_register, null);

        builder.setTitle(R.string.dialog_register_title);

        builder.setView(dialogView)
                .setPositiveButton(R.string.dialog_register_buttons_register, (dialog, which) -> {})
                .setNegativeButton(R.string.common_cancel, (dialog, which) -> {
                    dialog.dismiss();
                    errorCallback.onFinished(null);
                });

        AlertDialog registerDialog = builder.create();
        registerDialog.show();
        registerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            String username = ((EditText) dialogView.findViewById(R.id.dialog_register_username)).getText().toString();
            String password = ((EditText) dialogView.findViewById(R.id.dialog_register_password)).getText().toString();
            String repass = ((EditText) dialogView.findViewById(R.id.dialog_register_password_repeat)).getText().toString();
            String email = ((EditText) dialogView.findViewById(R.id.dialog_register_email)).getText().toString();

            try {
                if("".equals(username)) throw new Exception(context.getString(R.string.account_registration_username_required));
                if("".equals(password)) throw new Exception(context.getString(R.string.account_registration_password_required));
                if(!password.equals(repass)) throw new Exception(context.getString(R.string.account_registration_passwords_equal));

                register(username, password, email, result -> {
                    registerDialog.dismiss();
                    successCallback.onFinished(result);
                });
            } catch (Exception e) {
                ExceptionHandler.handleException(e, context, false);
            }
        });
    }


    /**
     * Perform the actual webrequest(s) to register
     * @param username The username
     * @param password The password entered by the user
     * @param email The emailaddress given by the user
     * @param callback The callbak function to execute when it's done
     */
    private void register(String username, String password, String email, AsyncActionCallback callback) {
        WebRequestCallbacks webRequestCallbacks = new WebRequestCallbacks() {
            @Override
            public HttpURLConnection onCreateConnection() throws Exception {
                List<NameValuePair> postParameters = new ArrayList<>();
                postParameters.add(new BasicNameValuePair("username", username));
                postParameters.add(new BasicNameValuePair("password", password));
                postParameters.add(new BasicNameValuePair("email", email));
                postParameters.add(new BasicNameValuePair("isApp", "true"));
                UrlEncodedFormEntity data = new UrlEncodedFormEntity(postParameters);

                URL url = new URL(Constants.HTTP_BASE + "account/register");
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode((int) data.getContentLength());

                data.writeTo(connection.getOutputStream());

                return connection;
            }

            @Override
            public String onValidateResponse(String content, int statusCode) throws Exception {
                switch(statusCode) {
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        throw new Exception(context.getString(R.string.account_webactions_missing_data));
                    case HttpURLConnection.HTTP_CONFLICT:
                        throw new Exception(context.getString(R.string.account_registration_conflict));
                    case HttpURLConnection.HTTP_INTERNAL_ERROR:
                        throw new Exception(context.getString(R.string.account_webactions_servererror));
                    case HttpURLConnection.HTTP_OK:
                        if("".equals(content)) throw new Exception(context.getString(R.string.account_webactions_unknown_error));
                        return content;
                    default:
                        throw new Exception(context.getString(R.string.account_webactions_unknown_error));
                }
            }

            @Override
            public void onProcessData(String data) {
                try {
                    processLogin(data);
                    callback.onFinished(data);
                } catch(Exception e) {
                    ExceptionHandler.handleException(new Exception(context.getString(R.string.account_registration_error), e), context, true);
                }
            }

            @Override
            public void onError(Exception exception) {
                ExceptionHandler.handleException(new Exception(context.getString(R.string.account_registration_error) + ": " + exception.getMessage(), exception), context, true);
            }
        };

        new WebActions().execute(webRequestCallbacks);
    }

    //endregion
    //region Applications

    /**
     * Add applications to your account
     * @param applications The list of applications to add
     * @param callback The callbak function to execute when it's done
     */
    public void setApplications(List<Application> applications, AsyncActionCallback callback) {
        if(!isSet()) return;
        if(applications == null || applications.size() == 0) {
            callback.onFinished(null);
            return;
        }

        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("key", apiKey));
        for(int i = 0; i < applications.size(); i++) {
            postParameters.add(new BasicNameValuePair("applications["+i+"]", applications.get(i).getLabel()));
        }

        applications(postParameters, "POST", callback);
    }

    /**
     * Update applications in your account
     * @param applicationList HashMap of Applications, Key = old application, Value = new application
     * @param callback The callbak function to execute when it's done
     */
    public void updateApplications(java.util.HashMap<Application, Application> applicationList, AsyncActionCallback callback) {
        if(!isSet()) return;
        if(applicationList == null || applicationList.size() == 0) {
            callback.onFinished(null);
            return;
        }

        HashMap<Application, Application> applications = new HashMap<>(applicationList);

        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("key", apiKey));
        for(int i = 0; i < applications.size(); i++) {
            postParameters.add(new BasicNameValuePair("applications["+i+"].Key", applications.keys().index(i).getLabel()));
            postParameters.add(new BasicNameValuePair("applications["+i+"].Value", applications.values().index(i).getLabel()));
        }

        applications(postParameters, "PUT", callback);
    }

    /**
     * Remove applications from your account
     * @param applications The list of applications to remove
     * @param callback The callbak function to execute when it's done
     */
    public void removeApplications(List<Application> applications, AsyncActionCallback callback) {
        if(!isSet()) return;
        if(applications == null || applications.size() == 0) {
            callback.onFinished(null);
            return;
        }

        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("key", apiKey));
        for(int i = 0; i < applications.size(); i++) {
            postParameters.add(new BasicNameValuePair("applications["+i+"]", applications.get(i).getLabel()));
        }

        applications(postParameters, "DELETE", callback);
    }

    /**
     * The actual web things
     * @param postParameters The data to send in the body
     * @param requestMehod The HTTP method
     * @param callback The callbak function to execute when it's done
     */
    private void applications(List<NameValuePair> postParameters, String requestMehod, AsyncActionCallback callback) {
        if(!isSet()) return;
        if(postParameters == null || postParameters.size() == 0) {
            callback.onFinished(null);
            return;
        }

        WebRequestCallbacks webRequestCallbacks = new WebRequestCallbacks() {
            @Override
            public HttpURLConnection onCreateConnection() throws Exception {
                UrlEncodedFormEntity data = new UrlEncodedFormEntity(postParameters);

                URL url = new URL(Constants.HTTP_BASE + "account/applications");
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                connection.setRequestMethod(requestMehod);
                connection.setFixedLengthStreamingMode((int) data.getContentLength());

                data.writeTo(connection.getOutputStream());

                return connection;
            }

            @Override
            public String onValidateResponse(String content, int statusCode) throws Exception {
                switch(statusCode) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        throw new Exception(context.getString(R.string.account_webactions_unauthorized));
                    case HttpURLConnection.HTTP_INTERNAL_ERROR:
                        throw new Exception(context.getString(R.string.account_webactions_servererror));
                    case HttpURLConnection.HTTP_OK:
                        return content;
                    default:
                        throw new Exception(context.getString(R.string.account_webactions_unknown_error));
                }
            }

            @Override
            public void onProcessData(String data) {
                callback.onFinished(data);
            }

            @Override
            public void onError(Exception exception) {
                ExceptionHandler.handleException(new Exception(context.getString(R.string.account_applications_error) + ": " + exception.getMessage()), context, true);
            }
        };

        new WebActions().execute(webRequestCallbacks);
    }

    //endregion
    //region Requests

    /**
     * Get the list of requests from web or local storage
     * @param successCallback The callback function to execute when it's finished
     */
    public void getRequests(AsyncActionCallback successCallback) {
        getRequests(successCallback, ex -> {});
    }

    /**
     * Get the list of requests from web or local storage
     * @param successCallback The callback function to execute when it's finished
     * @param errorCallback The callback function to execute when something's gone horribly wrong
     */
    public void getRequests(AsyncActionCallback successCallback, AsyncActionCallback errorCallback) {
        if (HelperFunctions.hasInternetConnection(context) && isSet()) {
            getRequestsFromWeb(data -> {
                List<Request> requests = (List<Request>) data;
                Request.removeAllRequests(context);
                Request.addRequests(requests, context);

                successCallback.onFinished(requests);
            }, errorCallback);
        } else {
            ArrayList<Request> requests = Request.getRequests(context);
            if(requests == null || requests.size() == 0) {
                errorCallback.onFinished(null);
            } else {
                successCallback.onFinished(requests);
            }
        }
    }

    /**
     * Get the list of requests from the web
     * @param successCallback The callback function to execute when it's finished
     * @param errorCallback The callback function to execute when something's gone horribly wrong
     */
    private void getRequestsFromWeb(AsyncActionCallback successCallback, AsyncActionCallback errorCallback) {
        if(!isSet()) return;

        WebRequestCallbacks webRequestCallbacks = new WebRequestCallbacks() {
            @Override
            public HttpURLConnection onCreateConnection() throws Exception {
                URL url = new URL(Constants.HTTP_BASE + "account/requests?key=" + getApiKey());

                return (HttpsURLConnection) url.openConnection();
            }

            @Override
            public String onValidateResponse(String content, int statusCode) throws Exception {
                switch (statusCode) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        throw new Exception(context.getString(R.string.account_webactions_unauthorized));
                    case HttpURLConnection.HTTP_INTERNAL_ERROR:
                        throw new Exception(context.getString(R.string.account_webactions_servererror));
                    case HttpURLConnection.HTTP_OK:
                        return content;
                    default:
                        throw new Exception(context.getString(R.string.account_webactions_unknown_error));
                }
            }

            @Override
            public void onProcessData(String data) {
                try {
                    SimpleDateFormat webFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    ArrayList<Request> requests = new ArrayList<>();

                    JSONArray base = new JSONArray(data);
                    for(int i = 0; i < base.length(); i++) {
                        JSONObject JSONRequest = base.getJSONObject(i);
                        Date requestTime;

                        try {
                            requestTime = webFormat.parse(JSONRequest.getString("requestTime"));
                        } catch (ParseException e) {
                            requestTime = Calendar.getInstance().getTime();
                        }

                        Request request = new Request(
                                JSONRequest.getString("applicationName"),
                                requestTime,
                                JSONRequest.getBoolean("isDone"),
                                JSONRequest.getBoolean("isLocal"),
                                JSONRequest.getBoolean("isAccepted")
                        );
                        requests.add(request);
                    }

                    successCallback.onFinished(requests);
                } catch (JSONException e) {
                    ExceptionHandler.handleException(new Exception("Couldn't process response", e), context, true);

                    errorCallback.onFinished(e);
                }
            }

            @Override
            public void onError(Exception exception) {
                ExceptionHandler.handleException(new Exception(context.getString(R.string.account_applications_error) + ": " + exception.getMessage()), context, true);
                errorCallback.onFinished(exception);
            }
        };

        new WebActions().execute(webRequestCallbacks);
    }

    public void handleRequest(boolean accept, Request request, AsyncActionCallback successCallback, AsyncActionCallback errorCallback) {
        if(!isSet()) return;
        if(request == null || request.isAnswered()) return;
        if(!Application.labelExists(request.getApplicationName(), context)) return;

        Application application = Application.getApplication(request.getApplicationName(), context);
        ExtendedTotp totp = new ExtendedTotp(application.getSecret(), new ExtendedClock());

        WebRequestCallbacks webRequestCallbacks = new WebRequestCallbacks() {
            @Override
            public HttpURLConnection onCreateConnection() throws Exception {
                URL url = new URL(Constants.HTTP_BASE + "request");
                List<NameValuePair> parameters = new ArrayList<>();
                parameters.add(new BasicNameValuePair("apiKey", getApiKey()));
                parameters.add(new BasicNameValuePair("code", accept ?totp.now() : "0"));
                parameters.add(new BasicNameValuePair("label", request.getApplicationName()));

                UrlEncodedFormEntity data = new UrlEncodedFormEntity(parameters);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode((int) data.getContentLength());

                data.writeTo(connection.getOutputStream());

                return connection;
            }

            @Override
            public String onValidateResponse(String content, int statusCode) throws Exception {
                switch(statusCode) {
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        throw new Exception(context.getString(R.string.account_webactions_missing_data));
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        throw new Exception(context.getString(R.string.account_webactions_unauthorized));
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        throw new Exception(context.getString(R.string.account_requests_notfound));
                    case HttpURLConnection.HTTP_INTERNAL_ERROR:
                        throw new Exception(context.getString(R.string.account_webactions_servererror));
                    case HttpURLConnection.HTTP_OK:
                        return content;
                    default:
                        throw new Exception(context.getString(R.string.account_webactions_unknown_error));
                }
            }

            @Override
            public void onProcessData(String data) {
                successCallback.onFinished(data);
            }

            @Override
            public void onError(Exception exception) {
                ExceptionHandler.handleException(new Exception(context.getString(R.string.account_requests_error) + ": " + exception.getMessage(), exception), context, true);
                errorCallback.onFinished(exception);
            }
        };

        new WebActions().execute(webRequestCallbacks);
    }

    //endregion

    //region WebActions

    private class WebActions extends AsyncTask<Account.WebRequestCallbacks, Exception, String> {

        @Override
        protected String doInBackground(WebRequestCallbacks... params) {
            callbacks = params[0];
            if(!HelperFunctions.hasInternetConnection(context)) {
                publishProgress(new Exception(context.getString(R.string.account_webactions_no_internet_connection)));
                return null;
            }
            try {
                HttpURLConnection connection = callbacks.onCreateConnection();

                String content = "";
                try {
                    Scanner scanner = new Scanner(connection.getInputStream());
                    while (scanner.hasNext()) {
                        content += scanner.nextLine();
                    }
                } catch(Exception e) {
                    // IO Exception, error
                } finally {
                    connection.disconnect();
                }

                return callbacks.onValidateResponse(content, connection.getResponseCode());
            } catch (Exception e) {
                publishProgress(e);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Exception... values) {
            callbacks.onError(values[0]);
            cancel(true);
        }

        @Override
        protected void onPostExecute(String s) {
            callbacks.onProcessData(s);
        }
    }

    //endregion
    //region Interfaces

    public interface WebRequestCallbacks {
        public HttpURLConnection onCreateConnection() throws Exception;
        public String onValidateResponse(String content, int statusCode) throws Exception;
        public void onProcessData(String data);
        public void onError(Exception exception);
    }

    public interface AsyncActionCallback {
        public void onFinished(Object data);
    }

    //endregion
}
