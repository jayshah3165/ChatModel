package com.echo.allscenarioapp.backend;


import com.echo.allscenarioapp.utils.Const;

public interface ResponseListener {
    // API ResponseModel Listener
    public void onResponse(String tag, Const.API_RESULT result, Object obj);

}
