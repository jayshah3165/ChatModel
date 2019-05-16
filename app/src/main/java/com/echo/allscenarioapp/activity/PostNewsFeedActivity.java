package com.echo.allscenarioapp.activity;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.adapter.AddMemberListAdapter;
import com.echo.allscenarioapp.api.ClearMassageAPi;
import com.echo.allscenarioapp.api.CreatePostAPI;
import com.echo.allscenarioapp.api.GetChatDetailListApi;
import com.echo.allscenarioapp.api.ReadAllAPi;
import com.echo.allscenarioapp.api.SendMessageApi;
import com.echo.allscenarioapp.api.UploadImageS3API;
import com.echo.allscenarioapp.backend.ResponseListener;
import com.echo.allscenarioapp.custom.CustomHeader;
import com.echo.allscenarioapp.custom.GPSTracker;
import com.echo.allscenarioapp.model.ChatDetailListModel;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Pref;
import com.echo.allscenarioapp.utils.Utils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostNewsFeedActivity extends AppCompatActivity implements View.OnClickListener, ResponseListener {
    @BindView(R.id.header)
    CustomHeader header;
    @BindView(R.id.edtCaptionText)
    EditText edtCaptionText;
    @BindView(R.id.img_upload_preview)
    SimpleDraweeView img_upload_preview;
    @BindView(R.id.imgCancelImage)
    ImageView imgCancelImage;
    @BindView(R.id.imgAudio)
    ImageView imgAudio;
    @BindView(R.id.imgLocation)
    ImageView imgLocation;
    @BindView(R.id.btnPost)
    TextView btnPost;
    Dialog dialog = null;
    private Dialog customDialog = null;
    CreatePostAPI createPostAPI = null;
    UploadImageS3API uploadImageS3API = null;
    String selectedImagePath = "";

    String type = "1"; // UploadImage(2=video 3=audio 4=image)
    String feedType = "1";//feed type  -> 1=Text, 2=Image, 3=Video, 4=Checking 5=Audio
    String mediaURL = "",thumbURL = "";
    private static final int REQUEST_VIDEO_CAPTURE = 400;
    //Current Location

    private GPSTracker gps;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private Geocoder geocoder;
    private List<Address> addresses;
    String address = "";
    Bitmap thumbBmp =null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_news_feed);
        Utils.addActivities("PostNewsFeedActivity", this);

        //For Get Id Of ALl View
        ButterKnife.bind(this);
        //For Applt Font style
        Utils.applyFontFace(this, this.findViewById(android.R.id.content).getRootView());
        checkGps();
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
        header.txtHeaderTitle.setText("Post");
        header.imgLeft.setVisibility(View.VISIBLE);
        header.imgRight.setVisibility(View.VISIBLE);
        header.imgRight.setBackground(getResources().getDrawable(R.drawable.add_white));



    }


    private void setDeletePostDialog() {
        dialog = new Dialog(PostNewsFeedActivity.this, R.style.Theme_DialogDeleteConversation);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_post);
        dialog.setCancelable(true);
        Utils.applyFontFace(PostNewsFeedActivity.this, dialog.findViewById(android.R.id.content).getRootView());
        final TextView btnImage = (TextView) dialog.findViewById(R.id.btnImage);
        final TextView btnVideo = (TextView) dialog.findViewById(R.id.btnVideo);
        final TextView btnAudio = (TextView) dialog.findViewById(R.id.btnAudio);
        final TextView btnCurrentLocation = (TextView) dialog.findViewById(R.id.btnCurrentLocation);
        final TextView btnCancel = (TextView) dialog.findViewById(R.id.btnCancel);

//        type =(2=video 3=audio 4=image)

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "4";
                feedType = "2";
                CropImage.activity(null)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(PostNewsFeedActivity.this);

                dialog.dismiss();
            }
        });

        btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "3";
                feedType = "5";
                try {
                    Intent soundRecorderIntent = new Intent();  // create intent
                    soundRecorderIntent.setAction(MediaStore.Audio.Media.RECORD_SOUND_ACTION);  // set action
                 //   startActivityForResult(soundRecorderIntent, Const.INTENT300); // start activity
                    startActivityForResult(Intent.createChooser(soundRecorderIntent, "Chose browser"),Const.INTENT300);





                } catch (ActivityNotFoundException e) {
                   Utils.showToastMessage(PostNewsFeedActivity.this, "There is no application in your device to record audio",false);
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });


        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thumbBmp =null;
                type = "2";
                feedType = "3";
                mediaURL ="";
                thumbURL ="";
                Intent videoCaptureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (videoCaptureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(videoCaptureIntent, REQUEST_VIDEO_CAPTURE);
                    dialog.dismiss();
                }
            }
        });



        btnCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                  selectedImagePath="";
                    feedType = "4";
                    Utils.print("LAT LONG______-------------------------------"+latitude+":::"+longitude);

                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses!=null && addresses.size()>0) {
                        address = addresses.get(0).getAddressLine(0);
                    }
                    Utils.print("ADDRESSSSSSSSSSSSSSSSSSS_--------------------------"+address);
                    img_upload_preview.setBackgroundColor(getResources().getColor(R.color.gray_color));
                    imgLocation.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
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

    private void checkGps() {

//        Utils.print("====================value >>>> CHECK GPS" + isFromOnActivityResult);
        if (gps == null)
            gps = new GPSTracker(PostNewsFeedActivity.this);
        if (geocoder == null)
             geocoder = new Geocoder(PostNewsFeedActivity.this, Locale.getDefault());


        if (Pref.getStringValue(PostNewsFeedActivity.this, Const.CURRENT_LATITUDE,"").equalsIgnoreCase("0.0") || Pref.getStringValue(PostNewsFeedActivity.this, Const.CURRENT_LONGITUDE,"").equalsIgnoreCase("0.0")) {

            if (gps.canGetLocation()) {


                try {
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    Utils.print("LAT---------------------LNG------------"+latitude+":::"+longitude);
                    Pref.setStringValue(PostNewsFeedActivity.this, Const.CURRENT_LATITUDE, latitude + "");
                    Pref.setStringValue(PostNewsFeedActivity.this, Const.CURRENT_LONGITUDE, longitude + "");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                gps.showSettingsAlert();


            }
        } else {
            try {

                latitude = Double.parseDouble(Pref.getStringValue(PostNewsFeedActivity.this, Const.CURRENT_LATITUDE,"0.0"));
                longitude = Double.parseDouble(Pref.getStringValue(PostNewsFeedActivity.this, Const.CURRENT_LONGITUDE,"0.0"));


            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }





    private void callApi(int tag, boolean isShowProgress) {
        if (Utils.isOnline(this)) {
                   Utils.showProgress(this);
            if (tag == 1) {
                if (createPostAPI == null)
                    createPostAPI = new CreatePostAPI(this, this);
                createPostAPI.execute(feedType,Utils.getTextValue(edtCaptionText),mediaURL,thumbURL,String.valueOf(latitude),String.valueOf(longitude),address);
            }  else if (tag == 2) {
                if (uploadImageS3API == null)
                    Utils.print("file upload path--------------------" + selectedImagePath);
                uploadImageS3API = new UploadImageS3API(this, this);
                uploadImageS3API.execute(selectedImagePath, type);
            }
        } else
            Utils.showToastMessage(this, getResources().getString(R.string.checkInternet), false);

    }

    //(2=video 3=audio 4=image)
    @Override
    public void onResponse(String tag, Const.API_RESULT result, Object obj) {
        Utils.dismissProgress();
        if (tag == Const.CREATE_POST_API && result == Const.API_RESULT.SUCCESS) {
            finish();

        }else if (tag == Const.UPLOAD_API && result == Const.API_RESULT.SUCCESS) {

            if( mediaURL==null || mediaURL.isEmpty())
                  mediaURL = uploadImageS3API.url;
           else if( thumbURL==null || thumbURL.isEmpty())
                thumbURL = uploadImageS3API.url;

            if(type.equalsIgnoreCase("2") &&( thumbURL==null ||  thumbURL.isEmpty()) && thumbBmp!=null){
                type ="4";
                selectedImagePath = Utils.saveImage(thumbBmp, PostNewsFeedActivity.this);
                thumbBmp =null;
                callApi(2,true);
            }else
            {
                Utils.print("MEDI URL THUMB URL ---------------------"+mediaURL+":::"+thumbURL);
                callApi(1, true);
            }






        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                img_upload_preview.setVisibility(View.VISIBLE);
                imgCancelImage.setVisibility(View.VISIBLE);
                img_upload_preview.setImageURI(result.getUri());
                selectedImagePath = getRealPathFromURI(result.getUri());

               // callApi(2, true);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == Const.INTENT300) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                selectedImagePath = getRealPathFromAudioURI(uri);
                img_upload_preview.setVisibility(View.VISIBLE);
                imgCancelImage.setVisibility(View.VISIBLE);
                imgAudio.setVisibility(View.VISIBLE);
                img_upload_preview.setBackgroundColor(getResources().getColor(R.color.gray_color));

             //   callApi(2, true);

            }
        }else if (resultCode == RESULT_OK && requestCode == REQUEST_VIDEO_CAPTURE) {
            Uri uri = data.getData();
            selectedImagePath = getRealPathFromURI(uri);

            img_upload_preview.setVisibility(View.VISIBLE);
            imgCancelImage.setVisibility(View.VISIBLE);
            imgAudio.setVisibility(View.GONE);
            thumbBmp =null;

            thumbBmp = ThumbnailUtils.createVideoThumbnail(selectedImagePath, MediaStore.Video.Thumbnails.MICRO_KIND);
            img_upload_preview.setImageBitmap(thumbBmp);
            Utils.print("THUMBNAIL BITMAP----------------------------------"+thumbBmp+"::"+selectedImagePath);


        }

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
    private boolean isValidate() {
        boolean isError = false;
        if (Utils.isEmptyText(edtCaptionText)) {
            edtCaptionText.requestFocus();
            edtCaptionText.setError(getResources().getString(R.string.strPleaseEnterComment));
            isError = true;
        }
        return isError;
    }

    //Click EVent Handling
    @OnClick({R.id.imgRight, R.id.btnPost, R.id.imgCancelImage})
    public void onClick(View view) {

        switch (view.getId()) {


            case R.id.imgRight:
                setDeletePostDialog();
                break;
            case R.id.btnPost:
                if (!isValidate()) {
                    thumbURL = "";
                    mediaURL = "";
                    if (selectedImagePath!=null && !selectedImagePath.isEmpty()) {
                        callApi(2, true);
                    }else
                        callApi(1, true);

                }
                break;


            case R.id.imgCancelImage:
                feedType = "1";
                selectedImagePath="";
              img_upload_preview.setVisibility(View.GONE);
              imgCancelImage.setVisibility(View.GONE);
              imgAudio.setVisibility(View.GONE);
              imgLocation.setVisibility(View.GONE);
                break;

        }
    }
}
