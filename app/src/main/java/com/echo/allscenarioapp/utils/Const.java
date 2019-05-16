package com.echo.allscenarioapp.utils;

import android.app.Activity;
import android.os.Environment;

import com.echo.allscenarioapp.custom.CustomDialog;

import java.util.HashMap;


public class Const
{
    public static String APP_NAME = "AllScenarioApp";
    public static final String PREF_FILE = APP_NAME + "_PREF";

    public static String APP_HOME = Environment.getExternalStorageDirectory().getPath() + "/" + APP_NAME;

    // Api ResponseModel
    public static enum API_RESULT
    {
        SUCCESS, FAIL
    }


    // API CALL
    public static String API_BASE_URL = "http://3.212.218.91";
    public static String API_HOST = API_BASE_URL + "/api/";
  //  http://3.212.218.91:2021/

    public static final String CHAT_SERVER_URL = API_BASE_URL + ":2021/";

    public static CustomDialog custDailog = null;
    public static HashMap<String, Activity> screen_al;

    public static String DEVICE_TYPE = "1";//1=android 2=ios


    public static final int INTENT100 = 100;
    public static final int INTENT200 = 200;
    public static final int INTENT300 = 300;
    public static final int INTENT400 = 400;
    public static final int INTENT500 = 500;
    public static final int INTENT600 = 600;
    public static final int INTENT700 = 700;
    public static final int INTENT_CAMERA = 403;
    public static final int INTENT_GALLERY = 503;
    public static final int MAP_ZOOM_LEVEL = 18;
    public static final int LOG_OUT_RESPONSE_CODE = 404;



    //Bucket Credintioal
    public static final String COGNITO_POOL_ID = "us-east-1:08b1f93a-e430-4105-a5fb-f3516faf5597";
    public static final String COGNITO_POOL_REGION = "us-east-1";
    public static final String BUCKET_NAME = "echomodel";
    public static final String BUCKET_FOLDER_NAME = "EchoModel";
    public static final String BUCKET_REGION = "us-east-1";


    // PREF
    public static String PREF_LANGUAGE = "PREF_LANGUAGE";
    public static String PREF_GCMTOKEN = "PREF_GCMTOKEN";


    public static String PREF_USERID = "PREF_USERID";
    public static String PREF_USER_PROFILE_PIC = "PREF_USER_PROFILE_PIC";
    public static String PREF_USER_EMAIL = "PREF_USER_EMAIL";
    public static String PREF_FULLNAME = "PREF_FULLNAME";
    public static String PREF_FIRSTNAME = "PREF_FIRSTNAME";
    public static String PREF_LAST_NAME = "PREF_LAST_NAME";
    public static String PREF_PHONE = "PREF_PHONE";

    public static String CURRENT_LATITUDE = "CURRENT_LATITUDE";
    public static String CURRENT_LONGITUDE = "CURRENT_LONGITUDE";
    public static String PUSH_NOTIFICATION = "PUSH_NOTIFICATION";



    public static String PREF_SOCIAL_ID = "PREF_SOCIAL_ID";
    public static String PREF_SOCIAL_TYPE = "PREF_SOCIAL_TYPE";
    public static String PREF_SOCIAL_TOKEN = "PREF_SOCIAL_TOKEN";
    public static String PREF_USER_PASSWORD = "PREF_USER_PASSWORD";
    public static String EN = "en";

    //All API Name Declare here
    public static final String UPLOAD_API = "UPLOAD_API";
    public static final String SOCIAL_LOGIN_API = "social_login";
    public static final String GET_CHAT_LIST_API = "get_chats";
    public static final String GET_FRIENDS_LIST_API = "get_users";
    public static final String SEND_MESSAGE_API = "send_message";
    public static final String GET_MSG_LIST_API = "getchatdetail";
    public static final String DELETE_CONVERSATION_API = "clearmessage";
    public static final String READ_ALL_API = "readall";
    public static final String ONLINE_CHAT_API = "onlineuser";
    public static final String CREATE_GROUP_API = "creategroup";
    public static final String LEFT_GROUP_API = "leftgroup";
    public static final String DELETE_GROUP_API = "deletegroup";
    public static final String REMOVE_CONVERSATION_API = "removeconversation";
    public static final String EDIT_GROUP_API = "editgroup";
    public static final String GET_GROUP_MEMBER_LIST_API = "get_group_members";
    public static final String GET_USER_LIST_API = "userlistgroup";
    public static final String ADD_GROUP_MEMBER_API = "add_member_in_group";




    public static final String GET_FEED_LIST_API = "getpost";
    public static final String GET_COMMENT_LIST_API = "getcommentlist";
    public static final String LIKE_API = "like";
    public static final String GET_LIKE_LIST_API = "getlikelist";
    public static final String DELETE_POST_API = "deletepost";
    public static final String DELETE_COMMENT_API = "deletecomment";
    public static final String ADD_COMMENT_API = "comment";
    public static final String CREATE_POST_API = "createpost";
    public static final String REMOVE_MEMBER_FROM_GROUP_API = "remove_member_from_group";


    }