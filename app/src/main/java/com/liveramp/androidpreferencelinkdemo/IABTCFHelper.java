package com.liveramp.androidpreferencelinkdemo;

import android.util.Log;

import com.iabtcf.decoder.TCString;
import com.iabtcf.exceptions.TCStringDecodeException;
import com.iabtcf.utils.IntIterable;
import com.iabtcf.utils.IntIterator;

import java.util.Set;

public class IABTCFHelper {

    // This class implements the IABtcfv2 decoder library, not maintained by LiveRamp
    // We'll use this to assist in decoding of the string
    // Our GDPR SDK does not support automatically reading in the string today

    static String LOG_TAG = IABTCFHelper.class.getSimpleName();
    static TCString decodedTcString;
    static String originalString;


    public static void setAndDecodeString(String s) {
        originalString = s;
        try {
            decodedTcString = TCString.decode(s);
        } catch (Exception e){
            // Boo hoo, we got a bad string. Try to test with legit strings, how about that?
            Log.e(LOG_TAG, e.getMessage());
        }
    }


    public static String getStringForStorage(){
        return originalString;
    }

    //region Helper methods for the CMP manual sync

    public static Set<Integer> getSpecialFeaturesAllowedAsSet(){
        return decodedTcString.getSpecialFeatureOptIns().toSet();
    }

    public static Set<Integer> getPurposesAllowedAsSet(){
        return decodedTcString.getPurposesConsent().toSet();
    }

    public static Set<Integer> getPurposesLegIntAllowedAsSet(){
        return decodedTcString.getPurposesLITransparency().toSet();
    }

    public static Set<Integer> getVendorsAllowedAsSet(){
        return decodedTcString.getVendorConsent().toSet();
    }

    public static Set<Integer> getVendorLegIntAllowedAsSet(){
        return decodedTcString.getVendorLegitimateInterest().toSet();
    }
    //endregion


    // Additional TCF fields - not needed.
    public static Boolean getPubAllowedPurposes() {
        if (decodedTcString.getPubPurposesConsent().isEmpty()) {
            return false;
        } return true;
    }

    public static Boolean getPubAllowedLegInt() {
        if (decodedTcString.getPubPurposesLITransparency().isEmpty()) {
            return false;
        } return true;
    }

    public static String getIteratorString(IntIterator iter) {

        if (!iter.hasNext()) {
            return "";
        }

        String buildString = "";

        do {
            buildString += " " + iter.next();
        } while (iter.hasNext());

        Log.i(LOG_TAG,"Formed: " + buildString);
        return buildString;
    }


    // Helper method so we can visualize the consent string
    public static String generateViewString() {

        Boolean hasLR;
        IntIterable specialFeaturesAllowed, purposesAllowed, purposesLegIntAllowed, vendorsAllowed, vendorLegIntAllowed;
        IntIterable pubAllowedPurposes, pubAllowedLegIntPurposes;
        Integer consentScreenID;

        try {
            decodedTcString.getVersion();

            // TODO: we gotta work on this one...
            specialFeaturesAllowed = decodedTcString.getSpecialFeatureOptIns();
            purposesAllowed = decodedTcString.getPurposesConsent();
            purposesLegIntAllowed = decodedTcString.getPurposesLITransparency(); // ???
            vendorsAllowed = decodedTcString.getVendorConsent();
            vendorLegIntAllowed = decodedTcString.getVendorLegitimateInterest();

            // Pub section
            pubAllowedPurposes = decodedTcString.getPubPurposesConsent();
            pubAllowedLegIntPurposes = decodedTcString.getPubPurposesLITransparency();

            consentScreenID = decodedTcString.getConsentScreen();

            // ID


            hasLR = decodedTcString.getVendorConsent().contains(97);
        } catch (TCStringDecodeException e) {
            Log.e(LOG_TAG, e.getMessage());
            throw e;
        }

        String outputDisplayString =
                "Has LR in Vendor Consent: " + hasLR.toString() + "\n" +
                        "specialFeaturesAllowed " + getIteratorString(specialFeaturesAllowed.intIterator()) + "\n" +
                        "purposesAllowed: " + getIteratorString(purposesAllowed.intIterator()) + "\n" +
                        "purposesLegIntAllowed: " + getIteratorString(purposesLegIntAllowed.intIterator()) + "\n" +
                        "vendorsAllowed: " + getIteratorString(vendorsAllowed.intIterator()) + "\n" +
                        "vendorLegIntAllowed: " + getIteratorString(vendorLegIntAllowed.intIterator()) + "\n" +
                        "consentScreenID" + consentScreenID + "\n" +
                        "pubAllowedPurposes: " + getIteratorString(pubAllowedPurposes.intIterator()) + "\n" +
                        "pubAllowedLegIntPurposes: " + getIteratorString(pubAllowedLegIntPurposes.intIterator()) + "\n" +
                        "Current Consent String: " + originalString + "\n\n\n" +
                        "";

        Log.d(LOG_TAG, "Output string: " + outputDisplayString);
        return outputDisplayString;
    }

}
