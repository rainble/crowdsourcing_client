package com.lab.se.crowdframe;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.lab.se.crowdframe.service.LocationService;
import com.lab.se.crowdframe.util.LocaleUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by lenovo2013 on 2017/3/9.
 */

public class CrowdFrameApplication  extends Application {
    int userId;
    String userName;
    double creditPublish;
    double creditWithdraw;
    int userFlag = -1;//0代表是当天第一次登录
    int userTag;//是否是软工实验室的

    @Override
    public void onCreate() {
        super.onCreate();

        JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);     		// 初始化 JPush

        SDKInitializer.initialize(this);
        //启动service
        Intent intent = new Intent(this,LocationService.class);
        this.startService(intent);
        //设置语言
        Locale locale=LocaleUtils.getUserLocale(this);
        LocaleUtils.updateLocale(this, locale);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Locale locale= LocaleUtils.getUserLocale(this);
        //系统语言改变了应用保持之前设置的语言
        if (locale != null) {
            Locale.setDefault(locale);
            Configuration configuration = new Configuration(newConfig);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLocale(locale);
            } else {
                configuration.locale = locale;
            }
            getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getCreditPublish() {
        return creditPublish;
    }

    public void setCreditPublish(double creditPublish) {
        this.creditPublish = creditPublish;
    }

    public double getCreditWithdraw() {
        return creditWithdraw;
    }

    public void setCreditWithdraw(double creditWithdraw) {
        this.creditWithdraw = creditWithdraw;
    }

    public int getUserFlag() {
        return userFlag;
    }

    public void setUserFlag(int userFlag) {
        this.userFlag = userFlag;
    }

    public int getUserTag() {
        return userTag;
    }

    public void setUserTag(int userTag) {
        this.userTag = userTag;
    }

}
