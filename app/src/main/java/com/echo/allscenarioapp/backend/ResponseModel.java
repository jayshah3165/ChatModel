package com.echo.allscenarioapp.backend;

import java.util.ArrayList;


public class ResponseModel<T> {

    public int success;
    public String message;

    public String is_online;
    public String group_owner_id;
    public String conversation_id;

    public String desc;
    public ArrayList<T> result;



}
