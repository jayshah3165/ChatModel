package com.echo.allscenarioapp.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.adapter.CommentListAdapter;
import com.echo.allscenarioapp.adapter.FriendsListAdapter;
import com.echo.allscenarioapp.api.AddCommentNewsFeedAPI;
import com.echo.allscenarioapp.api.DeleteCommentAPi;
import com.echo.allscenarioapp.api.GetCommentListApi;
import com.echo.allscenarioapp.api.GetFriendsListApi;
import com.echo.allscenarioapp.backend.ResponseListener;
import com.echo.allscenarioapp.custom.CustomHeader;
import com.echo.allscenarioapp.model.ChatDetailListModel;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Pref;
import com.echo.allscenarioapp.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CommentActivity extends AppCompatActivity implements View.OnClickListener, ResponseListener {
    @BindView(R.id.header)
    CustomHeader header;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.txtNoDataFound)
    TextView txtNoDataFound;
    @BindView(R.id.btnSend)
    TextView btnSend;
    @BindView(R.id.edtComment)
    EditText edtComment;
    CommentListAdapter commentListAdapter = null;
    GetCommentListApi getCommentListApi = null;
    DeleteCommentAPi deleteCommentAPi = null;
    AddCommentNewsFeedAPI addCommentNewsFeedAPI = null;
    String postID = "",ownerID = "";
    Dialog dialog = null;
    private int lastPos = -1;
    String type = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        Utils.addActivities("CommentActivity", this);

        //For Get Id Of ALl View
        ButterKnife.bind(this);
        //For Applt Font style
        Utils.applyFontFace(this, this.findViewById(android.R.id.content).getRootView());
        initialization();
    }

    public void initialization() {
        postID = getIntent().getStringExtra("POST_ID");
        ownerID = getIntent().getStringExtra("OWNER_ID");
        //Change StatusBar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.green_color));
        }


        //Set Header Details
        header.toolbar.setBackgroundColor(getResources().getColor(R.color.green_color));
        header.txtHeaderTitle.setText("Comments");
        header.imgLeft.setVisibility(View.VISIBLE);
        header.imgRight.setVisibility(View.GONE);

        //Bind Adapter To Recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        commentListAdapter = new CommentListAdapter(this, this);
        recyclerView.setAdapter(commentListAdapter);

        callApi(1);

    }

    //Call Api Function
    private void callApi(int tag) {
        if (Utils.isOnline(this)) {
            Utils.showProgress(this);
            if (tag == 1) {
                if (getCommentListApi == null)
                    getCommentListApi = new GetCommentListApi(this, this);
                getCommentListApi.execute(postID);
            } else if (tag == 2) {
                if (addCommentNewsFeedAPI == null)
                    addCommentNewsFeedAPI = new AddCommentNewsFeedAPI(this, this);
                addCommentNewsFeedAPI.execute(postID, Utils.getTextValue(edtComment));
            } else if (tag == 3) {
                if (deleteCommentAPi == null)
                    deleteCommentAPi = new DeleteCommentAPi(this, this);
                deleteCommentAPi.execute(postID,commentListAdapter.objList.get(lastPos).comment_id,type);
            }
        } else {
            Utils.showToastMessage(this, getResources().getString(R.string.checkInternet), false);
        }

    }

    private boolean isValidate() {
        boolean isError = false;
        if (Utils.isEmptyText(edtComment)) {
            edtComment.requestFocus();
            edtComment.setError(getResources().getString(R.string.strPleaseEnterComment));
            isError = true;
        }
        return isError;
    }


    private void setDeletecommentDialog() {
        dialog = new Dialog(CommentActivity.this, R.style.Theme_DialogDeleteConversation);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_comment);
        dialog.setCancelable(true);
        Utils.applyFontFace(CommentActivity.this, dialog.findViewById(android.R.id.content).getRootView());
        final TextView btnDeleteComment = (TextView) dialog.findViewById(R.id.btnDeleteComment);
        final TextView btnCancel = (TextView) dialog.findViewById(R.id.btnCancel);

        if (Pref.getStringValue(CommentActivity.this, Const.PREF_USERID, "").equalsIgnoreCase(commentListAdapter.objList.get(lastPos).user_id) || Pref.getStringValue(CommentActivity.this, Const.PREF_USERID, "").equalsIgnoreCase(ownerID) ){
            btnDeleteComment.setText("Delete Comment");
        }else {
            btnDeleteComment.setText("Spam Comment");
        }


        //type ->  1=delete comment 2=spam comment
        btnDeleteComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Pref.getStringValue(CommentActivity.this, Const.PREF_USERID, "").equalsIgnoreCase(commentListAdapter.objList.get(lastPos).user_id) || Pref.getStringValue(CommentActivity.this, Const.PREF_USERID, "").equalsIgnoreCase(ownerID) ){
                    type = "1";
                    callApi(3);
                    dialog.dismiss();

                }else {

                    type = "2";
                    callApi(3);
                    dialog.dismiss();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();
    }
    //CLick Event
    @OnClick({R.id.btnSend})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnSend:
                if (!isValidate()) {
                    callApi(2);
                }else {
                    Utils.showToastMessage(CommentActivity.this, "Please add Comment", false);
                }
            break;

            case R.id.imgMore:
                lastPos = Integer.parseInt(view.getTag().toString());
                setDeletecommentDialog();
                break;

        }
    }

    //APi Response Handling
    @Override
    public void onResponse(String tag, Const.API_RESULT result, Object obj) {
        Utils.dismissProgress();
        if (tag == Const.GET_COMMENT_LIST_API && result == Const.API_RESULT.SUCCESS) {
            try {


                if (result == Const.API_RESULT.SUCCESS) {
                    if (getCommentListApi.objList == null || getCommentListApi.objList.size() <= 0) {
                        recyclerView.setVisibility(View.GONE);
                        txtNoDataFound.setVisibility(View.VISIBLE);

                    } else {

                        recyclerView.setVisibility(View.VISIBLE);
                        txtNoDataFound.setVisibility(View.GONE);
                        commentListAdapter.addData(getCommentListApi.objList);
                    }
                } else {

                    recyclerView.setVisibility(View.GONE);
                    txtNoDataFound.setVisibility(View.VISIBLE);

                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }else if (tag == Const.ADD_COMMENT_API && result == Const.API_RESULT.SUCCESS) {

            edtComment.setText("");
            callApi(1);

        }else if (tag == Const.DELETE_COMMENT_API && result == Const.API_RESULT.SUCCESS) {

            callApi(1);
        }


    }


}
