package com.echo.allscenarioapp.api;

import android.app.Activity;
import android.content.Context;

import com.echo.allscenarioapp.backend.ResponseListener;
import com.echo.allscenarioapp.backend.ResponseModelService;
import com.echo.allscenarioapp.model.ChatDetailListModel;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Pref;
import com.echo.allscenarioapp.utils.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public class SendMessageApi {

    private final static String API = Const.SEND_MESSAGE_API;
    private ResponseListener responseListener = null;
    private Context context = null;
    private static IRequestData iRequestData = null;


    public SendMessageApi(Context _context, ResponseListener _responseListener) {
        this.context = _context;
        responseListener = _responseListener;
        iRequestData = Utils.retrofit.create(IRequestData.class);
    }

    // Request Api Param
    private interface IRequestData {
        @FormUrlEncoded
        @POST(API)
        Call<ResponseModelService> getResponseData(

                @Field("from_id") String from_id,
                @Field("to_id") String to_id,
                @Field("type") String types,  //(1=text message 2=video 3=audio 4=image)
                @Field("message") String message,
                @Field("media_url") String media_url,
                @Field("thumb_url") String thumb_url,
                @Field("is_group") String is_group,
                @Field("device_type") String device_type,
                @Field("token") String token,
                @Field("language") String language
        );
    }

    public Void execute(ChatDetailListModel chatListModel) {//String to_id, String type,String message,String media_url,String thumb_url

        try {
            Call<ResponseModelService> call = iRequestData.getResponseData(Pref.getStringValue(context, Const.PREF_USERID, ""), chatListModel.to_id,chatListModel.type,chatListModel.message,chatListModel.media_url, chatListModel.thumb_url,chatListModel.is_group,Const.DEVICE_TYPE, Pref.getStringValue(context, Const.PREF_GCMTOKEN, ""), Pref.getStringValue(context, Const.PREF_LANGUAGE, Const.EN));
            call.enqueue(new Callback<ResponseModelService>() {
                @Override
                public void onResponse(Call<ResponseModelService> call, Response<ResponseModelService> response) {
                    int status = 0;
                    String mesg = "";
                    try {
                        Utils.print("Message==>" + response.body().success + "::" + response.body().message + "");

                        status = response.body().success;
                        mesg = response.body().message;
                        if (status == 1) {
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    doCallBack(status, mesg);
                }

                @Override
                public void onFailure(Call<ResponseModelService> call, Throwable t) {
                    Utils.print("Upload error:" + t.getMessage());
                    doCallBack(-2, t.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * Send control back to the caller which includes
     * Status: Successful or Failure Message: Its an Object, if required
     */
    private void doCallBack(final int code, final String mesg) {
        ((Activity) context).runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (code == 1) {
                    responseListener.onResponse(API, Const.API_RESULT.SUCCESS, null);
                } else if (code >= 0) {
                    Utils.showToastMessage(context, mesg, false);
                    responseListener.onResponse(API, Const.API_RESULT.FAIL, null);
                } else if (code < 0) {
                    responseListener.onResponse(API, Const.API_RESULT.FAIL, null);
                }

            }
        });
    }
}