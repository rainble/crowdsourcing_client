package com.lab.se.crowdframe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lab.se.crowdframe.entity.SysApplication;
import com.lab.se.crowdframe.http.HttpUtil;
import com.lab.se.crowdframe.http.JSONUtils;
import com.lab.se.crowdframe.http.ServletResponseData;
import com.lab.se.crowdframe.servlet.ModifyUserInfoServlet;
import com.lab.se.crowdframe.util.LocaleUtils;
import com.lab.se.crowdframe.util.RSAUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Response;

public class AboutActivity extends AppCompatActivity {

    Toolbar mToolbar;
    LinearLayout instruction, privacy, share;
    //使用说明
    private ViewPager viewPager;
    private ImageView[] tips;
    private ImageView[] mImageViews;
    private int[] imgIdArray ;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        SysApplication.getInstance().addActivity(this);

        //返回按钮
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        preferences = getSharedPreferences("crowdframe", 0);

        instruction = (LinearLayout)findViewById(R.id.instruction);
        privacy = (LinearLayout)findViewById(R.id.privacy_policy);
        share = (LinearLayout)findViewById(R.id.share_app);

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);//创建对话框
                final View ll = (View) getLayoutInflater().inflate(R.layout.app_mask,null);

                builder.setView(ll);
                builder.setTitle(AboutActivity.this.getString(R.string.share_app));
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);//创建对话框
                final View ll = (View) getLayoutInflater().inflate(R.layout.privacy,null);
                WebView mWebView = (WebView) ll.findViewById(R.id.webView);
                // 设置支持JavaScript等
                WebSettings mWebSettings = mWebView.getSettings();
//                mWebSettings.setBuiltInZoomControls(true);
//                mWebSettings.setSupportZoom(true);
                mWebView.setHapticFeedbackEnabled(false);
                // mWebView.setInitialScale(0); // 改变这个值可以设定初始大小

//                final String mimeType = "text/html";
//                final String encoding = "utf-8";
//                final String html = "privacy_chinese.html";// TODO 从本地读取HTML文件

                if("".equals(preferences.getString("language", "")) || "chinese".equals(preferences.getString("language", ""))){
                    mWebView.loadUrl("file:///android_asset/privacy_chinese.html");
                } else {
                    mWebView.loadUrl("file:///android_asset/privacy_english.html");
                }

                builder.setView(ll);
                builder.setTitle(AboutActivity.this.getString(R.string.privacy_policy));
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        instruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);//创建对话框
                final View ll = (View) getLayoutInflater().inflate(R.layout.instruction,null);
                ViewGroup group = (ViewGroup)ll.findViewById(R.id.viewGroup);
                viewPager = (ViewPager)ll.findViewById(R.id.viewPager);
                //载入图片资源ID
                imgIdArray = new int[]{R.drawable.instruction_1, R.drawable.instruction_2,
                        R.drawable.instruction_3, R.drawable.instruction_4};
                //将点点加入到ViewGroup中
                tips = new ImageView[imgIdArray.length];
                for(int i=0; i<tips.length; i++){
                    ImageView imageView = new ImageView(AboutActivity.this);
                    imageView.setLayoutParams(new LinearLayout.LayoutParams(10,10));
                    tips[i] = imageView;
                    if(i == 0){
                        tips[i].setBackgroundResource(R.drawable.page_indicator_focused);
                    }else{
                        tips[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
                    }

                    LinearLayout.LayoutParams layoutParams =
                            new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                    layoutParams.leftMargin = 5;
                    layoutParams.rightMargin = 5;
                    group.addView(imageView, layoutParams);
                }
                //将图片装载到数组中
                mImageViews = new ImageView[imgIdArray.length];
                for(int i=0; i<mImageViews.length; i++){
                    ImageView imageView = new ImageView(AboutActivity.this);
                    mImageViews[i] = imageView;
                    imageView.setBackgroundResource(imgIdArray[i]);
                }

                //设置Adapter
                viewPager.setAdapter(new MyPictureAdapter());
                //设置监听，主要是设置点点的背景
                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        setImageBackground(position % mImageViews.length);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
                //设置ViewPager的默认项, 设置为长度的100倍，这样子开始就能往左滑动
                viewPager.setCurrentItem((mImageViews.length) * 100);

                builder.setView(ll);
                builder.setTitle(AboutActivity.this.getString(R.string.instruction));
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

    }

    /**
     * 设置选中的tip的背景
     * @param selectItems
     */
    private void setImageBackground(int selectItems){
        for(int i=0; i<tips.length; i++){
            if(i == selectItems){
                tips[i].setBackgroundResource(R.drawable.page_indicator_focused);
            }else{
                tips[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    public class MyPictureAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        /**
         * 载入图片进去，用当前的position 除以 图片数组长度取余数是关键
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ((ViewGroup)container).addView(mImageViews[position % mImageViews.length], 0);
            return mImageViews[position % mImageViews.length];
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewGroup)container).removeView(mImageViews[position % mImageViews.length]);

        }
    }


}
