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
import com.echo.allscenarioapp.adapter.AddMemberListAdapter;
import com.echo.allscenarioapp.adapter.EditGroupFriendsListAdapter;
import com.echo.allscenarioapp.adapter.GroupFriendsListAdapter;
import com.echo.allscenarioapp.api.AddGroupMemberAPi;
import com.echo.allscenarioapp.api.GetFriendsListApi;
import com.echo.allscenarioapp.api.GetUserListApi;
import com.echo.allscenarioapp.backend.ResponseListener;
import com.echo.allscenarioapp.custom.CustomHeader;
import com.echo.allscenarioapp.model.FriendsListModel;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddMemberListActivity extends AppCompatActivity implements View.OnClickListener, ResponseListener {
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

    GetUserListApi getUserListApi = null;
    AddGroupMemberAPi addGroupMemberAPi = null;
    private String search = "";
    ArrayList<FriendsListModel> objTempList = null;
    ArrayList<FriendsListModel> objCheckList = null;
    private AddMemberListAdapter addMemberListAdapter = null;
    private int lastPos = -1;
    String grupID = "" , convo_id = "";

    String ids= "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member_list);
        /* Use This Function to maintain stack of Open Activity*/
        Utils.addActivities("AddMemberListActivity", this);

        //For Get Id Of ALl View
        ButterKnife.bind(this);
        //For Applt Font style
        Utils.applyFontFace(this, this.findViewById(android.R.id.content).getRootView());
        initialization();
    }


    public void initialization() {
        grupID = getIntent().getStringExtra("GROUP_ID");
        convo_id = getIntent().getStringExtra("CONVERSATION_ID");
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
        addMemberListAdapter = new AddMemberListAdapter(this, this);
        lstFriendsList.setAdapter(addMemberListAdapter);

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
                if (getUserListApi == null)
                    getUserListApi = new GetUserListApi(this, this);
                getUserListApi.execute(grupID);
            }else if (tag == 2) {
                if (addGroupMemberAPi == null)
                    Utils.print("MEMBERRRRRRRRRRRRR_________IDSSSSSSSSSSSSSSSSSSSSSS---------------"+ids);
                    addGroupMemberAPi = new AddGroupMemberAPi(this, this);
                addGroupMemberAPi.execute(ids,grupID,convo_id);
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

                objCheckList =  new ArrayList<>();
                for (FriendsListModel model : addMemberListAdapter.objList) {


                    if (model.isselected == 1) {
                        objCheckList.add(model);
                    }


                }




                for (FriendsListModel friendsListModel : objCheckList) {

                    ids = ids + "," + friendsListModel.user_id;
                }

                if (ids.startsWith(","))
                    ids = ids.trim().substring(1);

                callApi(2);

                break;
        }
    }


    //APi Response Handling
    @Override
    public void onResponse(String tag, Const.API_RESULT result, Object obj) {
        Utils.dismissProgress();
        if (tag == Const.GET_USER_LIST_API && result == Const.API_RESULT.SUCCESS) {
            try {

                swipeContainer.setRefreshing(false);
                if (result == Const.API_RESULT.SUCCESS) {
                    if (getUserListApi.objList == null || getUserListApi.objList.size() <= 0) {
                        lstFriendsList.setVisibility(View.GONE);
                        txtNoDataFound.setVisibility(View.VISIBLE);

                    } else {

                        lstFriendsList.setVisibility(View.VISIBLE);
                        txtNoDataFound.setVisibility(View.GONE);
                        addMemberListAdapter.addData(getUserListApi.objList);
                    }
                } else {

                    lstFriendsList.setVisibility(View.GONE);
                    txtNoDataFound.setVisibility(View.VISIBLE);

                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }else if (tag == Const.ADD_GROUP_MEMBER_API && result == Const.API_RESULT.SUCCESS) {
            finish();
        }


    }


    //Search Function
    private void searchData() {
        if (getUserListApi == null || getUserListApi.objList == null || getUserListApi.objList.size() <= 0)
            return;
        if (search.isEmpty() || !edtSearch.getText().toString().trim().equalsIgnoreCase(search)) {
            search = edtSearch.getText().toString().trim();
            objTempList = new ArrayList<>();
            if (edtSearch.getText().toString().trim().isEmpty())
                objTempList.addAll(getUserListApi.objList);
            else {
                for (FriendsListModel friendsListModel : getUserListApi.objList) {
                    if (friendsListModel.fullname.toLowerCase().contains(edtSearch.getText().toString().trim().toLowerCase())) {
                        objTempList.add(friendsListModel);
                    }
                }
            }
            addMemberListAdapter.addData(objTempList);
        }
    }
}
