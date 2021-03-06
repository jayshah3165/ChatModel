package com.echo.allscenarioapp.api;

import android.app.Activity;
import android.content.Context;


import com.echo.allscenarioapp.backend.ResponseListener;
import com.echo.allscenarioapp.backend.ResponseModelService;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Pref;
import com.echo.allscenarioapp.utils.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public class AddCommentNewsFeedAPI {

    private final static String API = Const.ADD_COMMENT_API;
    private ResponseListener responseListener = null;
    private Context context = null;
    private static IRequestData iRequestData = null;
    public String mesg = "";

    public AddCommentNewsFeedAPI(Context _context, ResponseListener _responseListener) {
        this.context = _context;
        responseListener = _responseListener;
        iRequestData = Utils.retrofit.create(IRequestData.class);
    }

    // Request Api Param
    private interface IRequestData {
        @FormUrlEncoded
        @POST(API)
        Call<ResponseModelService> getResponseData(
                @Field("user_id") String user_id,
                @Field("post_id") String post_id,
                @Field("comment") String comment,
                @Field("device_type") String device_type,
                @Field("token") String token,
                @Field("language") String language
        );
    }

    public Void execute( String post_id,String comment) {
        try {
            Call<ResponseModelService> call = iRequestData.getResponseData(Pref.getStringValue(context, Const.PREF_USERID, ""),post_id,comment,
                    Const.DEVICE_TYPE,Pref.getStringValue(context, Const.PREF_GCMTOKEN, ""), Pref.getStringValue(context, Const.PREF_LANGUAGE, Const.EN));
            call.enqueue(new Callback<ResponseModelService>() {
                @Override
                public void onResponse(Call<ResponseModelService> call, Response<ResponseModelService> response) {
                    int status = 0;
//                    String mesg = "";
                    try {
                        Utils.print("Message==> " + response.body().success + "::" + response.body().message);
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