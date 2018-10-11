package com.lab.se.crowdframe.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;

import java.util.Locale;

/**
 * Created by lenovo2013 on 2017/6/7.
 */

public class LocaleUtils {

    private static final String LOCALE_KEY = "language";

    public static Locale getUserLocale(Context pContext) {
        SharedPreferences sp = pContext.getSharedPreferences("crowdframe", 0);
        String s = sp.getString(LOCALE_KEY, "");
        return stringToLocale(s);
    }


    public static Locale getCurrentLocale(Context pContext) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //7.0有多语言设置获取顶部的语言
            locale = pContext.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = pContext.getResources().getConfiguration().locale;
        }
        return locale;
    }

    public static void saveUserLocale(Context pContext, Locale pUserLocale) {
        SharedPreferences _SpLocal=pContext.getSharedPreferences("crowdframe", 0);
        SharedPreferences.Editor _Edit=_SpLocal.edit();
        String s = localeToString(pUserLocale);
        _Edit.putString(LOCALE_KEY, s);
        _Edit.apply();
    }

    private static String localeToString(Locale locale) {
       if(locale == Locale.CHINESE){
           return "chinese";
       } else {
           return "english";
       }
    }

    private static Locale stringToLocale(String localeString) {
       if("".equals(localeString) || "chinese".equals(localeString)){
           return Locale.CHINESE;
       } else {
           return Locale.ENGLISH;
       }
    }

    public static void updateLocale(Context pContext, Locale pNewUserLocale) {
        if (needUpdateLocale(pContext, pNewUserLocale)) {
            Configuration configuration = pContext.getResources().getConfiguration();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLocale(pNewUserLocale);
            } else {
                configuration.locale =pNewUserLocale;
            }
            DisplayMetrics _DisplayMetrics = pContext.getResources().getDisplayMetrics();
            pContext.getResources().updateConfiguration(configuration, _DisplayMetrics);
            saveUserLocale(pContext, pNewUserLocale);
        }
    }

    public static boolean needUpdateLocale(Context pContext, Locale pNewUserLocale) {
        return pNewUserLocale != null && !getCurrentLocale(pContext).equals(pNewUserLocale);
    }
}
