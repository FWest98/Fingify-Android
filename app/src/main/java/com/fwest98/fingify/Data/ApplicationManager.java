package com.fwest98.fingify.Data;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.fwest98.fingify.Database.DatabaseHelper;
import com.fwest98.fingify.Database.DatabaseManager;
import com.fwest98.fingify.Helpers.AsyncActionCallback;
import com.fwest98.fingify.Helpers.ExceptionHandler;
import com.fwest98.fingify.Helpers.HmacApiRequest;
import com.fwest98.fingify.Helpers.RequestQueue;
import com.fwest98.fingify.Models.Application;
import com.fwest98.fingify.R;
import com.fwest98.fingify.Settings.Constants;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fj.data.Either;

public class ApplicationManager {
    /* Database */

    public static ArrayList<Application> getApplications(Context context) {
        DatabaseHelper helper = DatabaseManager.getHelper(context);

        try {
            Dao<Application, ?> dao = helper.getDaoWithCache(Application.class);

            List<Application> result = dao.queryForAll();
            return new ArrayList<>(result);
        } catch (SQLException e) {
            ExceptionHandler.handleException(new Exception(context.getString(R.string.database_applications_load_error), e), context, true);
            return new ArrayList<>();
        }
    }

    public static Application getApplication(String label, Context context) {
        if(!labelExists(label, context)) return null;

        DatabaseHelper helper = DatabaseManager.getHelper(context);
        try {
            Dao<Application, ?> dao = helper.getDaoWithCache(Application.class);

            List<Application> result = dao.queryForEq("label", label);
            if(result.size() == 0) return null;
            return result.get(0);
        } catch (SQLException e) {
            ExceptionHandler.handleException(new Exception(context.getString(R.string.database_applications_load_error), e), context, true);
            return null;
        }
    }

    public static void addApplication(Application application, Context context) throws DuplicateApplicationException {
        addApplications(Collections.singletonList(application), context);
    }

    public static void addApplications(List<Application> applications, Context context) throws DuplicateApplicationException {
        for(Application application : applications) {
            if (secretExists(application.getSecret(), context) || labelExists(application.getLabel(), context)) {
                // This already exists
                throw new DuplicateApplicationException(context.getString(R.string.application_duplicate));
            }
        }

        DatabaseHelper helper = DatabaseManager.getHelper(context);
        try {
            Dao<Application, ?> dao = helper.getDaoWithCache(Application.class);

            for(Application application : applications) {
                dao.create(application);
            }
        } catch(SQLException e) {
            ExceptionHandler.handleException(new Exception(context.getString(R.string.database_applications_save_error), e), context, true);
            return;
        }

        // Add to internet
        addApplicationsToServer(applications, s -> {}, context);
    }

    public static void updateApplication(Application oldApplication, Application newApplication, Context context) {
        HashMap<Application, Application> applicationMap = new HashMap<>(1);
        applicationMap.put(oldApplication, newApplication);

        updateApplications(applicationMap, context);
    }

    public static void updateApplications(HashMap<Application, Application> applications, Context context) {
        DatabaseHelper helper = DatabaseManager.getHelper(context);
        try {
            Dao<Application, ?> dao = helper.getDaoWithCache(Application.class);

            for(Map.Entry<Application, Application> entry : applications.entrySet()) {
                DeleteBuilder<Application, ?> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where().eq("secret", entry.getKey().getSecret());
                deleteBuilder.delete();

                dao.create(entry.getValue());
            }
        } catch (SQLException e) {
            ExceptionHandler.handleException(new Exception(context.getString(R.string.database_applications_save_error), e), context, true);
            return;
        }

        // Propagate to server
        updateApplicationsToServer(applications, s -> {}, context);
    }

    public static boolean secretExists(String secret, Context context) {
        DatabaseHelper helper = DatabaseManager.getHelper(context);
        try {
            Dao<Application, ?> dao = helper.getDaoWithCache(Application.class);
            List<Application> results = dao.queryForEq("secret", secret);
            return results.size() > 0;
        } catch (SQLException e) {
            Log.e("ERROR", context.getString(R.string.database_applications_secrets_nocheck), e);
            return false;
        }
    }

    public static boolean labelExists(String label, Context context) {
        DatabaseHelper helper = DatabaseManager.getHelper(context);
        try {
            Dao<Application, ?> dao = helper.getDaoWithCache(Application.class);
            List<Application> results = dao.queryForEq("label", label);
            return results.size() > 0;
        } catch(SQLException e) {
            Log.e("ERROR", context.getString(R.string.database_applications_labels_nocheck), e);
            return false;
        }
    }

    public static void removeApplication(Application application, Context context) {
        removeApplications(Collections.singletonList(application), context);
    }

    public static void removeApplications(List<Application> applications, Context context) {
        DatabaseHelper helper = DatabaseManager.getHelper(context);
        try {
            Dao<Application, ?> dao = helper.getDaoWithCache(Application.class);

            for(Application application : applications) {
                DeleteBuilder<Application, ?> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where().eq("secret", application.getSecret());
                deleteBuilder.delete();
            }
        } catch(SQLException e) {
            ExceptionHandler.handleException(new Exception(context.getString(R.string.database_applications_remove_error), e), context, true);
            return;
        }

        // Remove from server
        removeApplicationsFromServer(applications, v -> {}, context);
    }

    /**
     * Add applications to your account
     * @param applications The list of applications to add
     * @param callback The callbak function to execute when it's done
     */
    static void addApplicationsToServer(List<Application> applications, AsyncActionCallback<Either<Throwable, ?>> callback, Context context) {
        if(applications == null || applications.size() == 0) {
            callback.onCallback(Either.right(null));
            return;
        }

        Map<String, String> params = new HashMap<>();
        for(int i = 0; i < applications.size(); i++) {
            params.put("applications[" + i + "]", applications.get(i).getLabel());
        }

        uploadUpdates(params, Request.Method.POST, callback, context);
    }

    /**
     * Update applications in your account
     * @param applicationList HashMap of Applications, Key = old application, Value = new application
     * @param callback The callbak function to execute when it's done
     */
    static void updateApplicationsToServer(HashMap<Application, Application> applicationList, AsyncActionCallback<Either<Throwable, ?>> callback, Context context) {
        if(applicationList == null || applicationList.size() == 0) {
            callback.onCallback(Either.right(null));
            return;
        }

        Map<String, String> params = new HashMap<>();
        int i = 0;
        for(Map.Entry<Application, Application> applicationEntry : applicationList.entrySet()) {
            params.put("applications[" + i + "].Key", applicationEntry.getKey().getLabel());
            params.put("applications[" + i + "].Value", applicationEntry.getValue().getLabel());
            i++;
        }

        uploadUpdates(params, Request.Method.PUT, callback, context);
    }

    /**
     * Remove applications from your account
     * @param applications The list of applications to remove
     * @param callback The callbak function to execute when it's done
     */
    static void removeApplicationsFromServer(List<Application> applications, AsyncActionCallback<Either<Throwable, ?>> callback, Context context) {
        if(applications == null || applications.size() == 0) {
            callback.onCallback(Either.right(null));
            return;
        }

        Map<String, String> params = new HashMap<>();
        for(int i = 0; i < applications.size(); i++) {
            params.put("applications[" + i + "]", applications.get(i).getLabel());
        }

        uploadUpdates(params, Request.Method.DELETE, callback, context);
    }

    private static void uploadUpdates(Map<String, String> params, int method, AsyncActionCallback<Either<Throwable, ?>> callback, Context context) {
        if(!AccountManager.isSet()) {
            Exception notLoggedInException = new Exception(context.getString(R.string.account_general_notloggedin));
            ExceptionHandler.handleException(notLoggedInException, context, false);
            callback.onCallback(Either.left(notLoggedInException));
            return;
        }

        HmacApiRequest<Either<Throwable, ?>> applicationsRequest = new HmacApiRequest<Either<Throwable, ?>>(
                method,
                Constants.HTTP_BASE + "account/applications",
                a -> Either.right(null),
                callback::onCallback,
                error -> {
                    ExceptionHandler.handleException(
                            new Exception(context.getString(R.string.applications_error) + ": " + error.getMessage(), error.getCause()), context, true
                    );
                    callback.onCallback(Either.left(error.getCause()));
                }, context)
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> baseMap = super.getParams();
                baseMap.putAll(params);
                return baseMap;
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                //noinspection ThrowableResultOfMethodCallIgnored
                VolleyError baseHandling = super.parseNetworkError(volleyError);

                if(baseHandling != volleyError) return baseHandling;
                if(volleyError.networkResponse == null) return baseHandling;

                return new VolleyError(context.getString(R.string.apirequests_unknown_error), volleyError);
            }
        };

        RequestQueue.addToRequestQueue(applicationsRequest, context);
    }

    public static class DuplicateApplicationException extends Exception {
        public DuplicateApplicationException(String message) {
            super(message);
        }
    }
}
