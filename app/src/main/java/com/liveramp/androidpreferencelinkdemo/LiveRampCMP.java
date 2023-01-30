package com.liveramp.androidpreferencelinkdemo;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.liveramp.mobilesdk.Error;
import com.liveramp.mobilesdk.LRConsentStringUpdateCallback;
import com.liveramp.mobilesdk.LRPrivacyManager;
import com.liveramp.mobilesdk.LRPrivacyManagerConfig;
import com.liveramp.mobilesdk.lrcallbacks.LRCompletionHandlerCallback;
import com.liveramp.mobilesdk.model.ConsentData;
import com.liveramp.mobilesdk.model.configuration.Configuration;
import com.liveramp.mobilesdk.tcstring.publishertc.PublisherConsent;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class LiveRampCMP {

    // A quick (shoddy) implementation of the GDPR SDK, specifically for demonstration purposes
    static final String FALLBACK_CONFIGURATION_FILE = "tcf_fallback_configuration.json";
    static final String APP_ID = "82146e51-db20-49e7-9cc3-30e425e3f5a5"; // For demo purposes only
    static final String LOG_TAG = LiveRampCMP.class.getSimpleName();

    // Example test string
    // static String lr_testString = "CPSzMzJPTeGtxADABCENB_CoAP_AAEJAAAAADGwBAAGABPADCAY0BjYAgADAAngBhAMaAAA.YAAAAAAAA4AA";

    // Call this function to "sync" the consent string from PrefLink enterprise only if one is found.
    public static void setUserConsentFromPrefLink(LRCompletionHandlerCallback callback, String tcString) {

        IABTCFHelper.setAndDecodeString(tcString);

        // Assemble the vendor stuff.
        Set<Integer> specialFeaturesAllowed = null, purposesAllowed = null, purposesLegIntAllowed = null, vendorsAllowed = null, vendorLegIntAllowed = null;

        // Leverage helper library to parse out the TCF string for the relevant bits.
        specialFeaturesAllowed = IABTCFHelper.getSpecialFeaturesAllowedAsSet();
        purposesAllowed = IABTCFHelper.getPurposesAllowedAsSet();
        purposesLegIntAllowed = IABTCFHelper.getPurposesLegIntAllowedAsSet();
        vendorsAllowed = IABTCFHelper.getVendorsAllowedAsSet();
        vendorLegIntAllowed = IABTCFHelper.getVendorLegIntAllowedAsSet();

        // Assemble the publisher portion of the consent
        PublisherConsent pubConsent = new PublisherConsent(IABTCFHelper.getPubAllowedPurposes(), IABTCFHelper.getPubAllowedLegInt());

        // Provide the consent data from above.
         ConsentData consentData = new ConsentData(specialFeaturesAllowed, purposesAllowed, purposesLegIntAllowed, vendorsAllowed, vendorLegIntAllowed, pubConsent);
        LRPrivacyManager.INSTANCE.giveConsent(callback, consentData, 0); // We're syncing this; so no screen was presented (0)
    }


    // NOTE: pass in app context
    public static void initializeCMP(Activity activity, Context appContext, Context activityContext, String uniqueIdentifier) {

        Configuration fallbackConfiguration = new Configuration(getFallbackConfiguration(appContext));

        LRPrivacyManager.INSTANCE.configure(new LRPrivacyManagerConfig(APP_ID, fallbackConfiguration, null)); // {What if we passed in.. instanceID: "12345" for my custom PL sync})

        LRPrivacyManager.INSTANCE.initialize(activityContext, new LRCompletionHandlerCallback() {
            @Override
            public void invoke(boolean success, @Nullable Error error) {
                if (success) {

                    Log.d(LOG_TAG, "CMP successfully initialized");

                    if (PLRequestHelper.isPrefLinkEnterpriseEnabled) {

                        // Call preference sync enterprise first to see if there is consent already for the given user
                        PLRequestHelper.fetchSubjectDataByIdentifier(activityContext, uniqueIdentifier,
                                new PLRequestHelper.PLApiResponseCallback() {
                            @Override
                            public void successCallback(String consentString) {
                                // This means we found a consent string; use it right away.
                                Log.d(LOG_TAG, "Got consent string back from PL: " + consentString);

                                // Set this within our helper class for later.
                                IABTCFHelper.setAndDecodeString(consentString);

                                // If the string failed to decode, it was likely a bad string; do not set it and just make a new one.
                                if (IABTCFHelper.decodedTcString == null) {
                                    Log.e(LOG_TAG, "Could not decode string; invalid. Making a new one now..");
                                    showPrompt(activity);
                                } else {
                                    // Use the CMP + IAB util library to set this consent "manually"
                                    setUserConsentFromPrefLink((b, error1) ->
                                                    Log.d(LOG_TAG, "Now, setUserConsentFromPrefLink: " + b),
                                            consentString);
                                }

                                                            }
                            @Override
                            public void notFoundCallback() {
                                // This means we didn't find a consent string for that user; generate one here.
                                Log.d(LOG_TAG, "User doesn't yet exist in PL - let us surface the prompty!");
                                showPrompt(activity);
                            }

                                    @Override
                            public void failureCallback() {
                                // This means something else unexpected happened; retry the request
                                Log.d(LOG_TAG, "Something failed; no consent string back: ");
                            }
                        });

                    } else {
                        // Show the prompt for now if it isn't enabled
                        // TODO: Check for whether we actually have consent
                        showPrompt(activity);
                    }



                } else {
                    // error state
                    Log.e(LOG_TAG, error.toString());
                }
            }
        });
    }


    public static void showPrompt(Activity activity){

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LRPrivacyManager.INSTANCE.presentUserInterface(new LRCompletionHandlerCallback() {
                    @Override
                    public void invoke(boolean b, @Nullable Error error) {
                        // UI presented.
                    }
                });
            }
        });
    }


    public static void setCallBackForConsentUpdate(LRConsentStringUpdateCallback cbFun) {
        LRPrivacyManager.INSTANCE.setConsentStringUpdateCallback(cbFun);
    }


    // Helper to read configuration from assets file
    @Nullable
    public static final byte[] getFallbackConfiguration(@NotNull Context context) {
        byte[] buffer = null;

        try {
            InputStream is = context.getResources()
                    .getAssets()
                    .open(FALLBACK_CONFIGURATION_FILE);
            int size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
        } catch (IOException e) {
            return null;
        }
        return buffer;
    }

}
