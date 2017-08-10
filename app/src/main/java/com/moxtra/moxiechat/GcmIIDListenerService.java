package com.moxtra.moxiechat;

import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

public class GcmIIDListenerService extends InstanceIDListenerService {
    private static final String TAG = "DEMO_IIDListenerService";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        Log.d(TAG, "onTokenRefresh");
    }
}
