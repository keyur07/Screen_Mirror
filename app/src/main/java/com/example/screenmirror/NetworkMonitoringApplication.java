package com.example.screenmirror;

import android.app.Application;
import android.util.Log;

import com.example.screenmirror.Manager.AppOpenManager;
import com.example.screenmirror.utility.NetworkMonitoringUtil;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class NetworkMonitoringApplication extends Application {
    public static final String TAG = NetworkMonitoringApplication.class.getSimpleName();

    public NetworkMonitoringUtil mNetworkMonitoringUtil;
    private static AppOpenManager appOpenManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() called");

        mNetworkMonitoringUtil = new NetworkMonitoringUtil(getApplicationContext());
        mNetworkMonitoringUtil.checkNetworkState();
        mNetworkMonitoringUtil.registerNetworkCallbackEvents();

        MobileAds.initialize(
                this,
                new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {
                    }
                });
        appOpenManager = new AppOpenManager(this);

    }
}
