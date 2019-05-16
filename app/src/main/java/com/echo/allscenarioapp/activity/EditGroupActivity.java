package com.echo.allscenarioapp.activity;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.adapter.EditGroupFriendsListAdapter;
import com.echo.allscenarioapp.adapter.FriendsListAdapter;
import com.echo.allscenarioapp.adapter.GroupFriendsListAdapter;
import com.echo.allscenarioapp.api.CreateGroupAPI;
import com.echo.allscenarioapp.api.DeleteGroupAPi;
import com.echo.allscenarioapp.api.EditGroupAPI;
import com.echo.allscenarioapp.api.GetGroupMemberListApi;
import com.echo.allscenarioapp.api.LeftGroupAPi;
import com.echo.allscenarioapp.api.RemoveMemberFromGroupAPi;
import com.echo.allscenarioapp.api.UploadImageS3API;
import com.echo.allscenarioapp.backend.ResponseListener;
import com.echo.allscenarioapp.custom.CustomHeader;
import com.echo.allscenarioapp.model.ChatDetailListModel;
import com.echo.allscenarioapp.model.FriendsListModel;
import com.echo.allscenarioapp.model.GroupListModel;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Pref;
import com.echo.allscenarioapp.utils.Utils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditGroupActivity extends AppCompatActivity implements View.OnClickListener, ResponseListener,View.OnLongClickListener {
    @BindView(R.id.header)
    CustomHeader header;
    @BindView(R.id.lstFriendsList)
    RecyclerView lstFriendsList;
    @BindView(R.id.imgProfilePic)
    SimpleDraweeView imgProfilePic;
    @BindView(R.id.edtCreateGruop)
    EditText edtCreateGruop;
    @BindView(R.id.btnAddmember)
    TextView btnAddmember;
    @BindView(R.id.btnLeftGroup)
    TextView btnLeftGroup;
    @BindView(R.id.btnDeleteGroup)
    TextView btnDeleteGroup;
    GroupListModel groupListModel = null;
    private EditGroupFriendsListAdapter editGroupFriendsListAdapter = null;

    EditGroupAPI editGroupAPI = null;
    DeleteGroupAPi deleteGroupAPi = null;
    UploadImageS3API uploadImageS3API = null;
    GetGroupMemberListApi getGroupMemberListApi = null;
    LeftGroupAPi leftGroupAPi = null;
    RemoveMemberFromGroupAPi removeMemberFromGroupAPi = null;
    String selectedImagePath = "";
    String grupName = "", grupImage = "", grupID = "", OwnerID = "", conversationID = "";
    private int lastPos = -1;
    Dialog dialogRemoveMember= null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);

        Utils.addActivities("EditGroupActivity", this);

        //For Get Id Of ALl View
        ButterKnife.bind(this);
        //For Applt Font style
        Utils.applyFontFace(this, this.findViewById(android.R.id.content).getRootView());
        initialization();
    }


    public void initialization() {
        grupImage = getIntent().getStringExtra("GROUP_IMAGE");
        grupName = getIntent().getStringExtra("GROUP_NAME");
        OwnerID = getIntent().getStringExtra("OWNER_ID");
        grupID = getIntent().getStringExtra("GRUP_ID");
        conversationID = getIntent().getStringExtra("CONVERSATION_ID");
        Utils.print("grup IDDDDDDDDDDDDDDD_-----------------------------------" + grupID);
        //Change StatusBar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.green_color));
        }


        //Set Header Details
        header.toolbar.setBackgroundColor(getResources().getColor(R.color.green_color));
        header.txtHeaderTitle.setText("Edit Group");
        header.imgLeft.setVisibility(View.VISIBLE);
        header.imgRight.setVisibility(View.VISIBLE);
        header.imgRight.setBackground(getResources().getDrawable(R.drawable.ic_tick_white));

        btnDeleteGroup.setVisibility(Pref.getStringValue(EditGroupActivity.this, Const.PREF_USERID, "").equalsIgnoreCase(OwnerID)?View.VISIBLE:View.GONE);
        btnAddmember.setVisibility(Pref.getStringValue(EditGroupActivity.this, Const.PREF_USERID, "").equalsIgnoreCase(OwnerID)?View.VISIBLE:View.GONE);


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        lstFriendsList.setLayoutManager(layoutManager);
        editGroupFriendsListAdapter = new EditGroupFriendsListAdapter(this, this,this);
        lstFriendsList.setAdapter(editGroupFriendsListAdapter);

        callApi(3);
        if (grupName != null && !grupName.isEmpty()) {
            edtCreateGruop.setText(grupName);
        }
        if (grupImage != null && !grupImage.isEmpty()) {
            imgProfilePic.setImageURI(Uri.parse(grupImage));

        }


    }
    @Override
    public boolean onLongClick(View view) {

        switch (view.getId()) {
            case R.id.row:
                lastPos = Integer.parseInt(view.getTag().toString());
                if (Pref.getStringValue(EditGroupActivity.this, Const.PREF_USERID, "").equalsIgnoreCase(OwnerID) && !Pref.getStringValue(EditGroupActivity.this, Const.PREF_USERID, "").equalsIgnoreCase(editGroupFriendsListAdapter.objList.get(lastPos).user_id)) {
                    setDeleteLeftGroup();
                }
                break;
        }
        return true;
    }
    private void setDeleteLeftGroup() {
        dialogRemoveMember = new Dialog(this);
        dialogRemoveMember.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogRemoveMember.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogRemoveMember.setContentView(R.layout.dialog_deleteleftgrup);
        dialogRemoveMember.setCancelable(true);
        dialogRemoveMember.getWindow().setGravity(Gravity.CENTER);
        Utils.applyFontFace(this, dialogRemoveMember.findViewById(android.R.id.content).getRootView());
        final TextView btnLeftGroup = (TextView) dialogRemoveMember.findViewById(R.id.btnLeftGroup);
        final TextView btnCancel = (TextView) dialogRemoveMember.findViewById(R.id.btnCancel);

        btnLeftGroup.setText("Remove from Group");

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogRemoveMember.dismiss();
            }
        });
        btnLeftGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                callApi(6);
                /*if (Pref.getStringValue(EditGroupActivity.this, Const.PREF_USERID, "").equalsIgnoreCase(editGroupFriendsListAdapter.objList.get(lastPos).user_id)){
                    callApi(6);
                }else {
                    return;
                }
*/
                dialogRemoveMember.dismiss();
            }
        });
        dialogRemoveMember.show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        callApi(3);
    }

    private void callApi(int tag) {
        if (Utils.isOnline(this)) {
            Utils.showProgress(this);
            if (tag == 1) {
                if (editGroupAPI == null)
                    editGroupAPI = new EditGroupAPI(this, this);
                editGroupAPI.execute(Utils.getTextValue(edtCreateGruop), grupImage, grupID);
            } else if (tag == 2) {
                if (uploadImageS3API == null)
                    uploadImageS3API = new UploadImageS3API(this, this);
                uploadImageS3API.execute(selectedImagePath, "4");
            } else if (tag == 3) {
                if (getGroupMemberListApi == null)
                    getGroupMemberListApi = new GetGroupMemberListApi(this, this);
                getGroupMemberListApi.execute(grupID);
            } else if (tag == 4) {
                if (deleteGroupAPi == null)
                    deleteGroupAPi = new DeleteGroupAPi(this, this);
                deleteGroupAPi.execute(grupID);
            }else if (tag == 5) {
                if (leftGroupAPi == null)
                    leftGroupAPi = new LeftGroupAPi(this, this);
                leftGroupAPi.execute(grupID,conversationID);
            }else if (tag == 6) {
                if (removeMemberFromGroupAPi == null)
                    removeMemberFromGroupAPi = new RemoveMemberFromGroupAPi(this, this);
                removeMemberFromGroupAPi.execute(grupID,editGroupFriendsListAdapter.objList.get(lastPos).user_id,conversationID);
            }
        } else {
            Utils.showToastMessage(this, getResources().getString(R.string.checkInternet), false);
        }
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

    private void saveValue() {
        if (groupListModel == null)
            groupListModel = new GroupListModel();

        groupListModel.group_name = Utils.getTextValue(edtCreateGruop);
        groupListModel.group_id = grupID;
        groupListModel.group_image = grupImage;


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
    @OnClick({R.id.imgProfilePic, R.id.imgRight, R.id.btnDeleteGroup, R.id.btnLeftGroup,R.id.btnAddmember})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgProfilePic:
                CropImage.activity(null)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
                break;

            case R.id.imgRight:
                saveValue();
                if (selectedImagePath != null && !selectedImagePath.isEmpty())
                    callApi(2);
                else
                    callApi(1);
                break;


            case R.id.btnDeleteGroup:
                callApi(4);
                break;
            case R.id.btnLeftGroup:
                callApi(5);
                break;

            case R.id.btnAddmember:
                startActivity(new Intent(EditGroupActivity.this,AddMemberListActivity.class).putExtra("GROUP_ID",grupID).putExtra("CONVERSATION_ID",conversationID));
                break;
        }
    }

    @Override
    public void onResponse(String tag, Const.API_RESULT result, Object obj) {
        Utils.dismissProgress();
        if (tag == Const.EDIT_GROUP_API && result == Const.API_RESULT.SUCCESS) {

            ChatDetailListModel chatModel = new ChatDetailListModel();
            chatModel.fullname = editGroupAPI.grupName;
            chatModel.image = editGroupAPI.grupImage;
            chatModel.user_id = grupID;
            chatModel.is_group = "1";
            chatModel.conversation_id = conversationID;


            setResult(RESULT_OK,new Intent().putExtra("CHATMODEL",chatModel));
//            startActivity(new Intent(EditGroupActivity.this, OneToOneChatActivity.class).putExtra("CHATMODEL", chatModel));
            finish();
        } else if (tag == Const.UPLOAD_API && result == Const.API_RESULT.SUCCESS) {
//            Toast.makeText(SignUpActivity.this, getResources().getString(R.string.strImageSuccess), Toast.LENGTH_SHORT).show();
            if (groupListModel == null)
                groupListModel = new GroupListModel();
            grupImage = uploadImageS3API.url;
            callApi(1);
        } else if (tag == Const.GET_GROUP_MEMBER_LIST_API && result == Const.API_RESULT.SUCCESS) {

            try {


                if (result == Const.API_RESULT.SUCCESS) {
                    if (getGroupMemberListApi.objList == null || getGroupMemberListApi.objList.size() <= 0) {
                        lstFriendsList.setVisibility(View.GONE);


                    } else {

                        lstFriendsList.setVisibility(View.VISIBLE);

                        editGroupFriendsListAdapter.addData(getGroupMemberListApi.objList);
                    }
                } else {

                    lstFriendsList.setVisibility(View.GONE);


                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        }else if (tag == Const.DELETE_GROUP_API && result == Const.API_RESULT.SUCCESS) {

            finish();

        }else if (tag == Const.LEFT_GROUP_API && result == Const.API_RESULT.SUCCESS) {

            finish();
        }else if (tag == Const.REMOVE_MEMBER_FROM_GROUP_API && result == Const.API_RESULT.SUCCESS) {

           callApi(3);
        }
    }

}
