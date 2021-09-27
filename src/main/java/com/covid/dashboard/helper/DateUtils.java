package com.covid.dashboard.helper;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class DateUtils {

    private DateUtils(){}

    public static Date getDateFormatted(String date , String format) {
        Date formatedDate =null;
        try {
            formatedDate=new SimpleDateFormat(format).parse(date);
        }catch (Exception e){
            log.error("Unable to parse the date.",e);
        }
        return formatedDate;
    }

    public static String getDateFormatted(Date date , String format) {
        String formatedDate =null;
        try {
            formatedDate=new SimpleDateFormat(format).format(date);
        }catch (Exception e){
            log.error("Unable to parse the date.",e);
        }
        return formatedDate;
    }
}
