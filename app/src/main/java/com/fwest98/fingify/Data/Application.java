package com.fwest98.fingify.Data;

import android.app.Activity;
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
import java.util.List;

import lombok.Getter;

public class Application implements Serializable {
    private static final long serialVersionUID = 7865678;

    @DatabaseField(generatedId = true) private int id;

    @DatabaseField @Getter private String label;
    @DatabaseField @Getter private String secret;
    @DatabaseField @Getter private String user;
    @DatabaseField @Getter private AuthenticationType type;

    public Application() {}

    public Application(String label, String secret, String user) {
        this.label = label;
        this.secret = secret;
        this.user = user;
        this.type = AuthenticationType.TOTP;
    }

    public Application(String label, String secret, String user, AuthenticationType type) {
        this.label = label;
        this.secret = secret;
        this.type = type;
        this.user = user;
    }

    //region Database

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

    public static void addApplication(Application application, Activity activity) {
        if(secretExists(application.getSecret(), activity) || labelExists(application.getLabel(), activity)) {
            // This already exists
            return;
        }

        DatabaseHelper helper = DatabaseManager.getHelper(activity);
        try {
            Dao<Application, ?> dao = helper.getDaoWithCache(Application.class);

            dao.create(application);

            // Add to internetz
            Account.getInstance(activity).setApplications(Arrays.asList(application), s -> {});
        } catch(SQLException e) {
            ExceptionHandler.handleException(new Exception(activity.getString(R.string.database_applications_save_error), e), activity, true);
        }
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

    public static void removeApplication(Application application, Activity activity) {
        DatabaseHelper helper = DatabaseManager.getHelper(activity);
        try {
            Dao<Application, ?> dao = helper.getDaoWithCache(Application.class);

            DeleteBuilder<Application, ?> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("secret", application.getSecret());
            deleteBuilder.delete();

            Account.getInstance(activity).removeApplications(Arrays.asList(application), v -> {});
        } catch(SQLException e) {
            Log.e("ERROR", activity.getString(R.string.database_applications_remove_error), e);
        }
    }

    //endregion
}
