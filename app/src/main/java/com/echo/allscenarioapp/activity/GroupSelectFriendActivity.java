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
import com.echo.allscenarioapp.adapter.GroupFriendsListAdapter;
import com.echo.allscenarioapp.api.GetFriendsListApi;
import com.echo.allscenarioapp.backend.ResponseListener;
import com.echo.allscenarioapp.custom.CustomHeader;
import com.echo.allscenarioapp.model.ChatDetailListModel;
import com.echo.allscenarioapp.model.FriendsListModel;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupSelectFriendActivity extends AppCompatActivity implements View.OnClickListener, ResponseListener {
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

    GetFriendsListApi getFriendsListApi = null;
    private String search = "";
    ArrayList<FriendsListModel> objTempList = null;
    ArrayList<FriendsListModel> objCheckList = null;
    private GroupFriendsListAdapter groupFriendsListAdapter = null;
    private int lastPos = -1;

    String strFriends = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_select_friend);
        /* Use This Function to maintain stack of Open Activity*/
        Utils.addActivities("GroupSelectFriendActivity", this);

        //For Get Id Of ALl View
        ButterKnife.bind(this);
        //For Applt Font style
        Utils.applyFontFace(this, this.findViewById(android.R.id.content).getRootView());
        initialization();
    }

    public void initialization() {

        //Change StatusBar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.green_color));
        }


        //Set Header Details
        header.toolbar.setBackgroundColor(getResources().getColor(R.color.green_color));
        header.txtHeaderTitle.setText("Select Friends");
        header.imgLeft.setVisibility(View.VISIBLE);
        header.imgRight.setVisibility(View.VISIBLE);
        header.imgRight.setBackground(getResources().getDrawable(R.drawable.ic_tick_white));

        //Bind Adapter To Recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        lstFriendsList.setLayoutManager(layoutManager);
        groupFriendsListAdapter = new GroupFriendsListAdapter(this, this);
        lstFriendsList.setAdapter(groupFriendsListAdapter);

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
                if (getFriendsListApi == null)
                    getFriendsListApi = new GetFriendsListApi(this, this);
                getFriendsListApi.execute();
            }
        } else {
            swipeContainer.setRefreshing(false);
            Utils.showToastMessage(this, getResources().getString(R.string.checkInternet), false);
        }

    }

    //CLick Event
    @OnClick({R.id.imgRight})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.row:
                lastPos = Integer.parseInt(view.getTag().toString());
                break;

            case R.id.imgRight:

                objCheckList = new ArrayList<>();

                for (FriendsListModel model : groupFriendsListAdapter.objList) {


                    if (model.isselected == 1) {

                        objCheckList.add(model);
                    }


                }
                if (objCheckList.size() > 0) {
                    //Pass Selected friend Arraylist
                    startActivity(new Intent(GroupSelectFriendActivity.this, CreateGroupActivity.class).putExtra("LIST", objCheckList));
                } else {
                    Utils.showToastMessage(GroupSelectFriendActivity.this, "Please Select Atleast One Friend", false);
                }
                break;
        }
    }


    //APi Response Handling
    @Override
    public void onResponse(String tag, Const.API_RESULT result, Object obj) {
        Utils.dismissProgress();
        if (tag == Const.GET_FRIENDS_LIST_API && result == Const.API_RESULT.SUCCESS) {
            try {

                swipeContainer.setRefreshing(false);
                if (result == Const.API_RESULT.SUCCESS) {
                    if (getFriendsListApi.objList == null || getFriendsListApi.objList.size() <= 0) {
                        lstFriendsList.setVisibility(View.GONE);
                        txtNoDataFound.setVisibility(View.VISIBLE);

                    } else {

                        lstFriendsList.setVisibility(View.VISIBLE);
                        txtNoDataFound.setVisibility(View.GONE);
                        groupFriendsListAdapter.addData(getFriendsListApi.objList);

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
        if (getFriendsListApi == null || getFriendsListApi.objList == null || getFriendsListApi.objList.size() <= 0)
            return;
        if (search.isEmpty() || !edtSearch.getText().toString().trim().equalsIgnoreCase(search)) {
            search = edtSearch.getText().toString().trim();
            objTempList = new ArrayList<>();
            if (edtSearch.getText().toString().trim().isEmpty())
                objTempList.addAll(getFriendsListApi.objList);
            else {
                for (FriendsListModel friendsListModel : getFriendsListApi.objList) {
                    if (friendsListModel.fullname.toLowerCase().contains(edtSearch.getText().toString().trim().toLowerCase())) {
                        objTempList.add(friendsListModel);
                    }
                }
            }
            groupFriendsListAdapter.addData(objTempList);
        }
    }
}
