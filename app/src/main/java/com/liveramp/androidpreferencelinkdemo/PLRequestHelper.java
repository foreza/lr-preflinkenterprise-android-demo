package com.liveramp.androidpreferencelinkdemo;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PLRequestHelper {

    private static String LOG_TAG = PLRequestHelper.class.getSimpleName();

    private static RequestQueue requestQueue;
    static final boolean isPrefLinkEnterpriseEnabled = true;

    // TODO: Remove code duplication here. It'll be here now for example purposes -
    // Also - note that the POST data depends on the schema set up within pref link
    public static void addSubjectDataForIdentifier(Context ctx, String identifier, String consentString, PLApiResponseCallback callback) {

        // Create a RequestQueue to handle the HTTP request
        if (requestQueue != null) {
            requestQueue = Volley.newRequestQueue(ctx);
        }

        // Create JSON object
        JSONObject obj = null;
        try {
            obj = new JSONObject();
            obj.put("identifying_field", "custom_user_id");           // Note: Your identifying_field MUST be present as well!
            obj.put("custom_user_id", Integer.parseInt(identifier));        // You must first ensure this value exists in the schema - create this in the Console
            obj.put("consentString", consentString);                        // Also remember to set the right type. You'll get a 400 if the type isn't correct
        } catch (JSONException e) {
            // TODO: Handle.
        }

        if (obj == null) {
            Log.e(LOG_TAG, "JSON object was null");
            return;
        }

        String requestURL = PLEnterpriseConstants.endpoint;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, requestURL, obj,
                response -> {
                    // Handle the JSON response here
                    String retString = "test";
                    try {
                        retString = response.getJSONObject("data").getString("consentString");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callback.successCallback(retString);
                },
                error -> {
                    Log.e(LOG_TAG, util_getErrorFromByteArr(error.networkResponse.data) +
                            "Status code: " + error.networkResponse.statusCode);
                    callback.failureCallback();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("data-access-key", PLEnterpriseConstants.DATA_KEY);
                return params;
            }
        };

        // Add the request to the RequestQueue
        requestQueue.add(request);
    }


    // Nifty little util method to grab out the response
    private static String util_getErrorFromByteArr(byte[] dataArr) {
        return new String(dataArr, StandardCharsets.UTF_8);
    }

    public static void fetchSubjectDataByIdentifier(Context ctx, String identifier, PLApiResponseCallback callback) {
        // Create a RequestQueue to handle the HTTP request
        requestQueue = Volley.newRequestQueue(ctx);

        String requestURL = PLEnterpriseConstants.endpoint + identifier;

        // Make a GET request to the specified endpoint
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestURL, null,
                response -> {
                    // Handle the JSON response here
                    String retString = "test";
                    try {
                        // We're only messing with consent string but we might want other things
                        retString = response.getJSONObject("data").getString("consentString");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callback.successCallback(retString);
                },
                error -> {
                    Log.e(LOG_TAG, util_getErrorFromByteArr(error.networkResponse.data) +
                            "Status code: " + error.networkResponse.statusCode);

                    // If the user doesn't exist; we should get a 404 not found.
                    // Signal to our CMP helper class that we should add the user now!
                    if (error.networkResponse.statusCode == 404) {
                        callback.notFoundCallback();
                    } else {
                        // Fire our generic error otherwise. If you get this, retry the request.
                        callback.failureCallback();
                    }

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("data-access-key", PLEnterpriseConstants.DATA_KEY);
                return params;
            }
        };

        // Add the request to the RequestQueue
        requestQueue.add(request);
    }


    // Quick interface so we can pass data back and forth
    public interface PLApiResponseCallback {

        void successCallback(String consentString);
        void notFoundCallback();
        void failureCallback();

    }

    // I trust you'll find a better way to store this.
    protected class PLEnterpriseConstants {


        // TODO: Replace with your instance ID and account ID
        static final String instanceId = "";     // "TODO";
        static final String accountId = "";       // "TODO";

        // TODO: Fill this in. Use your own app key for this.
        static final String DATA_KEY = "";

        static final String endpoint = "https://data-api." + instanceId + "." + accountId + ".preferencelink.com/v1/subject-data/";

    }

}



