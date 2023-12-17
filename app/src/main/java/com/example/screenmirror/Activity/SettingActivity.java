package com.example.screenmirror.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.screenmirror.Constants;
import com.example.screenmirror.Manager.AppManager;
import com.example.screenmirror.R;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

public class SettingActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {
    private Spinner spinner;
    private String[] quality = new String[]{"HD","Medium","Low"};
    private BroadcastReceiver _localListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleLocalBroadcast(intent);
        }
    };
    private LocalBroadcastManager _localBroadcaster;
    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initLocalBroadcaster();
        _localBroadcaster.sendBroadcast(new Intent(Constants.SETTING_REQUEST));

        RequestInterstitial(SplashActivity.InterstitialID2);
        NativeAds(SplashActivity.NativeId2);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showInterstitial();
            }
        });

        findViewById(R.id.rate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                rateus();
            }
        });

        findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String body = "Best video and status downloader app....DOWNLOAD NOW....";
                String sub = "http://play.google.com/store/apps/details?id="+getPackageName();
                intent.putExtra(Intent.EXTRA_TEXT, body);
                intent.putExtra(Intent.EXTRA_TEXT, sub);
                startActivity(Intent.createChooser(intent, "Share Using"));
            }
        });


        findViewById(R.id.more_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent("android.intent.action.VIEW").setData(Uri.parse("http://play.google.com/store/apps/details?id="+getPackageName()));
                startActivity(intent);
            }
        });

        TextView textView = findViewById(R.id.version);
        try {
            textView.setText(this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        findViewById(R.id.txt_policy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent("android.intent.action.VIEW").setData(Uri.parse(SplashActivity.Policy));
                startActivity(intent);
            }
        });
        AppManager.ChangeStatusBarColor(SettingActivity.this);

        spinner = findViewById(R.id.Spinner);
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,quality);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(aa);

        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        if(i==0){
            saveAll("100");
        }else if(i==1){
            saveAll("75");
        }else if(i==2) {
            saveAll("50");
        }else {
            saveAll("100");
        }

    }

    @Override
    public void onBackPressed() {
        showInterstitial();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    public void rateus() {
        final Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.rate_us_dailog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        final RatingBar ratingBar = (RatingBar) dialog.findViewById(R.id.rating_bar);
        dialog.findViewById(R.id.txtnotnow).setOnClickListener(new View.OnClickListener() { // from class: m.a.a.b.w
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Dialog dialog2 = dialog;
                dialog2.dismiss();
            }
        });
        dialog.findViewById(R.id.txtsubmit).setOnClickListener(new View.OnClickListener() { // from class: m.a.a.b.c0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                dialog.dismiss();
                Intent  intent = new Intent("android.intent.action.VIEW").setData(Uri.parse("http://play.google.com/store/apps/details?id="+getPackageName()));
                startActivity(intent);
            }
        });
        dialog.show();
    }



    public void saveAll(String quality){
        Intent toSend = new Intent(Constants.SETTING_CHANGED_EVENT);
        toSend.putExtra(Constants.SETTING_SERVER_PORT, "8080");
        toSend.putExtra(Constants.SETTING_JPEG_QUALI, quality);
        _localBroadcaster.sendBroadcast(toSend);
    }


    private void handleLocalBroadcast(Intent rawIntent) {
        String action = rawIntent.getAction();
        if (action == null) {
            return;
        }
        switch (action) {
            case Constants.SETTING_INFO_EVENT:
                break;
            default:
                return;
        }
    }


    private void initLocalBroadcaster() {
        if (_localBroadcaster == null) {
            _localBroadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
            _localBroadcaster.registerReceiver(_localListener, getIntentFilter());
        }
    }

    private IntentFilter getIntentFilter() {
        IntentFilter toReturn = new IntentFilter(Constants.SETTING_INFO_EVENT);
        return toReturn;
    }

    public void NativeAds(String nativeId) {

        AdLoader.Builder builder = new AdLoader.Builder(this, nativeId)
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                        FrameLayout frameLayout = findViewById(R.id.nativeads);
                        NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.nativelayout, null);
                        populateNativeAdView(nativeAd, adView);
                        frameLayout.removeAllViews();
                        frameLayout.addView(adView);
                    }
                });

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
            }
        }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {

        adView.setMediaView(adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));

        // The headline and mediaContent are guaranteed to be in every NativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());
        adView.getMediaView().setImageScaleType(ImageView.ScaleType.CENTER_CROP);

        // These assets aren't guaranteed to be in every NativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);

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
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        SettingActivity.this.interstitialAd = interstitialAd;
                        Log.i("TAG", "onAdLoaded");

                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @RequiresApi(api = Build.VERSION_CODES.Q)
                                    @Override
                                    public void onAdDismissedFullScreenContent() {

                                        SettingActivity.this.interstitialAd = null;

                                    }


                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        SettingActivity.this.interstitialAd = null;

                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                    }
                                });
                    }

                    @RequiresApi(api = Build.VERSION_CODES.Q)
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i("TAG", loadAdError.getMessage());
                        interstitialAd = null;


                    }
                });
    }

    private void showInterstitial() {

        if (interstitialAd != null) {
            interstitialAd.show(this);

            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    finish();
                    super.onAdDismissedFullScreenContent();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    RequestInterstitial(SplashActivity.InterstitialID);
                    super.onAdFailedToShowFullScreenContent(adError);
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                }
            });

        } else {

            finish();

        }

    }


}