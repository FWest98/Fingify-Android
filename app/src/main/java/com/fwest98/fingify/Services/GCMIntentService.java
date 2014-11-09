package com.fwest98.fingify.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fwest98.fingify.Helpers.HelperFunctions;
import com.fwest98.fingify.R;
import com.fwest98.fingify.Receivers.GCMBroadcastReceiver;
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
                .setContentText("Coderequest")
                .setContentTitle("Coderequest")
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_action_edit)
                .setStyle(new Notification.BigTextStyle().bigText("test"))
                .addAction(R.drawable.state_done, "Accept", acceptPIntent)
                .addAction(R.drawable.state_rejected, "Reject", rejectPIntent);

        if(openPopupWithoutNotification) {
            context.startActivity(verifyIntent);
        } else {
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