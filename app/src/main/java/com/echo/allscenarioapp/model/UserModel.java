package com.echo.allscenarioapp.model;

import java.io.Serializable;

/**
 * Created by EchoIT on 2/14/2018.
 */

public class UserModel implements Serializable {


    // Login Response
    public String user_id = "";
    public String fullname = "";
    public String email = "";
    public String phone = "";
    public String image = "";
    public String social_id = "";//FacebookID , GoogleID,normal = 0
    public int social_type ;//gmail =  2, fb = 1
    public String social_token = "";//Facebook Token, Google Token,normal=""



}
