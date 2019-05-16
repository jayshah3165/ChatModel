package com.echo.allscenarioapp.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.adapter.FriendsListAdapter;
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

public class FriendsListActivity extends AppCompatActivity implements View.OnClickListener, ResponseListener {
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
    private FriendsListAdapter friendsListAdapter = null;
    private int lastPos = -1;
    PopupWindow pw = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        /* Use This Function to maintain stack of Open Activity*/
        Utils.addActivities("FriendsListActivity", this);

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
        header.txtHeaderTitle.setText("Friends");
        header.imgLeft.setVisibility(View.VISIBLE);
        header.imgRight.setVisibility(View.VISIBLE);
        header.imgRight.setBackground(getResources().getDrawable(R.drawable.more));

        //Bind Adapter To Recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        lstFriendsList.setLayoutManager(layoutManager);
        friendsListAdapter = new FriendsListAdapter(this, this);
        lstFriendsList.setAdapter(friendsListAdapter);

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

                ChatDetailListModel chatDetailListModel = new ChatDetailListModel();
                chatDetailListModel.user_id = friendsListAdapter.objList.get(lastPos).user_id;
                chatDetailListModel.image = friendsListAdapter.objList.get(lastPos).image;
                chatDetailListModel.fullname = friendsListAdapter.objList.get(lastPos).fullname;
                chatDetailListModel.conversation_id = "0";
                chatDetailListModel.is_group = "0";

                startActivity(new Intent(FriendsListActivity.this, OneToOneChatActivity.class).putExtra("CHATMODEL", chatDetailListModel));
                break;
            case R.id.imgRight:
                initiatePopupWindow(view);
                break;
        }
    }

    private void initiatePopupWindow(View v) {
        try {
            //We need to get the instance of the LayoutInflater, use the context of this activity
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            pw = new PopupWindow(inflater.inflate(R.layout.more_popup_widow, null), RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, false);
            pw.showAsDropDown(v, 0, 0);
            View layout = pw.getContentView();


            TextView txtClearConversation = (TextView) layout.findViewById(R.id.txtClearConversation);
            TextView txtBlock = (TextView) layout.findViewById(R.id.txtBlock);
            TextView txtCancel = (TextView) layout.findViewById(R.id.txtCancel);


            //Open GroupFriend List Activity
            txtClearConversation.setText("Create Group");
            txtClearConversation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(FriendsListActivity.this, GroupSelectFriendActivity.class));
                    pw.dismiss();
                }
            });
            txtBlock.setText("News Feed");
            txtBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(FriendsListActivity.this, NewsFeedActivity.class));
                }
            });

            txtCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pw.dismiss();
                }
            });


            // display the popup in the center
            pw.showAtLocation(v, Gravity.CENTER, 0, 0);
            pw.setBackgroundDrawable(new BitmapDrawable());
            pw.setOutsideTouchable(true);

        } catch (Exception e) {
            e.printStackTrace();
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
                        friendsListAdapter.addData(getFriendsListApi.objList);
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
            friendsListAdapter.addData(objTempList);
        }
    }
}
