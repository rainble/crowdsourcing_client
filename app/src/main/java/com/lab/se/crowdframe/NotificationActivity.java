package com.lab.se.crowdframe;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Iterator;

import cn.jpush.android.api.JPushInterface;

public class NotificationActivity extends AppCompatActivity {

    public static final double SMALL_WIN_H_SCALE = 0.67;
    public static final double SMALL_WIN_W_SCALE = 0.72;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setHomeAsUpIndicator(R.drawable.notification_delete);


        TextView title = (TextView)findViewById(R.id.notification_title);
        TextView content = (TextView)findViewById(R.id.notification_content);
//        TextView contractTime = (TextView)findViewById(R.id.contract_time);

        Intent intent = getIntent();
        if (null != intent) {
            Bundle bundle = getIntent().getExtras();
//            title.setText(bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE));
//            content.setText(bundle.getString(JPushInterface.EXTRA_ALERT));
            String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
            try{
                JSONObject json = new JSONObject(extra);
                //接任务返回的推送消息
                if((Integer.parseInt(json.getString("type")) == 1)){
                    if((Integer.parseInt(json.getString("result")) == 1)){
                        title.setText(NotificationActivity.this.getString(R.string.accept_task_successfully)+json.getString("taskTitle"));
                        content.setText(NotificationActivity.this.getString(R.string.accept_task_successfully_desc1) + json.getString("contractTime") + NotificationActivity.this.getString(R.string.accept_task_successfully_desc2) + json.getString("stageDesc"));
                    }
                    else{
                        title.setText(NotificationActivity.this.getString(R.string.accept_task_fail)+json.getString("taskTitle"));
                        content.setVisibility(View.GONE);
                    }
                }
                //发布的任务被完成时的推送消息
                else if((Integer.parseInt(json.getString("type")) == 2)){
                    title.setText(NotificationActivity.this.getString(R.string.published_task_completed));
                    content.setText(NotificationActivity.this.getString(R.string.published_task_completed_desc1) + json.getString("taskTitle") +
                            NotificationActivity.this.getString(R.string.published_task_completed_desc2) + json.getString("workerName") +
                            NotificationActivity.this.getString(R.string.published_task_completed_desc3) + json.getString("finishTime") +
                            NotificationActivity.this.getString(R.string.published_task_completed_desc4));
                }
                //有人发布任务时的推送消息
                else if((Integer.parseInt(json.getString("type")) == 3)){
                    title.setText(NotificationActivity.this.getString(R.string.task_published));
                    content.setText(NotificationActivity.this.getString(R.string.task_published_desc1) + json.getString("requester") +
                            NotificationActivity.this.getString(R.string.task_published_desc2) + json.getString("taskTitle") +
                            NotificationActivity.this.getString(R.string.task_published_desc3) + json.getString("totalReward") +
                            NotificationActivity.this.getString(R.string.task_published_desc4)+ json.getString("ddl")) ;
                }
            }catch (Exception e){
                Log.e("JPush", "json exception");
            }

        }
    }


    private void resizeActivity(){
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        setFinishOnTouchOutside(false);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        layoutParams.copyFrom(getWindow().getAttributes());
        layoutParams.gravity = Gravity.CENTER;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        layoutParams.width = (int) (displayMetrics.widthPixels * SMALL_WIN_W_SCALE);
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        layoutParams.dimAmount = 0.7f;
        layoutParams.alpha = 1.0f;
        getWindow().setAttributes(layoutParams);

        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        View view = getWindow().getDecorView();
        if(view != null) {
            view.setBackgroundResource(R.drawable.bg_layout_shape);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        overridePendingTransition(R.anim.activity_dialog_close_enter, R.anim.activity_dialog_close_exit);
        resizeActivity();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_dialog_close_enter, R.anim.activity_dialog_close_exit);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
