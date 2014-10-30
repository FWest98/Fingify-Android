package com.fwest98.fingify.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

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
                    createNotification(this, false);
                }
            }
        }
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    public static void createNotification(Context context, boolean open) {
        Intent verifyIntent = new Intent(Intent.ACTION_MAIN, null);
        verifyIntent.setClass(context, VerifyCodeRequestActivity.class);
        verifyIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                                | Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_NO_USER_ACTION);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, verifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(open) {
            context.startActivity(verifyIntent);
            return;
        }

        Notification.Builder builder = new Notification.Builder(context)
                .setContentText("test")
                .setContentTitle("text")
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_action_edit)
                .setStyle(new Notification.BigTextStyle().bigText("test"))
                .setFullScreenIntent(pendingIntent, true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
}