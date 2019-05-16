package com.echo.allscenarioapp.activity;

import android.content.Intent;
import android.database.Cursor;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.services.s3.model.S3DataSource;
import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.adapter.CreateGroupFriendsListAdapter;
import com.echo.allscenarioapp.adapter.EditGroupFriendsListAdapter;
import com.echo.allscenarioapp.adapter.FriendsListAdapter;
import com.echo.allscenarioapp.adapter.GroupFriendsListAdapter;
import com.echo.allscenarioapp.api.CreateGroupAPI;
import com.echo.allscenarioapp.api.UploadImageS3API;
import com.echo.allscenarioapp.backend.ResponseListener;
import com.echo.allscenarioapp.custom.CustomHeader;
import com.echo.allscenarioapp.model.ChatDetailListModel;
import com.echo.allscenarioapp.model.FriendsListModel;
import com.echo.allscenarioapp.model.GroupListModel;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Utils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateGroupActivity extends AppCompatActivity implements View.OnClickListener, ResponseListener {
    @BindView(R.id.header)
    CustomHeader header;
    @BindView(R.id.lstFriendsList)
    RecyclerView lstFriendsList;
    @BindView(R.id.imgProfilePic)
    SimpleDraweeView imgProfilePic;
    @BindView(R.id.edtCreateGruop)
    EditText edtCreateGruop;
    @BindView(R.id.btnCreateGroup)
    TextView btnCreateGroup;
    GroupListModel groupListModel = null;
    private CreateGroupFriendsListAdapter createGroupFriendsListAdapter = null;
    private int lastPos = -1;
    CreateGroupAPI createGroupAPI = null;
    UploadImageS3API uploadImageS3API = null;
    String selectedImagePath = "";
    String ids= "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        Utils.addActivities("CreateGroupActivity", this);

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
        header.txtHeaderTitle.setText("Create Group");
        header.imgLeft.setVisibility(View.VISIBLE);
        header.imgRight.setVisibility(View.GONE);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        //get and set selected friend list from GroupselectFriendActivity
        lstFriendsList.setLayoutManager(layoutManager);
        createGroupFriendsListAdapter = new CreateGroupFriendsListAdapter(this,this);
        createGroupFriendsListAdapter.addData((ArrayList<FriendsListModel>) getIntent().getSerializableExtra("LIST"));
        lstFriendsList.setAdapter(createGroupFriendsListAdapter);



        for (FriendsListModel friendsListModel : createGroupFriendsListAdapter.objList) {

           ids = ids + "," + friendsListModel.user_id;
        }

        if (ids.startsWith(","))
            ids = ids.trim().substring(1);

            Utils.print("IDSSSSSSSSSSSSSSSSSSSSSS---------------"+ids);




    }


    private void callApi(int tag) {
        if (Utils.isOnline(this)) {
            Utils.showProgress(this);
            if (tag == 1) {
                if (createGroupAPI == null)
                    createGroupAPI = new CreateGroupAPI(this, this);
                createGroupAPI.execute(groupListModel);
            } else if (tag == 2) {

                if (uploadImageS3API == null)
                    uploadImageS3API = new UploadImageS3API(this, this);
                uploadImageS3API.execute(selectedImagePath, "4");
            }
        } else {
            Utils.showToastMessage(this, getResources().getString(R.string.checkInternet), false);
        }
    }

    private void saveValue() {
        if (groupListModel == null)
            groupListModel = new GroupListModel();

        groupListModel.group_name = Utils.getTextValue(edtCreateGruop);
        groupListModel.member_ids =ids;
        Utils.print("APIIIIIIIIIIIIIIIII -------------IDSSSSSSSSSSSSSSSSSSSSSS---------------"+groupListModel.member_ids);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imgProfilePic.setImageURI(result.getUri());
                selectedImagePath = getRealPathFromURI(result.getUri());

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isValidate() {
        boolean isError = false;
        if (Utils.isEmptyText(edtCreateGruop)) {
            edtCreateGruop.requestFocus();
            edtCreateGruop.setError(getResources().getString(R.string.strPleaseEnterGroupName));
            isError = true;
        }
        return isError;
    }


    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
    //CLick Event
    @OnClick({R.id.imgProfilePic,R.id.btnCreateGroup})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgProfilePic:
                CropImage.activity(null)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
                break;

            case R.id.btnCreateGroup:
                if (!isValidate()) {
                    saveValue();
                    if (selectedImagePath != null && !selectedImagePath.isEmpty())
                        callApi(2);
                    else
                        callApi(1);
                }
                break;
        }
    }
    @Override
    public void onResponse(String tag, Const.API_RESULT result, Object obj) {
        Utils.dismissProgress();
        if (tag == Const.CREATE_GROUP_API && result == Const.API_RESULT.SUCCESS) {

            Utils.print("GRUP RESPONSE -----------------------------"+createGroupAPI.group_id+":::"+createGroupAPI.grupName);



            ChatDetailListModel chatModel = new ChatDetailListModel();
            chatModel.fullname = createGroupAPI.grupName;
            chatModel.user_id = createGroupAPI.group_id;
            chatModel.conversation_id = createGroupAPI.conv_id;
            chatModel.image = createGroupAPI.grupImage;
            chatModel.member_id = createGroupAPI.memberID;
            chatModel.is_group = "1";


            //Pass Chat Model To OneTOOneChat Activity
            startActivity(new Intent(CreateGroupActivity.this,OneToOneChatActivity.class).putExtra("CHATMODEL",chatModel));

        } else if (tag == Const.UPLOAD_API && result == Const.API_RESULT.SUCCESS) {
//            Toast.makeText(SignUpActivity.this, getResources().getString(R.string.strImageSuccess), Toast.LENGTH_SHORT).show();
            if (groupListModel == null)
                groupListModel = new GroupListModel();
            groupListModel.group_image = uploadImageS3API.url;
            callApi(1);
        }
    }


}
