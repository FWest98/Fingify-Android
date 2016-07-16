package com.fwest98.fingify.Settings;

import com.fwest98.fingify.BuildConfig;

public class Constants {
    /* Strings */
    public static final String FINGERPRINT_AUTHENTICATION_SETTING = "app_fingerprintAuthentication";
    public static final String NOTIFICATION_SETTING = "app_notifications";
    public static final String NOTIFICATION_POPUP_SETTING = "app_notification_popup";
    public static final String ACCOUNT_DESC_SETTING = "account_desc";
    public static final String ACCOUNT_LOGIN_SETTING = "account_login";

    public static final String HTTP_BASE_RELEASE = "https://api.fingify.nl/";
    public static final String HTTP_BASE_DEBUG = "http://fingify.testserver.test.home/api/";
    public static final String HTTP_BASE = BuildConfig.GRADLE_DEBUG ? HTTP_BASE_DEBUG : HTTP_BASE_RELEASE;
    public static final String API_VERSION = "1";
    public static final int TIMEOUT_MILLIS = 5000;

    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PLAY_SENDER_ID = "966987002744";
}
