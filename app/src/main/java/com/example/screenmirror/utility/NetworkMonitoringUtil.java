package com.example.screenmirror.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.screenmirror.model.NetworkStateManager;

public class NetworkMonitoringUtil extends ConnectivityManager.NetworkCallback {
    public static final String TAG = NetworkMonitoringUtil.class.getSimpleName();

    private final NetworkRequest mNetworkRequest;
    private final ConnectivityManager mConnectivityManager;
    private final NetworkStateManager mNetworkStateManager;

    // Constructor
    public NetworkMonitoringUtil(Context context) {
        mNetworkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();

        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mNetworkStateManager = NetworkStateManager.getInstance();
    }

    @Override
    public void onAvailable(@NonNull Network network) {
        super.onAvailable(network);
        Log.d(TAG, "onAvailable() called: Connected to network");
        mNetworkStateManager.setNetworkConnectivityStatus(true);
    }

    @Override
    public void onLost(@NonNull Network network) {
        super.onLost(network);
        Log.e(TAG, "onLost() called: with: Lost network connection");
        mNetworkStateManager.setNetworkConnectivityStatus(false);
    }

    public void registerNetworkCallbackEvents() {
        Log.d(TAG, "registerNetworkCallbackEvents() called");
        mConnectivityManager.registerNetworkCallback(mNetworkRequest, this);
    }

    public void checkNetworkState() {
        try {
            NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
            mNetworkStateManager.setNetworkConnectivityStatus(networkInfo != null
                    && networkInfo.isConnected());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
