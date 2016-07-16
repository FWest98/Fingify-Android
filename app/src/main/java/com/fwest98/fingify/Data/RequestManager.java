package com.fwest98.fingify.Data;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.VolleyError;
import com.fwest98.fingify.Database.DatabaseHelper;
import com.fwest98.fingify.Database.DatabaseManager;
import com.fwest98.fingify.Helpers.AsyncActionCallback;
import com.fwest98.fingify.Helpers.ExceptionHandler;
import com.fwest98.fingify.Helpers.ExtendedClock;
import com.fwest98.fingify.Helpers.ExtendedTotp;
import com.fwest98.fingify.Helpers.HelperFunctions;
import com.fwest98.fingify.Helpers.HmacApiRequest;
import com.fwest98.fingify.Helpers.RequestQueue;
import com.fwest98.fingify.Models.Application;
import com.fwest98.fingify.Models.Request;
import com.fwest98.fingify.R;
import com.fwest98.fingify.Settings.Constants;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fj.data.Either;

public class RequestManager {
    /**
     * Get the list of requests from web or local storage
     * @param callback The callback function to execute when it's finished
     */
    public static void getRequests(AsyncActionCallback<Either<Throwable, List<Request>>> callback, Context context) {
        if(!AccountManager.isSet()) callback.onCallback(Either.right(null));

        if (HelperFunctions.hasInternetConnection(context)) {
            getRequestsFromWeb(data -> {
                if(data.isRight()) {
                    List<Request> requests = data.right().value();
                    if(requests == null || requests.size() == 0) {
                        callback.onCallback(Either.left(null));
                        return;
                    }

                    removeAllRequests(context);
                    addRequests(requests, context);

                    callback.onCallback(data);
                } else {
                    List<Request> requests = getRequestsFromDatabase(100, context);
                    if(requests == null || requests.size() == 0) {
                        callback.onCallback(Either.left(null));
                    } else {
                        callback.onCallback(Either.right(requests));
                    }
                }
            }, context);
        } else {
            ArrayList<Request> requests = getRequestsFromDatabase(100, context);
            if(requests == null || requests.size() == 0) {
                callback.onCallback(Either.left(null));
            } else {
                callback.onCallback(Either.right(requests));
            }
        }
    }

    private static ArrayList<Request> getRequestsFromDatabase(int limit, Context context) {
        DatabaseHelper helper = DatabaseManager.getHelper(context);
        try {
            Dao<Request, ?> dao = helper.getDaoWithCache(Request.class);

            List<Request> result = dao.queryBuilder().orderBy("requestTime", false).limit((long) limit).query();
            return new ArrayList<>(result);
        } catch (SQLException e) {
            ExceptionHandler.handleException(new Exception(context.getString(R.string.database_requests_load_error), e), context, true);
            return new ArrayList<>();
        }
    }

    /**
     * Get the list of requests from the web
     * @param callback The callback function to execute when it's finished
     */
    private static void getRequestsFromWeb(AsyncActionCallback<Either<Throwable, List<Request>>> callback, Context context) {
        if(!AccountManager.isSet()) {
            callback.onCallback(Either.left(new Exception(context.getString(R.string.account_general_notloggedin))));
            return;
        }

        HmacApiRequest<JSONArray> getRequestsRequest = new HmacApiRequest<JSONArray>(
                Method.GET,
                Constants.HTTP_BASE + "account/requests",
                JSONArray::new,
                response -> {
                    List<Request> requests = new ArrayList<>();

                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject webRequest = response.getJSONObject(i);
                            Request request = new Request(webRequest);
                            requests.add(request);
                        }

                        callback.onCallback(Either.right(requests));
                    } catch(JSONException e) {
                        ExceptionHandler.handleException(new Exception(context.getString(R.string.applications_error), e), context, true);
                        callback.onCallback(Either.left(e));
                    }
                },
                error -> {
                    ExceptionHandler.handleException(
                            new Exception(context.getString(R.string.applications_error) + ": " + error.getMessage(), error.getCause()), context, true
                    );
                    callback.onCallback(Either.left(error.getCause()));
                }, context) {
            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                //noinspection ThrowableResultOfMethodCallIgnored
                VolleyError baseHandling = super.parseNetworkError(volleyError);

                if(baseHandling != volleyError) return baseHandling;
                if(volleyError.networkResponse == null) return baseHandling;

                return new VolleyError(context.getString(R.string.apirequests_unknown_error), baseHandling);
            }
        };

        RequestQueue.addToRequestQueue(getRequestsRequest, context);
    }

    private static void addRequest(Request request, Context context) {
        addRequests(Collections.singletonList(request), context);
    }

    private static void addRequests(List<Request> requests, Context context) {
        DatabaseHelper helper = DatabaseManager.getHelper(context);
        try {
            Dao<Request, ?> dao = helper.getDaoWithCache(Request.class);

            for(Request request : requests) {
                dao.create(request);
            }
        } catch(SQLException e) {
            ExceptionHandler.handleException(new Exception(context.getString(R.string.database_requests_save_error), e), context, true);
        }
    }

    public static void removeRequest(Request request, Context context) {
        DatabaseHelper helper = DatabaseManager.getHelper(context);
        try {
            Dao<Request, ?> dao = helper.getDaoWithCache(Request.class);

            DeleteBuilder<Request, ?> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("id", request.getId());
            deleteBuilder.delete();
        } catch (SQLException e) {
            Log.e("ERROR", context.getString(R.string.database_requests_remove_error));
        }
    }

    private static void removeAllRequests(Context context) {
        DatabaseHelper helper = DatabaseManager.getHelper(context);
        try {
            Dao<Request, ?> dao = helper.getDaoWithCache(Request.class);

            dao.deleteBuilder().delete();
        } catch (SQLException e) {
            Log.e("ERROR", context.getString(R.string.database_requests_remove_error));
        }
    }

    public static void handleRequest(boolean accept, Request request, AsyncActionCallback<Either<Throwable, ?>> callback, Context context) {
        if(!AccountManager.isSet()) {
            callback.onCallback(Either.left(new Exception(context.getString(R.string.account_general_notloggedin))));
            return;
        }
        if(request == null || request.isAnswered()) {
            callback.onCallback(Either.left(new Exception(context.getString(R.string.requests_already_done))));
            return;
        }
        if(!ApplicationManager.labelExists(request.getApplicationName(), context)) {
            callback.onCallback(Either.left(new Exception(context.getString(R.string.requests_application_not_exists))));
            return;
        }

        Application application = ApplicationManager.getApplication(request.getApplicationName(), context);
        ExtendedTotp totp = new ExtendedTotp(application.getSecret(), new ExtendedClock());

        HmacApiRequest<Either<Throwable, ?>> handleRequestRequest = new HmacApiRequest<Either<Throwable, ?>>(
                Method.POST,
                Constants.HTTP_BASE + "request",
                a -> Either.right(null),
                callback::onCallback,
                error -> {
                    ExceptionHandler.handleException(
                            new Exception(context.getString(R.string.requests_error) + ": " + error.getMessage(), error.getCause()), context, true
                    );
                    callback.onCallback(Either.left(error.getCause()));
                }, context
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = super.getParams();
                params.put("code", accept ? totp.now() : "0");
                params.put("requestId", request.getId().toString());
                return params;
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                //noinspection ThrowableResultOfMethodCallIgnored
                VolleyError baseHandling = super.parseNetworkError(volleyError);

                if(baseHandling != volleyError) return baseHandling;
                if(volleyError.networkResponse == null) return baseHandling;

                switch(volleyError.networkResponse.statusCode) {
                    case 400:
                        return new VolleyError(context.getString(R.string.apirequests_missing_data), baseHandling);
                    case 404:
                        return new VolleyError(context.getString(R.string.requests_notfound), baseHandling);
                    default:
                        return new VolleyError(context.getString(R.string.apirequests_unknown_error), baseHandling);
                }
            }
        };

        RequestQueue.addToRequestQueue(handleRequestRequest, context);
    }
}
