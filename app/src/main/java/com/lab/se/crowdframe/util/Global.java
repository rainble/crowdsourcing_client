package com.lab.se.crowdframe.util;

import java.text.SimpleDateFormat;
import java.sql.Timestamp;

/**
 * Created by lwh on 2017/6/5.
 */

public class Global {
    public static int localVersion = 0;
    public static int serverVersion = 0;
    public static String downloadUrl = "http://118.178.94.215:8080/MobiCrowdsourcing/download/CrowdFrame.apk";
    public static String downloadDir = "CrowdFrame/download/";

    public static String getTime(Timestamp time){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = format.format(time);
        return timeStr;
    }
}
