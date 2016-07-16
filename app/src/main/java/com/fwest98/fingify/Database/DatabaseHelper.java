package com.fwest98.fingify.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fwest98.fingify.Models.Application;
import com.fwest98.fingify.Models.Request;
import com.fwest98.fingify.R;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.Hashtable;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "fingify.db";
    private static final int DATABASE_VERSION = 4;

    private Hashtable<String, Dao<?, Integer>> daoHashtable = new Hashtable<>();

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Application.class);
            TableUtils.createTable(connectionSource, Request.class);
        } catch (SQLException e) {
            Log.e("DBHelper", "Error generating database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            if (oldVersion == 1) {
                // Upgrade from 1
                TableUtils.createTable(connectionSource, Request.class);
            }
            if(oldVersion <= 2) {
                // Upgrade from 1 or 2
                TableUtils.dropTable(connectionSource, Request.class, true);
                TableUtils.createTable(connectionSource, Request.class);
            }
            if(oldVersion <= 3) {
                // Upgrade to 4
                TableUtils.dropTable(connectionSource, Request.class, true);
                TableUtils.createTable(connectionSource, Request.class);
            }
        } catch (SQLException e) {
            Log.e("DBHelper", "Error upgrading database", e);
            throw new RuntimeException(e);
        }
    }

    public <T> Dao<T, Integer> getDaoWithCache(Class<T> type) throws SQLException {
        String className = type.getName();
        if(daoHashtable.contains(className)) {
            return (Dao<T, Integer>) daoHashtable.get(className);
        }
        Dao<T, Integer> newDao = this.<Dao<T, Integer>, T>getDao(type);
        daoHashtable.put(className, newDao);

        return newDao;
    }

    @Override
    public void close() {
        super.close();
        daoHashtable = null;
    }
}
