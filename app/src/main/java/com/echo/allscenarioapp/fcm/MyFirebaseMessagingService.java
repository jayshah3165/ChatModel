package com.echo.allscenarioapp.fcm;


import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.activity.MainActivity;
import com.echo.allscenarioapp.activity.OneToOneChatActivity;
import com.echo.allscenarioapp.activity.SplashActivity;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Pref;
import com.echo.allscenarioapp.utils.Utils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
       // {data=EchoIt Dev sent new message}
        Utils.print("remoteMessage: " + remoteMessage.getData().toString());
        Utils.print("From: " + remoteMessage.getFrom());

        Message message = new Message();
        message.obj = remoteMessage;
        handler.sendMessage(message);

        //{body=[{"tag":"voice_calling","fullname":"Jaydip","message":"is calling you","userid":"36"}], icon=ic_launcher, sound=default, title=[{"tag":"voice_calling","fullname":"Jaydip","message":"is calling you","userid":"36"}]}
        Intent pushNotification = new Intent(Const.PUSH_NOTIFICATION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);


    }

    private void parsing(String data) {

        try {
            Utils.print("=====================DATA >>> " + data);
            JSONObject jsonObject = new JSONArray(data).getJSONObject(0);
           /* Utils.print(TAG, "message: " + jsonObject.getString("message"));
            Utils.print(TAG, "tag: " + jsonObject.getString("tag"));*/
            if (OneToOneChatActivity.oneToOneChatActivity!=null && !OneToOneChatActivity.oneToOneChatActivity.isFinishing()){
                if (!OneToOneChatActivity.oneToOneChatActivity.chatListModel.user_id.equalsIgnoreCase(jsonObject.optString("user_id"))){

                    sendNotification(jsonObject);
                }
            }else {
                sendNotification(jsonObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();// ignore this error ,this error not show if user come from notification   otherwise its shows always
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
          //  {data=EchoIt Dev sent new message}
            RemoteMessage remoteMessage = (RemoteMessage) msg.obj;
            // Check if message contains a data payload.
            if (remoteMessage.getData().size() > 0) {
                Utils.print("Message SZDASD data payload: " + remoteMessage.getData().get("body"));
                parsing(remoteMessage.getData().get("body"));

//                {body=[{"tag":"momentlike","message":"mark like your moment"}], icon=ic_launcher, sound=default, title=[{"tag":"momentlike","message":"mark like your moment"}]}
            }

            // Check if message contains a notification payload.
            if (remoteMessage.getNotification() != null) {
                // Utils.print(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
                // Utils.print(TAG, "Message Notification Body: " + remoteMessage.getNotification().getTag());
//                Message Notification Body: [{"tag":"voice_calling",// "fullname":"Jaydip","message":"is calling you","userid":"36"}]
                Utils.print("===========remoteMessage.getNotification().getBody() >>> " + remoteMessage.getNotification().getBody());
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONArray(remoteMessage.getNotification().getBody()).getJSONObject(0);
                    sendNotification(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    };

    private void sendNotification(JSONObject jsonObject) {





        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
// get the info from the currently running task
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

        Log.i("current task :", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());



        String message =  jsonObject.optString("msg");


        Utils.print("==========jsonObject====value >> " + jsonObject);




        PendingIntent pendingIntent = null;
        int noti_type = jsonObject.optInt("tag");
        String user_id = jsonObject.optString("user_id");

        if (Pref.getStringValue(this, Const.PREF_USERID, "") == null || Pref.getStringValue(this, Const.PREF_USERID, "").trim().isEmpty() || Pref.getStringValue(this, Const.PREF_USERID, "").equalsIgnoreCase("0")) {

            Intent intent2 = new Intent(this, MainActivity.class);
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //| IntentCompat.FLAG_ACTIVITY_CLEAR_TASK
            pendingIntent = PendingIntent.getActivity(this, 0, intent2, PendingIntent.FLAG_ONE_SHOT);

        } else {

            Intent intent1 = new Intent(this, MainActivity.class);

            Utils.print("======notificationListAdapter.objList.get(lastPos).noti_type  >> " + noti_type + "::" + user_id);
            if (noti_type == 1 ) {
                //Single Moment
                intent1 = new Intent(this, MainActivity.class);
            }


            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //| IntentCompat.FLAG_ACTIVITY_CLEAR_TASK
            pendingIntent = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_ONE_SHOT);


        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Random r = new Random();
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = null;

        int id = r.nextInt(80 - 65) + 65;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel mChannel = new NotificationChannel(String.valueOf(id), message, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription(message);
            mChannel.setLightColor(Color.CYAN);
            mChannel.canShowBadge();
            mChannel.setShowBadge(true);

            notificationBuilder = new NotificationCompat.Builder(this, String.valueOf(id));
            notificationBuilder.setSmallIcon(getNotificationIcon(notificationBuilder))
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.mipmap.ic_launcher))
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setChannelId(String.valueOf(id))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

            notificationManager.createNotificationChannel(mChannel);


        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notificationBuilder = new NotificationCompat.Builder(this, String.valueOf(id));
            notificationBuilder.setSmallIcon(getNotificationIcon(notificationBuilder))
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.mipmap.ic_launcher))
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        } else {
            notificationBuilder = new NotificationCompat.Builder(this, String.valueOf(id));
            notificationBuilder.setSmallIcon(getNotificationIcon(notificationBuilder))
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.mipmap.ic_launcher))
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);
        }


        notificationManager.notify(id, notificationBuilder.build());
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        wl.acquire(6000);


    }
    private int getNotificationIcon(NotificationCompat.Builder notificationBuilder) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(Color.WHITE);
            return R.mipmap.ic_launcher;

        }
        return  R.mipmap.ic_launcher;
    }


}
