package com.echo.allscenarioapp.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.adapter.ChatDetailListAdapter;
import com.echo.allscenarioapp.api.ClearMassageAPi;
import com.echo.allscenarioapp.api.GetChatDetailListApi;
import com.echo.allscenarioapp.api.ReadAllAPi;
import com.echo.allscenarioapp.api.SendMessageApi;
import com.echo.allscenarioapp.api.UploadImageS3API;
import com.echo.allscenarioapp.backend.ResponseListener;
import com.echo.allscenarioapp.chat.ChatCallBack;
import com.echo.allscenarioapp.chat.SocketIOManager;
import com.echo.allscenarioapp.model.ChatDetailListModel;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Pref;
import com.echo.allscenarioapp.utils.Utils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OneToOneChatActivity extends AppCompatActivity implements View.OnClickListener, ResponseListener, ChatCallBack {
    @BindView(R.id.lstChatList)
    RecyclerView lstChatList;
    @BindView(R.id.txtFriendsName)
    TextView txtFriendsName;
    @BindView(R.id.txtOnline)
    TextView txtOnline;
    @BindView(R.id.imgCross)
    ImageView imgCross;
    @BindView(R.id.imgAttachment)
    ImageView imgAttachment;
    @BindView(R.id.imgCamera)
    ImageView imgCamera;
    @BindView(R.id.edtComments)
    EditText edtComments;
    @BindView(R.id.imgRecord)
    ImageView imgRecord;
    @BindView(R.id.imgSend)
    ImageView imgSend;

    @BindView(R.id.imgProfilePic)
    SimpleDraweeView imgProfilePic;
    @BindView(R.id.llWriteComment)
    RelativeLayout llWriteComment;

    private static final int PICKFILE_RESULT_CODE = 1;
    //API
    GetChatDetailListApi getChatDetailListApi = null;
    SendMessageApi sendMessageApi = null;
    UploadImageS3API uploadImageS3API = null;
    ClearMassageAPi clearMassageAPi = null;
    ReadAllAPi readAllAPi = null;

    //Adapter
    ChatDetailListAdapter chatDetailListAdapter = null;


    //Create Obj
    PopupWindow pw = null;

    private int lastPos = -1;
    String type = "";
    String selectedImagePath = "";
    boolean isOnActityResult = false;
    LinearLayoutManager layoutManager;
    Dialog dialogAttachment = null;
    //Model
   public ChatDetailListModel chatListModel = null;
   public ChatDetailListModel chatDetailListModel = null;
    private static final int REQUEST_VIDEO_CAPTURE = 300;
    String mediaURL = "",thumbURL = "";
    public  static OneToOneChatActivity oneToOneChatActivity = null;
    boolean isCallMessageapi = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_to_one_chat);
        /* Use This Function to maintain stack of Open Activity*/
        Utils.addActivities("OneToOneChatActivity", this);

        //For Get Id Of ALl View
        ButterKnife.bind(this);
        //For Apply Font style
        Utils.applyFontFace(this, this.findViewById(android.R.id.content).getRootView());
        initialization();
    }


    private void initialization() {
        oneToOneChatActivity = this;
        //Set Keyboard send button to sent msg
        edtComments.setImeOptions(EditorInfo.IME_ACTION_SEND);
        edtComments.setRawInputType(InputType.TYPE_CLASS_TEXT);

        //Allow All Permission
        Utils.checkAllpermission(this);

        chatListModel = (ChatDetailListModel) getIntent().getSerializableExtra("CHATMODEL");


        if (chatListModel.fullname != null && !chatListModel.fullname.isEmpty()) {
            txtFriendsName.setText(chatListModel.fullname);

        }


        imgProfilePic.setActualImageResource(R.drawable.avatar);
        if (chatListModel.image != null && !chatListModel.image.isEmpty()) {
            imgProfilePic.setImageURI(Uri.parse(chatListModel.image));

        }

        //Bind ADapter to Recyclerview
        layoutManager = new LinearLayoutManager(this);
        lstChatList.setLayoutManager(layoutManager);

        chatDetailListAdapter = new ChatDetailListAdapter(this, this);
        lstChatList.setAdapter(chatDetailListAdapter);




        callApi(1, true);


        //KeyBoard Send Button FUnction
        edtComments.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @SuppressLint("StringFormatInvalid")
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    /* handle action here */
                    handled = true;
                    type = "1";
                    if (Utils.getTextValue(edtComments).isEmpty()) {
                        edtComments.setError(String.format(getResources().getString(R.string.strEnterComment), getResources().getString(R.string.strEmail).toLowerCase()));
                    } else {
                        setValueBeforeSendMessage();

                        if (!isCallMessageapi) {
                            isCallMessageapi = true;
                            callApi(3, true);
                        }

                    }
                }
                return handled;
            }
        });


    }


    private void setValueBeforeSendMessage()
    {
        chatDetailListModel= new ChatDetailListModel();
        chatDetailListModel.message = edtComments.getText().toString().trim();
        chatDetailListModel.media_url = type.equalsIgnoreCase("1")?"":mediaURL;
        chatDetailListModel.type = type;
        chatDetailListModel.to_id = chatListModel.user_id;
        chatDetailListModel.is_group = chatListModel.is_group;
        chatDetailListModel.thumb_url = thumbURL;

        String mediaURL = "",thumbURL = "";
    }
    //Attachment Dialog For Image ,video,file  //(1=text message 2=video 3=audio 4=image)
    private void setAttachmentDialog() {
        dialogAttachment = new Dialog(OneToOneChatActivity.this, R.style.Theme_DialogDeleteConversation);
        dialogAttachment.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogAttachment.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogAttachment.setContentView(R.layout.dialog_attachment);
        dialogAttachment.setCancelable(true);
        Utils.applyFontFace(OneToOneChatActivity.this, dialogAttachment.findViewById(android.R.id.content).getRootView());
        final TextView btnImage = (TextView) dialogAttachment.findViewById(R.id.btnImage);
        final TextView btnVideo = (TextView) dialogAttachment.findViewById(R.id.btnVideo);
        final TextView btnAttachment = (TextView) dialogAttachment.findViewById(R.id.btnAttachment);
        final TextView btnCancel = (TextView) dialogAttachment.findViewById(R.id.btnCancel);
        //Open File Documents Intent
        btnAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                type = "5";
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                Intent i = Intent.createChooser(intent, "File");
                startActivityForResult(i, PICKFILE_RESULT_CODE);

                dialogAttachment.dismiss();

            }
        });
        //Set Intent For Image Cropper
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "4";
                CropImage.activity(null)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(OneToOneChatActivity.this);

                dialogAttachment.dismiss();
            }
        });

        //Set Intent For Video upload
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "2";
                Intent videoCaptureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (videoCaptureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(videoCaptureIntent, REQUEST_VIDEO_CAPTURE);
                    dialogAttachment.dismiss();
                }
            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAttachment.dismiss();
            }
        });


        dialogAttachment.show();
    }

    //Open PopupWindow Here
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

            txtClearConversation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callApi(5, true);
                    pw.dismiss();
                }
            });

            txtBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pw.dismiss();
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

    //Call Api

    private void callApi(int tag, boolean isShowProgress) {
        if (Utils.isOnline(this)) {
            Utils.showProgress(this);
            if (tag == 1) {
                if (getChatDetailListApi == null)
                    getChatDetailListApi = new GetChatDetailListApi(this, this);
                getChatDetailListApi.execute(chatListModel.user_id,chatListModel.is_group,chatListModel.conversation_id);
            } else if (tag == 3) {
                if (sendMessageApi == null)
                    sendMessageApi = new SendMessageApi(this, this);
                sendMessageApi.execute(chatDetailListModel);
            } else if (tag == 4) {
                if (uploadImageS3API == null)
                    Utils.print("file upload path--------------------" + selectedImagePath);
                uploadImageS3API = new UploadImageS3API(this, this);
                uploadImageS3API.execute(selectedImagePath, type);
            } else if (tag == 5) {
                if (clearMassageAPi == null)
                    clearMassageAPi = new ClearMassageAPi(this, this);
                clearMassageAPi.execute(getChatDetailListApi.convrsation_id, chatListModel.user_id, chatListModel.is_group);
            } else if (tag == 6) {
                if (readAllAPi == null)
                    readAllAPi = new ReadAllAPi(this, this);
                readAllAPi.execute(getChatDetailListApi.convrsation_id);
            }
        } else
            Utils.showToastMessage(this, getResources().getString(R.string.checkInternet), false);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        callApi(6, false);
    }

    //Click EVent Handling
    @OnClick({R.id.imgProfilePic, R.id.imgSend, R.id.imgCross, R.id.imgCamera, R.id.imgRecord, R.id.imgAttachment})
    public void onClick(View view) {
        if (pw != null && pw.isShowing())
            pw.dismiss();
        switch (view.getId()) {


            case R.id.imgProfilePic:

                if (chatListModel.is_group.equalsIgnoreCase("1")) {
                    startActivityForResult(new Intent(OneToOneChatActivity.this, EditGroupActivity.class).putExtra("GROUP_NAME", chatListModel.fullname).putExtra("GROUP_IMAGE", chatListModel.image)
                            .putExtra("OWNER_ID", getChatDetailListApi.ownerID).putExtra("GRUP_ID", chatListModel.user_id).putExtra("CONVERSATION_ID", chatListModel.conversation_id),Const.INTENT400);
                }
                break;
            case R.id.imgCross:
                callApi(6, false);
                break;
            case R.id.imgRecord:

                //Record AUdio StartActivityForResult Call
                //(1=text message 2=video 3=audio 4=image)
                type = "3";
                try {
                    Intent soundRecorderIntent = new Intent();  // create intent
                    soundRecorderIntent.setAction(MediaStore.Audio.Media.RECORD_SOUND_ACTION);  // set action
                    startActivityForResult(soundRecorderIntent, Const.INTENT300); // start activity
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "There is no application in your device to record audio", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                break;
            case R.id.imgCamera:
                setAttachmentDialog();
                break;

            case R.id.imgAudio:
                //Play Audio using Inbuilt device app
                lastPos = Integer.parseInt(view.getTag().toString());
                Intent i = new Intent();
                i.setAction(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.parse(chatDetailListAdapter.objList.get(lastPos).media_url), "audio/*");
                startActivity(i);
                break;
            case R.id.imgAudioMy:
                //Play Audio using Inbuilt device app
                lastPos = Integer.parseInt(view.getTag().toString());
                Intent ii = new Intent();
                ii.setAction(Intent.ACTION_VIEW);
                ii.setDataAndType(Uri.parse(chatDetailListAdapter.objList.get(lastPos).media_url), "audio/*");
                startActivity(ii);
                break;


            case R.id.imgplay:
                //Play Video using Inbuilt device app
                lastPos = Integer.parseInt(view.getTag().toString());
                Intent video = new Intent(Intent.ACTION_VIEW);
                video.setDataAndType(Uri.parse(chatDetailListAdapter.objList.get(lastPos).media_url), "video/*");
                startActivity(video);

                break;


            case R.id.imgplayMy:
                //Play video using Inbuilt device app
                lastPos = Integer.parseInt(view.getTag().toString());
                Intent videoIntent = new Intent(Intent.ACTION_VIEW);
                videoIntent.setDataAndType(Uri.parse(chatDetailListAdapter.objList.get(lastPos).media_url), "video/*");
                startActivity(videoIntent);

                break;
            case R.id.imgAttachment:
                initiatePopupWindow(view);
                break;
        }
    }


    //Response method for APi
    @Override
    public void onResponse(String tag, Const.API_RESULT result, Object obj) {
        Utils.dismissProgress();
        if (tag == Const.GET_MSG_LIST_API) {
            try {


                if (result == Const.API_RESULT.SUCCESS) {
                    //Show Online Text Here via COndition
                    txtOnline.setVisibility(getChatDetailListApi.isonline.equalsIgnoreCase("1") ? View.VISIBLE : View.GONE);

                    //Set Chatlist in Adapter
                    if (getChatDetailListApi.objList == null || getChatDetailListApi.objList.size() <= 0) {
                        lstChatList.setVisibility(View.GONE);
                    } else {
                        lstChatList.setVisibility(View.VISIBLE);
                        chatDetailListAdapter.addData(getChatDetailListApi.objList);
                        layoutManager.scrollToPosition(getChatDetailListApi.objList.size() - 1);
                        MainApplication.getInstance().getSoketMangerObject().ReadMessage(chatListModel.user_id);

                        llWriteComment.setVisibility(chatListModel.is_group.equalsIgnoreCase("1")&&chatListModel.is_left.equalsIgnoreCase("1")?View.GONE:View.VISIBLE);
                    }
                } else {
                    lstChatList.setVisibility(View.GONE);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else if (tag == Const.SEND_MESSAGE_API) {

            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edtComments.getWindowToken(), 0);


            //Send Details TO Socket Io
            JSONObject jsonObject = new JSONObject();
            try {
                Utils.print("JSON MEMEBRID_-----------------------"+chatListModel.member_id);
                jsonObject.put("user_id", Pref.getStringValue(OneToOneChatActivity.this, Const.PREF_USERID, ""))
                        .put("to_id", chatListModel.user_id) //Chat List APi
                        .put("image", Pref.getStringValue(OneToOneChatActivity.this, Const.PREF_USER_PROFILE_PIC, "")) //Chat List APi
                        .put("type", chatDetailListModel.type) //Chat List APi
                        .put("uname", Pref.getStringValue(OneToOneChatActivity.this, Const.PREF_FULLNAME, "")) //Chat List APi
                        .put("message", edtComments.getText().toString().trim()) //Chat List APi
                        .put("media_url", chatDetailListModel.media_url)
                        .put("thumb_url", chatDetailListModel.thumb_url)
                        .put("conversation_id", chatListModel.conversation_id) //Chat List APi
                        .put("conversation_id", chatListModel.is_group)
                        .put("member_id", chatListModel.member_id)
                        .put("device_type", Const.DEVICE_TYPE)
                        .put("language", Pref.getStringValue(OneToOneChatActivity.this, Const.PREF_LANGUAGE, "EN"))
                        .put("token", Pref.getStringValue(OneToOneChatActivity.this, Const.PREF_GCMTOKEN, ""));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            isCallMessageapi = false;
            MainApplication.getInstance().getSoketMangerObject().SendMessage(jsonObject.toString());
            edtComments.setText("");
           //callApi(1, true);
        } else if (tag == Const.UPLOAD_API && result == Const.API_RESULT.SUCCESS) {

            if( mediaURL==null || mediaURL.isEmpty()) {
                thumbURL="";
                mediaURL = uploadImageS3API.url;
            }
            else if( thumbURL==null || thumbURL.isEmpty())
                thumbURL = uploadImageS3API.url;

            if(type.equalsIgnoreCase("2") &&( thumbURL==null ||  thumbURL.isEmpty())){
                type ="4";
               Bitmap thumbBmp = ThumbnailUtils.createVideoThumbnail(selectedImagePath, MediaStore.Video.Thumbnails.MICRO_KIND);
                selectedImagePath = Utils.saveImage(thumbBmp, OneToOneChatActivity.this);
                thumbBmp =null;
                callApi(4,true);
            }else
            {
                if(thumbURL!=null && !thumbURL.isEmpty())
                    type ="2";
                setValueBeforeSendMessage();
                Utils.print("MEDI URL THUMB URL ---------------------"+mediaURL+":::"+thumbURL);
                callApi(3, true);
            }




            callApi(3, true);
        } else if (tag == Const.DELETE_CONVERSATION_API && result == Const.API_RESULT.SUCCESS) {
            chatDetailListAdapter.notifyDataSetChanged();
            callApi(1, true);


        } else if (tag == Const.READ_ALL_API && result == Const.API_RESULT.SUCCESS) {

           finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


         if (resultCode == RESULT_OK && requestCode == Const.INTENT400){
             chatListModel = (ChatDetailListModel) data.getSerializableExtra("CHATMODEL");
             callApi(1,true);

         }
         else  if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                selectedImagePath = getRealPathFromURI(result.getUri());

                callApi(4, true);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == Const.INTENT300) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                selectedImagePath = getRealPathFromAudioURI(uri);
                callApi(4, true);

            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_VIDEO_CAPTURE) {
            Uri uri = data.getData();
            selectedImagePath = getRealPathFromURI(uri);

            callApi(4, true);


        } else if (resultCode == RESULT_OK && requestCode == PICKFILE_RESULT_CODE) {
          //  selectedImagePath = data.getData().getPath();

             try {
                 Uri selectedImageUri = data.getData();
                 selectedImagePath = getRealPathFromURI_API19(OneToOneChatActivity.this, selectedImageUri);
                 if (selectedImagePath == null) {
                     Utils.showToastMessage(OneToOneChatActivity.this, "Please select file from file manager", false);
                 } else {
                     Utils.showProgress(OneToOneChatActivity.this);
                     String extension = selectedImagePath.substring(selectedImagePath.lastIndexOf("."));
                     type = "5";

                     Utils.print("=================== VALUE >>> " + extension + ":::" + selectedImagePath);
                     if (extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".jpeg") || extension.equalsIgnoreCase(".png"))
                         type = "5";
                     else if (extension.equalsIgnoreCase(".mp4") || extension.equalsIgnoreCase(".AVI") || extension.equalsIgnoreCase(".mov") || extension.equalsIgnoreCase(".WMV") || extension.equalsIgnoreCase(".FLV"))
                         type = "5";
                     callApi(4,true);
                 }
                 Log.e("FILE==>", getRealPathFromURI_API19(OneToOneChatActivity.this, selectedImageUri) + "");
             } catch (Exception e) {
                 Utils.showToastMessage(OneToOneChatActivity.this, "Please select file from file manager", false);
                 e.printStackTrace();
             }


        }

    }
    public static String getRealPathFromURI_API19(Context context, Uri uri)
    {
        String filePath = "";

        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            if ("primary".equalsIgnoreCase(type)) {
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else {
                filePath = "/storage/" + type + "/" + split[1];
                return filePath;
            }

        } else if (isDownloadsDocument(uri)) {
            // DownloadsProvider
            final String id = DocumentsContract.getDocumentId(uri);
            final Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

            Cursor cursor = null;
            final String column = "_data";
            final String[] projection = {column};

            try {
                cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int index = cursor.getColumnIndexOrThrow(column);
                    String result = cursor.getString(index);
                    cursor.close();
                    return result;
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        } else if (DocumentsContract.isDocumentUri(context, uri)) {
            // MediaProvider
            String wholeID = DocumentsContract.getDocumentId(uri);

            // Split at colon, use second item in the array
            String[] ids = wholeID.split(":");
            String id;
            String type;
            if (ids.length > 1) {
                id = ids[1];
                type = ids[0];
            } else {
                id = ids[0];
                type = ids[0];
            }

            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }

            final String selection = "_id=?";
            final String[] selectionArgs = new String[]{id};
            final String column = "_data";
            final String[] projection = {column};
            Cursor cursor = context.getContentResolver().query(contentUri,
                    projection, selection, selectionArgs, null);

            if (cursor != null) {
                int columnIndex = cursor.getColumnIndex(column);

                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
            return filePath;
        } else {
            String[] proj = {MediaStore.Audio.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                if (cursor.moveToFirst())
                    filePath = cursor.getString(column_index);
                cursor.close();
            }
            return filePath;
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
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

    public String getRealPathFromAudioURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
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


                        if (chatListModel.is_group.equalsIgnoreCase("1") && jsonObject.optString("to_id").equalsIgnoreCase(chatListModel.user_id)|| ((jsonObject.optString("user_id").equalsIgnoreCase(Pref.getStringValue(OneToOneChatActivity.this, Const.PREF_USERID, "")) && jsonObject.optString("to_id").equalsIgnoreCase(String.valueOf(chatListModel.user_id))) ||
                                (jsonObject.optString("user_id").equalsIgnoreCase(chatListModel.user_id) && jsonObject.optString("to_id").equalsIgnoreCase(Pref.getStringValue(OneToOneChatActivity.this, Const.PREF_USERID, ""))))) {

                            ChatDetailListModel chatDetailListModel = new ChatDetailListModel();
                            chatDetailListModel.user_id = jsonObject.optString("user_id");
                            chatDetailListModel.to_id = jsonObject.optString("to_id");
                            chatDetailListModel.type = jsonObject.optString("type");
                            chatDetailListModel.message = jsonObject.optString("message");
                            chatDetailListModel.fullname = jsonObject.optString("uname");
                            chatDetailListModel.media_url = jsonObject.optString("media_url");
                            chatDetailListModel.thumb_url = jsonObject.optString("thumb_url");
                            chatDetailListModel.created = Utils.millisToDate(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
                            chatDetailListAdapter.objList.add(chatDetailListModel);
                            chatDetailListAdapter.notifyDataSetChanged();
                            lstChatList.smoothScrollToPosition(chatDetailListAdapter.objList.size());
                            isCallMessageapi = false;

                            if (chatDetailListModel.to_id.equalsIgnoreCase(chatDetailListModel.user_id)) {
                                MainApplication.getInstance().getSoketMangerObject().ReadMessage(jsonObject.optString("user_id"));
                            }

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();

                    }

                } else if (tag == SocketIOManager.CHAT_RESPONSE.USER_ONLINE) {
                    //Tag for if user Online
                    String json = new Gson().toJson(obj);
                    try {
                        JSONObject jsonObject = new JSONObject((String) new JSONArray(json).get(0));

                        if (chatListModel.user_id.equalsIgnoreCase(jsonObject.optString("user_id"))) {
                            txtOnline.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }  else if (tag == SocketIOManager.CHAT_RESPONSE.USER_OFFLINE) {//Tag for if user Offline
                    String json = new Gson().toJson(obj);

                    try {

                        JSONObject jsonObject = new JSONObject((String) new JSONArray(json).get(0));
                        if (chatListModel.user_id.equalsIgnoreCase(jsonObject.optString("user_id"))) {
                            txtOnline.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } else if (tag == SocketIOManager.CHAT_RESPONSE.USER_READ_MESSAGE) {//Tag for if user read msg
                    String json = new Gson().toJson(obj);
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject((String) new JSONArray(json).get(0));
                        if (chatListModel.user_id.equalsIgnoreCase(jsonObject.optString("user_id")) && Pref.getStringValue(OneToOneChatActivity.this, Const.PREF_USERID, "").equalsIgnoreCase(jsonObject.optString("to_id"))) {

                            for (int i = 0; i < chatDetailListAdapter.objList.size(); i++) {
                                chatDetailListAdapter.objList.get(i).is_read = 1;
                                chatDetailListAdapter.notifyDataSetChanged();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



                }
            }
        });


    }

    //Socket Io onf Online here
    @Override
    protected void onResume() {
        super.onResume();

        if (!isOnActityResult) {
            MainApplication.getInstance().socketOnline();
            MainApplication.getInstance().getSoketMangerObject().setChatCallBack(OneToOneChatActivity.this);
        }


        isOnActityResult = false;
    }


    //Socket Io onf Destroy here

  /*  @Override
    protected void onDestroy() {
        super.onDestroy();

        MainApplication.getInstance().socketOffline();
    }
*/

}
