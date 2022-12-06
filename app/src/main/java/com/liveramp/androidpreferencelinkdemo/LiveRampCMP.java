package com.liveramp.androidpreferencelinkdemo;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.liveramp.mobilesdk.Error;
import com.liveramp.mobilesdk.LRConsentStringUpdateCallback;
import com.liveramp.mobilesdk.LRPrivacyManager;
import com.liveramp.mobilesdk.LRPrivacyManagerConfig;
import com.liveramp.mobilesdk.LRPrivacyManagerKt;
import com.liveramp.mobilesdk.lrcallbacks.LRCompletionHandlerCallback;
import com.liveramp.mobilesdk.lrcallbacks.tcfcommands.TcDataCallback;
import com.liveramp.mobilesdk.model.ConsentData;
import com.liveramp.mobilesdk.model.configuration.Configuration;
import com.liveramp.mobilesdk.model.tcfcommands.TCData;
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
    static String lr_testString = "CPSzMzJPTeGtxADABCENB_CoAP_AAEJAAAAADGwBAAGABPADCAY0BjYAgADAAngBhAMaAAA.YAAAAAAAA4AA";

    // TODO: Can our SDK have a method that basically does this?
    public static void setUserConsentFromPrefLink(LRCompletionHandlerCallback callback, String tcString) {

        IABTCFHelper.setAndDecodeString(tcString);

        Set<Integer> specialFeaturesAllowed = null, purposesAllowed = null, purposesLegIntAllowed = null, vendorsAllowed = null, vendorLegIntAllowed = null;

        specialFeaturesAllowed = IABTCFHelper.getSpecialFeaturesAllowedAsSet();
        purposesAllowed = IABTCFHelper.getPurposesAllowedAsSet();
        purposesLegIntAllowed = IABTCFHelper.getPurposesLegIntAllowedAsSet();
        vendorsAllowed = IABTCFHelper.getVendorsAllowedAsSet();
        vendorLegIntAllowed = IABTCFHelper.getVendorLegIntAllowedAsSet();

        ConsentData consentData = new ConsentData(specialFeaturesAllowed, purposesAllowed, purposesLegIntAllowed, vendorsAllowed, vendorLegIntAllowed, new PublisherConsent(true, true));

        LRPrivacyManager.INSTANCE.giveConsent(callback, consentData, 1); // what does that int do?
    }


    // NOTE: pass in app context
    public static void initializeCMP(Context appContext, Context activityContext) {

        Configuration fallbackConfiguration = new Configuration(getFallbackConfiguration(appContext));
        LRPrivacyManager.INSTANCE.configure(new LRPrivacyManagerConfig(APP_ID, fallbackConfiguration, null));

        LRPrivacyManager.INSTANCE.initialize(activityContext, new LRCompletionHandlerCallback() {
            @Override
            public void invoke(boolean success, @Nullable Error error) {
                if (success) {

                    Log.d(LOG_TAG, "CMP successfully initialized");

                    // Call preference sync enterprise first to see if there is consent already for the given user
                    // TODO: Do API call to pref link
                    // If there is, set the method in the CMP
                    setUserConsentFromPrefLink(new LRCompletionHandlerCallback() {
                        @Override
                        public void invoke(boolean b, @Nullable Error error) {
                            Log.d(LOG_TAG, "setUserConsentFromPrefLink: " + b);
                        }
                    }, lr_testString);

                } else {
                    // error state
                    Log.e(LOG_TAG, error.toString());
                }
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
