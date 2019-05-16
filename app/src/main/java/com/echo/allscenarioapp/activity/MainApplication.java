package com.echo.allscenarioapp.activity;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.echo.allscenarioapp.chat.ChatCallBack;
import com.echo.allscenarioapp.chat.SocketIOManager;
import com.echo.allscenarioapp.utils.Utils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.FirebaseApp;

/**
 * Created by mahendra on 28-Apr-17.
 */

public class MainApplication extends MultiDexApplication {

    //SocketIO
    private SocketIOManager socketIOManager = null;
    private static MainApplication mainApplication = null;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(getApplicationContext());
        Fresco.initialize(this);
        Utils.setToken(this);
        //Create SocketIO obj
        mainApplication = this;
        //  Utils.setToken(this);

    }

    public static MainApplication getInstance() {
        return mainApplication;
    }

    public SocketIOManager getSoketMangerObject() {
        if (socketIOManager == null)
            socketIOManager = new SocketIOManager(this);

        return socketIOManager;
    }

    public void socketOnline() {
        if (socketIOManager == null)
            socketIOManager = new SocketIOManager(this);

        socketIOManager.online();
        socketIOManager.OnNotifyWithCompletionHandler();

    }


    public void socketOffline() {
        if (socketIOManager != null) {
            socketIOManager.offline();
        }

    }
}
