package com.example.screenmirror.Manager;

import android.app.Activity;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Window;
import android.view.WindowManager;

import com.example.screenmirror.Activity.Connection;
import com.example.screenmirror.Activity.MainActivity;
import com.example.screenmirror.Activity.TutorialActivity;
import com.example.screenmirror.R;

public class AppManager {

    public static boolean IsWifiOn=false;

    public static void StartMAinActivity(Activity activity){
        activity.startActivity(new Intent(activity, MainActivity.class));
    }

    public static void ChangeStatusBarColor(Activity activity) {

        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.color.app_theme);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setNavigationBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
    }

    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    public static void StartConnectionActivity(Activity activity){
        activity.startActivity(new Intent(activity, Connection.class));
    }


    public static void StartTutorialActivity(Activity activity){
        activity.startActivity(new Intent(activity, TutorialActivity.class));
    }

}
