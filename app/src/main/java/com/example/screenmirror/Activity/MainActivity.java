package com.example.screenmirror.Activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.example.screenmirror.Manager.AppManager;
import com.example.screenmirror.R;
import com.example.screenmirror.model.NetworkStateManager;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MainActivity extends AppCompatActivity {

    public Dialog dialog;
    private InterstitialAd interstitialAd;
    private int number;
    private RewardedAd mRewardedAd;
    public boolean ISAdWatched;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NetworkStateManager.getInstance().getNetworkConnectivityStatus()
                .observe(this, activeNetworkStateObserver);

        NativeAds(SplashActivity.NativeId);
        RequestInterstitial(SplashActivity.InterstitialID);
        NativeAds_two(SplashActivity.NativeId2);


        AppManager.ChangeStatusBarColor(MainActivity.this);

        findViewById(R.id.rl_cast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                number = 2;
                showInterstitial();

            }
        });

        findViewById(R.id.btn_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number = 3;
                showInterstitial();

            }
        });

        findViewById(R.id.play_games).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent("android.intent.action.VIEW").setData(Uri.parse(SplashActivity.Qureka_Id));
                startActivity(intent);

            }
        });


        findViewById(R.id.rl_browser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (AppManager.IsWifiOn) {
                    RewardDialog();
                } else {
                    WifiTurnOnDialog();
                }

            }
        });

        findViewById(R.id._info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                number = 4;
                showInterstitial();

            }
        });

        findViewById(R.id.rl_wifi_status).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
            }
        });
        findViewById(R.id.rl_play_games).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.intent.action.VIEW").setData(Uri.parse(SplashActivity.Qureka_Id));
                startActivity(intent);
            }
        });


    }

    @SuppressLint("ResourceAsColor")
    public void WifiStatus(boolean IsNetwork) {
        AppManager.IsWifiOn = IsNetwork;
        if (IsNetwork) {

            findViewById(R.id.rl_wifi_status).setBackgroundTintList(getResources().getColorStateList(R.color.Green));
            TextView textView = findViewById(R.id.wifi_status);
            textView.setText("connected");

        } else {

            findViewById(R.id.rl_wifi_status).setBackgroundTintList(getResources().getColorStateList(R.color.Red));
            TextView textView = findViewById(R.id.wifi_status);
            textView.setText("Disconnected");

        }

    }

    private final Observer<Boolean> activeNetworkStateObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean isConnected) {

            Log.e("data", "data" + isConnected);
            WifiStatus(isConnected);

            if (isConnected) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }

        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        WifiStatus(AppManager.isNetworkAvailable(MainActivity.this));
    }

    @Override
    protected void onPause() {
        super.onPause();
        WifiStatus(AppManager.isNetworkAvailable(MainActivity.this));
    }

    private void WifiTurnOnDialog() {

        dialog = new Dialog(MainActivity.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.wifi_status_dialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        dialog.findViewById(R.id.txt_turn_on).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
            }
        });

        dialog.findViewById(R.id.txt_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }

    private void RewardDialog() {

        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.reward_dialog);
        dialog.setCancelable(false);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        dialog.findViewById(R.id.ic_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();

            }
        });

        dialog.findViewById(R.id.rl_reward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowRewardAd(SplashActivity.RewardId);
            }
        });

    }

    private void ShowRewardAd(String interstitialID) {

        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Ads Loading...");
        progressDialog.show();
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, interstitialID,
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {

                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
                        mRewardedAd = null;
                    }


                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        progressDialog.dismiss();
                        ShowReward();
                    }
                });

    }

    private void ShowReward() {

        if (mRewardedAd != null) {
            mRewardedAd.show(MainActivity.this, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    ISAdWatched=true;

                }


            });
        } else {

            AppManager.StartConnectionActivity(MainActivity.this);
        }

        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();

                if(ISAdWatched){
                    if(dialog!=null){
                        dialog.dismiss();
                    }

                    AppManager.StartConnectionActivity(MainActivity.this);
                    ISAdWatched=false;
                    return;
                }
                if(dialog!=null){
                    dialog.dismiss();
                }
                ShowWatchRewardAginDialog(SplashActivity.RewardId2);

            }
        });
    }

    private void ShowWatchRewardAginDialog(String rewardId) {

        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.skiped_reward_dialog);
        dialog.setCancelable(false);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        dialog.findViewById(R.id.ic_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();

            }
        });

        dialog.findViewById(R.id.rl_reward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowRewardAd("ca-app-pub-3940256099942544/5224354917");
            }
        });

    }


    @Override
    public void onBackPressed() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this, R.style.BottomSheetDialogTheme);
        View bottom = LayoutInflater.from(MainActivity.this).inflate(R.layout.exit_dailog, null);
        bottomSheetDialog.setContentView(bottom);
        bottomSheetDialog.show();

        AdLoader.Builder builder = new AdLoader.Builder(this, SplashActivity.NativeId2)
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                        FrameLayout frameLayout = bottom.findViewById(R.id.nativeadss);
                        NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.nativelayout, null);
                        populateNativeAdView(nativeAd, adView);
                        try {
                            if (frameLayout != null) {

                                frameLayout.removeAllViews();
                                frameLayout.addView(adView);
                            }

                        } catch (Exception e) {

                        }

                    }
                });

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
            }
        }).build();
        adLoader.loadAd(new AdRequest.Builder().build());


        Button exit = bottom.findViewById(R.id.exit);
        Button notnow = bottom.findViewById(R.id.notnow);

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                finish();
                finishAffinity();
                System.exit(0);
            }
        });
        notnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });

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
                        MainActivity.this.interstitialAd = interstitialAd;
                        Log.i("TAG", "onAdLoaded");

                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @RequiresApi(api = Build.VERSION_CODES.Q)
                                    @Override
                                    public void onAdDismissedFullScreenContent() {

                                        MainActivity.this.interstitialAd = null;

                                    }


                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        MainActivity.this.interstitialAd = null;

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
                    if (number == 1) {

                        if (AppManager.IsWifiOn) {
                            RewardDialog();
                        } else {
                            WifiTurnOnDialog();
                        }

                    } else if (number == 2) {
                        startActivity(new Intent("android.settings.CAST_SETTINGS"));
                    } else if (number == 3) {
                        startActivity(new Intent(MainActivity.this, SettingActivity.class));
                    } else if (number == 4) {
                        AppManager.StartTutorialActivity(MainActivity.this);
                    }
                    RequestInterstitial(SplashActivity.InterstitialID);
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

            if (number == 1) {

                if (AppManager.IsWifiOn) {
                    RewardDialog();
                } else {
                    WifiTurnOnDialog();
                }

            } else if (number == 2) {
                startActivity(new Intent("android.settings.CAST_SETTINGS"));
            } else if (number == 3) {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
            } else if (number == 4) {
                AppManager.StartTutorialActivity(MainActivity.this);
            }

        }

    }

    public void NativeAds_two(String nativeId) {

        AdLoader.Builder builder = new AdLoader.Builder(this, nativeId)
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                        RelativeLayout frameLayout = findViewById(R.id.frmad_one);
                        frameLayout.setVisibility(View.VISIBLE);
                        NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.small_native, null);
                        populateNativeAdView_two(nativeAd, adView);
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

    private void populateNativeAdView_two(NativeAd nativeAd, NativeAdView adView) {


        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        // The headline and mediaContent are guaranteed to be in every NativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

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
            ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
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

}