package com.example.screenmirror.Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.screenmirror.BackgroundService;
import com.example.screenmirror.Capturing.CaptureService;
import com.example.screenmirror.Constants;
import com.example.screenmirror.HTTP_Server.ServerService;
import com.example.screenmirror.Manager.AppManager;
import com.example.screenmirror.R;
import com.example.screenmirror.Settings.SettingsService;
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


public class Connection extends AppCompatActivity {
    private DrawerLayout _DrawerLayout;

    private boolean _IsHttpServerRunning_shadow = false;
    private String _HttpServerPort_shadow = "" + Constants.DEFAULT_HTTP_SERVER_PORT;
    private String _IpAddress_shadow = "";

    private LocalBroadcastManager _localBroadcaster;
    private BroadcastReceiver _localListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleLocalBroadcast(intent);
        }
    };
    private InterstitialAd interstitialAd;


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, BackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        setContentView(R.layout.activity_connection);
        NativeAds_two(SplashActivity.NativeId2);
        checkAndAskForInternetPermission();

        AppManager.ChangeStatusBarColor(Connection.this);
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showInterstitial();
            }
        });

        findViewById(R.id.btn_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);

            }
        });

        initLocalBroadcaster();
        RequestInterstitial(SplashActivity.InterstitialID2);
        startService(new Intent(getApplicationContext(), ServerService.class));
        startService(new Intent(getApplicationContext(), SettingsService.class));
        startService(new Intent(getApplicationContext(), CaptureService.class));

        requestIpUpdate();
        requestPortUpdate();

        findViewById(R.id.btn_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                TextView textView = findViewById(R.id.txt_ip);
                String sub = textView.getText().toString();
                intent.putExtra(Intent.EXTRA_TEXT, sub);
                startActivity(Intent.createChooser(intent, "Share Using"));
            }
        });

        findViewById(R.id.btn_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                TextView textView = findViewById(R.id.txt_ip);
                ClipData clip = ClipData.newPlainText("data", textView.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(Connection.this, "Text Copied!", Toast.LENGTH_SHORT).show();

            }
        });


        initStartButton();
    }

    private void startHttpServer() {
        if (_localBroadcaster != null) {
            Intent toSend = new Intent(Constants.SERVER_HTTP_EVENT_NAME_COMMAND);
            toSend.putExtra(Constants.SERVER_HTTP_COMMAND, Constants.SERVER_HTTP_START);
            _localBroadcaster.sendBroadcast(toSend);
        }
        requestIpUpdate();
    }

    private void stopHttpServer() {
        if (_localBroadcaster != null) {
            Intent toSend = new Intent(Constants.SERVER_HTTP_EVENT_NAME_COMMAND);
            toSend.putExtra(Constants.SERVER_HTTP_COMMAND, Constants.SERVER_HTTP_STOP);
            _localBroadcaster.sendBroadcast(toSend);
        }
    }

    private void initStartButton() {
        TextView startButton = findViewById(R.id.buttonStart);
        startButton.setText(R.string.buttonStart);
        View.OnClickListener listenerToSet = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Connection.this._IsHttpServerRunning_shadow) {
                    Toast.makeText(Connection.this, "Server get Stopped", Toast.LENGTH_SHORT).show();
                    Connection.this.stopHttpServer();
                    Connection.this.stopCapturing();
                    return;
                }
                Toast.makeText(Connection.this, "Server get Started", Toast.LENGTH_SHORT).show();
                Connection.this.startHttpServer();
                Connection.this.TryToStartCaptureService();
            }
        };
        startButton.setOnClickListener(listenerToSet);
    }

    private void stopCapturing() {
        if (_localBroadcaster != null) {
            Intent toSend = new Intent(Constants.CAPTURE_EVENT_NAME_COMMAND);
            toSend.putExtra(Constants.CAPTURE_COMMAND, Constants.CAPTURE_STOP);
            _localBroadcaster.sendBroadcast(toSend);
        }
        requestIpUpdate();
    }

    private void updateDisplayedValuesOn() {
        TextView ipInfo = findViewById(R.id.yourIPText);
        ipInfo.setText(getString(R.string.ipInfoServerOn));

        TextView ipAddressText = findViewById(R.id.ip);
        TextView ipAddressText_one = findViewById(R.id.txt_ip);
        String URI = _IpAddress_shadow + ":" + _HttpServerPort_shadow;
        ipAddressText.setText(URI);
        ipAddressText_one.setText(URI);

        TextView button = findViewById(R.id.buttonStart);
        button.setText(R.string.buttonStop);

    }

    private void updateDisplayedValuesOff() {
        TextView ipInfo = findViewById(R.id.yourIPText);
        ipInfo.setText(R.string.ipInfoServerOff);

        TextView ipAddressText = findViewById(R.id.ip);
        TextView ipAddressText_one = findViewById(R.id.txt_ip);
        ipAddressText.setText(_IpAddress_shadow + "");
        ipAddressText_one.setText(_IpAddress_shadow + "");

        TextView button = findViewById(R.id.buttonStart);
        button.setText(R.string.buttonStart);

    }

    /**
     * Can only used if localBroadcastManager is active.
     */
    private void requestIpUpdate() {
        if (_localBroadcaster != null) {
            Intent temp = new Intent(Constants.IP_REQUEST);
            _localBroadcaster.sendBroadcast(temp);
        } else {
            if (Constants.InDebugging) {
                Log.i("LocalBroadcast Main", "Illegal State access");
            }
        }
    }

    private void handleIpUpdate(Intent rawIntent) {
        String addr = rawIntent.getStringExtra(Constants.IP_ANSWER_ADDRESS);
        Log.e("data", addr);
        String isRunning = rawIntent.getStringExtra(Constants.IP_ANSWER_FLAG_RUN);

        if (addr != null && isRunning != null) {
            _IpAddress_shadow = addr;
            if (isRunning == Constants.SERVER_HTTP_IS_RUNNING_TRUE) {
                _IsHttpServerRunning_shadow = true;
                updateDisplayedValuesOn();
            } else if (isRunning == Constants.SERVER_HTTP_IS_RUNNING_FALSE) {
                _IsHttpServerRunning_shadow = false;
                updateDisplayedValuesOff();
            }
        }
    }

    /**
     * Can only used if localBroadcastManager is active.
     */
    private void requestPortUpdate() {
        if (_localBroadcaster != null) {
            Intent temp = new Intent(Constants.SETTING_REQUEST);
            _localBroadcaster.sendBroadcast(temp);
        } else {
            if (Constants.InDebugging) {
                Log.i("LocalBroadcast Main", "Illegal State access");
            }
        }
    }

    private void handlePortChange(Intent rawIntent) {
        String port = "8080";
        if (port != null && port != _HttpServerPort_shadow) {
            _HttpServerPort_shadow = port;
            Log.e("data", port);
            if (_IsHttpServerRunning_shadow) {
                updateDisplayedValuesOn();
            } else {
                updateDisplayedValuesOff();
            }
        }
    }

    private void handleImageChange(Intent rawIntent) {
        byte[] data = rawIntent.getByteArrayExtra(Constants.IMAGE_DATA_NAME);
        if (data != null) {
            if (_IsHttpServerRunning_shadow) {
                Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
                // ImageView view = findViewById(R.id.imagePreview);
                // view.setImageBitmap(image);
            } else {
                //resetImageAndQr();
            }
        }
    }

    private void handleLocalBroadcast(Intent rawIntent) {
        String action = rawIntent.getAction();
        if (action == null) {
            return;
        }
        switch (action) {
            case Constants.IP_ANSWER:
                handleIpUpdate(rawIntent);
                break;
            case Constants.IMAGE_EVENT_NAME:
                handleImageChange(rawIntent);
                break;
            case Constants.QR_CODE_EVENT:
                setQRImage(rawIntent);
                break;
            case Constants.SETTING_INFO_EVENT:
                handlePortChange(rawIntent);
                break;
            case Constants.SETTING_CHANGED_EVENT:
                handlePortChange(rawIntent);
                break;
            case Constants.CLIENT_CONNECTED_EVENT:
                updateConnectedClients(rawIntent);
                break;
            default:
                return;
        }
    }

    private IntentFilter getIntentFilter() {
        IntentFilter toReturn = new IntentFilter(Constants.IMAGE_EVENT_NAME);
        toReturn.addAction(Constants.IP_ANSWER);
        toReturn.addAction(Constants.QR_CODE_EVENT);
        toReturn.addAction(Constants.SETTING_INFO_EVENT);
        toReturn.addAction(Constants.SETTING_CHANGED_EVENT);
        toReturn.addAction(Constants.CLIENT_CONNECTED_EVENT);
        return toReturn;
    }

    private synchronized void setQRImage(Intent data) {
        Parcelable p = data.getParcelableExtra(Constants.QR_CODE_DATA);
        Bitmap b = (Bitmap) p;
        //  ImageView imageView = findViewById(R.id.QRImage);
        //imageView.setImageBitmap(b);
    }


    private void checkAndAskForInternetPermission() {
        //Prüfe auf Permissions für Internet
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            if (Constants.InDebugging) {
                Log.i("Main", "Keine Perms");
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, Constants.INTERNET_PERMISSION);
        } else {
            if (Constants.InDebugging) {
                Log.i("Main", "Hat Perms");
            }
        }
    }

    private void TryToStartCaptureService() {
        MediaProjectionManager temp = null;
        //Result checking in callback methode onActivityResult()
        try {
            if (Constants.InDebugging) {
                Log.d("BeforeServiceStart", "Now trying to get Mediamanager.");
            }
            temp = (MediaProjectionManager) getSystemService(getApplicationContext().MEDIA_PROJECTION_SERVICE);
            startActivityForResult(temp.createScreenCaptureIntent(), Constants.REQUEST_CODE_SCREEN_CAPTURE);
        } catch (Exception ex) {
            if (Constants.InDebugging) {
                Log.d("BeforeServiceStart", ex.getMessage());
            }
        }
    }

    private void updateConnectedClients(Intent intent) {
        Log.i("MainActivity", "UpdateClientAnzahl");
        // TextView tv = findViewById(R.id.connectedClients);
        int amount = intent.getIntExtra(Constants.CLIENT_AMOUNT, -1);
        String temp = getString(R.string.connected_clients) + " " + amount;
        // tv.setText(temp);
    }

    private void initLocalBroadcaster() {
        if (_localBroadcaster == null) {
            _localBroadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
            _localBroadcaster.registerReceiver(_localListener, getIntentFilter());
        }
    }

    private void cleanLocalBroadcaster() {
        if (_localBroadcaster != null) {
            _localBroadcaster.unregisterReceiver(_localListener);
            _localBroadcaster = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        initLocalBroadcaster();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanLocalBroadcaster();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_SCREEN_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                Intent toSend = new Intent(Constants.CAPTURE_EVENT_NAME_COMMAND);
                toSend.putExtra(Constants.CAPTURE_COMMAND, Constants.CAPTURE_INIT);
                toSend.putExtra(Constants.CAPTURE_MEDIA_GRANTING_TOKEN_INTENT, data);
                _localBroadcaster.sendBroadcast(toSend);
                Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show();
            } else {
                if (Constants.InDebugging) {
                    Log.d("BeforeServiceStarting", "Request failed.");
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                _DrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
                        Connection.this.interstitialAd = interstitialAd;
                        Log.i("TAG", "onAdLoaded");

                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @RequiresApi(api = Build.VERSION_CODES.Q)
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        Connection.this.interstitialAd = null;
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        Connection.this.interstitialAd = null;
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

            finish();

        }

    }

    public void NativeAds_two(String nativeId) {

        AdLoader.Builder builder = new AdLoader.Builder(this, nativeId)
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                        RelativeLayout frameLayout = findViewById(R.id.frmad);
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


