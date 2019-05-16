package com.echo.allscenarioapp.api;

import android.app.Activity;
import android.content.Context;

import com.echo.allscenarioapp.backend.ResponseListener;
import com.echo.allscenarioapp.backend.ResponseModel;
import com.echo.allscenarioapp.model.UserModel;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Pref;
import com.echo.allscenarioapp.utils.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public class SocialLoginAPI {

    private final static String API = Const.SOCIAL_LOGIN_API;
    private ResponseListener responseListener = null;
    private Context context = null;
    private static IRequestData iRequestData = null;


    public SocialLoginAPI(Context _context, ResponseListener _responseListener) {
        this.context = _context;
        responseListener = _responseListener;
        iRequestData = Utils.retrofit.create(IRequestData.class);
    }

    // Request Api Param
    private interface IRequestData {
        @FormUrlEncoded
        @POST(API)
        Call<ResponseModel<UserModel>> getResponseData( //ResponseModel<>
                                                        @Field("fullname") String fullname,
                                                        @Field("phone") String phone,
                                                        @Field("image") String image,
                                                        @Field("social_id") String social_id,//FacebookID, GoogleID
                                                        @Field("social_token") String social_token,//Facebook Token, Google Token
                                                        @Field("social_type") String social_type,//1 – Facebook, 2 – Google, 0 - normal
                                                        @Field("email") String email,
                                                        @Field("device_type") String deviceType, //1 > Android ,2 - iphone
                                                        @Field("token") String token,// Firebase notification Token
                                                        @Field("language") String language
        );

    }

    public Void execute(final UserModel userModel) {
        try {

            Call<ResponseModel<UserModel>> call = iRequestData.getResponseData
                    (userModel.fullname,userModel.phone,userModel.image,userModel.social_id,userModel.social_token,String.valueOf(userModel.social_type),userModel.email,  Const.DEVICE_TYPE, Pref.getStringValue(context, Const.PREF_GCMTOKEN, ""), Pref.getStringValue(context, Const.PREF_LANGUAGE, Const.EN));
            call.enqueue(new Callback<ResponseModel<UserModel>>() {


                @Override
                public void onResponse(Call<ResponseModel<UserModel>> call, Response<ResponseModel<UserModel>> response) {
                    int status = 0;
                    String mesg = "";
                    try {
                        Utils.print("Message==>"+ response.body().success + "::" + response.body().message + "");

                        status = response.body().success;
                        mesg = response.body().message;
                        if (status == 1) {

                            Pref.setStringValue(context, Const.PREF_USERID, String.valueOf(response.body().result.get(0).user_id));
                            Pref.setStringValue(context, Const.PREF_FULLNAME, String.valueOf(response.body().result.get(0).fullname));

                            Pref.setStringValue(context, Const.PREF_USER_EMAIL, String.valueOf(response.body().result.get(0).email));
                            Pref.setStringValue(context, Const.PREF_USER_PROFILE_PIC, String.valueOf(response.body().result.get(0).image));
                            Pref.setStringValue(context, Const.PREF_PHONE, String.valueOf(response.body().result.get(0).phone));



                            Pref.setStringValue(context, Const.PREF_SOCIAL_ID, String.valueOf(response.body().result.get(0).social_id));
                            Pref.setStringValue(context, Const.PREF_SOCIAL_TOKEN, response.body().result.get(0).social_token);
                            Pref.setIntValue(context, Const.PREF_SOCIAL_TYPE, 1);//1 – Facebook, 2 – Google,0 > other


                           /* Bundle params = new Bundle();
                            if(userModel.social_type == 1 && !userModel.social_id.isEmpty()) {
                                params.putString("fb_id", userModel.social_id);
                                MainApplication.logCustomEvent(context, "giftzy_fb_login", params);
                            }else if(userModel.social_type == 2 && !userModel.social_id.isEmpty())
                            {
                                MainApplication.logCustomEvent(context, "giftzy_google_login", params);
                            }else
                            {
                                MainApplication.logCustomEvent(context, "giftzy_email_login", params);
                            }*/
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    doCallBack(status, mesg);
                }

                @Override
                public void onFailure(Call<ResponseModel<UserModel>> call, Throwable t) {
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
                }else  if (code == 2) {
                    responseListener.onResponse(API, Const.API_RESULT.FAIL, code);
                } else if (code >= 0)
                {
                    Utils.showToastMessage(context, mesg, false);
                    responseListener.onResponse(API, Const.API_RESULT.FAIL, code);
                } else if (code < 0) {
                    responseListener.onResponse(API, Const.API_RESULT.FAIL, code);
                }

            }
        });
    }
}