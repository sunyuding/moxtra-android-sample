package com.moxtra.moxiechat;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.moxtra.sdk.ChatClient;
import com.moxtra.sdk.client.ChatClientDelegate;
import com.moxtra.sdk.notification.NotificationManager;

public class GcmRegistrationService extends IntentService {
    private static final String TAG = "DEMO_Registration";
    private static final String SENDER_ID = "3367684136";

    public GcmRegistrationService() {
        super("GcmRegistrationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!checkPlayServices()) {
            enableNotification(null);
            return;
        }
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.d(TAG, "GCM Registration token:" + token);
//            PreferenceUtil.setGcmRegId(this, token);
            enableNotification(token);
        } catch (Exception e) {
            Log.e(TAG, "Get Token error", e);
            enableNotification(null);
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        Log.d(TAG, "Google Play Service availability check result = " + resultCode);
        return (resultCode == ConnectionResult.SUCCESS);
    }

    private void enableNotification(String token) {
        ChatClientDelegate ccd = ChatClient.getClientDelegate();
        if (ccd == null) {
            Log.e(TAG, "ClientClient is null!");
            return;
        }

        NotificationManager nm = ccd.getNotificationManager();
        if (nm == null) {
            Log.e(TAG, "NotificationManager is null!");
            return;
        }
        if (TextUtils.isEmpty(token)) {
            nm.useBuiltInPushNotification(MoxtraNotificationService.class.getName());
            Log.d(TAG, "use long connection notification");
        } else {
            nm.setNotificationDeviceToken(token);
            Log.d(TAG, "use GCM notification");
        }
    }
}
