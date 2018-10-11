package com.lab.se.crowdframe;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lab.se.crowdframe.entity.SysApplication;
import com.lab.se.crowdframe.http.HttpUtil;
import com.lab.se.crowdframe.http.JSONUtils;
import com.lab.se.crowdframe.http.ServletResponseData;
import com.lab.se.crowdframe.jpush.JPushUtil;
import com.lab.se.crowdframe.servlet.RegisterServlet;
import com.lab.se.crowdframe.util.RSAUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import okhttp3.Call;
import okhttp3.Response;

public class Register extends AppCompatActivity {

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    private EditText usernameView,passwordView,repasswordView,phoneView;
    private String username,password,repassword,phone;
    private Button registerButton;
    private TextView loginLink;
    ProgressDialog progressDialog;
    //userTag
    RadioGroup userTagRroup;
    RadioButton softwareTag, otherTag;

    //极光推送设置别名
    private static final String TAG = "JPush";
    private static final int MSG_SET_ALIAS = 1001;

    //设置密码可见/不可见
    ImageButton eyePassword, eyePasswordAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        SysApplication.getInstance().addActivity(this);

        progressDialog = new ProgressDialog(Register.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("注册...");
        preferences = getSharedPreferences("crowdframe", 0);
        editor = preferences.edit();
        usernameView = (EditText)findViewById(R.id.user_name);
        passwordView = (EditText)findViewById(R.id.input_password);
        repasswordView = (EditText)findViewById(R.id.input_repassword);
        phoneView = (EditText)findViewById(R.id.input_phone);
        registerButton = (Button)findViewById(R.id.btn_signup);
        loginLink = (TextView)findViewById(R.id.link_login);

        userTagRroup = (RadioGroup)findViewById(R.id.user_tag_group);
        softwareTag = (RadioButton)findViewById(R.id.software_tag);
        otherTag = (RadioButton)findViewById(R.id.other_tag);

        eyePassword = (ImageButton)findViewById(R.id.register_eye);
        eyePasswordAgain = (ImageButton)findViewById(R.id.register_again_eye);

        eyePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(Register.this, passwordView.getInputType()+"", Toast.LENGTH_SHORT).show();
                if( InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD != passwordView.getInputType()){
                    passwordView.setInputType( InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    passwordView.setSelection(passwordView.getText().length());
                    eyePassword.setImageResource(R.drawable.register_eye_gray);
                } else {
                    //默认状态显示密码--设置文本 要一起写才能起作用 InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                    passwordView.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordView.setSelection(passwordView.getText().length());
                    eyePassword.setImageResource(R.drawable.register_eye);
                }
            }
        });

        eyePasswordAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD != repasswordView.getInputType()){
                    repasswordView.setInputType( InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    repasswordView.setSelection(repasswordView.getText().length());
                    eyePasswordAgain.setImageResource(R.drawable.register_eye_gray);
                } else {
                    //默认状态显示密码--设置文本 要一起写才能起作用 InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                    repasswordView.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    repasswordView.setSelection(repasswordView.getText().length());
                    eyePasswordAgain.setImageResource(R.drawable.register_eye);
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()){
                    progressDialog.show();
                    register();
                }
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),Login.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

    }

    private boolean validate() {
        boolean valid = true;
        username = usernameView.getText().toString();
        password = passwordView.getText().toString();
        repassword = repasswordView.getText().toString();
        phone = phoneView.getText().toString();

        if(username.isEmpty()){
            usernameView.setError(this.getString(R.string.register_username_error));
            valid = false;
        }
        else{
            usernameView.setError(null);
        }
        if (password.isEmpty() || password.length() < 5 ) {
            passwordView.setError(this.getString(R.string.register_password_tooshort));
            valid = false;
        } else {
            passwordView.setError(null);
        }

        if (repassword.isEmpty() || repassword.length() < 4 || !(repassword.equals(password))) {
            repasswordView.setError(this.getString(R.string.register_password_notmatch));
            valid = false;
        } else {
            repasswordView.setError(null);
        }
        if(isMobileNo(phone)){
            phoneView.setError(null);
        }
        else{
            phoneView.setError(this.getString(R.string.register_phone_not_correct));
            valid = false;
        }
        return valid;
    }

    private boolean isMobileNo(String phone){
        Pattern p = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$");
        Matcher m = p.matcher(phone);
        System.out.println(m.matches()+"---");
        return m.matches();
    }

    private void register() {

        /*发送post登录请求*/
        final HashMap<String, String> postData = new HashMap<String, String>();
        final RegisterServlet.RequestBO requestBO = new RegisterServlet.RequestBO();
        try{
            //数据加密
            final String encryptedUserName = RSAUtils.encryptByPublicKey(username);
            final String encryptedPassword = RSAUtils.encryptByPublicKey(password);
            requestBO.setAccount(encryptedUserName);
            requestBO.setPassword(encryptedPassword);
            requestBO.setPhone(phone);
            //add user tag
            if(softwareTag.isChecked()){
                requestBO.setUserTag(1);
            } else {
                requestBO.setUserTag(0);
            }
            postData.put("data", JSONUtils.toJSONString(requestBO));

            String servlet = "RegisterServlet";
            HttpUtil.doPost(servlet, postData, new okhttp3.Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        Log.d("error", response.body().string());
                        ServletResponseData responseData = JSONUtils.toBean(response.body().string(), ServletResponseData.class);
                        //判断是否注册成功，如果成功将返回的userId注册成极光推送的别名
                        int result = responseData.getResult();
                        if (result == 1) {
                            RegisterServlet.ResponseBO responseBO = JSONUtils.toBean(responseData.getData(), RegisterServlet.ResponseBO.class);
                            editor.putString("username",username);
                            editor.putString("password",password);
                            editor.commit();
                            setAlias(responseBO.getUserId());

                        } else {
                            showRegisterFailed(result);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showRegisterFailed(-2);
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    showRegisterFailed(-2);
                }

            });
        }
        catch(Exception e){

        }
    }

    //极光推送设置别名
    private void setAlias(int userId) {
        String alias = String.valueOf(userId);
        if (TextUtils.isEmpty(alias)) {
            //Toast.makeText(Register.this, "alias empty", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"alias empty");
            return;
        }
        if (!JPushUtil.isValidTagAndAlias(alias)) {
            //Toast.makeText(Register.this, "invalid alias", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"invalid alias");
            return;
        }

        //调用JPush API设置Alias
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS, alias));
    }


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_SET_ALIAS) {
                Log.d(TAG, "Set alias in handler.");
                JPushInterface.setAliasAndTags(getApplicationContext(), (String) msg.obj, null, mAliasCallback);
            }
        }
    };

    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs;
            switch (code) {
                case 0:
                    logs = "Set tag and alias success";
                    Log.i(TAG, logs);
                    showRegisterSuccessed();
                    break;

                case 6002:
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    Log.i(TAG, logs);
                    if (JPushUtil.isConnected(getApplicationContext())) {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
                    } else {
                        Log.i(TAG, "No network");
                    }
                    break;

                default:
                    logs = "Failed with errorCode = " + code;
                    Log.e(TAG, logs);
            }

            //JPushUtil.showToast(logs, getApplicationContext());
        }

    };


    //show the register successed message
    private void showRegisterSuccessed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                progressDialog.dismiss();//回到登录界面
                Toast.makeText(Register.this,
                        Register.this.getString(R.string.register_successfully), Toast.LENGTH_SHORT).show();
                Register.this.finish();
            }
        });
    }

    //show the register failed message
    private void showRegisterFailed(final int result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                progressDialog.dismiss();//回到登录界面
                if (result == -1) { //用户名已存在
                    usernameView.setError(Register.this.getString(R.string.user_name_exist));
                    usernameView.requestFocus();
                } else if (result == -2) { //服务器异常
                    Toast.makeText(Register.this,
                            Register.this.getString(R.string.internal_server_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
