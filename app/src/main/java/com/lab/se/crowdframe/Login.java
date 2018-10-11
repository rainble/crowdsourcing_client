package com.lab.se.crowdframe;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lab.se.crowdframe.entity.SysApplication;
import com.lab.se.crowdframe.http.HttpUtil;
import com.lab.se.crowdframe.http.JSONUtils;
import com.lab.se.crowdframe.http.ServletResponseData;
import com.lab.se.crowdframe.servlet.LoginServlet;
import com.lab.se.crowdframe.util.Global;
import com.lab.se.crowdframe.util.RSAUtils;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Response;

public class Login extends AppCompatActivity {
    private static final String TAG = "Login";
    //判断是否有登录信息，实现自动登录
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    // UI references.
    private AutoCompleteTextView mUserNameView;
    private EditText mPasswordView;
    private Button loginButton;
    private String userName, password;
    private TextView signupLink;
    CrowdFrameApplication app;
    ProgressDialog  progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 避免从桌面启动程序后，会重新实例化入口类的activity
        if (!this.isTaskRoot()) { // 判断当前activity是不是所在任务栈的根
            Intent intent = getIntent();
            if (intent != null) {
                String action = intent.getAction();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                    finish();
                    return;
                }
            }
        }
        setContentView(R.layout.login);
        SysApplication.getInstance().addActivity(this);
        progressDialog = new ProgressDialog(Login.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("登录中...");

        app = (CrowdFrameApplication) getApplication();
        preferences = getSharedPreferences("crowdframe", 0);
        editor = preferences.edit();

        mUserNameView = (AutoCompleteTextView) findViewById(R.id.user_name);
        mPasswordView = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.login_button);
        signupLink = (TextView)findViewById(R.id.link_signup);

        initGlobal();//检查版本号

    }

    public void initGlobal(){
        try{
            Global.localVersion = getPackageManager().getPackageInfo(getPackageName(),0).versionCode; //设置本地版本号
            //查询服务器上版本号
            String servlet = "GetServerVersionServlet";
            HttpUtil.sendOkHttpRequestByGet(servlet,new okhttp3.Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String result = response.body().string();
                    Log.d(TAG,String.format("The server version is %s",result));
                    Global.serverVersion = Integer.parseInt(result.trim());
                    Log.d(TAG,String.format("The server version is %d",Global.serverVersion));

                    doThings();
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    //安卓不允许在子线程中进行UI操作，所以要借助于runOnUiThread方法
    private void doThings() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                //进行版本更新
                if(Global.localVersion < Global.serverVersion){
                    //发现新版本，提示用户更新
                    AlertDialog.Builder alert = new AlertDialog.Builder(Login.this);
                    alert.setTitle(Login.this.getString(R.string.update))
                            .setMessage(Login.this.getString(R.string.update_message))
                            .setPositiveButton(Login.this.getString(R.string.update_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //开启更新服务UpdateService
                                    //这里为了把update更好模块化，可以传一些updateService依赖的值
                                    //如布局ID，资源ID，动态获取的标题,这里以app_name为例
//                                    Intent updateIntent = new Intent(Login.this, UpdateService.class);
//                                    updateIntent.putExtra("titleId",R.string.app_name);
//                                    startService(updateIntent);
                                    UpdateManager updateManager = new UpdateManager(Login.this);
                                    updateManager.showDownloadDialog();
                                }
                            });
                    alert.create().show();
                }
                //进行登录操作
                else{
                    //判断是否有登录信息，实现自动登录
                    if (!judgeLogin()) {
                        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                            @Override
                            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                                    attemptLogin();
                                    return true;
                                }
                                return false;
                            }
                        });

                        loginButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Store values at the time of the login attempt.
                                attemptLogin();
                            }
                        });


                        signupLink.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // Start the Signup activity
                                System.out.println("signup start");
                                Intent intent = new Intent(getApplicationContext(), Register.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                                System.out.println("signup end");
                            }
                        });
                    }
                    else{
                        if (progressDialog != null && !progressDialog.isShowing()){
                            progressDialog.show();
                        }
                        login();
                    }
                }
            }
        });
    }
    /**
     * judge whether can find login information in the SharedPreferences
     *
     * @return whether need to show the login page
     */
    private boolean judgeLogin() {
        userName = preferences.getString("username", null);
        password = preferences.getString("password", null);
        if (null != userName && null != password) {
            return true;
        } else {
            return false;
        }
    }

    private void attemptLogin() {
        // Reset errors.
        mUserNameView.setError(null);
        mPasswordView.setError(null);

        userName = mUserNameView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(userName)) {
            mUserNameView.setError(getString(R.string.error_field_required));
            focusView = mUserNameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            progressDialog.show();
            login();
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private void login() {

        /*发送post登录请求*/
        final HashMap<String, String> postData = new HashMap<String, String>();
        final LoginServlet.RequestBO requestBO = new LoginServlet.RequestBO();

        try{
            //数据加密
            final String encryptedUserName = RSAUtils.encryptByPublicKey(userName);
            final String encryptedPassword = RSAUtils.encryptByPublicKey(password);
            requestBO.setAccount(encryptedUserName);
            requestBO.setPassword(encryptedPassword);

            Log.d("lwh",String.format("The encryptedUserName is %s and the encryptedPassword is %s",requestBO.getAccount(),requestBO.getPassword()));

            Log.d("lwh",userName + "  " + password);
            postData.put("data", JSONUtils.toJSONString(requestBO));

            String servlet = "LoginServlet";
            HttpUtil.doPost(servlet, postData, new okhttp3.Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        ServletResponseData responseData = JSONUtils.toBean(response.body().string(), ServletResponseData.class);
                        //判断是否登录成功，如果成功将返回的userId存入application
                        int result = responseData.getResult();
                        if (result == 1) {
                            LoginServlet.ResponseBO responseBO = JSONUtils.toBean(responseData.getData(), LoginServlet.ResponseBO.class);
                            editor.putString("username",userName);
                            editor.putString("password",password);
                            editor.commit();

                            app.setUserId(responseBO.getUserId());
                            app.setUserName(userName);
                            app.setCreditPublish(responseBO.getCreditPublish());
                            app.setCreditWithdraw(responseBO.getCreditWithdraw());

                            Intent i = new Intent(Login.this, EntryActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            showLoginFailed(result);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showLoginFailed(-4);
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    showLoginFailed(-5);
                }

            });
        }
        catch(Exception e){

        }
    }

    //show the login failed message
    private void showLoginFailed(final int result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                progressDialog.dismiss();//回到登录界面
                if (result == -1) { //用户名不存在
                    mUserNameView.setError(Login.this.getString(R.string.user_name_not_exist));
                    mUserNameView.requestFocus();
                } else if (result == -2) { //密码错误
                    mPasswordView.setError(Login.this.getString(R.string.wrong_password));
                    mPasswordView.requestFocus();
                } else if (result == -3) {
                    Toast.makeText(Login.this,
                            Login.this.getString(R.string.internal_server_error), Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(Login.this,
                            String.valueOf(result), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

}
