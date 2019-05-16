package com.echo.allscenarioapp.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.api.SocialLoginAPI;
import com.echo.allscenarioapp.backend.ResponseListener;
import com.echo.allscenarioapp.model.UserModel;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Utils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, ResponseListener {
    @BindView(R.id.imgGPlus)
    ImageView imgGPlus;
    @BindView(R.id.imgFb)
    ImageView imgFb;

    private boolean doubleBackToExitPressedOnce = false;

    private static final int RC_SIGN_IN = 9001;


    GoogleApiClient mGoogleApiClient = null;
    private SocialLoginAPI socialLoginAPI = null;
    private UserModel userModel = null;

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //For Full Screen and Hide titlebar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        /* Use This Function to maintain stack of Open Activity*/


        Utils.addActivities("LoginActivity", this);
        //For Get Id Of ALl View
        ButterKnife.bind(this);
        //For Applt Font style
        Utils.applyFontFace(this, this.findViewById(android.R.id.content).getRootView());
        initialisation();
    }


    public void initialisation() {

        //For Facebook login FUnction
        handleFacebookLogin();


        //Google Login FUnction
        googleLoginnew();


    }

    //CLick Event
    @OnClick({R.id.imgGPlus, R.id.imgFb})
    public void onClick(View view) {
        switch (view.getId()) {


            case R.id.imgGPlus:
                signInNew();
                break;
            case R.id.imgFb:
                LoginManager.getInstance().logOut();
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "email"));
                break;

        }
    }


    //Back Press Call
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Utils.closeAllScreens();
            finish();
            return;
        }
        this.doubleBackToExitPressedOnce = true;

        Utils.showToastMessage(this, getResources().getString(R.string.appExitMessage), true);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    //Call APi Function
    private void callApi(int tag) {
        if (Utils.isOnline(
                this)) {
            Utils.showProgress(this);
            if (tag == 1) {
                if (socialLoginAPI == null)
                    socialLoginAPI = new SocialLoginAPI(this, this);
                socialLoginAPI.execute(userModel);
            }
        } else
            Utils.showToastMessage(this, getResources().getString(R.string.checkInternet), false);

    }


    //Facebook Data Handling
    private void handleFacebookLogin() {

        //Create callback manager obj
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        // App code
                        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
                        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

                        Utils.print("==========================value >>> " + accessToken + ":::" + isLoggedIn + "::" + loginResult.getAccessToken().getToken());


                        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject user, GraphResponse response) {
                                if (response.getError() != null)
                                    return;
                                //Get & set Data from fb
                                userModel = new UserModel();
                                userModel.email = user.optString("email") != null && !user.optString("email").equalsIgnoreCase("null") ? user.optString("email") : "";
                                userModel.social_id = user.optString("id");
                                userModel.social_type = 1; //Facebook
                                userModel.social_token = loginResult.getAccessToken().getToken();
                                userModel.fullname = (user.optString("first_name") != null && !user.optString("first_name").equalsIgnoreCase("null") ? user.optString("first_name") : "") + (user.optString("last_name") != null && !user.optString("last_name").equalsIgnoreCase("null") ? user.optString("last_name") : "");

                                try {
                                    userModel.image = user.getJSONObject("picture").getJSONObject("data").optString("url");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                callApi(1);


                            }
                        });
                        //pass Bundle
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,first_name,last_name,picture.width(400).height(400),name,email");
                        request.setParameters(parameters);
                        request.executeAsync();

                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        exception.printStackTrace();

                    }
                });


    }

    //Responce For Api Call
    @Override
    public void onResponse(String tag, Const.API_RESULT result, Object obj) {
        Utils.dismissProgress();
        if (tag == Const.SOCIAL_LOGIN_API) {
            if (result == Const.API_RESULT.SUCCESS) {
                try {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                if (userModel.social_type == 2) {
                    try {
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));

                } else if (userModel.social_type == 1)
                    LoginManager.getInstance().logOut();


            }

        }
    }


    //GPlus gooleclient Obj
    private void googleLoginnew() {
        //Request for email and profile to Gplus
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Utils.print("" + connectionResult.getErrorCode() + ":::" + connectionResult.getErrorMessage());
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


    }

    //Google Plus Authentication
    private void signInNew() {
        Utils.showProgress(LoginActivity.this);
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            Utils.dismissProgress();
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            //Check Result Success
            if (result.isSuccess()) {
                final GoogleSignInAccount account = result.getSignInAccount();

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {

                            if (account != null) {
                                //Retrive Data From Gplus Account
                                userModel = new UserModel();
                                userModel.email = account.getEmail();
                                userModel.social_id = account.getId();
                                userModel.social_type = 2; //google plus
                                userModel.social_token = account.getIdToken();
                                userModel.fullname = (account.getDisplayName().contains(" ") ? account.getDisplayName().split(" ")[0] : account.getDisplayName()) + (account.getDisplayName().contains(" ") ? account.getDisplayName().split(" ")[1] : account.getDisplayName());
                                userModel.image = account.getPhotoUrl() != null && !account.getPhotoUrl().toString().equalsIgnoreCase("null") ? account.getPhotoUrl().toString() : "";
                                callApi(1);


                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                AsyncTask.execute(runnable);

            } else {
            }


        } else
            callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

}
