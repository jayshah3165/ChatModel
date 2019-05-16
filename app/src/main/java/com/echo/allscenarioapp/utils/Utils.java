package com.echo.allscenarioapp.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.custom.CustomDialog;
import com.echo.allscenarioapp.custom.TouchImageView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Utils {

    public static boolean DO_SOP = true;

    //For Retrofir use [Api Call]
    public static HttpLoggingInterceptor getlogging() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return logging;
    }
    public static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .readTimeout(5, TimeUnit.MINUTES)
            .connectTimeout(5, TimeUnit.MINUTES)
            .addInterceptor(getlogging())
            .build();
    public static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Const.API_HOST)
            .addConverterFactory(GsonConverterFactory.create()).client(okHttpClient)
            .build();


    //For Print HashKey
    public static void printHashKey(Context ctx) {


        try {
            PackageInfo info = ctx.getPackageManager().getPackageInfo(
                    "com.echo.allscenarioapp", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("Utils :: KEY HASH",
                        Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Utils :: ", "" + e);
        } catch (NoSuchAlgorithmException e) {
            Log.e("Utils :: ", "" + e);
        }

    }
    public static boolean isEmptyText(View view) {
        if (view == null)
            return true;
        else
            return getTextValue(view).equalsIgnoreCase("");
    }

    //For Print value or else into Logcat
    public static void print(String mesg) {
        if (Utils.DO_SOP) {

            if (mesg.length() > 1000) {
                int maxLogSize = 1000;
                for (int i = 0; i <= mesg.length() / maxLogSize; i++) {
                    int start = i * maxLogSize;
                    int end = (i + 1) * maxLogSize;
                    end = end > mesg.length() ? mesg.length() : end;
                    Log.v("Print ::", mesg.substring(start, end));
                }
            } else {
                Log.i("Print ::", mesg);
            }
        }
    }

    public static String saveImage(Bitmap newBitmap, Context context) {

        String savePath = "";
        File imageFile = null;
//        name =name.isEmpty()? "tempFile":name;

//        Utils.print("=========newBitmap >> " + newBitmap);
        try {
//            imageFile =File.createTempFile(name, ".JPG", Environment.getExternalStorageDirectory());
            imageFile = File.createTempFile("tempFile", ".JPG", context.getExternalCacheDir());
        } catch (IOException e) {
            e.printStackTrace();
            imageFile = new File("/mnt/sdcard/tempFitFile.jpg");
        }
        if (imageFile.exists())
            imageFile.delete();

        savePath = imageFile.getAbsolutePath();
        Utils.print("===========file >>> " + savePath + ">>>" + imageFile.getAbsolutePath());
        try {
            FileOutputStream out = new FileOutputStream(imageFile);
            newBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return savePath;
    }
    //For Logout Clear All the preference
    public static void logOut(Context context) {
        Pref.removeValue(context, Const.PREF_USERID);
        Pref.removeValue(context, Const.PREF_LANGUAGE);
        Pref.removeValue(context, Const.PREF_GCMTOKEN);
        Pref.removeValue(context, Const.PREF_USER_PROFILE_PIC);
        Pref.removeValue(context, Const.PREF_FULLNAME);

    }


    public static void print(Exception e) {
        if (Utils.DO_SOP) {
            e.printStackTrace();
        }
    }

    /*Check Network Status  : Connect with internet or not
     * if isOnline function return true means connect with internet well
     * */
    public static boolean isOnline(Context context) {
        try {
            ConnectivityManager conMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = conMgr.getActiveNetworkInfo();
            if (info != null && info.isConnected())
                return true;
            else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /* Find index of Sting from String Array*/
    public static int indexOfStringArray(String[] strArray, String strFind) {
        int index;

        for (index = 0; index < strArray.length; index++)
            if (strArray[index].equals(strFind))
                break;

        return index >= strArray.length ? 0 : index;
    }


    /* Convert millisecond to Data  */
    public static String millisToDate(long millis, String format) {
        return new SimpleDateFormat(format, Locale.US).format(new Date(millis));
    }
    public static long getStringToMillsTime(String stringTime) {
        if (stringTime.trim().isEmpty())
            return 0;
        return getStringToMillsTime(stringTime, "yyyy-MM-dd HH:mm:ss", true);
    }


    public static String getAbbreviatedTimeSpan(long neededTimeMilis) {
        Calendar nowTime = Calendar.getInstance();
        Calendar neededTime = Calendar.getInstance();
        neededTime.setTimeInMillis(neededTimeMilis);

        if ((neededTime.get(Calendar.YEAR) == nowTime.get(Calendar.YEAR))) {

            if ((neededTime.get(Calendar.MONTH) == nowTime.get(Calendar.MONTH))) {

                if (neededTime.get(Calendar.DATE) - nowTime.get(Calendar.DATE) == 1) {
                    //here return like "Tomorrow at 12:00"
                    return "Tomorrow  " ;//+new SimpleDateFormat("dd MMM,yyy").format(new Date(neededTimeMilis));

                } else if (nowTime.get(Calendar.DATE) == neededTime.get(Calendar.DATE)) {
                    //here return like "Today at 12:00"
                    return "Today  " ;//+ DateFormat.format("dd MMM,yyy", neededTime);

                } else if (nowTime.get(Calendar.DATE) - neededTime.get(Calendar.DATE) == 1) {
                    //here return like "Yesterday at 12:00"
                    return "Yesterday  ";// + DateFormat.format("dd MMM,yyy", neededTime);

                } else {
                    //here return like "May 31, 12:00"
                    return new SimpleDateFormat("MMM dd,yyy").format(new Date(neededTimeMilis));
                }

            } else {
                //here return like "May 31, 12:00"
                return  new SimpleDateFormat("MMM dd,yyy").format(new Date(neededTimeMilis));
            }

        } else {
            //here return like "May 31 2010, 12:00" - it's a different year we need to show it
            return new SimpleDateFormat("MMM dd,yyy").format(new Date(neededTimeMilis));
        }
    }
    //Get Date Like [second,minuts,hours,Days]
    public static String getAbbreviatedTimeSpanForReview(long timeMillis) {
        long span = Math.max(System.currentTimeMillis() - timeMillis, 0);
        if (span >= DateUtils.YEAR_IN_MILLIS) {
            return (span / DateUtils.YEAR_IN_MILLIS) <= 1 ? "Last year" : ((span / DateUtils.YEAR_IN_MILLIS) + " years ago");
//            return timeMillisToDate(timeMillis, "MM/dd/yyyy");
        }
        if (span >= DateUtils.WEEK_IN_MILLIS) {
            return (span / DateUtils.WEEK_IN_MILLIS) <= 1 ? "Last week" : ((span / DateUtils.WEEK_IN_MILLIS) + " weeks ago");
        }
        if (span >= DateUtils.DAY_IN_MILLIS) {
//            return ((span / DateUtils.DAY_IN_MILLIS) + " Day Ago");
            return (span / DateUtils.DAY_IN_MILLIS) <= 1 ? "Yesterday" : ((span / DateUtils.DAY_IN_MILLIS) + " days ago");
        }
       /* if (span >= DateUtils.DAY_IN_MILLIS) {
            return (span / DateUtils.DAY_IN_MILLIS) > 3 ? timeMillisToDate(timeMillis, "MM/dd/yyyy") : ((span / DateUtils.DAY_IN_MILLIS) <= 1 ? (span / DateUtils.DAY_IN_MILLIS) + " days ago" : (span / DateUtils.DAY_IN_MILLIS) + " day ago");
        }*/
        if (span >= DateUtils.HOUR_IN_MILLIS) {
            return (span / DateUtils.HOUR_IN_MILLIS) <= 1 ? "Last hour" : ((span / DateUtils.HOUR_IN_MILLIS) + " hours ago");
        }
        if (span >= DateUtils.MINUTE_IN_MILLIS) {
            return (span / DateUtils.MINUTE_IN_MILLIS) + ((span / DateUtils.MINUTE_IN_MILLIS) <= 1 ? " Minute ago" : " minutes ago");
        }
        return (span / DateUtils.SECOND_IN_MILLIS) == 0 ? "Just now" : (span / DateUtils.SECOND_IN_MILLIS) + " seconds ago";
    }

    //Convert string to Date
    public static Date convertStringToDate(String strDate, String parseFormat) {
        Utils.print("parseeeeeeeeee--------------------------"+parseFormat+":::"+strDate);

        try {
            if (!strDate.isEmpty())
                return new SimpleDateFormat(parseFormat, Locale.US).parse(strDate);
            else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Convert date string to string
    public static String convertDateStringToString(String strDate,String currentFormat, String parseFormat) {
        try {
            if (strDate != null && !strDate.isEmpty())
                return convertDateToString( convertStringToDate(strDate, currentFormat), parseFormat);
            else
                return "";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //COnvert date to string
    public static String convertDateToString(Date objDate, String parseFormat) {
        try {

            return new SimpleDateFormat(parseFormat, Locale.US).format(objDate);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    //FIrebase Notification
    public static void setToken(Context context) {
        FirebaseApp.initializeApp(context);
        String token = FirebaseInstanceId.getInstance().getToken() + "";
        Utils.print("Toekn==============" + token);
        if (token != null && !token.equalsIgnoreCase("null") && !token.isEmpty())
            Pref.setStringValue(context, Const.PREF_GCMTOKEN, token);
    }



    /* Use This Function to maintain stack of Open Activity*/

    public static void addActivities(String key, Activity _activity) {
        if (Const.screen_al == null)
            Const.screen_al = new HashMap<String, Activity>();
        if (_activity != null)
            Const.screen_al.put(key, _activity);
    }

    /* Close All Open activity from back stack */
    public static void closeAllScreens() {
        closeAllScreens("");
    }


    /* Close Specific Open activity from back stack */
    public static void closeAllScreens(String key) {

        if (Const.screen_al == null || Const.screen_al.size() <= 0)
            return;
        if (key != null && !key.equalsIgnoreCase("")) {
            if (Const.screen_al.get(key) != null)
                Const.screen_al.get(key).finish();
        } else {
            for (Iterator<Map.Entry<String, Activity>> it = Const.screen_al
                    .entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Activity> entry = it.next();

                if (entry.getValue() != null) {
                    entry.getValue().finish();
                    it.remove();
                }

            }
        }
    }

    /* Use this function to make two digit of date or time */

    public static String pad(int c) {
        return c >= 10 ? String.valueOf(c) : "0" + String.valueOf(c);
    }

    public static String removePad(int c) {
        return c >= 10 ? String.valueOf(c) : (String.valueOf(c).length() > 1 ? String.valueOf(c).substring(1) : String.valueOf(c));
    }



    /* This Function User for apply Font in app*/

    public static void applyFontFace(Context context, final View view) {
        try {
            if (view instanceof ViewGroup) {

                ViewGroup vg = (ViewGroup) view;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    try {
                        applyFontFace(context, child);
                    } catch (NullPointerException e) {
                        continue;
                    }

                }
            } else if (view instanceof TextInputLayout) {
                if (view.getTag() == null)
                    return;

                if (Integer.parseInt(view.getTag().toString()) == 100)
                    ((TextInputLayout) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_light.ttf"));
                else if (Integer.parseInt(view.getTag().toString()) == 200)
                    ((TextInputLayout) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_reguler.ttf"));
                else if (Integer.parseInt(view.getTag().toString()) == 300)
                    ((TextInputLayout) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_bold.ttf"));

            } else if (view instanceof TextInputEditText) {
                if (view.getTag() == null)
                    return;

                if (Integer.parseInt(view.getTag().toString()) == 100)
                    ((TextInputEditText) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_light.ttf"));
                else if (Integer.parseInt(view.getTag().toString()) == 200)
                    ((TextInputEditText) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_reguler.ttf"));
                else if (Integer.parseInt(view.getTag().toString()) == 300)
                    ((TextInputEditText) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_bold.ttf"));


            } else if (view instanceof RadioButton) {
                if (view.getTag() == null)
                    return;

                if (Integer.parseInt(view.getTag().toString()) == 100)
                    ((RadioButton) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_light.ttf"));
                else if (Integer.parseInt(view.getTag().toString()) == 200)
                    ((RadioButton) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_reguler.ttf"));
                else if (Integer.parseInt(view.getTag().toString()) == 300)
                    ((RadioButton) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_bold.ttf"));

            } else if (view instanceof CheckBox) {
                if (view.getTag() == null)
                    return;

                if (Integer.parseInt(view.getTag().toString()) == 100)
                    ((CheckBox) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_light.ttf"));
                else if (Integer.parseInt(view.getTag().toString()) == 200)
                    ((CheckBox) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_reguler.ttf"));
                else if (Integer.parseInt(view.getTag().toString()) == 300)
                    ((CheckBox) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_bold.ttf"));


            } else if (view instanceof Button) {
                if (view.getTag() == null)
                    return;
                if (Integer.parseInt(view.getTag().toString()) == 100)
                    ((Button) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_light.ttf"));
                else if (Integer.parseInt(view.getTag().toString()) == 200)
                    ((Button) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_reguler.ttf"));
                else if (Integer.parseInt(view.getTag().toString()) == 300)
                    ((Button) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_bold.ttf"));


            } else if (view instanceof EditText) {
                if (view.getTag() == null)
                    return;
                if (Integer.parseInt(view.getTag().toString()) == 100)
                    ((EditText) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_light.ttf"));
                else if (Integer.parseInt(view.getTag().toString()) == 200)
                    ((EditText) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_reguler.ttf"));
                else if (Integer.parseInt(view.getTag().toString()) == 300)
                    ((EditText) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_bold.ttf"));

            } else if (view instanceof TextView) {
                if (view.getTag() == null)
                    return;

                if (view.getTag().equals("GoogleCopyrights"))
                    return;
                if (Integer.parseInt(view.getTag().toString()) == 100)
                    ((TextView) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_light.ttf"));
                else if (Integer.parseInt(view.getTag().toString()) == 200)
                    ((TextView) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_reguler.ttf"));
                else if (Integer.parseInt(view.getTag().toString()) == 300)
                    ((TextView) view).setTypeface(Typeface.createFromAsset(context.getAssets(), "aller_bold.ttf"));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //For Show Progress bar
    public static void showProgress(Context context) {
        try {
            if (Const.custDailog != null && Const.custDailog.isShowing())
                Const.custDailog.dismiss();

            if (Const.custDailog == null)
                Const.custDailog = new CustomDialog(context);
            Const.custDailog.setCancelable(false);
            Const.custDailog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //dismiss progress bar
    public static void dismissProgress() {
        if (Const.custDailog != null && Const.custDailog.isShowing())
            Const.custDailog.dismiss();
        Const.custDailog = null;
    }

    //FOr Hide keyboard
    public static void hideKeyBoard(Context context, View view) {
        try {
            if (context == null)
                return;
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (view != null) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //For SHow Keyboard
    public static void showKeyBoard(Context context) {
        try {
            if (context == null)
                return;
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //For SHow Toast Msg
    public static void showToastMessage(Context context, String mgs, boolean isShort) {
        Toast.makeText(context, mgs, isShort ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
    }




    //Get Value From Textview or Edittext
    public static String getTextValue(View view) {
        if (view instanceof EditText)
            return ((EditText) view).getText().toString().trim();
        else if (view instanceof TextView)
            return ((TextView) view).getText().toString().trim();
        else
            return "";

    }







    //AmazoneS3bucket code
    private static AmazonS3Client sS3Client;
    private static CognitoCachingCredentialsProvider sCredProvider;
    private static TransferUtility sTransferUtility;


    private static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        if (sCredProvider == null) {
            sCredProvider = new CognitoCachingCredentialsProvider(
                    context.getApplicationContext(),
                    Const.COGNITO_POOL_ID,
                    Regions.fromName(Const.COGNITO_POOL_REGION));
        }

        return sCredProvider;
    }

    public static AmazonS3Client getS3Client(Context context) {
        if (sS3Client == null) {
            sS3Client = new AmazonS3Client(getCredProvider(context.getApplicationContext()));
            sS3Client.setRegion(Region.getRegion(Regions.fromName(Const.BUCKET_REGION)));
        }
        return sS3Client;
    }

    public static TransferUtility getTransferUtility(Context context) {
        if (sTransferUtility == null) {
            sTransferUtility = new TransferUtility(getS3Client(context.getApplicationContext()),
                    context.getApplicationContext());
        }

        return sTransferUtility;
    }





    //Check All Permission When APp Open
    public static final int PERMISSION_REQUEST_CODE = 1;

    public static boolean checkpermission(Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return true;
            } else {
                return false;
            }
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            return true;
        }
    }


    public static boolean checkAllpermission(Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
                return true;
            } else {
                return false;
            }
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS,
                    Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
            return false;
        }
    }

    public static boolean checkPermission(Activity activity) {
        int resultWrite = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT >= 23) {
            if (resultWrite == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }


    public static void requestPermission(Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(activity, "Please allow write external Storage", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }












    public static Activity scanForActivity(Context cont) {
        if (cont == null)
            return null;
        else if (cont instanceof Activity)
            return (Activity) cont;
        else if (cont instanceof ContextWrapper)
            return scanForActivity(((ContextWrapper) cont).getBaseContext());

        return null;
    }





    //Set Time string to Mills
    public static long getStringToMillsTime(String stringTime, String dateFormat, boolean isUtc) {

        try {
            Log.e("Original Date :", stringTime);
            DateFormat formatter;
            formatter = new SimpleDateFormat(dateFormat, Locale.getDefault());
            if (isUtc)
                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = formatter.parse(stringTime);
            Log.e("Converted Date :", date.toString());
            return date.getTime();
        } catch (ParseException e) {
            System.out.println("Exception :" + e);
            return 0;
        }
    }

    //Show IMage in Preview
    public static Dialog setDialogImage(Context context, String path) {
        final Dialog dialogImage = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialogImage.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogImage.setContentView(R.layout.dailog_image);
        dialogImage.show();

        TouchImageView imgProfilePic1 = (TouchImageView) dialogImage.findViewById(R.id.imgProfilePic1);


        ImageView imgCancel = (ImageView) dialogImage.findViewById(R.id.imgCancel);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.avatar);
        requestOptions.error(R.drawable.avatar);
        if (path != null && !path.isEmpty()) {
            if (path.startsWith("http"))
                Glide.with(context).setDefaultRequestOptions(requestOptions).load(path).into(imgProfilePic1);
            else
                Glide.with(context).setDefaultRequestOptions(requestOptions).load(Uri.parse(new File(path).toString())).into(imgProfilePic1);

        }



        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogImage.dismiss();
            }
        });
        return dialogImage;
    }

    public static float sp2px(Resources resources, float sp) {
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }


    public static String getDurationMark(String filePath, MediaMetadataRetriever retriever) {
        try {
            retriever.setDataSource(filePath);
        } catch (Exception e) {
            Log.e("getDurationMark", e.toString());
            return "?:??";
        }
        String time = null;

        //fix for the gallery picker crash
        // if it couldn't detect the media file
        try {
            time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        } catch (Exception ex) {
            Log.e("getDurationMark", ex.toString());
        }

        //fix for the gallery picker crash
        // if it couldn't extractMetadata() of a media file
        //time was null
        time = time == null ? "0" : time.isEmpty() ? "0" : time;
        //bam crash - no more :)
        int timeInMillis = Integer.parseInt(time);
        int duration = timeInMillis / 1000;
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append(":");
        }
        if (minutes < 10) {
            sb.append("0").append(minutes);
        } else {
            sb.append(minutes);
        }
        sb.append(":");
        if (seconds < 10) {
            sb.append("0").append(seconds);
        } else {
            sb.append(seconds);
        }
        return sb.toString();
    }

    public static void showLog(String tag, String msg) {
        Log.e(tag, msg);
    }

}
