package com.echo.allscenarioapp.model;

import java.io.Serializable;

/**
 * Created by EchoIT on 10/18/2018.
 */

public class ChatDetailListModel implements Serializable {

    public String id = "";
    public String message = "";
    public String type = "";//1=text message 2=video 3=audio 4=image
    public String fullname = "";
    public String media_url = "";
    public String conversation_id = "";


    public String from_id = "";
    public String to_id = "";
    public String user_id = "";
    public String thumb_url = "";

    public int is_read;
    public String created = "";
    public String name = "";
    public String member_id = "";


    public String image = "";
    public int isHeader;
    public String unread = "";
    public String is_group = "";
    public String is_left = "";


}
