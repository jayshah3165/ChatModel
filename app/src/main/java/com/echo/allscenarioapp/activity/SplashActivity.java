package com.echo.allscenarioapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.backend.StickyService;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Pref;
import com.echo.allscenarioapp.utils.Utils;

import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //For Full Screen and Hide titlebar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);
        Intent stickyService = new Intent(this, StickyService.class);
        startService(stickyService);

        /* Use This Function to maintain stack of Open Activity*/
        Utils.addActivities("SplashActivity", this);
        Utils.setToken(this);
        //For Get Id Of ALl View
        ButterKnife.bind(this);
        //For Applt Font style
        Utils.applyFontFace(this, this.findViewById(android.R.id.content).getRootView());
        //For Print Hash key
        Utils.printHashKey(this);

        initialization();
    }

    //Set TImer of SPlash Page And CHeck User id is already Exist or not
    private void initialization() {

        //Utils.setToken(this);
        new Thread() {
            public void run() {
                try {
                    sleep(SPLASH_TIME_OUT);
                    if (!Pref.getStringValue(SplashActivity.this, Const.PREF_USERID, "").equalsIgnoreCase(""))
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    else
                        startActivity(new Intent(SplashActivity.this, SelectApiActivity.class));
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
