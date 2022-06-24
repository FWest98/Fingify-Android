package com.fwest98.fingify.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.fwest98.fingify.Helpers.ExceptionHandler;
import com.fwest98.fingify.Helpers.HelperFunctions;
import com.fwest98.fingify.Models.Request;
import com.fwest98.fingify.R;
import com.fwest98.fingify.Receivers.GCMBroadcastReceiver;
import com.fwest98.fingify.Settings.Constants;
import com.fwest98.fingify.Activities.VerifyCodeRequestActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

public class GCMIntentService extends IntentService {

    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if(!extras.isEmpty()) {
            switch(messageType) {
                case GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE: {
                    // Handle notification, create notification...
                    // Create JSON Object from the extras bundle
                    Request request;
                    try {
                        JSONObject json = new JSONObject();
                        for (String key : extras.keySet()) {
                            json.put(key, extras.get(key));
                        }

                        request = new Request(json);
                    } catch(JSONException e) {
                        ExceptionHandler.handleException(new Exception(getString(R.string.requests_error_incoming), e), getApplicationContext(), true);
                        break;
                    }
                    createNotification(request, this, !HelperFunctions.isScreenLocked(this));
                }
            }
        }
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    public static void createNotification(Request request, Context context, boolean openPopupWithoutNotification) {
        String popupSetting = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.NOTIFICATION_POPUP_SETTING, "1");
        boolean notificationSetting = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.NOTIFICATION_SETTING, true);

        if(!notificationSetting) return;


        Intent verifyIntent = new Intent(Intent.ACTION_MAIN);
        verifyIntent.setClass(context, VerifyCodeRequestActivity.class);
        verifyIntent.putExtra("request", request);
        verifyIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                                | Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_NO_USER_ACTION);

        Intent acceptIntent = new Intent(VerifyCodeRequestActivity.INTENT_ACCEPT);
        acceptIntent.putExtra("request", request);
        acceptIntent.setClass(context, VerifyCodeRequestActivity.class);

        Intent rejectIntent = new Intent(VerifyCodeRequestActivity.INTENT_REJECT);
        rejectIntent.putExtra("request", request);
        rejectIntent.setClass(context, VerifyCodeRequestActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, verifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent acceptPIntent = PendingIntent.getActivity(context, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent rejectPIntent = PendingIntent.getActivity(context, 0, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context)
                .setContentText(context.getString(R.string.fragment_verifycoderequest_requesttext) + ": " + request.getApplicationName())
                .setContentTitle(context.getString(R.string.notification_title))
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_notification)
                .setStyle(new Notification.BigTextStyle().bigText(context.getString(R.string.fragment_verifycoderequest_requesttext) + ": " + request.getApplicationName()))
                .addAction(R.drawable.ic_action_done, context.getString(R.string.fragment_requests_list_item_button_accept), acceptPIntent)
                .addAction(R.drawable.ic_action_reject, context.getString(R.string.fragment_requests_list_item_button_reject), rejectPIntent);

        if(openPopupWithoutNotification && popupSetting == "2") {
            context.startActivity(verifyIntent);
        } else if(popupSetting != "0") {
            builder.setFullScreenIntent(pendingIntent, true);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    public static void removeNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }
}