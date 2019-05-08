package com.amazonaws.project;

import android.app.Application;
import android.content.Intent;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        getApplicationContext().startService(new Intent(getApplicationContext(), TransferService.class));
        getApplicationContext().startService(new Intent(getApplicationContext(),BackGroundMusicService.class));
    }
}
