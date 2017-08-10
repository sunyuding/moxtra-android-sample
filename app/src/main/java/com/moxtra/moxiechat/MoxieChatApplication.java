package com.moxtra.moxiechat;

import android.app.Application;

import com.moxtra.sdk.ChatClient;

public class MoxieChatApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ChatClient.initialize(this);
    }
}
