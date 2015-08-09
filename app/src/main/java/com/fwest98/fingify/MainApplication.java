package com.fwest98.fingify;

import android.app.Application;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

@ReportsCrashes(
    httpMethod = HttpSender.Method.PUT,
    reportType = HttpSender.Type.JSON,
    formUri = "http://logging.fingify.nl:5984/acra-fingify/_design/acra-storage/_update/report",
    formUriBasicAuthLogin = "fingify_reporter",
    formUriBasicAuthPassword = "c#%&%A#kE3#9"
)
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ACRA.init(this);
    }
}
