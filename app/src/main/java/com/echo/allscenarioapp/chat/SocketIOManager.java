package com.echo.allscenarioapp.chat;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.echo.allscenarioapp.backend.StickyService;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Pref;
import com.echo.allscenarioapp.utils.Utils;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by EchoIT on 7/17/2017.
 */


public class SocketIOManager {

    Context context = null;
    ChatCallBack chatCallBack = null;
    Handler handler = null;
    Runnable handlerRunnable = null;

    public static enum CHAT_RESPONSE {
        USER_ONLINE, USER_OFFLINE, USER_START_TYPING, USER_STOP_TYPING, NEW_MESSAGE, NEW_VIDEO_MESSAGE,USER_READ_MESSAGE
    }

    private Socket mSocket;

    {
        try {
            mSocket = IO.socket(Const.CHAT_SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public SocketIOManager(Context context) {
        handler = new Handler();
        this.context = context;
    }

    public Socket getSocket() {
        return mSocket;
    }

    //Socket Online
    public void online() {

        mSocket.connect();
        ConnectToServerWithCompletionHandler();
    }
    //Socket Offline
    public void offline() {
        StopNotify();
        mSocket.disconnect();
        DisConnectToServerWithCompletionHandler();
    }


    public void setChatCallBack( ChatCallBack mchatCallBack) {
        chatCallBack = mchatCallBack;
    }

    public void logOut()// pass callback
    {
        mSocket.connect();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", Pref.getStringValue(context, Const.PREF_USERID, ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("CHAT_LOGOUT", jsonObject.toString());
//        offline();
    }

    public void ConnectToServerWithCompletionHandler()// pass callback
    {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", Pref.getStringValue(context, Const.PREF_USERID, ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Utils.print(" ====================== pass Soket ONLINE USER " + jsonObject.toString());
        mSocket.emit("ONLINE", jsonObject.toString());
    }


    public void DisConnectToServerWithCompletionHandler() { // pass CallBack
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", Pref.getStringValue(context, Const.PREF_USERID, ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Utils.print(" ====================== pass Soket Offlin USER " + jsonObject.toString());

        mSocket.emit("OFFLINE", jsonObject.toString());

//        completionHandler();
    }


    public void SendMessage(String message) {
        mSocket.emit("SEND_MESSAGE", message);
    }

    public void SendVideoMessage(String message) {
        mSocket.emit("SEND_VIDEO_MESSAGE", message);
    }


    public void StartTyping(String toid) {
        Utils.print("TZYPINGGGGG-----SOCKET IO------------------------");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", Pref.getStringValue(context, Const.PREF_USERID, "")).put("to_id",toid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Utils.print("TZYPINGGGGG-----SOCKET IO-------In-----------------");
        mSocket.emit("START_TYPING", jsonObject.toString());
    }

    public void StopTyping() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", Pref.getStringValue(context, Const.PREF_USERID, ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("STOP_TYPING", jsonObject.toString());
    }

    public void ReadMessage(String toid) {
        Utils.print("read msggggggggggg---------------------------");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", Pref.getStringValue(context, Const.PREF_USERID, "")).put("to_id",toid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("READ_MESSAGE", jsonObject.toString());
    }


    public void OnNotifyWithCompletionHandler() {

        StopNotify();

        mSocket.on("USER_ONLINE", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                Utils.print("================= USER USER_ONLINE Call " + "::" + new Gson().toJson(args).toString() + ":::" + context);

                callCallBack(CHAT_RESPONSE.USER_ONLINE, args);

            }
        });

        mSocket.on("USER_OFFLINE", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                Utils.print("================= USER Offline Call " + "::" + new Gson().toJson(args).toString() + ":::" + context);

                callCallBack(CHAT_RESPONSE.USER_OFFLINE, args);
            }
        });
        mSocket.on("USER_READ_MESSAGE", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                callCallBack(CHAT_RESPONSE.USER_READ_MESSAGE, args);


            }
        });

        mSocket.on("USER_START_TYPING", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                callCallBack(CHAT_RESPONSE.USER_START_TYPING, args);
            }
        });

        mSocket.on("USER_STOP_TYPING", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                callCallBack(CHAT_RESPONSE.USER_STOP_TYPING, args);
            }
        });

        mSocket.on("NEW_MESSAGE", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                callCallBack(CHAT_RESPONSE.NEW_MESSAGE, args);


            }
        });

        mSocket.on("NEW_VIDEO_MESSAGE", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                callCallBack(CHAT_RESPONSE.NEW_VIDEO_MESSAGE, args);

            }
        });


    }


    private void callCallBack(final SocketIOManager.CHAT_RESPONSE response, final Object args) {
        Utils.print("socketmanager callback------------------------");
        if (chatCallBack != null) {
            Utils.print("socketmanager CALLBACK OBJ ----------------------" + chatCallBack);
            chatCallBack.onChatResponse(response, args);
        }
    }

    public void StopNotify() {
        mSocket.off("USER_ONLINE");
        mSocket.off("USER_OFFLINE");
        mSocket.off("USER_START_TYPING");
        mSocket.off("USER_STOP_TYPING");
        mSocket.off("NEW_MESSAGE");
        mSocket.off("USER_READ_MESSAGE");
        mSocket.off("NEW_VIDEO_MESSAGE");


    }


}
