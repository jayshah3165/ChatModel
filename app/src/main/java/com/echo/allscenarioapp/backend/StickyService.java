package com.echo.allscenarioapp.backend;

/**
 * Created by Lenovo on 30-03-2018.
 */

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.echo.allscenarioapp.activity.MainApplication;
import com.echo.allscenarioapp.activity.OneToOneChatActivity;
import com.echo.allscenarioapp.api.OnlineChatAPI;
import com.echo.allscenarioapp.chat.SocketIOManager;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Pref;
import com.echo.allscenarioapp.utils.Utils;

import java.util.List;


public class StickyService extends Service {
    public boolean isApiCall =false;
    public boolean isBackCall =false;
    Handler handler = new Handler();

    private Runnable periodicUpdate = new Runnable() {
        @Override
        public void run() {
            isAppIsInBackground(StickyService.this);
//            Utils.print("=======2000 - SystemClock.elapsedRealtime()%1000 >> " + (2000 - SystemClock.elapsedRealtime()%1000));
            handler.postDelayed(periodicUpdate, 2000 - SystemClock.elapsedRealtime()%1000);
            // whatever you want to do below

        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        try {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                isApiCall = false;
//            Utils.print("===========Sticky onStartCommand================== YAHOOOOOOOOO APP IS KILLLLL >>>> ");
                if (!Pref.getStringValue(StickyService.this, Const.PREF_USERID, "").isEmpty() && !Pref.getStringValue(StickyService.this, Const.PREF_USERID, "").equalsIgnoreCase("0")) {
                    new OnlineChatAPI(this, new ResponseListener() {
                        @Override
                        public void onResponse(String tag, Const.API_RESULT result, Object obj) {

                        }
                    }).execute("0");

                    MainApplication.getInstance().socketOffline();

                    Utils.print("socketio offffffffffflineeeeeeeee----------------------------------");
                }
            } else
                handler.post(periodicUpdate);
        }catch (NullPointerException n)
        {
            handler.post(periodicUpdate);
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

//        Utils.print("===========onBind================== YAHOOOOOOOOO onDestroy >>>> ");
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        Utils.print("===========onDestroy================== YAHOOOOOOOOO onDestroy >>>> ");
    }


    @Override
    public boolean onUnbind(Intent intent) {
//        Utils.print("===========onUnbind================== YAHOOOOOOOOO onUnbind >>>> ");
        return super.onUnbind(intent);

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {


//        Utils.print("============================= YAHOOOOOOOOO APP IS KILLLLL >>>> ");
        Intent restartServiceTask = new Intent(getApplicationContext(),this.getClass());
        restartServiceTask.setPackage(getPackageName());
        restartServiceTask.putExtra("logout","true");
        PendingIntent restartPendingIntent = PendingIntent.getService(getApplicationContext(), 1,restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);
        super.onTaskRemoved(rootIntent);
    }

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH)
        {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName()))
                        {
                            if(!isApiCall && !Pref.getStringValue(context, Const.PREF_USERID, "").isEmpty()&& !Pref.getStringValue(context, Const.PREF_USERID, "").equalsIgnoreCase("0")) {
                                isApiCall = true;
                                isBackCall =false;
                                new OnlineChatAPI(this, new ResponseListener() {
                                    @Override
                                    public void onResponse(String tag, Const.API_RESULT result, Object obj) {

                                    }
                                }).execute("1");


                                MainApplication.getInstance().socketOnline();
                                Utils.print("socketio--------onlineeeee--------------------------");
                            }
                            isInBackground = false;
                        }
                    }
                }else
                {
                    isApiCall = false;
                    if(!isBackCall &&!Pref.getStringValue(context, Const.PREF_USERID, "").isEmpty()&& !Pref.getStringValue(context, Const.PREF_USERID, "").equalsIgnoreCase("0")) {
                        isBackCall = true;
                        new OnlineChatAPI(this, new ResponseListener() {
                            @Override
                            public void onResponse(String tag, Const.API_RESULT result, Object obj) {

                            }
                        }).execute("0");

                        MainApplication.getInstance().socketOffline();
                        Utils.print("socketio--------offline--------------------------");
                    }
//                    Utils.print("====!!  333=======IS IN BACKGROUND >>>> ");
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {

                if(!isApiCall && !Pref.getStringValue(context, Const.PREF_USERID, "").isEmpty()&& !Pref.getStringValue(context, Const.PREF_USERID, "").equalsIgnoreCase("0")) {
                    isApiCall = true;
                    isBackCall =false;
                    new OnlineChatAPI(this, new ResponseListener() {
                        @Override
                        public void onResponse(String tag, Const.API_RESULT result, Object obj) {

                        }
                    }).execute("1");
                    MainApplication.getInstance().socketOnline();
                    Utils.print("socketio--------online--------------------------");
                }
                isInBackground = false;
            }else
            {
                isApiCall = false;
                if(!isBackCall && !Pref.getStringValue(context, Const.PREF_USERID, "").isEmpty()&& !Pref.getStringValue(context, Const.PREF_USERID, "").equalsIgnoreCase("0")) {
                    isBackCall =true;
                    new OnlineChatAPI(this, new ResponseListener() {
                        @Override
                        public void onResponse(String tag, Const.API_RESULT result, Object obj) {

                        }
                    }).execute("0");

                    MainApplication.getInstance().socketOffline();
                    Utils.print("socketio--------online--------------------------");
                }
//                Utils.print("========@@===IS IN BACKGROUND >>>> ");
            }
        }

        return isInBackground;
    }
}