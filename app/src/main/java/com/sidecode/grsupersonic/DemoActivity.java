package com.sidecode.grsupersonic;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.supersonic.adapters.supersonicads.SupersonicConfig;
import com.supersonic.mediationsdk.logger.SupersonicError;
import com.supersonic.mediationsdk.model.Placement;
import com.supersonic.mediationsdk.sdk.InterstitialListener;
import com.supersonic.mediationsdk.sdk.OfferwallListener;
import com.supersonic.mediationsdk.sdk.RewardedVideoListener;
import com.supersonic.mediationsdk.sdk.Supersonic;
import com.supersonic.mediationsdk.sdk.SupersonicFactory;
import com.supersonic.mediationsdk.utils.SupersonicUtils;
import com.sidecode.grsupersonic.R;
import com.supersonicads.sdk.agent.SupersonicAdsAdvertiserAgent;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class DemoActivity extends Activity implements RewardedVideoListener, OfferwallListener, InterstitialListener {

    private final String TAG = "DemoActivity";
    private Button mVideoButton;
    private Button mOfferwallButton;
    private Button mInterstitialButton;
    private Button mInterstitialShowButton;
    private Supersonic mSupersonicInstance;

    private boolean mInterstitialWasInitialized;

    private Placement mPlacement;

    private static GoogleAnalytics analytics;
    private static Tracker tracker;

    public static GoogleAnalytics analytics() {
        return analytics;
    }

    public static Tracker tracker() {
        return tracker;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        analytics = GoogleAnalytics.getInstance(this);


        setContentView(R.layout.activity_demo);
        tracker = analytics.newTracker("UA-84481758-1");
        tracker.enableExceptionReporting(true);
        // Enable Remarketing, Demographics & Interests reports
        // https://developers.google.com/analytics/devguides/collection/android/display-features
        tracker.enableAdvertisingIdCollection(true);

        // Enable automatic activity tracking for your app
        tracker.enableAutoActivityTracking(true);


        if (savedInstanceState != null) {
            mInterstitialWasInitialized = savedInstanceState.getBoolean("is_was_initialized", false);
        }

        String userId = "userId";
        String appKey = "551a23fd";

        // create the supersonic instance - this should be called when the activity starts
        mSupersonicInstance = SupersonicFactory.getInstance();

        // Supersonic Advertiser SDK call
        SupersonicAdsAdvertiserAgent.getInstance().reportAppStarted(this);
        // Be sure to set a listener to each product that is being initiated
        // set the Supersonic rewarded video listener
        mSupersonicInstance.setRewardedVideoListener(this);
        // init the supersonic rewarded video
        mSupersonicInstance.initRewardedVideo(this, appKey, userId);
        // set the Supersonic offerwall listener
        mSupersonicInstance.setOfferwallListener(this);
        // init the supersonic offerwall
        // set client side callbacks for the offerwall
        SupersonicConfig.getConfigObj().setClientSideCallbacks(true);
        mSupersonicInstance.initOfferwall(this, appKey, userId);
        // set the interstitial listener
        mSupersonicInstance.setInterstitialListener(this);
        // init the supersonic interstitial
        mSupersonicInstance.initInterstitial(this, appKey, userId);

        initUIElements();


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("is_was_initialized", mInterstitialWasInitialized);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // call the supersonic onResume method
        if (mSupersonicInstance != null)
            mSupersonicInstance.onResume(this);
        updateButtonsState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // call the supersonic onPause method
        if (mSupersonicInstance != null)
            mSupersonicInstance.onPause(this);
        updateButtonsState();
    }

    /**
     * Handle the button state according to the status of the supersonic producs
     */
    private void updateButtonsState() {
        if (mSupersonicInstance != null) {
            handleVideoButtonState(mSupersonicInstance.isRewardedVideoAvailable());
            handleOfferwallButtonState(mSupersonicInstance.isOfferwallAvailable());
        } else {
            handleVideoButtonState(false);
            handleOfferwallButtonState(false);
        }
        updateInterstitialButtonsState();
    }

    private void updateInterstitialButtonsState() {
        Log.d(TAG, "updateInterstitialButtonsState");
        if (mSupersonicInstance != null) {
            handleInterstitialButtonState(mInterstitialWasInitialized);
            DemoActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleInterstitialShowButtonState(mSupersonicInstance.isInterstitialReady());
                }
            });
        } else {
            handleInterstitialButtonState(false);
            handleInterstitialShowButtonState(false);
        }
    }


    /**
     * initialize the UI elements of the activity
     */
    private void initUIElements() {
        mVideoButton = (Button) findViewById(R.id.rv_button);
        mVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Video")
                        .build());
                // check if video is available
                if (mSupersonicInstance.isRewardedVideoAvailable())
                    //show rewarded video
                    mSupersonicInstance.showRewardedVideo();
            }
        });

        mOfferwallButton = (Button) findViewById(R.id.ow_button);
        mOfferwallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("OfferWall")
                        .build());
                //show the offerwall
                if (mSupersonicInstance.isOfferwallAvailable())
                    mSupersonicInstance.showOfferwall();
            }
        });

        mInterstitialButton = (Button) findViewById(R.id.is_button_1);
        mInterstitialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSupersonicInstance.loadInterstitial();
            }
        });


        mInterstitialShowButton = (Button) findViewById(R.id.is_button_2);
        mInterstitialShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Interstitial")
                        .build());

                // check if interstitial is available
                if (mSupersonicInstance.isInterstitialReady()) {
                    //show the interstitial
                    mSupersonicInstance.showInterstitial();
                }
            }
        });

        TextView versionTV = (TextView) findViewById(R.id.version_txt);
        versionTV.setText(getResources().getString(R.string.version) + " " + SupersonicUtils.getSDKVersion());
    }


    /**
     * Set the Rewareded Video button state according to the product's state
     *
     * @param available if the video is available
     */
    public void handleVideoButtonState(final boolean available) {
        final String text;
        final int color;
        if (available) {
            color = Color.BLUE;
            text = getResources().getString(R.string.show) + " " + getResources().getString(R.string.rv);
        } else {
            color = Color.BLACK;
            text = getResources().getString(R.string.initializing) + " " + getResources().getString(R.string.rv);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVideoButton.setTextColor(color);
                mVideoButton.setText(text);
                mVideoButton.setEnabled(available);

            }
        });
    }

    /**
     * Set the Rewareded Video button state according to the product's state
     *
     * @param available if the offerwall is available
     */
    public void handleOfferwallButtonState(final boolean available) {
        final String text;
        final int color;
        if (available) {
            color = Color.BLUE;
            text = getResources().getString(R.string.show) + " " + getResources().getString(R.string.ow);
        } else {
            color = Color.BLACK;
            text = getResources().getString(R.string.initializing) + " " + getResources().getString(R.string.ow);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mOfferwallButton.setTextColor(color);
                mOfferwallButton.setText(text);
                mOfferwallButton.setEnabled(available);

            }
        });

    }

    /**
     * Set the Interstitial button state according to the product's state
     *
     * @param available if the interstitial is available
     */
    public void handleInterstitialButtonState(final boolean available) {
        Log.d(TAG, "handleInterstitialButtonState | available: " + available);
        final String text;
        final int color;
        if (available) {
            color = Color.BLUE;
            text = getResources().getString(R.string.load) + " " + getResources().getString(R.string.is);
        } else {
            color = Color.BLACK;
            text = getResources().getString(R.string.initializing) + " " + getResources().getString(R.string.is);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mInterstitialButton.setTextColor(color);
                mInterstitialButton.setText(text);
                mInterstitialButton.setEnabled(available);
            }
        });

    }

    /**
     * Set the Show Interstitial button state according to the product's state
     *
     * @param available if the interstitial is available
     */
    public void handleInterstitialShowButtonState(final boolean available) {
        final int color;
        if (available) {
            color = Color.BLUE;
        } else {
            color = Color.BLACK;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mInterstitialShowButton.setTextColor(color);
                mInterstitialShowButton.setEnabled(available);
            }
        });
    }

    // --------- Supersonic Rewarded Video Listener ---------
    @Override
    public void onRewardedVideoInitSuccess() {
        // called on init success of rewarded video
        Log.d(TAG, "onRewardedVideoInitSuccess");
    }

    @Override
    public void onRewardedVideoInitFail(SupersonicError supersonicError) {
        // called on init fail of rewarded video
        // you can get the error data by accessing the supersonicError object
        // supersonicError.getErrorCode();
        // supersonicError.getErrorMessage();
        Log.d(TAG, "onRewardedVideoInitFail" + " " + supersonicError);
    }

    @Override
    public void onRewardedVideoAdOpened() {
        // called when the video is opened
        Log.d(TAG, "onRewardedVideoAdOpened");
    }

    @Override
    public void onRewardedVideoAdClosed() {
        // called when the video is closed
        Log.d(TAG, "onRewardedVideoAdClosed");
        // here we show a dialog to the user if he was rewarded
        if (mPlacement != null) {
            // if the user was rewarded
            showRewardDialog(mPlacement);
            mPlacement = null;
        }
    }

    @Override
    public void onVideoAvailabilityChanged(final boolean available) {
        // called when the video availbility has changed
        Log.d(TAG, "onVideoAvailabilityChanged" + " " + available);
        handleVideoButtonState(available);
    }


    @Override
    public void onVideoStart() {
        // called when the video has started
        Log.d(TAG, "onVideoStart");
    }

    @Override
    public void onVideoEnd() {
        // called when the video has ended
        Log.d(TAG, "onVideoEnd");
    }

    @Override
    public void onRewardedVideoAdRewarded(Placement placement) {
        // called when the video has been rewarded and a reward can be given to the user
        Log.d(TAG, "onRewardedVideoAdRewarded" + " " + placement);
        mPlacement = placement;

    }

    @Override
    public void onRewardedVideoShowFail(SupersonicError supersonicError) {
        // called when the video has failed to show
        // you can get the error data by accessing the supersonicError object
        // supersonicError.getErrorCode();
        // supersonicError.getErrorMessage();
        Log.d(TAG, "onRewardedVideoShowFail" + " " + supersonicError);
    }

    // --------- Supersonic Offerwall Listener ---------

    @Override
    public void onOfferwallInitSuccess() {
        // called when the offerwall has initiated successfully
        Log.d(TAG, "onOfferwallInitSuccess");
        handleOfferwallButtonState(true);
    }

    @Override
    public void onOfferwallInitFail(SupersonicError supersonicError) {
        // called when the offerwall has failed to init
        // you can get the error data by accessing the supersonicError object
        // supersonicError.getErrorCode();
        // supersonicError.getErrorMessage();
        Log.d(TAG, "onOfferwallInitFail" + " " + supersonicError);
    }

    @Override
    public void onOfferwallOpened() {
        // called when the offerwall has opened
        Log.d(TAG, "onOfferwallOpened");
    }

    @Override
    public void onOfferwallShowFail(SupersonicError supersonicError) {
        // called when the offerwall failed to show
        // you can get the error data by accessing the supersonicError object
        // supersonicError.getErrorCode();
        // supersonicError.getErrorMessage();
        Log.d(TAG, "onOfferwallShowFail" + " " + supersonicError);
    }

    @Override
    public boolean onOfferwallAdCredited(int credits, int totalCredits, boolean totalCreditsFlag) {
        Log.d(TAG, "onOfferwallAdCredited" + " credits:" + credits + " totalCredits:" + totalCredits + " totalCreditsFlag:" + totalCreditsFlag);
        return false;
    }

    @Override
    public void onGetOfferwallCreditsFail(SupersonicError supersonicError) {
        // you can get the error data by accessing the supersonicError object
        // supersonicError.getErrorCode();
        // supersonicError.getErrorMessage();
        Log.d(TAG, "onGetOfferwallCreditsFail" + " " + supersonicError);
    }

    @Override
    public void onOfferwallClosed() {
        // called when the offerwall has closed
        Log.d(TAG, "onOfferwallClosed");
    }

    // --------- Supersonic Interstitial Listener ---------
    @Override
    public void onInterstitialInitSuccess() {
        // called the interstitial has initiated successfully
        Log.d(TAG, "onInterstitialInitSuccess");
        mInterstitialWasInitialized = true;
        updateInterstitialButtonsState();
    }

    @Override
    public void onInterstitialInitFailed(SupersonicError supersonicError) {
        // called when the interstitial has failed it initialize
        // you can get the error data by accessing the supersonicError object
        // supersonicError.getErrorCode();
        // supersonicError.getErrorMessage();
        Log.d(TAG, "onInterstitialInitFail" + " " + supersonicError);
        mInterstitialWasInitialized = false;
        updateInterstitialButtonsState();
    }

    @Override
    public void onInterstitialReady() {
        // called when the interstitial is ready
        Log.d(TAG, "onInterstitialReady");
        updateInterstitialButtonsState();
    }

    @Override
    public void onInterstitialLoadFailed(SupersonicError supersonicError) {
        // called when the interstitial has failed to load
        // you can get the error data by accessing the supersonicError object
        // supersonicError.getErrorCode();
        // supersonicError.getErrorMessage();
        Log.d(TAG, "onInterstitialLoadFailed" + " " + supersonicError);
        updateInterstitialButtonsState();
    }

    @Override
    public void onInterstitialOpen() {
        // called when the interstitial is shown
        Log.d(TAG, "onInterstitialOpen");
    }

    @Override
    public void onInterstitialClose() {
        // called when the interstitial has been closed
        Log.d(TAG, "onInterstitialAdClosed");
        updateInterstitialButtonsState();
    }

    @Override
    public void onInterstitialShowSuccess() {
        // called when the interstitial has been successfully shown
        Log.d(TAG, "onInterstitialShowSuccess");
    }

    @Override
    public void onInterstitialShowFailed(SupersonicError supersonicError) {
        // called when the interstitial has failed to show
        // you can get the error data by accessing the supersonicError object
        // supersonicError.getErrorCode();
        // supersonicError.getErrorMessage();
        Log.d(TAG, "onInterstitialShowFail" + " " + supersonicError);
        updateInterstitialButtonsState();
    }

    @Override
    public void onInterstitialClick() {
        // called when the interstitial has been clicked
        Log.d(TAG, "onInterstitialAdClicked");
    }

    public void showRewardDialog(Placement placement) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DemoActivity.this);
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setTitle(getResources().getString(R.string.rewarded_dialog_header));
        builder.setMessage(getResources().getString(R.string.rewarded_dialog_message) + " " + placement.getRewardAmount() + " " + placement.getRewardName());
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }



}
