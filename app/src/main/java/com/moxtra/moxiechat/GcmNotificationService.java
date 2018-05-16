/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moxtra.moxiechat;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.moxtra.sdk.ChatClient;
import com.moxtra.sdk.notification.NotificationManager;

public class GcmNotificationService extends GcmListenerService {
    private static final String TAG = "DEMO_GcmIntentService";
    private static final int NOTIFICATION_ID = 0x1;
    private android.app.NotificationManager mNotificationManager;

    public GcmNotificationService() {
        super();
        Log.d(TAG, "GcmNotificationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mNotificationManager = (android.app.NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onMessageReceived(String s, Bundle bundle) {
        super.onMessageReceived(s, bundle);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        if (bundle != null && !bundle.isEmpty()) {
            // After getting a notification,
            // call isValidRemoteNotification to preprocess it and check if it is a Moxtra message.
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

    // If it is a moxtra message, process and send the notification as shown in the code snippet below:
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
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
