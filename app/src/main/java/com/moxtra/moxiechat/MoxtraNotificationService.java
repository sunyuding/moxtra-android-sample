package com.moxtra.moxiechat;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.moxtra.sdk.ChatClient;
import com.moxtra.sdk.notification.BasePushIntentService;
import com.moxtra.sdk.notification.NotificationManager;

public class MoxtraNotificationService extends BasePushIntentService {
    private static final String TAG = "DEMO_LCIntentService";
    private static final int NOTIFICATION_ID = 1;

    @Override
    protected void onMessage(Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        if (extras != null && !extras.isEmpty()) {
            NotificationManager notificationManager = ChatClient.getClientDelegate().getNotificationManager();
            if (notificationManager.isValidRemoteNotification(intent)) {
                int type = notificationManager.getValidNotificationType(intent);
                String title = notificationManager.getNotificationMessageText(this, intent);
                Log.i(TAG, "Here comes a notification: type=" + type + ", title=" + title);
                sendNotification(title, null, intent);
            } else {
                Log.w(TAG, "Ignore invalid remote notification.");
            }
        } else {
            Log.w(TAG, "Ignore notification without any extended data.");
        }
    }

    private void sendNotification(String msg, Uri uri, Intent intent) {
        Intent notificationIntent = new Intent(this, ChatListActivity.class);
        if (intent != null) {
            notificationIntent.putExtras(intent);
        }
        PendingIntent contentIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(getApplicationInfo().labelRes))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentText(msg)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);

        if (uri != null) {
            mBuilder.setSound(uri);
        }

        mBuilder.setContentIntent(contentIntent);
        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
