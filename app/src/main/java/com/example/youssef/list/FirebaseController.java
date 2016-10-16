package com.example.youssef.list;

import android.*;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by Youssef on 10/14/2016.
 */

public class FirebaseController {
    private Activity mActivity;

    public FirebaseController(Activity activity){
        mActivity = activity;
    }
    public String getAccount() {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= 23){
            boolean hasPermission = (ContextCompat.checkSelfPermission(mActivity,
                    android.Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        112);
            }
        }
        if(true){
            Account[] accounts = AccountManager.get(mActivity).
                    getAccountsByType("com.google");
            if (accounts.length == 0) {
                return null;
            }
            return accounts[0].name;
        }
        return null;
    }
    public String getFirebaseToken(){
        String accountName = getAccount();

// Initialize the scope using the client ID you got from the Console.
        final String scope = "audience:server:client_id:"
                + "690398649295-34acu0fdeu07ob8k0e2dk7lisakdnpp0.apps.googleusercontent.com";
        String idToken = null;
        try {
            idToken = GoogleAuthUtil.getToken(mActivity.getApplicationContext(), accountName, scope);
        } catch (Exception e) {
            Log.d("error", "exception while getting idToken: " + e);
        }
        return idToken;
    }
    public String addNotificationKey(
            String senderId, String userEmail, String registrationId, String idToken)
            throws IOException, JSONException {
        URL url = new URL("https://android.googleapis.com/gcm/googlenotification");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);

        // HTTP request header
        con.setRequestProperty("project_id", senderId);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestMethod("POST");
//        con.connect();

        // HTTP request
        JSONObject data = new JSONObject();
        data.put("operation", "add");
        data.put("notification_key_name", userEmail);
        data.put("registration_ids", new JSONArray(Arrays.asList(registrationId)));
        data.put("id_token", idToken);

        OutputStream os = con.getOutputStream();
        os.write(data.toString().getBytes("UTF-8"));
        os.close();

        // Read the response into a string
        InputStream is = con.getInputStream();
        String responseString = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
        is.close();

        // Parse the JSON string and return the notification key
        JSONObject response = new JSONObject(responseString);
        return response.getString("notification_key");

    }
    public String createGroup(String senderId, String userEmail, String registrationId, String idToken)
            throws IOException, JSONException {
        URL url = new URL("https://android.googleapis.com/gcm/googlenotification");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);

        // HTTP request header
        con.setRequestProperty("project_id", senderId);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestMethod("POST");
        con.connect();

        // HTTP request
        JSONObject data = new JSONObject();
        data.put("operation", "add");
        data.put("notification_key_name", userEmail);
        data.put("registration_ids", new JSONArray(Arrays.asList(registrationId)));
        data.put("id_token", idToken);

        OutputStream os = con.getOutputStream();
        os.write(data.toString().getBytes("UTF-8"));
        os.close();

        // Read the response into a string
        InputStream is = con.getInputStream();
        String responseString = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
        is.close();

        // Parse the JSON string and return the notification key
        JSONObject response = new JSONObject(responseString);
        return response.getString("notification_key");

    }
}
