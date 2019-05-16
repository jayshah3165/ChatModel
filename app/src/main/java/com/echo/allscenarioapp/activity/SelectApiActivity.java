package com.echo.allscenarioapp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SelectApiActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.btnPhpApi)
    TextView btnPhpApi;
    @BindView(R.id.btnNodeApi)
    TextView btnNodeApi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_select_api);

        //CHange STatusBar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        /* Use This Function to maintain stack of Open Activity*/
        Utils.addActivities("LoginActivity", this);
        //For Get Id Of ALl View
        ButterKnife.bind(this);
        //For Applt Font style
        Utils.applyFontFace(this, this.findViewById(android.R.id.content).getRootView());
    }
    //Click Event Handling
    @OnClick({R.id.btnPhpApi,R.id.btnNodeApi})
    public void onClick(View view) {
        switch (view.getId()) {


            case R.id.btnPhpApi:
                startActivity(new Intent(SelectApiActivity.this,LoginActivity.class));
                break;
            case R.id.btnNodeApi:
                startActivity(new Intent(SelectApiActivity.this,LoginActivity.class));
                break;

        }
    }
}
