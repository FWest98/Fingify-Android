package com.fwest98.fingify.Data;

import android.content.Context;
import android.util.Log;

import com.fwest98.fingify.Database.DatabaseHelper;
import com.fwest98.fingify.Database.DatabaseManager;
import com.fwest98.fingify.Helpers.ExceptionHandler;
import com.fwest98.fingify.R;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import lombok.Getter;

public class Request implements Serializable {
    private static final long serialVersionUID = 7865671;

    @DatabaseField(generatedId = true) private int id;

    @DatabaseField @Getter private String applicationName;
    @DatabaseField @Getter private Date requestTime;
    @DatabaseField @Getter private boolean answered;
    @DatabaseField @Getter private boolean thisDevice;
    @DatabaseField @Getter private boolean accepted;

    public Request() {}

    public Request(String applicationName, Date requestTime) {
        this.applicationName = applicationName;
        this.requestTime = requestTime;
        this.answered = false;
        this.thisDevice = true;
        this.accepted = false;
    }

    public Request(String applicationName, Date requestTime, boolean answered, boolean thisDevice, boolean accepted) {
        this.applicationName = applicationName;
        this.requestTime = requestTime;
        this.answered = answered;
        this.thisDevice = thisDevice;
        this.accepted = accepted;
    }

    //region Database

    public static ArrayList<Request> getRequests(Context context) {
        return getRequests(10000, context);
    }
    public static ArrayList<Request> getRequests(int limit, Context context) {
        DatabaseHelper helper = DatabaseManager.getHelper(context);
        try {
            Dao<Request, ?> dao = helper.getDaoWithCache(Request.class);

            List<Request> result = dao.queryBuilder().orderBy("requestTime", false).limit(limit).query();
            return new ArrayList<>(result);
        } catch (SQLException e) {
            ExceptionHandler.handleException(new Exception(context.getString(R.string.database_requests_load_error), e), context, true);
            return new ArrayList<>();
        }
    }

    public static void addRequest(Request request, Context context) {
        addRequests(Arrays.asList(request), context);
    }

    public static void addRequests(List<Request> requests, Context context) {
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
            deleteBuilder.where().eq("id", request.id);
            deleteBuilder.delete();
        } catch (SQLException e) {
            Log.e("ERROR", context.getString(R.string.database_requests_remove_error));
        }
    }

    public static void removeAllRequests(Context context) {
        DatabaseHelper helper = DatabaseManager.getHelper(context);
        try {
            Dao<Request, ?> dao = helper.getDaoWithCache(Request.class);

            dao.deleteBuilder().delete();
        } catch (SQLException e) {
            Log.e("ERROR", context.getString(R.string.database_requests_remove_error));
        }
    }

    //endregion
}
