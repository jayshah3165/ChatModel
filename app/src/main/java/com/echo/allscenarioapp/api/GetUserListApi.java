package com.echo.allscenarioapp.api;

import android.app.Activity;
import android.content.Context;

import com.echo.allscenarioapp.backend.ResponseListener;
import com.echo.allscenarioapp.backend.ResponseModel;
import com.echo.allscenarioapp.model.FriendsListModel;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Pref;
import com.echo.allscenarioapp.utils.Utils;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by EchoIT on 11/29/2018.
 */

public class GetUserListApi {

    private final static String API = Const.GET_USER_LIST_API;
    private ResponseListener responseListener = null;
    private Context context = null;
    private static IRequestData iRequestData = null;
    public ArrayList<FriendsListModel> objList = null;

    public GetUserListApi(Context _context, ResponseListener _responseListener) {
        this.context = _context;
        responseListener = _responseListener;
        iRequestData = Utils.retrofit.create(IRequestData.class);
    }

    // Request Api Param
    private interface IRequestData {
        @FormUrlEncoded
        @POST(API)
        Call<ResponseModel<FriendsListModel>> getResponseData(
                @Field("user_id") String user_id,
                @Field("group_id") String group_id,
                @Field("device_type") String device_type,
                @Field("token") String token,
                @Field("language") String language
        );
    }

    public Void execute(String group_id) {


       try {

            Call<ResponseModel<FriendsListModel>> call = iRequestData.getResponseData(Pref.getStringValue(context, Const.PREF_USERID,""),group_id, Const.DEVICE_TYPE, Pref.getStringValue(context, Const.PREF_GCMTOKEN,""), Pref.getStringValue(context, Const.PREF_LANGUAGE, "EN"));
            call.enqueue(new Callback<ResponseModel<FriendsListModel>>() {


                @Override
                public void onResponse(Call<ResponseModel<FriendsListModel>> call, Response<ResponseModel<FriendsListModel>> response) {
                    int status = 0;
                    String mesg = "";
                    try {
                        Utils.print("Message==>"+ response.body().success + "::" + response.body().message + "");

                        status = response.body().success;
                        mesg = response.body().message;

                        objList = new ArrayList<FriendsListModel>();
                        if (status == 1) {
                            objList.addAll(response.body().result);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    doCallBack(status, mesg);
                }

                @Override
                public void onFailure(Call<ResponseModel<FriendsListModel>> call, Throwable t) {
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
