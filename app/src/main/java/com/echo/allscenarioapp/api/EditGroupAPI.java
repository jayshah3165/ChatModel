package com.echo.allscenarioapp.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;


import com.echo.allscenarioapp.backend.ResponseListener;
import com.echo.allscenarioapp.backend.ResponseModel;
import com.echo.allscenarioapp.backend.ResponseModelService;
import com.echo.allscenarioapp.model.GroupListModel;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Pref;
import com.echo.allscenarioapp.utils.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public class EditGroupAPI {

    private final static String API = Const.EDIT_GROUP_API;
    private ResponseListener responseListener = null;
    private Context context = null;
    private static IRequestData iRequestData = null;
    public String grupName = "",grupImage = "";

    public EditGroupAPI(Context _context, ResponseListener _responseListener) {
        this.context = _context;
        responseListener = _responseListener;
        iRequestData = Utils.retrofit.create(IRequestData.class);
    }

    // Request Api Param
    private interface IRequestData {
        @FormUrlEncoded
        @POST(API)
        Call<ResponseModel<GroupListModel>> getResponseData(

                @Field("user_id") String user_id,
                @Field("group_name") String group_name,
                @Field("group_image") String group_image,
                @Field("group_id") String group_id,
                @Field("device_type") String device_type,
                @Field("token") String token,
                @Field("language") String language
        );
    }

    public Void execute(String group_name,String group_image,String group_id) {
        try {
            Call<ResponseModel<GroupListModel>> call = iRequestData.getResponseData( Pref.getStringValue(context, Const.PREF_USERID, ""),group_name,group_image,group_id,
                    Const.DEVICE_TYPE,Pref.getStringValue(context, Const.PREF_GCMTOKEN, ""), Pref.getStringValue(context, Const.PREF_LANGUAGE, Const.EN));
            call.enqueue(new Callback<ResponseModel<GroupListModel>>() {
                @Override
                public void onResponse(Call<ResponseModel<GroupListModel>> call, Response<ResponseModel<GroupListModel>> response) {
                    int status = 0;
                    String mesg = "";
                    try {
                        Utils.print("Message==>"+ response.body().success + "::" + response.body().message + "");

                        status = response.body().success;
                        mesg = response.body().message;
                        if (status == 1) {
                            grupName = response.body().result.get(0).group_name;
                            grupImage = response.body().result.get(0).group_image;


                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    doCallBack(status, mesg);
                }

                @Override
                public void onFailure(Call<ResponseModel<GroupListModel>> call, Throwable t) {
                    Utils.print("Upload error:"+ t.getMessage());
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