package com.echo.allscenarioapp.api;

import android.app.Activity;
import android.content.Context;

import com.echo.allscenarioapp.backend.ResponseListener;
import com.echo.allscenarioapp.backend.ResponseModel;
import com.echo.allscenarioapp.model.ChatDetailListModel;
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
 * Created by EchoIT on 10/18/2018.
 */

public class GetChatDetailListApi {
    private final static String API = Const.GET_MSG_LIST_API;
    private ResponseListener responseListener = null;
    private Context context = null;
    private static IRequestData iRequestData = null;
   public String title = "";
    public String convrsation_id = "",ownerID = "";
    public String isonline = "";

    public ArrayList<ChatDetailListModel> objList = null;

    public GetChatDetailListApi(Context _context, ResponseListener _responseListener) {
        this.context = _context;
        responseListener = _responseListener;
        iRequestData = Utils.retrofit.create(IRequestData.class);
    }

    // Request Api Param
    private interface IRequestData {
        @FormUrlEncoded
        @POST(API)
        Call<ResponseModel<ChatDetailListModel>> getResponseData(
                @Field("user_id") String user_id,
                @Field("friend_id") String friend_id,
                @Field("is_group") String is_group,
                @Field("conversation_id") String conversation_id,
                @Field("device_type") String device_type,
                @Field("token") String token,
                @Field("language") String language
        );
    }

    public Void execute(String friend_id,String is_group,String conversation_id) {
        try {
            Utils.print("toidddddddd--------------------------------"+friend_id);

            Call<ResponseModel<ChatDetailListModel>> call = iRequestData.getResponseData(Pref.getStringValue(context,Const.PREF_USERID,""),friend_id, is_group, conversation_id,Const.DEVICE_TYPE, Pref.getStringValue(context, Const.PREF_GCMTOKEN, ""), Pref.getStringValue(context, Const.PREF_LANGUAGE, Const.EN));
            call.enqueue(new Callback<ResponseModel<ChatDetailListModel>>() {
                @Override
                public void onResponse(Call<ResponseModel<ChatDetailListModel>> call, Response<ResponseModel<ChatDetailListModel>> response) {
                    int status = 0;
                    String mesg = "";

                    try {
                        Utils.print("Message==>" + response.body().success + "::" + response.body().message + "");

                        status = response.body().success;
                        mesg = response.body().message;


                        isonline = response.body().is_online;
                        convrsation_id = response.body().conversation_id;
                        ownerID = response.body().group_owner_id;
                        objList = new ArrayList<>();
                        String lastDate = "";

                        //2019-04-17 10:38:16
                        if (status == 1) {
                            if(response.body().result!=null && response.body().result.size()>0)
                            {
                                for (ChatDetailListModel chatDetailListModel :response.body().result)
                                {
                                    if(!lastDate.equalsIgnoreCase(Utils.convertDateStringToString(chatDetailListModel.created,"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd")))
                                    {

                                        Utils.print("=============value >> " + lastDate +"::" +  Utils.convertDateStringToString(chatDetailListModel.created,"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd"));
                                        lastDate = Utils.convertDateStringToString(chatDetailListModel.created,"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd");
                                        ChatDetailListModel Model = new ChatDetailListModel();
                                        Model.isHeader = 1;
                                        Model.created = chatDetailListModel.created;
                                        objList.add(Model);
                                    }
                                    objList.add(chatDetailListModel);


                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    doCallBack(status, mesg);
                }

                @Override
                public void onFailure(Call<ResponseModel<ChatDetailListModel>> call, Throwable t) {
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
