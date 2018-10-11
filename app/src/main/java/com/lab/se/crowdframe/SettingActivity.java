package com.lab.se.crowdframe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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

public class SettingActivity extends AppCompatActivity {

    Toolbar mToolbar;
    CrowdFrameApplication app;
    LinearLayout changePassword, changeTag, changeLanguage;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        SysApplication.getInstance().addActivity(this);

        app = (CrowdFrameApplication)getApplication();
        preferences = getSharedPreferences("crowdframe", 0);
        editor = preferences.edit();
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
        changePassword = (LinearLayout)findViewById(R.id.change_password);
        changeTag = (LinearLayout)findViewById(R.id.change_tag);
        changeLanguage = (LinearLayout)findViewById(R.id.change_language);

        changeLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);//创建对话框
                final LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(R.layout.change_language,null);
                final RadioButton chinese = (RadioButton)ll.findViewById(R.id.chinese);
                final RadioButton english = (RadioButton)ll.findViewById(R.id.english);
                final int lan;
                if("".equals(preferences.getString("language", "")) || "chinese".equals(preferences.getString("language", ""))){
                    chinese.setChecked(true);
                    lan = 1;
                } else {
                    english.setChecked(true);
                    lan = 2;
                }
                builder.setView(ll);
                builder.setTitle(SettingActivity.this.getString(R.string.change_language));
                builder.setPositiveButton(SettingActivity.this.getString(R.string.dialog_update), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        if((chinese.isChecked() && lan == 2) || (english.isChecked() && lan == 1)){
                            if(chinese.isChecked()){
                                LocaleUtils.updateLocale(SettingActivity.this, Locale.CHINESE);
                            } else {
                                LocaleUtils.updateLocale(SettingActivity.this, Locale.ENGLISH);
                            }
                            Intent intent = new Intent(SettingActivity.this, EntryActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            // 杀掉进程
//                            android.os.Process.killProcess(android.os.Process.myPid());
//                            System.exit(0);
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        changeTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);//创建对话框
                final LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(R.layout.change_user_tag,null);
                final RadioButton other = (RadioButton)ll.findViewById(R.id.change_tag_other);
                final RadioButton software = (RadioButton)ll.findViewById(R.id.change_tag_software);
                if(app.getUserTag() == 1){
                    software.setChecked(true);
                } else {
                    other.setChecked(true);
                }
                builder.setView(ll);
                builder.setTitle(SettingActivity.this.getString(R.string.change_tag));
                builder.setPositiveButton(SettingActivity.this.getString(R.string.dialog_update), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        if((software.isChecked() && app.getUserTag() == 0) ||
                                            (other.isChecked() && app.getUserTag() == 1)){
                            final HashMap<String,String> postData = new HashMap<String, String>();
                            final ModifyUserInfoServlet.RequestBO requestBO = new ModifyUserInfoServlet.RequestBO();
                            requestBO.setId(app.getUserId());
                            requestBO.setAvatar("");
                            requestBO.setPassword("");
                            if(software.isChecked()){
                                requestBO.setTag(1);
                            } else {
                                requestBO.setTag(0);
                            }
                            postData.put("data", JSONUtils.toJSONString(requestBO));
                            String servlet = "ModifyUserInfoServlet";
                            HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    try {
                                        // response只能call一次
                                        ServletResponseData responseData = JSONUtils
                                                .toBean(response.body().string(), ServletResponseData.class);
                                        int result = responseData.getResult();
                                        if (result == 1) {
                                            ModifyUserInfoServlet.ResponseBO responseBO = JSONUtils.toBean(responseData.getData(),
                                                    ModifyUserInfoServlet.ResponseBO.class);
                                            app.setUserTag(responseBO.getTag());
                                            showMessage("标签修改成功！");
                                        } else {
                                            showMessage("标签修改失败！");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                @Override
                                public void onFailure(Call call, IOException e) {
                                }
                            });
                        } else {
                            showMessage("不需要修改= =");
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);//创建对话框
                final LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(R.layout.change_password,null);
                builder.setView(ll);
                builder.setTitle(SettingActivity.this.getString(R.string.change_password));
                final AlertDialog dialog = builder.create();
                dialog.show();
                Button cancel = (Button)ll.findViewById(R.id.cancel_button);
                Button confirm = (Button)ll.findViewById(R.id.confirm_modify_button);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText e1 = (EditText) ll.findViewById(R.id.new_password);
                        EditText e2 = (EditText) ll.findViewById(R.id.new_password_again);
                        final String p1 = e1.getText().toString();
                        final String p2 = e2.getText().toString();
                        if(p1.isEmpty() || p1.length() < 4){
                            e1.setError("密码格式错误！");
                        } else if( !p2.equals(p1)){
                            e2.setError("密码确认错误！");
                        } else {
                            final HashMap<String,String> postData = new HashMap<String, String>();
                            final ModifyUserInfoServlet.RequestBO requestBO = new ModifyUserInfoServlet.RequestBO();
                            requestBO.setId(app.getUserId());
                            requestBO.setAvatar("");
                            try {
                                requestBO.setPassword(RSAUtils.encryptByPublicKey(p1));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            requestBO.setTag(-1);
                            postData.put("data", JSONUtils.toJSONString(requestBO));

                            String servlet = "ModifyUserInfoServlet";
                            HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    try {
                                        // response只能call一次
                                        ServletResponseData responseData = JSONUtils
                                                .toBean(response.body().string(), ServletResponseData.class);
                                        int result = responseData.getResult();
                                        if (result == 1) {
                                            ModifyUserInfoServlet.ResponseBO responseBO = JSONUtils.toBean(responseData.getData(),
                                                    ModifyUserInfoServlet.ResponseBO.class);
                                            editor.putString("password",p1);
                                            editor.commit();
                                            showMessage("密码修改成功！");
                                            dialog.dismiss();

                                        } else {
                                            showMessage("密码修改失败！");
                                        }
                                    } catch (Exception e) {
                                        dialog.dismiss();
                                        e.printStackTrace();
                                    }
                                }
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    showMessage("密码修改失败！");
                                }
                            });
                        }
                    }
                });

            }
        });

    }

    private void showMessage(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                Toast.makeText(SettingActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
