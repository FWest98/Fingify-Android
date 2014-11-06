package com.fwest98.fingify.Data;

import android.content.Context;
import android.util.Log;

import com.fwest98.fingify.Database.DatabaseHelper;
import com.fwest98.fingify.Database.DatabaseManager;
import com.fwest98.fingify.Helpers.ExceptionHandler;
import com.fwest98.fingify.Helpers.ExtendedClock;
import com.fwest98.fingify.Helpers.ExtendedTotp;
import com.fwest98.fingify.R;
import com.fwest98.fingify.Settings.Constants;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.Serializable;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

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
            ExceptionHandler.handleException(new Exception(context.getString(R.string.database_applications_load_error), e), context, true);
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
            ExceptionHandler.handleException(new Exception(context.getString(R.string.database_applications_save_error), e), context, true);
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
            Log.e("ERROR", "Could not remove request");
        }
    }

    public static void removeAllRequests(Context context) {
        DatabaseHelper helper = DatabaseManager.getHelper(context);
        try {
            Dao<Request, ?> dao = helper.getDaoWithCache(Request.class);

            dao.deleteBuilder().delete();
        } catch (SQLException e) {
            Log.e("ERROR", "Could not remove requests");
        }
    }

    //endregion
    //region Webrequests

    public static void processRequest(boolean accept, Request request, Context context) throws Exception {
        if(request == null) return;
        if(request.isAnswered()) return;
        if(!Application.labelExists(request.getApplicationName(), context)) {
            return;
        }

        /* Send accept request */
        /* Get TOTP code */
        Application application = Application.getApplication(request.getApplicationName(), context);
        ExtendedTotp totp = new ExtendedTotp(application.getSecret(), new ExtendedClock());

        URL url = new URL(Constants.HTTP_BASE + "request");
        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("apiKey", Account.getApiKey()));
        postParameters.add(new BasicNameValuePair("code", totp.now()));
        postParameters.add(new BasicNameValuePair("label", request.getApplicationName()));

        UrlEncodedFormEntity data = new UrlEncodedFormEntity(postParameters);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode((int) data.getContentLength());

        data.writeTo(connection.getOutputStream());

        String content = "";
        try {
            Scanner scanner = new Scanner(connection.getInputStream());
            while(scanner.hasNext()) {
                content += scanner.nextLine();
            }
        } catch (Exception e) {

        } finally {
            connection.disconnect();
        }


    }

    //endregion
}
