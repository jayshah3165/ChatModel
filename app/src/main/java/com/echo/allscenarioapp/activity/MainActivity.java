package com.echo.allscenarioapp.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.adapter.ChatListAdapter;
import com.echo.allscenarioapp.api.ClearMassageAPi;
import com.echo.allscenarioapp.api.GetChatListApi;
import com.echo.allscenarioapp.api.LeftGroupAPi;
import com.echo.allscenarioapp.api.RemoveConversationAPi;
import com.echo.allscenarioapp.backend.ResponseListener;
import com.echo.allscenarioapp.chat.ChatCallBack;
import com.echo.allscenarioapp.chat.SocketIOManager;
import com.echo.allscenarioapp.custom.CustomHeader;
import com.echo.allscenarioapp.model.ChatDetailListModel;
import com.echo.allscenarioapp.model.UserModel;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Pref;
import com.echo.allscenarioapp.utils.Utils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ResponseListener, View.OnLongClickListener, ChatCallBack {
    @BindView(R.id.header)
    CustomHeader header;

    @BindView(R.id.imgProfile)
    ImageView imgProfile;
    @BindView(R.id.btnAddFriends)
    ImageView btnAddFriends;
    @BindView(R.id.txtEmail)
    TextView txtEmail;
    @BindView(R.id.txtName)
    TextView txtName;


    //Chat Module
    @BindView(R.id.lstChatList)
    RecyclerView lstChatList;
    @BindView(R.id.edtSearch)
    EditText edtSearch;
    @BindView(R.id.txtNoDataFound)
    TextView txtNoDataFound;
    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;

    GetChatListApi getChatListApi = null;
    private String search = "";
    ArrayList<ChatDetailListModel> objTempList = null;
    private ChatListAdapter chatListAdapter = null;
    private int lastPos = -1;
    private UserModel userModel = null;
    private boolean doubleBackToExitPressedOnce = false;
    RemoveConversationAPi removeConversationAPi  = null;
    LeftGroupAPi leftGroupAPi = null;
    boolean isOnActityResult = false;
    Dialog dialogDeleteLeft = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Use This Function to maintain stack of Open Activity*/
        Utils.addActivities("MainActivity", this);
        Utils.setToken(this);
        //For Get Id Of ALl View
        ButterKnife.bind(this);
        Utils.checkAllpermission(MainActivity.this);
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

        //Received User Model From LoginActivity


        //Set Header Details
        header.toolbar.setBackgroundColor(getResources().getColor(R.color.green_color));
        header.txtHeaderTitle.setText("Home");
        header.imgLeftSecond.setVisibility(View.VISIBLE);
        header.imgLeftSecond.setBackground(getResources().getDrawable(R.drawable.ic_tab_rss_active));

        header.imgRight.setVisibility(View.VISIBLE);
        header.imgRight.setBackground(getResources().getDrawable(R.drawable.ic_menu_logout));


        //Set Value
        if (Pref.getStringValue(MainActivity.this, Const.PREF_FULLNAME,"")!=null && !Pref.getStringValue(MainActivity.this, Const.PREF_FULLNAME,"").isEmpty()) {
            txtName.setText(Pref.getStringValue(MainActivity.this, Const.PREF_FULLNAME,""));
        }

        if (Pref.getStringValue(MainActivity.this, Const.PREF_USER_PROFILE_PIC,"") != null && !Pref.getStringValue(MainActivity.this, Const.PREF_USER_PROFILE_PIC,"").equalsIgnoreCase("null") && !Pref.getStringValue(MainActivity.this, Const.PREF_USER_PROFILE_PIC,"").isEmpty()) {
            Glide.with(MainActivity.this).asBitmap().load(Uri.parse(Pref.getStringValue(MainActivity.this, Const.PREF_USER_PROFILE_PIC,""))).into(imgProfile);
        }


        //Adapter Bind With RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        lstChatList.setLayoutManager(layoutManager);
        chatListAdapter = new ChatListAdapter(this, this, this);
        lstChatList.setAdapter(chatListAdapter);

       // callApi(1);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callApi(1);
            }
        });

        //Search Function Call From Search Edittext
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




    //Call APi FUnction
    private void callApi(int tag) {
        if (Utils.isOnline(this)) {
            Utils.showProgress(this);
            if (tag == 1) {
                if (getChatListApi == null)
                    getChatListApi = new GetChatListApi(this, this);
                getChatListApi.execute();
            } else if (tag == 2) {
                if (removeConversationAPi == null)
                    removeConversationAPi = new RemoveConversationAPi(this, this);
                removeConversationAPi.execute(chatListAdapter.objList.get(lastPos).user_id,chatListAdapter.objList.get(lastPos).conversation_id,chatListAdapter.objList.get(lastPos).is_group);
            } else if (tag == 3) {
                if (leftGroupAPi == null)
                    leftGroupAPi = new LeftGroupAPi(this, this);
                leftGroupAPi.execute(chatListAdapter.objList.get(lastPos).user_id,chatListAdapter.objList.get(lastPos).conversation_id);
            }
        } else {
            swipeContainer.setRefreshing(false);
            Utils.showToastMessage(this, getResources().getString(R.string.checkInternet), false);
        }

    }

    //Click Event
    @OnClick({R.id.btnAddFriends,R.id.imgLeftSecond,R.id.imgRight})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnAddFriends:
                startActivity(new Intent(MainActivity.this, FriendsListActivity.class));
                break;

            case R.id.row:
                lastPos = Integer.parseInt(view.getTag().toString());

                //put Chat Details To Chat Model
                ChatDetailListModel chatModel = new ChatDetailListModel();
                chatModel.fullname = chatListAdapter.objList.get(lastPos).fullname;
                chatModel.user_id = chatListAdapter.objList.get(lastPos).user_id;
                chatModel.conversation_id = chatListAdapter.objList.get(lastPos).conversation_id;
                chatModel.image = chatListAdapter.objList.get(lastPos).image;
                chatModel.is_group = chatListAdapter.objList.get(lastPos).is_group;
                chatModel.member_id = chatListAdapter.objList.get(lastPos).member_id;
                Utils.print("MEMEBRIDDDDDDDDDDDDDD---------------------"+chatModel.member_id+":::"+chatListAdapter.objList.get(lastPos).member_id);


                //Pass Chat Model To OneTOOneChat Activity
                startActivity(new Intent(MainActivity.this, OneToOneChatActivity.class).putExtra("CHATMODEL", chatModel));
                break;

            case R.id.imgLeftSecond:
                startActivity(new Intent(MainActivity.this, NewsFeedActivity.class));
                break;

            case R.id.imgRight:
                Utils.logOut(this);
                startActivity(new Intent(this, LoginActivity.class));
                finish();

                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {

        switch (view.getId()) {
            case R.id.row:
                lastPos = Integer.parseInt(view.getTag().toString());
                setDeleteLeftGroup();
                break;
        }
        return true;
    }

    private void setDeleteLeftGroup() {
        dialogDeleteLeft = new Dialog(this);
        dialogDeleteLeft.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogDeleteLeft.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogDeleteLeft.setContentView(R.layout.dialog_deleteleftgrup);
        dialogDeleteLeft.setCancelable(true);
        dialogDeleteLeft.getWindow().setGravity(Gravity.CENTER);
        Utils.applyFontFace(this, dialogDeleteLeft.findViewById(android.R.id.content).getRootView());
        final TextView btnLeftGroup = (TextView) dialogDeleteLeft.findViewById(R.id.btnLeftGroup);
        final TextView btnCancel = (TextView) dialogDeleteLeft.findViewById(R.id.btnCancel);

        btnLeftGroup.setText(chatListAdapter.objList.get(lastPos).is_group.equalsIgnoreCase("1")?"Left Group":"Delete Conversation");

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDeleteLeft.dismiss();
            }
        });
        btnLeftGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (chatListAdapter.objList.get(lastPos).is_group.equalsIgnoreCase("1")){
                    callApi(3);
                }else {
                    callApi(2);
                }

                dialogDeleteLeft.dismiss();
            }
        });
        dialogDeleteLeft.show();
    }


    //Response From APi
    @Override
    public void onResponse(String tag, Const.API_RESULT result, Object obj) {
        Utils.dismissProgress();
        if (tag == Const.GET_CHAT_LIST_API && result == Const.API_RESULT.SUCCESS) {
            try {

                swipeContainer.setRefreshing(false);
                if (result == Const.API_RESULT.SUCCESS) {
                    if (getChatListApi.objList == null || getChatListApi.objList.size() <= 0) {
                        lstChatList.setVisibility(View.GONE);
                        txtNoDataFound.setVisibility(View.VISIBLE);

                    } else {

                        lstChatList.setVisibility(View.VISIBLE);
                        txtNoDataFound.setVisibility(View.GONE);
                        chatListAdapter.addData(getChatListApi.objList);
                    }
                } else {

                    lstChatList.setVisibility(View.GONE);
                    txtNoDataFound.setVisibility(View.VISIBLE);

                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }else if (tag == Const.REMOVE_CONVERSATION_API && result == Const.API_RESULT.SUCCESS) {
            chatListAdapter.notifyDataSetChanged();
            callApi(1);


        }else if (tag == Const.LEFT_GROUP_API && result == Const.API_RESULT.SUCCESS) {
            chatListAdapter.notifyDataSetChanged();
            callApi(1);


        }

    }

    // Back Pressed Handling
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


    //Seach Data From List Function By Name
    private void searchData() {
        if (getChatListApi == null || getChatListApi.objList == null || getChatListApi.objList.size() <= 0)
            return;
        if (search.isEmpty() || !edtSearch.getText().toString().trim().equalsIgnoreCase(search)) {
            search = edtSearch.getText().toString().trim();
            objTempList = new ArrayList<>();
            if (edtSearch.getText().toString().trim().isEmpty())
                objTempList.addAll(getChatListApi.objList);
            else {
                for (ChatDetailListModel chatListModel : getChatListApi.objList) {
                    if (chatListModel.fullname.toLowerCase().contains(edtSearch.getText().toString().trim().toLowerCase())) {
                        objTempList.add(chatListModel);
                    }
                }
            }
            chatListAdapter.addData(objTempList);
        }
    }

    @Override
    public void onChatResponse(final SocketIOManager.CHAT_RESPONSE tag, final Object obj) {


        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Tag for Receive msg
                if (tag == SocketIOManager.CHAT_RESPONSE.NEW_MESSAGE) {

                    String json = new Gson().toJson(obj);

                    try {

                        //receive Details from Socket Io and set into model
                        JSONObject jsonObject = new JSONObject((String) new JSONArray(json).get(0));
                        boolean isCompare = false;

                        if (jsonObject.optString("member_id").contains(",")){
                            List<String> arrayListMember = Arrays.asList(jsonObject.optString("member_id").split(","));

                            for (int i = 0 ;i<arrayListMember.size();i++){

                                  if (arrayListMember.get(i).equalsIgnoreCase(Pref.getStringValue(MainActivity.this, Const.PREF_USERID, ""))) {
                                      isCompare = true;
                                      break;
                                  }


                            }

                        }else if (jsonObject.optString("member_id").equalsIgnoreCase(Pref.getStringValue(MainActivity.this, Const.PREF_USERID, ""))){
                            isCompare = true;
                        }


                        if (isCompare){
                            callApi(1);

                        }



                        } catch (JSONException e) {
                        e.printStackTrace();

                    }

                }
            }
        });


    }
    @Override
    protected void onResume() {
        super.onResume();
        callApi(1);
        if (!isOnActityResult) {
            MainApplication.getInstance().socketOnline();
            MainApplication.getInstance().getSoketMangerObject().setChatCallBack(MainActivity.this);
        }


        isOnActityResult = false;
    }


    //Socket Io onf Destroy here

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.print("MainActivity scoekt opfff-------------------");
        MainApplication.getInstance().socketOffline();
    }



}
