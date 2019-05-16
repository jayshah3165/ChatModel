package com.echo.allscenarioapp.api;

import android.app.Activity;
import android.content.Context;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.echo.allscenarioapp.backend.FileUtils;
import com.echo.allscenarioapp.backend.ResponseListener;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Utils;

import java.io.File;


public class UploadImageS3API {


    public final static String API = Const.UPLOAD_API;
    private ResponseListener responseListener;
    private Context context;
    TransferUtility transferUtility;
    public String url = "";

    public UploadImageS3API(Context context, ResponseListener _responseListener) {
        this.context = context;
        responseListener = _responseListener;
        transferUtility = Utils.getTransferUtility(context);
    }

    public void execute(String filePath, String message_type) {

        long time = System.currentTimeMillis();
        String fileName = "";
        // (1=text message 2=video 3=audio 4=image)
        if (message_type.equalsIgnoreCase("3")) {//Audio
            fileName = Const.BUCKET_FOLDER_NAME + "/android_" + time + ".mp3";
        } else if (message_type.equalsIgnoreCase("5")) {//Gif

            fileName = Const.BUCKET_FOLDER_NAME+"/android_" + time + FileUtils.getExtension(filePath);
        } else if (message_type.equalsIgnoreCase("2")) {//Video

            fileName = Const.BUCKET_FOLDER_NAME + "/android_" + time + ".mp4";
        } else if (message_type.equalsIgnoreCase("4")) {//Image
            fileName = Const.BUCKET_FOLDER_NAME + "/android_" + time + ".jpg";

        }
            Utils.print("messege type------------------------"+message_type+"::::"+fileName);

        if (filePath == null || !new File(filePath).exists()) {
            Utils.showToastMessage(context, "Could not find the filepath of the selected file", true);
            doCallBack(-1, "");
            return;
        }


        url = "https://" + Const.BUCKET_NAME + ".s3.us-east-1.amazonaws.com/" + fileName;
        Utils.print("URL==>" + url);
        File file = new File(filePath);
        TransferObserver observer = transferUtility.upload(Const.BUCKET_NAME, fileName, file);
        observer.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                System.out.println(" onStateChanged id >>>> " + id + "" + state.toString());
                if (state.equals(TransferState.COMPLETED)) {
                    doCallBack(1, "Success");
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                System.out.println(" onProgress id >>> " + id + " " + bytesCurrent + "  " + bytesTotal);
            }

            @Override
            public void onError(int id, Exception ex) {
                ex.printStackTrace();
                System.out.println(" onError id >>>> " + id + "");
                doCallBack(-1, ex.toString());
            }
        });

    }


    /*
     * Send control back to the caller which includes
     * Status: Successful or Failure Message: Its an Object, if required
     */
    private void doCallBack(final int code, final String mesg) {
        ((Activity) context).runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (code == 1) {
                    responseListener.onResponse(API, Const.API_RESULT.SUCCESS, null);
                } else if (code >= 0) {
                    Utils.showToastMessage(context, mesg, false);
                    responseListener.onResponse(API, Const.API_RESULT.FAIL, null);
                } else if (code < 0) {
                    responseListener.onResponse(API, Const.API_RESULT.FAIL, null);
                }

            }
        });
    }
}