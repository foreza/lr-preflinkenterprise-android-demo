package com.liveramp.androidpreferencelinkdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.liveramp.mobilesdk.LRConsentStringUpdateCallback;

public class MainActivity extends AppCompatActivity  {
    String LOG_TAG = "PrefLink-AndroidDemo";

    Integer currentUserID = 1984; // As an example.

    // Used for fallback config - remember to add into your src/main/assets
    // String config_url = "https://gdpr-wrapper.privacymanager.io/gdpr/82146e51-db20-49e7-9cc3-30e425e3f5a5/gdpr-mobile-liveramp.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         setupView(this);
    }


    // Helper method that will init our CMP, and set callbacks
    public void initCMP(Context ctx, String uniqueIdentifier) {

        // CMP must be initialized before we do anything
        LiveRampCMP.initializeCMP(MainActivity.this, getApplicationContext(), ctx, uniqueIdentifier);

        // Set callback so we know if consent state changes
        LiveRampCMP.setCallBackForConsentUpdate(new LRConsentStringUpdateCallback() {

            @Override
            // Every time we get an updated consent string, attempt to persist it to PL right away.
            public void vendorStringUpdated(@NonNull String s) {
                Log.d(LOG_TAG, "vendorStringUpdated: " + s);

                IABTCFHelper.setAndDecodeString(s);

                if (PLRequestHelper.isPrefLinkEnterpriseEnabled) {

                    PLRequestHelper.addSubjectDataForIdentifier(ctx, uniqueIdentifier,
                            IABTCFHelper.getStringForStorage(), new PLRequestHelper.PLApiResponseCallback() {
                                @Override
                                public void successCallback(String consentString) {
                                    Log.d(LOG_TAG, "(vendorStringUpdated) PL updated with: " + consentString);
                                }

                                @Override
                                public void notFoundCallback() {
                                    // Hmm..
                                    Log.e(LOG_TAG, "(vendorStringUpdated) PL somehow failed to find: ");
                                }

                                @Override
                                public void failureCallback() {
                                    Log.e(LOG_TAG, "(vendorStringUpdated) PL somehow failed to update with: ");
                                }
                            });
                }
                // this stays here
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
                    ((TextView)findViewById(R.id.valueForUser)).setText(currentUserID.toString());
                }
            });
        } catch (Exception e) {
            Log.e(LOG_TAG, "failed with:", e);
        }

    }

    private void setupView(Context ctx){

        Button btnCMP = findViewById(R.id.buttonInitCMp);

        btnCMP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                currentUserID = getUserIDFromInput();
                initCMP(ctx, getUserIDFromInput().toString());
            }
        });

    }

    private Integer getUserIDFromInput() {

        EditText editText = findViewById(R.id.editTextNumber);

        Integer retValue;
        try {
            retValue = Integer.parseInt(editText.getText().toString());

            if (retValue < 1000 || retValue > 9999) {
                // We'll make this easy for now..
                retValue = 1984;
            }

        } catch (Exception e) {
            retValue = 1984;
        }

        return retValue;
    }


}