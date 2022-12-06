package com.liveramp.androidpreferencelinkdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.liveramp.mobilesdk.LRConsentStringUpdateCallback;

public class MainActivity extends AppCompatActivity  {
    String LOG_TAG = "PrefLink-AndroidDemo";

    // Used for fallback config - remember to add into your src/main/assets
    // String config_url = "https://gdpr-wrapper.privacymanager.io/gdpr/82146e51-db20-49e7-9cc3-30e425e3f5a5/gdpr-mobile-liveramp.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCMP();
    }


    // Helper method that will init our CMP, and set callbacks
    public void initCMP() {

        // CMP must be initialized before we do anything
        LiveRampCMP.initializeCMP(getApplicationContext(), this);

        // Set callback so we know if consent state changes
        LiveRampCMP.setCallBackForConsentUpdate(new LRConsentStringUpdateCallback() {
            @Override
            public void vendorStringUpdated(@NonNull String s) {
                Log.d(LOG_TAG, "vendorStringUpdated: " + s);
                IABTCFHelper.setAndDecodeString(s);
                updateViewForString();
            }

            @Override
            public void customVendorStringUpdated(@NonNull String s) {
                Log.d(LOG_TAG, "customVendorStringUpdated: " + s);
            }

            @Override
            public void vendorStringUpdateFailed() {
                Log.e(LOG_TAG, "vendorStringUpdateFailed: ");

            }

            @Override
            public void customVendorStringUpdateFailed() {
                Log.e(LOG_TAG, "customVendorStringUpdateFailed: ");
            }
        });
    }


    // Sample app purposes: update the view with the string
    public void updateViewForString() {

        // For testing purposes, we'll leverage our 3P lib to generate a string that we can throw up
        String outputDisplayString = IABTCFHelper.generateViewString();

        try {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView)findViewById(R.id.consentStringInfo)).setText(outputDisplayString);
                }
            });
        } catch (Exception e) {
            Log.e(LOG_TAG, "failed with:", e);
        }

    }
}