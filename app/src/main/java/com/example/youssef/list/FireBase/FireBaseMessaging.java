package com.example.youssef.list.fireBase;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.youssef.list.LoginActivity;
import com.example.youssef.list.MainActivity;
import com.example.youssef.list.R;
import com.example.youssef.list.presenters.ListPresenter;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by Youssef on 10/11/2016.
 */

public class FireBaseMessaging extends FirebaseMessagingService {
    public static String TAG = "FirebaseNotification";
    private boolean notified = false;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Map<String,String> messageMap = remoteMessage.getData();
            SharedPreferences sp = getApplication().getSharedPreferences("account",MODE_PRIVATE);
            String curUserId = sp.getString("username","");
            try {
                JSONObject messageData = new JSONObject(messageMap.get("message"));
                String username = messageData.getString("username");
                String action = messageData.getString("action");
                String item = messageData.getString("item");
                if(!username.equals(curUserId) && !notified) {
                    if(!isRunning())
                        sendNotification(action, username, item);
                    else
                        EventBus.getDefault().post(new ListPresenter.MessageEvent("refresh"));
                }
//                sendNotification(action, username, item);

                Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
//            sendNotification(remoteMessage.getNotification().getBody());
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    private boolean isRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> services = activityManager
                .getRunningTasks(Integer.MAX_VALUE);
        boolean isActivityFound = false;

        if (services.get(0).topActivity.getPackageName().toString()
                .equalsIgnoreCase(getPackageName().toString())) {
            isActivityFound = true;
        }

        if (isActivityFound)
            return true;
         else
            return false;


    }
    private void sendNotification(String action, String username, String item) {
        notified = true;
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                notified = false;
//            }
//        }, 60000);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String title = "";
        String message = "";
        message += username + getResources().getString(R.string.just);
        if(action.equals("add")) {
            message += getResources().getString(R.string.added);
            title = getResources().getString(R.string.new_item_title);
        }else if (action.equals("remove")){
            message += getResources().getString(R.string.removed);
            title = getResources().getString(R.string.removed_title);
        }else if(action.equals("check")){
            message += getResources().getString(R.string.checked);
            title = getResources().getString(R.string.checked_title);
        }else if(action.equals("uncheck")){
            message += getResources().getString(R.string.unchecked);
            title = getResources().getString(R.string.unchecked_title);
        }
        message += item;
        if(action.equals("add")) {
            message += getResources().getString(R.string.to_the_list);
        }else if(action.equals("remove")){
            message += getResources().getString(R.string.from_the_list);
        }else if(action.equals("check"))
            message += getResources().getString(R.string.on_the_list);
        Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.ic_notification);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
