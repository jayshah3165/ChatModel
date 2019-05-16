package com.echo.allscenarioapp.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.adapter.FeedListAdapter;
import com.echo.allscenarioapp.adapter.FriendsListAdapter;
import com.echo.allscenarioapp.api.DeletePostAPi;
import com.echo.allscenarioapp.api.GetFeedListApi;
import com.echo.allscenarioapp.api.GetFriendsListApi;
import com.echo.allscenarioapp.api.LikeAPI;
import com.echo.allscenarioapp.backend.ResponseListener;
import com.echo.allscenarioapp.custom.CustomHeader;
import com.echo.allscenarioapp.model.ChatDetailListModel;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Pref;
import com.echo.allscenarioapp.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NewsFeedActivity extends AppCompatActivity implements View.OnClickListener, ResponseListener {
    @BindView(R.id.header)
    CustomHeader header;
    @BindView(R.id.lstFeedList)
    RecyclerView lstFeedList;
    @BindView(R.id.llWriteSomething)
    LinearLayout llWriteSomething;
    @BindView(R.id.txtNoDataFound)
    TextView txtNoDataFound;

    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;
    FeedListAdapter feedListAdapter = null;
    GetFeedListApi getFeedListApi = null;
    LikeAPI likeAPI = null;
    private int lastPos = -1;
    DeletePostAPi deletePostAPi = null;
    Dialog dialog = null;
    String type ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_feed);
        Utils.addActivities("NewsFeedActivity", this);

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
        header.txtHeaderTitle.setText("News Feed");
        header.imgLeft.setVisibility(View.VISIBLE);
        header.imgRight.setVisibility(View.GONE);

        //Bind Adapter To Recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        lstFeedList.setLayoutManager(layoutManager);
        feedListAdapter = new FeedListAdapter(this, this);
        lstFeedList.setAdapter(feedListAdapter);

        callApi(1);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callApi(1);
            }
        });


    }

    //Call Api Function
    private void callApi(int tag) {
        if (Utils.isOnline(this)) {
            Utils.showProgress(this);
            if (tag == 1) {
                if (getFeedListApi == null)
                    getFeedListApi = new GetFeedListApi(this, this);
                getFeedListApi.execute();
            } else if (tag == 2) {
                if (likeAPI == null)
                    likeAPI = new LikeAPI(this, this);
                likeAPI.execute(feedListAdapter.objList.get(lastPos).feed_id, "1", feedListAdapter.objList.get(lastPos).is_like.equalsIgnoreCase("1") ? "0" : "1");
            } else if (tag == 3) {
                if (deletePostAPi == null)
                    deletePostAPi = new DeletePostAPi(this, this);
                deletePostAPi.execute(feedListAdapter.objList.get(lastPos).feed_id,type);
            }
        } else {
            swipeContainer.setRefreshing(false);
            Utils.showToastMessage(this, getResources().getString(R.string.checkInternet), false);
        }

    }

    //CLick Event
    @OnClick({R.id.llWriteSomething})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rrComment:
            case R.id.rrCommentView:
                lastPos = Integer.parseInt(view.getTag().toString());
                startActivity(new Intent(NewsFeedActivity.this, CommentActivity.class).putExtra("POST_ID", feedListAdapter.objList.get(lastPos).feed_id).putExtra("OWNER_ID",feedListAdapter.objList.get(lastPos).user_id));
                break;
            case R.id.imgLikeUser:
                lastPos = Integer.parseInt(view.getTag().toString());
                callApi(2);
                break;

            case R.id.imgMore:
                lastPos = Integer.parseInt(view.getTag().toString());
                setDeletePostDialog();
                break;

            case R.id.imgAudio:
                //Play Audio using Inbuilt device app
                lastPos = Integer.parseInt(view.getTag().toString());
                Intent i = new Intent();
                i.setAction(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.parse(feedListAdapter.objList.get(lastPos).media_url), "audio/*");
                startActivity(i);
                break;

            case R.id.play_image:
                //Play Video using Inbuilt device app
                lastPos = Integer.parseInt(view.getTag().toString());
                Intent video = new Intent(Intent.ACTION_VIEW);
                video.setDataAndType(Uri.parse(feedListAdapter.objList.get(lastPos).media_url), "video/*");
                startActivity(video);

                break;

            case R.id.img_upload_preview:
                //Play Video using Inbuilt device app
                lastPos = Integer.parseInt(view.getTag().toString());
                Utils.setDialogImage(NewsFeedActivity.this,feedListAdapter.objList.get(lastPos).thumb_url);
                break;

            case R.id.llWriteSomething:
                startActivity(new Intent(NewsFeedActivity.this, PostNewsFeedActivity.class));
                break;

            case R.id.rrLikeView:
                lastPos = Integer.parseInt(view.getTag().toString());
                startActivity(new Intent(NewsFeedActivity.this, LikeListActivity.class).putExtra("POST_ID", feedListAdapter.objList.get(lastPos).feed_id));
                break;


        }
    }

    private void setDeletePostDialog() {
        dialog = new Dialog(NewsFeedActivity.this, R.style.Theme_DialogDeleteConversation);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_newsfeed);
        dialog.setCancelable(true);
        Utils.applyFontFace(NewsFeedActivity.this, dialog.findViewById(android.R.id.content).getRootView());
        final TextView btnShare = (TextView) dialog.findViewById(R.id.btnShare);
        final TextView btnDeletePost = (TextView) dialog.findViewById(R.id.btnDeletePost);
        final TextView btnCancel = (TextView) dialog.findViewById(R.id.btnCancel);

        btnDeletePost.setText(Pref.getStringValue(NewsFeedActivity.this, Const.PREF_USERID, "").equalsIgnoreCase(feedListAdapter.objList.get(lastPos).user_id)?"Delete Post":"Spam Post");
       //type -> 1= delete post 2= spam post
        btnDeletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Pref.getStringValue(NewsFeedActivity.this, Const.PREF_USERID, "").equalsIgnoreCase(feedListAdapter.objList.get(lastPos).user_id)){
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
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
       callApi(1);
    }

    @Override
    public void onResponse(String tag, Const.API_RESULT result, Object obj) {
        Utils.dismissProgress();
        if (tag == Const.GET_FEED_LIST_API && result == Const.API_RESULT.SUCCESS) {
            try {
                swipeContainer.setRefreshing(false);

                if (result == Const.API_RESULT.SUCCESS) {
                    if (getFeedListApi.objList == null || getFeedListApi.objList.size() <= 0) {
                        lstFeedList.setVisibility(View.GONE);
                        txtNoDataFound.setVisibility(View.VISIBLE);

                    } else {

                        lstFeedList.setVisibility(View.VISIBLE);
                        txtNoDataFound.setVisibility(View.GONE);
                        feedListAdapter.addData(getFeedListApi.objList);
                    }
                } else {

                    lstFeedList.setVisibility(View.GONE);
                    txtNoDataFound.setVisibility(View.VISIBLE);

                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else if (tag == Const.LIKE_API && result == Const.API_RESULT.SUCCESS) {
            feedListAdapter.objList.get(lastPos).is_like = feedListAdapter.objList.get(lastPos).is_like.equalsIgnoreCase("1") ? "0" : "1";
            feedListAdapter.objList.get(lastPos).number_of_likes = String.valueOf(feedListAdapter.objList.get(lastPos).is_like.equalsIgnoreCase("1") ? (Integer.parseInt(feedListAdapter.objList.get(lastPos).number_of_likes) + 1) : (Integer.parseInt(feedListAdapter.objList.get(lastPos).number_of_likes) - 1));
            feedListAdapter.notifyDataSetChanged();
        }else if (tag == Const.DELETE_POST_API && result == Const.API_RESULT.SUCCESS) {
            callApi(1);
        }

    }

}
