package com.fwest98.fingify.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.fwest98.fingify.Helpers.HelperFunctions;
import com.fwest98.fingify.R;
import com.fwest98.fingify.Receivers.GCMBroadcastReceiver;
import com.fwest98.fingify.Settings.Constants;
import com.fwest98.fingify.VerifyCodeRequestActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

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
                    createNotification(extras.getString("message"), this, !HelperFunctions.isScreenLocked(this));
                }
            }
        }
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    public static void createNotification(String message, Context context, boolean openPopupWithoutNotification) {
        String popupSetting = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.NOTIFICATION_POPUP_SETTING, "1");
        boolean notificationSetting = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.NOTIFICATION_SETTING, true);

        if(!notificationSetting) return;


        Intent verifyIntent = new Intent(Intent.ACTION_MAIN);
        verifyIntent.setClass(context, VerifyCodeRequestActivity.class);
        verifyIntent.putExtra("applicationName", message);
        verifyIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                                | Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_NO_USER_ACTION);

        Intent acceptIntent = new Intent(VerifyCodeRequestActivity.INTENT_ACCEPT);
        acceptIntent.putExtra("applicationName", message);
        acceptIntent.setClass(context, VerifyCodeRequestActivity.class);

        Intent rejectIntent = new Intent(VerifyCodeRequestActivity.INTENT_REJECT);
        rejectIntent.putExtra("applicationName", message);
        rejectIntent.setClass(context, VerifyCodeRequestActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, verifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent acceptPIntent = PendingIntent.getActivity(context, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent rejectPIntent = PendingIntent.getActivity(context, 0, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context)
                .setContentText(context.getString(R.string.fragment_verifycoderequest_requesttext) + ": " + message)
                .setContentTitle(context.getString(R.string.notification_title))
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setStyle(new Notification.BigTextStyle().bigText(context.getString(R.string.fragment_verifycoderequest_requesttext) + ": " + message))
                .addAction(R.drawable.ic_action_accept, context.getString(R.string.fragment_requests_list_item_button_accept), acceptPIntent)
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