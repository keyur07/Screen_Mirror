package com.example.screenmirror.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import android.window.SplashScreen;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.screenmirror.Manager.AppManager;
import com.example.screenmirror.Manager.AppOpenManager;
import com.example.screenmirror.R;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends AppCompatActivity {

    public  static String bannerId,NativeId,InterstitialID,appOpen,RewardId;
    public  static String bannerId2,NativeId2,InterstitialID2,RewardId2;
    public static String Qureka_Id,ONESIGNAL_APP_ID;
    public static String Policy;
    private InterstitialAd interstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        RequestAdsId();
    }


    public void RequestAdsId()
    {

        String url ="https://unisoftex.com/ads/screenshare.json";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    appOpen = jsonObject.getString("AppOpenID_one");
                    InterstitialID = jsonObject.getString("InterstitialID_one");
                    NativeId = jsonObject.getString("NativeID_one");
                    bannerId = jsonObject.getString("BannerID_one");

                    InterstitialID2 = jsonObject.getString("InterstitialID_two");
                    NativeId2 = jsonObject.getString("NativeID_two");
                    bannerId2 = jsonObject.getString("BannerID_two");

                    RewardId = jsonObject.getString("RewardID_one");
                    Qureka_Id = jsonObject.getString("Qureka_ID");
                    Policy = jsonObject.getString("Policy");
                    ONESIGNAL_APP_ID = jsonObject.getString("One_Signal_App_ID");

                    AppOpenManager.AD_UNIT_ID=SplashActivity.appOpen;

                    OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
                    RequestInterstitial(SplashActivity.InterstitialID);
                    // OneSignal Initialization
                    OneSignal.initWithContext(SplashActivity.this);
                    OneSignal.setAppId(ONESIGNAL_APP_ID);

                } catch (JSONException e) {

                    ShowtestAds();
                    e.printStackTrace();
                    Toast.makeText(SplashActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    Log.e("AdsId",e.toString());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                ShowtestAds();
                AppManager.StartMAinActivity(SplashActivity.this);
                Log.e("AdsId",error.toString());

            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(SplashActivity.this);
        requestQueue.add(stringRequest);

    }

    public void ShowtestAds(){

        bannerId = "/6499/example/banner";
        NativeId = "/6499/example/native";
        InterstitialID ="/6499/example/interstitial";
        RewardId="/6499/example/rewarded";

        bannerId2 = "/6499/example/banner";
        RewardId2="/6499/example/rewarded";
        NativeId2 = "/6499/example/native";
        InterstitialID2 ="/6499/example/interstitial";

    }

    public void RequestInterstitial(String IntersId) {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(
                this,
                IntersId,
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {

                        SplashActivity.this.interstitialAd = interstitialAd;
                        showInterstitial();
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @RequiresApi(api = Build.VERSION_CODES.Q)
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        AppManager.StartMAinActivity(SplashActivity.this);
                                        SplashActivity.this.interstitialAd = null;
                                        finish();
                                    }
                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        AppManager.StartMAinActivity(SplashActivity.this);

                                        SplashActivity.this.interstitialAd = null;
                                        finish();
                                    }
                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        interstitialAd = null;
                        AppManager.StartMAinActivity(SplashActivity.this);
                        finish();


                    }
                });
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (interstitialAd != null) {
            interstitialAd.show(SplashActivity.this);
        } else {
            AppManager.StartMAinActivity(SplashActivity.this);
            finish();

        }
    }

}