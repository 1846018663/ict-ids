package com.hnu.ict.ids.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static Date millisecondToDate(String time){
        Date date = new Date();
        date.setTime(Long.valueOf(time));
        return date;
    }


    public static Date millisecondToDate(Long time){
        Date date = new Date();
        date.setTime(time);
        return date;
    }



}
