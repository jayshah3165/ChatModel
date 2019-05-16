package com.echo.allscenarioapp.backend;

import android.widget.DatePicker;


public interface DateSetListener {
    // API Response Listener
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth);

}