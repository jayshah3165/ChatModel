package com.echo.allscenarioapp.activity;

import android.content.Intent;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.adapter.FriendsListAdapter;
import com.echo.allscenarioapp.adapter.LikeListAdapter;
import com.echo.allscenarioapp.api.GetFriendsListApi;
import com.echo.allscenarioapp.api.GetLikeListApi;
import com.echo.allscenarioapp.backend.ResponseListener;
import com.echo.allscenarioapp.custom.CustomHeader;
import com.echo.allscenarioapp.model.ChatDetailListModel;
import com.echo.allscenarioapp.model.FriendsListModel;
import com.echo.allscenarioapp.model.LikeListModel;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LikeListActivity extends AppCompatActivity implements View.OnClickListener, ResponseListener {
    @BindView(R.id.header)
    CustomHeader header;
    @BindView(R.id.lstFriendsList)
    RecyclerView lstFriendsList;
    @BindView(R.id.edtSearch)
    EditText edtSearch;
    @BindView(R.id.txtNoDataFound)
    TextView txtNoDataFound;
    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;

    GetLikeListApi getLikeListApi = null;
    private String search = "";
    ArrayList<LikeListModel> objTempList = null;
    private LikeListAdapter likeListAdapter = null;
    private int lastPos = -1;
    String postID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_list);
        /* Use This Function to maintain stack of Open Activity*/
        Utils.addActivities("LikeListActivity", this);

        //For Get Id Of ALl View
        ButterKnife.bind(this);
        //For Applt Font style
        Utils.applyFontFace(this, this.findViewById(android.R.id.content).getRootView());
        initialization();
    }


    public void initialization() {
        postID = getIntent().getStringExtra("POST_ID");
        //Change StatusBar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.green_color));
        }


        //Set Header Details
        header.toolbar.setBackgroundColor(getResources().getColor(R.color.green_color));
        header.txtHeaderTitle.setText("Likes");
        header.imgLeft.setVisibility(View.VISIBLE);
        header.imgRight.setVisibility(View.GONE);

        //Bind Adapter To Recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        lstFriendsList.setLayoutManager(layoutManager);
        likeListAdapter = new LikeListAdapter(this, this);
        lstFriendsList.setAdapter(likeListAdapter);

        callApi(1);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callApi(1);
            }
        });

        //Search Function call
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchData();
            }
        });


    }

    //Call Api Function
    private void callApi(int tag) {
        if (Utils.isOnline(this)) {
            Utils.showProgress(this);
            if (tag == 1) {
                if (getLikeListApi == null)
                    getLikeListApi = new GetLikeListApi(this, this);
                getLikeListApi.execute(postID, "1");//type =1 feedlike
            }
        } else {
            swipeContainer.setRefreshing(false);
            Utils.showToastMessage(this, getResources().getString(R.string.checkInternet), false);
        }

    }

    @OnClick({})
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }

    //APi Response Handling
    @Override
    public void onResponse(String tag, Const.API_RESULT result, Object obj) {
        Utils.dismissProgress();
        if (tag == Const.GET_LIKE_LIST_API && result == Const.API_RESULT.SUCCESS) {
            try {

                swipeContainer.setRefreshing(false);
                if (result == Const.API_RESULT.SUCCESS) {
                    if (getLikeListApi.objList == null || getLikeListApi.objList.size() <= 0) {
                        lstFriendsList.setVisibility(View.GONE);
                        txtNoDataFound.setVisibility(View.VISIBLE);

                    } else {

                        lstFriendsList.setVisibility(View.VISIBLE);
                        txtNoDataFound.setVisibility(View.GONE);
                        likeListAdapter.addData(getLikeListApi.objList);
                    }
                } else {

                    lstFriendsList.setVisibility(View.GONE);
                    txtNoDataFound.setVisibility(View.VISIBLE);

                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

    }


    //Search Function
    private void searchData() {
        if (getLikeListApi == null || getLikeListApi.objList == null || getLikeListApi.objList.size() <= 0)
            return;
        if (search.isEmpty() || !edtSearch.getText().toString().trim().equalsIgnoreCase(search)) {
            search = edtSearch.getText().toString().trim();
            objTempList = new ArrayList<>();
            if (edtSearch.getText().toString().trim().isEmpty())
                objTempList.addAll(getLikeListApi.objList);
            else {
                for (LikeListModel likeListModel : getLikeListApi.objList) {
                    if (likeListModel.fullname.toLowerCase().contains(edtSearch.getText().toString().trim().toLowerCase())) {
                        objTempList.add(likeListModel);
                    }
                }
            }
            likeListAdapter.addData(objTempList);
        }
    }
}
