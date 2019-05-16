package com.echo.allscenarioapp.model;

import java.io.Serializable;

/**
 * Created by EchoIT on 2/14/2018.
 */

public class FeedListModel implements Serializable {


    public String feed_id = "";
    public String feed_type ="";//  1=Text, 2=Image, 3=Video, 4=Checking 5=Audio
    public String title = "";
    public String user_id = "";
    public String user_image = "";

    public String message = "";
    public String media_url = "";
    public String thumb_url = "";
    public String number_of_likes = "";
    public String number_of_comments = "";
    public String is_like = "";//1= like,2=unlike
    public String fullname = "";
    public String lattitude = "";
    public String longitude = "";
    public String created = "";






}
