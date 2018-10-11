package com.lab.se.crowdframe;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.bumptech.glide.Glide;
import com.lab.se.crowdframe.entity.OngoingTask;
import com.lab.se.crowdframe.entity.SysApplication;
import com.lab.se.crowdframe.http.HttpUtil;
import com.lab.se.crowdframe.http.JSONUtils;
import com.lab.se.crowdframe.http.ServletResponseData;
import com.lab.se.crowdframe.servlet.AbortTaskServlet;
import com.lab.se.crowdframe.servlet.CompleteTaskServlet;
import com.lab.se.crowdframe.servlet.GetPunishCreditServlet;
import com.lab.se.crowdframe.servlet.GetTaskInfoServlet;
import com.lab.se.crowdframe.servlet.QueryTaskInfoServlet;
import com.lab.se.crowdframe.servlet.UploadImage;
import com.lab.se.crowdframe.util.Global;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

public class AcceptOngoingTaskActivity extends AppCompatActivity implements OnGetRoutePlanResultListener {

    Toolbar mToolbar;
    CrowdFrameApplication app;
    OngoingTask ongoingTask;

    public static final int START_TAKE_PHOTO = 1, START_CHOOSE_PHOTO = 2,END_TAKE_PHOTO = 3, END_CHOOSE_PHOTO = 4;
    private ImageView start_picture,end_picture;
    private Uri imageUri;
    String startImagePath, endImagePath;
    List<CompleteTaskServlet.OutputBO> feedbacks = new ArrayList<CompleteTaskServlet.OutputBO>();
    Map<OngoingTask.Output, View> outputViews = new HashMap<OngoingTask.Output, View>();//便于收集回答，integer为output类型
    CompleteTaskServlet.OutputBO startImageFeedback , endImageFeedback;

    //系统检测位置信息
    RoutePlanSearch mSearch;
    LatLng myLatlng = null;
    //location service
    IntentFilter filter = new IntentFilter("com.lab.se.updateLocation");
    LocationBroadCaseReceiver receiver = new LocationBroadCaseReceiver();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_ongoing_task);
        SysApplication.getInstance().addActivity(this);
        app = (CrowdFrameApplication)getApplication();
        progressDialog = new ProgressDialog(AcceptOngoingTaskActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("上传数据中...");

        //返回按钮
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(this.getString(R.string.ongoing_task));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ongoingTask = (OngoingTask)getIntent().getSerializableExtra("ongoingTask");
        fillTask();

        //定位检测设置监听器
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(AcceptOngoingTaskActivity.this);
    }


    public void fillTask(){
        TextView stageDesc = (TextView) findViewById(R.id.stage_desc);
        stageDesc.setText(ongoingTask.getStageDesc());
        stageDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTaskDetail(ongoingTask.getTaskId(), 0);
            }
        });

        TextView  currentStage = (TextView) findViewById(R.id.current_stage);
        currentStage.setText(String.valueOf(ongoingTask.getCurrentStage()));
        TextView tv = (TextView) findViewById(R.id.task_title);
        tv.setText(ongoingTask.getTaskTitle());
        TextView   contract= (TextView) findViewById(R.id.task_contract);
        contract.setText(Global.getTime(ongoingTask.getContract()));
        TextView reward = (TextView)findViewById(R.id.stage_reward);
        reward.setText(String.valueOf(ongoingTask.getReward()));

        start_picture = (ImageView) findViewById(R.id.start_picture);
        end_picture = (ImageView) findViewById(R.id.end_picture);

        //start location
        final OngoingTask.Location start = ongoingTask.getLocations().get(0);
        if(start.getType() != -1){
            LinearLayout startTitle = (LinearLayout)findViewById(R.id.start_title);
            startTitle.setVisibility(View.VISIBLE);

            TextView startAddress = (TextView)findViewById(R.id.start_location_address);
            startAddress.setText(start.getAddress().replace("=", "\n " + this.getString(R.string.detail_address)));
            Button checkStartMap = (Button)findViewById(R.id.start_check_map_button);
            checkStartMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(AcceptOngoingTaskActivity.this, CheckMapActivity.class);
                    i.putExtra("lat",start.getLatitude());
                    i.putExtra("lng",start.getLongitude());
                    startActivity(i);
                }
            });

            if(null != start.getInputs() && start.getInputs().size() > 0){
                TableLayout startExtra  = (TableLayout)findViewById(R.id.start_extra_information);
                startExtra.setVisibility(View.VISIBLE);
                for(OngoingTask.Input input : start.getInputs()){
                    TextView t1 = new TextView(AcceptOngoingTaskActivity.this);
                    t1.setText(input.getDesc()+": ");
                    TextView t2 = new TextView(AcceptOngoingTaskActivity.this);
                    t2.setText(input.getValue());
                    TableRow row = new TableRow(AcceptOngoingTaskActivity.this);
                    row.addView(t1);
                    row.addView(t2);
                    startExtra.addView(row, new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                }
            }
            if(null !=  start.getOutputs() && start.getOutputs().size() > 0){
                TableLayout startFeedback  = (TableLayout)findViewById(R.id.start_feedback);
                startFeedback.setVisibility(View.VISIBLE);
                for(OngoingTask.Output output : start.getOutputs()){

                    if(output.getType() == 0){
                        TextView t1 = (TextView)startFeedback.findViewById(R.id.start_feedback_pic_desc);
                        t1.setVisibility(View.VISIBLE);
                        t1.setText(output.getDesc());

                        LinearLayout startUpload = (LinearLayout) startFeedback.findViewById(R.id.start_feedback_upload);
                        startUpload.setVisibility(View.VISIBLE);

                        //获取拍照和选择照片的按钮
                        Button startTakePhoto = (Button)startUpload.findViewById(R.id.start_feedback_take_photo);
                        Button startSelectPhoto = (Button)startUpload.findViewById(R.id.start_feedback_select_photo);
                        //拍照
                        startTakePhoto.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                takePhoto(START_TAKE_PHOTO);
                            }
                        });
                        //选择照片
                        startSelectPhoto.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                selectPhoto(START_CHOOSE_PHOTO);
                            }
                        });

                    } else if(output.getType() == 1){//text
                        LinearLayout v = (LinearLayout)getLayoutInflater().inflate(R.layout.fill_feedback_text, null);
                        TextView desc = (TextView)v.findViewById(R.id.feedback_text_desc);
                        desc.setText(output.getDesc());
                        startFeedback.addView(v);
                        outputViews.put(output, v);
                    } else if(output.getType() == 2){//number
                        LinearLayout v = (LinearLayout)getLayoutInflater().inflate(R.layout.fill_feedback_number, null);
                        TextView desc = (TextView)v.findViewById(R.id.feedback_number_desc);
                        desc.setText(output.getDesc());
                        startFeedback.addView(v);
                        outputViews.put(output, v);
                    } else if(output.getType() == 3){//enum
                        LinearLayout v = (LinearLayout)getLayoutInflater().inflate(R.layout.fill_feedback_enum, null);
                        TextView desc = (TextView)v.findViewById(R.id.feedback_enum_desc);
                        desc.setText(output.getDesc());
                        Spinner value = (Spinner)v.findViewById(R.id.feedback_enum_value);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                                        android.R.layout.simple_spinner_item, output.getEntries().split(";"));
                        value.setAdapter(adapter);
                        startFeedback.addView(v);
                        outputViews.put(output, v);
                    }
                }
            }
        }

        //end location
        final OngoingTask.Location end = ongoingTask.getLocations().get(1);
        if(end.getType() != -1){
            LinearLayout endTitle = (LinearLayout)findViewById(R.id.end_title);
            endTitle.setVisibility(View.VISIBLE);

            TextView endAddress = (TextView)findViewById(R.id.end_location_address);
            endAddress.setText(end.getAddress().replace("=",  "\n " + this.getString(R.string.detail_address)));
            Button checkEndMap = (Button)findViewById(R.id.end_check_map_button);
            checkEndMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(AcceptOngoingTaskActivity.this, CheckMapActivity.class);
                    i.putExtra("lat",end.getLatitude());
                    i.putExtra("lng",end.getLongitude());
                    startActivity(i);
                }
            });

            if(null != end.getInputs() && end.getInputs().size() > 0){
                TableLayout endExtra  = (TableLayout)findViewById(R.id.end_extra_information);
                endExtra.setVisibility(View.VISIBLE);

                for(OngoingTask.Input input : end.getInputs()){
                    TextView t1 = new TextView(AcceptOngoingTaskActivity.this);
                    t1.setText(input.getDesc()+": ");

                    TextView t2 = new TextView(AcceptOngoingTaskActivity.this);
                    t2.setText(input.getValue());

                    TableRow row = new TableRow(AcceptOngoingTaskActivity.this);
                    row.addView(t1);
                    row.addView(t2);
                    endExtra.addView(row, new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                }
            }
            if(null != end.getOutputs() && end.getOutputs().size() > 0){
                TableLayout endFeedback  = (TableLayout)findViewById(R.id.end_feedback);
                endFeedback.setVisibility(View.VISIBLE);
                for(OngoingTask.Output output : end.getOutputs()){
                    if(output.getType() == 0){
                        TextView t1 = (TextView)endFeedback.findViewById(R.id.end_feedback_pic_desc);
                        t1.setVisibility(View.VISIBLE);
                        t1.setText(output.getDesc());

                        LinearLayout endUpload = (LinearLayout) endFeedback.findViewById(R.id.end_feedback_upload);
                        endUpload.setVisibility(View.VISIBLE);

                        //获取拍照和选择照片的按钮
                        Button endTakePhoto = (Button)endUpload.findViewById(R.id.end_feedback_take_photo);
                        Button endSelectPhoto = (Button)endUpload.findViewById(R.id.end_feedback_select_photo);
                        //拍照
                        endTakePhoto.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                takePhoto(END_TAKE_PHOTO);
                            }
                        });
                        //选择照片
                        endSelectPhoto.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                selectPhoto(END_CHOOSE_PHOTO);
                            }
                        });

                    }else if(output.getType() == 1){//text
                        LinearLayout v = (LinearLayout)getLayoutInflater().inflate(R.layout.fill_feedback_text, null);
                        TextView desc = (TextView)v.findViewById(R.id.feedback_text_desc);
                        desc.setText(output.getDesc());
                        endFeedback.addView(v);
                        outputViews.put(output, v);
                    } else if(output.getType() == 2){//number
                        LinearLayout v = (LinearLayout)getLayoutInflater().inflate(R.layout.fill_feedback_number, null);
                        TextView desc = (TextView)v.findViewById(R.id.feedback_number_desc);
                        desc.setText(output.getDesc());
                        endFeedback.addView(v);
                        outputViews.put(output, v);
                    } else if(output.getType() == 3){//enum
                        LinearLayout v = (LinearLayout)getLayoutInflater().inflate(R.layout.fill_feedback_enum, null);
                        TextView desc = (TextView)v.findViewById(R.id.feedback_enum_desc);
                        desc.setText(output.getDesc());
                        Spinner value = (Spinner)v.findViewById(R.id.feedback_enum_value);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                                android.R.layout.simple_spinner_item, output.getEntries().split(";"));
                        value.setAdapter(adapter);
                        endFeedback.addView(v);
                        outputViews.put(output, v);
                    }
                }
            }
        }

        //click the "ABORT" button
        Button abortButton = (Button)findViewById(R.id.abort_task);
        abortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 /*放弃任务-阶段一*/
                final HashMap<String,String> postData = new HashMap<String, String>();
                final GetPunishCreditServlet.RequestBO requestBO = new GetPunishCreditServlet.RequestBO();
                requestBO.setUserId(app.getUserId());
                requestBO.setStageId(ongoingTask.getStageId());
                postData.put("data", JSONUtils.toJSONString(requestBO));

                String servlet = "GetPunishCreditServlet";
                HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            // response只能call一次
                            ServletResponseData responseData = JSONUtils
                                    .toBean(response.body().string(), ServletResponseData.class);
                            int result = responseData.getResult();
                            if(result == 1){
                                GetPunishCreditServlet.ResponseBO responseBO = JSONUtils.toBean(responseData.getData(),
                                        GetPunishCreditServlet.ResponseBO.class);
                                confirmAbort(responseBO.getCreditPunish());
                            } else{
                                Log.i("111", "error"+JSON.toJSONString(responseData.getData()));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {

                    }
                });
            }
        });

        //click the "FINISH" button
        Button finishTaskButton = (Button)findViewById(R.id.finish_task);
        finishTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != myLatlng){
                    //判断feedback是否都填了
                    boolean fillFeedback = true;
                    feedbacks.clear();
                    if(start.getType() != -1 && null != start.getOutputs() && start.getOutputs().size() > 0){
                        for(OngoingTask.Output output : start.getOutputs()){
                            if(output.getType() == 0){
                                if(start_picture.getDrawable() == null){
                                    fillFeedback = false;
                                } else {
                                    startImageFeedback = new CompleteTaskServlet.OutputBO();
                                    startImageFeedback.setId(output.getId());
                                    startImageFeedback.setType(output.getType());
                                    startImageFeedback.setActionId(output.getActionId());
                                    startImageFeedback.setDesc(output.getDesc());
                                    startImageFeedback.setActive(output.isActive());
                                    startImageFeedback.setIntervalValue(output.getIntervalValue());
                                    startImageFeedback.setLowBound(output.getLowBound());
                                    startImageFeedback.setUpBound(output.getUpBound());
                                    startImageFeedback.setEntries(output.getEntries());
                                    startImageFeedback.setAggregaMethod(output.getAggregaMethod());
                                }
                            } else {
                                View l = outputViews.get(output);
                                CompleteTaskServlet.OutputBO o = new CompleteTaskServlet.OutputBO();
                                o.setId(output.getId());
                                o.setActionId(output.getActionId());
                                o.setType(output.getType());
                                o.setDesc(output.getDesc());
                                o.setActive(output.isActive());
                                o.setIntervalValue(output.getIntervalValue());
                                o.setLowBound(output.getLowBound());
                                o.setUpBound(output.getUpBound());
                                o.setEntries(output.getEntries());
                                o.setAggregaMethod(output.getAggregaMethod());
                                if(output.getType() == 1){//text
                                    EditText et = (EditText)l.findViewById(R.id.feedback_text_value);
                                    if(TextUtils.isEmpty(et.getText())){
                                        fillFeedback = false;
                                    } else {
                                        o.setValue(et.getText().toString());
                                    }
                                } else if(output.getType() == 2){//number
                                    EditText et = (EditText)l.findViewById(R.id.feedback_number_value);
                                    if(TextUtils.isEmpty(et.getText())){
                                        fillFeedback = false;
                                    } else {
                                        o.setValue(et.getText().toString());
                                    }
                                } else if(output.getType() == 3){//enum
                                    Spinner spinner = (Spinner)l.findViewById(R.id.feedback_enum_value);
                                    o.setValue(spinner.getSelectedItem().toString());
                                }
                                feedbacks.add(o);

                            }
                        }
                    }
                    if(end.getType() != -1 && null != end.getOutputs() && end.getOutputs().size() > 0){
                        for(OngoingTask.Output output : end.getOutputs()){
                            if(output.getType() == 0){
                                if(end_picture.getDrawable() == null){
                                    fillFeedback = false;
                                }else {
                                    endImageFeedback = new CompleteTaskServlet.OutputBO();
                                    endImageFeedback.setId(output.getId());
                                    endImageFeedback.setType(output.getType());
                                    endImageFeedback.setActionId(output.getActionId());
                                    endImageFeedback.setDesc(output.getDesc());
                                    endImageFeedback.setActive(output.isActive());
                                    endImageFeedback.setIntervalValue(output.getIntervalValue());
                                    endImageFeedback.setLowBound(output.getLowBound());
                                    endImageFeedback.setUpBound(output.getUpBound());
                                    endImageFeedback.setEntries(output.getEntries());
                                    endImageFeedback.setAggregaMethod(output.getAggregaMethod());
                                }
                            } else {
                                View l = outputViews.get(output);
                                CompleteTaskServlet.OutputBO o = new CompleteTaskServlet.OutputBO();
                                o.setId(output.getId());
                                o.setActionId(output.getActionId());
                                o.setType(output.getType());
                                o.setDesc(output.getDesc());
                                o.setActive(output.isActive());
                                o.setIntervalValue(output.getIntervalValue());
                                o.setLowBound(output.getLowBound());
                                o.setUpBound(output.getUpBound());
                                o.setEntries(output.getEntries());
                                o.setAggregaMethod(output.getAggregaMethod());
                                if(output.getType() == 1){//text
                                    EditText et = (EditText)l.findViewById(R.id.feedback_text_value);
                                    if(TextUtils.isEmpty(et.getText())){
                                        fillFeedback = false;
                                    } else {
                                        o.setValue(et.getText().toString());
                                    }
                                } else if(output.getType() == 2){//number
                                    EditText et = (EditText)l.findViewById(R.id.feedback_number_value);
                                    if(TextUtils.isEmpty(et.getText())){
                                        fillFeedback = false;
                                    } else {
                                        o.setValue(et.getText().toString());
                                    }
                                } else if(output.getType() == 3){//enum
                                    Spinner spinner = (Spinner)l.findViewById(R.id.feedback_enum_value);
                                    o.setValue(spinner.getSelectedItem().toString());
                                }
                                feedbacks.add(o);
                            }
                        }
                    }
                    if(fillFeedback == true){
                        PlanNode stNode = PlanNode.withLocation(myLatlng);
                        PlanNode enNode = PlanNode.withLocation(new LatLng(end.getLatitude(), end.getLongitude()));
                        mSearch.walkingSearch((new WalkingRoutePlanOption()).from(stNode).to(enNode));
                    } else {
                        feedbacks.clear();
                        Toast.makeText(AcceptOngoingTaskActivity.this,
                                AcceptOngoingTaskActivity.this.getString(R.string.need_to_fill_feedback), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void confirmAbort(final double punishCredit){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                AlertDialog.Builder builder = new AlertDialog.Builder(AcceptOngoingTaskActivity.this);//创建对话框
                builder.setMessage(AcceptOngoingTaskActivity.this.getString(R.string.abort_task_detail_1)
                        + punishCredit + AcceptOngoingTaskActivity.this.getString(R.string.abort_task_detail_2));
                builder.setTitle(AcceptOngoingTaskActivity.this.getString(R.string.abort_task));
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                          /*放弃任务-阶段二*/
                        final HashMap<String,String> postData = new HashMap<String, String>();
                        final AbortTaskServlet.RequestBO requestBO = new AbortTaskServlet.RequestBO();
                        requestBO.setUserId(app.getUserId());
                        requestBO.setStageId(ongoingTask.getStageId());
                        requestBO.setCreditPunish(punishCredit);
                        postData.put("data", JSONUtils.toJSONString(requestBO));

                        String servlet = "AbortTaskServlet";
                        HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    // response只能call一次
                                    ServletResponseData responseData = JSONUtils
                                            .toBean(response.body().string(), ServletResponseData.class);
                                    int result = responseData.getResult();
                                    if(result == 1){
                                        Intent i = new Intent(AcceptOngoingTaskActivity.this, TaskListActivity.class);
                                        setResult(7, i);
                                        finish();
                                    } else{
                                        Log.i("111", "error"+JSON.toJSONString(responseData.getData()));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(Call call, IOException e) {

                            }
                        });
                    }
                });
                builder.create().show();
            }
        });
    }


    //拍照
    private void takePhoto(int indicator){
        //创建File对象，用于存储拍照后的照片
        File outputImage = new File(getExternalCacheDir(),"output_image.jpg");
        try{
            //创建实例，如果已经存在，则先删除再创建
            if(outputImage.exists())
                outputImage.delete();
            outputImage.createNewFile();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        //将File对象转化为Uri对象，从7.0开始有不同处理方式
        if(Build.VERSION.SDK_INT >= 24){
            imageUri = FileProvider.getUriForFile(AcceptOngoingTaskActivity.this,
                    "com.lab.se.crowdframe.fileprovider",outputImage);
        }else{
           imageUri = Uri.fromFile(outputImage);
        }
        //启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,indicator);
    }

    //选择照片
    private void selectPhoto(int indicator){
        if(ContextCompat.checkSelfPermission(AcceptOngoingTaskActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(AcceptOngoingTaskActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},indicator);
        }
        else{
            openAlbum(indicator);
        }
    }

    private void openAlbum(int indicator){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*;video/*");
        startActivityForResult(intent,indicator);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults){
        switch(requestCode){
            case START_CHOOSE_PHOTO:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openAlbum(START_CHOOSE_PHOTO);
                else
                    Toast.makeText(this,AcceptOngoingTaskActivity.this.getString(R.string.deny_permission),Toast.LENGTH_SHORT).show();
                break;
            case END_CHOOSE_PHOTO:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openAlbum(END_CHOOSE_PHOTO);
                else
                    Toast.makeText(this,AcceptOngoingTaskActivity.this.getString(R.string.deny_permission),Toast.LENGTH_SHORT).show();
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case START_TAKE_PHOTO:
                if(resultCode == RESULT_OK){
                    try{
                        //将拍摄的照片显示出来
                        start_picture.setVisibility(View.VISIBLE);
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        start_picture.setImageBitmap(bitmap);
                        savePhoto(bitmap, START_TAKE_PHOTO);
                    }
                    catch(FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
                break;
            case END_TAKE_PHOTO:
                if(resultCode == RESULT_OK){
                    try{
                        //将拍摄的照片显示出来
                        end_picture.setVisibility(View.VISIBLE);
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        end_picture.setImageBitmap(bitmap);
                        savePhoto(bitmap, END_TAKE_PHOTO);
                    }
                    catch(FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
                else{

                }
                break;
            case START_CHOOSE_PHOTO:
                if(resultCode == RESULT_OK){
                    if(Build.VERSION.SDK_INT >= 19){
                        handleImageOnKitKat(data,START_CHOOSE_PHOTO);
                    }
                    else{
                        handleImageBeforeKitKat(data,START_CHOOSE_PHOTO);
                    }
                }
                break;
            case END_CHOOSE_PHOTO:
                if(resultCode == RESULT_OK){
                    if(Build.VERSION.SDK_INT >= 19){
                        handleImageOnKitKat(data,END_CHOOSE_PHOTO);
                    }
                    else{
                        handleImageBeforeKitKat(data,END_CHOOSE_PHOTO);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void savePhoto(Bitmap bitmap, int indicator){
        /*
          将临时照片存储
        */
        //设置存储路径和文件名
        String name = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
        String fileNmae = this.getApplicationContext().getFilesDir().toString() + File.separator + "image/" + name + ".jpg";
        String imagePath = fileNmae;

        File myCaptureFile = new File(fileNmae);
        try {
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                if(!myCaptureFile.getParentFile().exists()){
                    myCaptureFile.getParentFile().mkdirs();
                }
                //将照片写入之前指定的路径和文件名
                BufferedOutputStream bos;
                bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                bos.flush();
                bos.close();
                if(indicator == START_TAKE_PHOTO){
                    startImagePath = imagePath;
                } else {
                    endImagePath = imagePath;
                }
            }else{
                Toast toast= Toast.makeText(AcceptOngoingTaskActivity.this, "保存失败，SD卡无效", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data,int indicator) {
        String imagePath = null;
        Uri uri = data.getData();

        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        displayImage(imagePath,indicator); // 根据图片路径显示图片
        if(indicator == START_CHOOSE_PHOTO){
            startImagePath = imagePath;
        } else {
            endImagePath = imagePath;
        }
    }

    private void handleImageBeforeKitKat(Intent data, int indicator) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath,indicator);
        if(indicator == START_CHOOSE_PHOTO){
            startImagePath = imagePath;
        } else {
            endImagePath = imagePath;
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath, int indicator) {
        if (imagePath != null) {
            //压缩图片，避免内存溢出
            BitmapFactory.Options options = new BitmapFactory.Options();
            //只获取图片的大小信息，而不是将整张图片载入内存，避免内存溢出
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath,options);
            //压缩
            int height = options.outHeight;
            int width = options.outWidth;
            int inSampleSize = 5; // 默认像素压缩比例，压缩为原图的1/2
            int minLen = Math.min(height, width); // 原图的最小边长
            if(minLen > 1000) { // 如果原始图像的最小边长大于100dp（此处单位我认为是dp，而非px）
                float ratio = (float)minLen / 1000.0f; // 计算像素压缩比例
                inSampleSize = (int)ratio;
            }
            options.inJustDecodeBounds = false; // 计算好压缩比例后，这次可以去加载原图了
            options.inSampleSize = inSampleSize; // 设置为刚才计算的压缩比例
            Bitmap bm = BitmapFactory.decodeFile(imagePath, options); // 解码文件
            if(indicator == START_CHOOSE_PHOTO){
                start_picture.setVisibility(View.VISIBLE);
                start_picture.setScaleType(ImageView.ScaleType.FIT_XY);
                start_picture.setImageBitmap(bm);
            }
            else{
                end_picture.setVisibility(View.VISIBLE);
                end_picture.setScaleType(ImageView.ScaleType.FIT_XY);
                end_picture.setImageBitmap(bm);
            }
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(AcceptOngoingTaskActivity.this,
                    "Sorry, we cannot get the distance between the your location and the destination.",
                    Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            Toast.makeText(this,"The information of the location is wrong!",Toast.LENGTH_SHORT).show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            // 直接判断第一条路线的距离
            RouteLine route = result.getRouteLines().get(0);
            Toast.makeText(AcceptOngoingTaskActivity.this, route.getDistance() + " meters away from the destination", Toast.LENGTH_SHORT).show();

            //还要判断其他条件，比如有没有上传图片或其他信息……
            if(route.getDistance() < 500){
//                Toast.makeText(AcceptOngoingTaskActivity.this,
//                        "You can complete the ongoingTask.", Toast.LENGTH_SHORT).show();
                if((null != startImagePath && !startImagePath.isEmpty()) || (null != endImagePath && !endImagePath.isEmpty())){
                    progressDialog.show();
                    //上传图片
                    final HashMap<String,String> postData = new HashMap<String, String>();
                    String time = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
                    String name = app.getUserId() + "-" + time;
                    postData.put("imageName",name);
                    if(null != startImagePath){
                        postData.put("startImagePath",startImagePath);
                    } else {
                        postData.put("startImagePath","");
                    }
                    if(null != endImagePath){
                        postData.put("endImagePath",endImagePath);
                    } else {
                        postData.put("endImagePath","");
                    }
                    HttpUtil.uploadImage(postData,new okhttp3.Callback() {
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            ServletResponseData responseData = JSONUtils
                                    .toBean(response.body().string(), ServletResponseData.class);
                            int result = responseData.getResult();
                            if(result == 1){
                                UploadImage.ResponseBO responseBO = JSONUtils.toBean(responseData.getData(),
                                                                UploadImage.ResponseBO.class);
                                if(null != startImageFeedback){
                                    startImageFeedback.setValue(responseBO.getStartImageUri());
                                    feedbacks.add(startImageFeedback);
                                }
                                if(null != endImageFeedback){
                                    endImageFeedback.setValue(responseBO.getEndImageUri());
                                    feedbacks.add(endImageFeedback);
                                }
                               completeTask();
                            }

                        }
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    progressDialog.show();
                    completeTask();
                }

            } else {
                Toast.makeText(AcceptOngoingTaskActivity.this,
                        "You haven't launch the end location.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void completeTask(){
         /*完成任务*/
        final HashMap<String,String> postData = new HashMap<String, String>();
        final CompleteTaskServlet.RequestBO requestBO = new CompleteTaskServlet.RequestBO();
        requestBO.setUserId(app.getUserId());
        requestBO.setStageId(ongoingTask.getStageId());
        Date date = new Date();
        requestBO.setFinishTime(new Timestamp(date.getTime()));
        requestBO.setLatitude(myLatlng.latitude);
        requestBO.setLongitude(myLatlng.longitude);
        requestBO.setOutputs(feedbacks);
        postData.put("data", JSONUtils.toJSONString(requestBO));

        String servlet = "CompleteTaskServlet";
        HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    // response只能call一次
                    ServletResponseData responseData = JSONUtils
                            .toBean(response.body().string(), ServletResponseData.class);
                    int result = responseData.getResult();
                    if(result == 1){
                        Intent i = new Intent(AcceptOngoingTaskActivity.this, TaskListActivity.class);
                        setResult(7, i);
                        finish();
                    } else if(result == -2){
                        showCompleteFailed(AcceptOngoingTaskActivity.this.getString(R.string.already_completed));
                    }
                    else{
                        Log.i("111", "error"+JSON.toJSONString(responseData.getData()));
                        showCompleteFailed(JSON.toJSONString(responseData.getData()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    //show the complete task failed message
    private void showCompleteFailed(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                progressDialog.dismiss();
                Toast.makeText(AcceptOngoingTaskActivity.this,result, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //receive the location message
    class LocationBroadCaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            double latitude = intent.getDoubleExtra("lat",0);
            double longitude = intent.getDoubleExtra("lng",0);
            if(null == myLatlng || latitude != myLatlng.latitude || longitude != myLatlng.longitude){
                myLatlng = new LatLng(latitude, longitude);
            }
        }
    }

    /**
     * click the "DETAIL" button and get the ongoingTask detail from the server
     * @param taskId
     * @param showType : 0:not show the input value
     */
    public void getTaskDetail(int taskId, final int showType){
        /*获取任务详情*/
        final HashMap<String,String> postData = new HashMap<String, String>();
        final QueryTaskInfoServlet.RequestBO requestBO = new QueryTaskInfoServlet.RequestBO();
        requestBO.setTaskId(taskId);
        requestBO.setUserId(app.getUserId());
        postData.put("data", JSONUtils.toJSONString(requestBO));

        String servlet = "QueryTaskInfoServlet";
        HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    // response只能call一次
                    ServletResponseData responseData = JSONUtils
                            .toBean(response.body().string(), ServletResponseData.class);
                    int result = responseData.getResult();
                    if(result == 1){
                        QueryTaskInfoServlet.ResponseBO responseBO =
                                JSONUtils.toBean(responseData.getData(), QueryTaskInfoServlet.ResponseBO.class);
                        showTaskDetail(responseBO, showType);
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    // show the ongoingTask detail dialog
    private void showTaskDetail(final QueryTaskInfoServlet.ResponseBO responseBO, final int showType){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                AlertDialog.Builder builder = new android.app.AlertDialog.Builder(AcceptOngoingTaskActivity.this);//创建对话框
                ScrollView sv = (ScrollView) getLayoutInflater().inflate(R.layout.task_detail,null);
                TextView taskTitle = (TextView)sv.findViewById(R.id.task_title);
                taskTitle.setText(responseBO.getTitle());
                TextView taskProgress = (TextView)sv.findViewById(R.id.task_progress);
                taskProgress.setText(responseBO.getProgress());
                TextView taskDesc = (TextView)sv.findViewById(R.id.task_desc);
                taskDesc.setText(responseBO.getDescription());
                TextView taskDdl = (TextView)sv.findViewById(R.id.task_ddl);
                taskDdl.setText(Global.getTime(responseBO.getDeadline()));

                List<QueryTaskInfoServlet.ResponseBO.StageBO> stages = responseBO.getStages();
                LinearLayout taskDetailLl = (LinearLayout)sv.findViewById(R.id.task_detail_ll);
                for(int i= 0; i < stages.size(); i++){
                    QueryTaskInfoServlet.ResponseBO.StageBO stage = stages.get(i);
                    TableLayout stageTable = (TableLayout)getLayoutInflater().inflate(R.layout.stage_detail, null);
                    TextView stageName = (TextView)stageTable.findViewById(R.id.stage_name);
                    stageName.setText(stage.getName());
                    TextView stageDesc = (TextView)stageTable.findViewById(R.id.stage_desc);
                    stageDesc.setText(stage.getDesc());
                    TextView stageReward = (TextView)stageTable.findViewById(R.id.stage_reward);
                    stageReward.setText(stage.getReward()+"");

                    TextView stageWorkerDesc = (TextView)stageTable.findViewById(R.id.stage_worker_desc);
                    TextView stageWorkerValue = (TextView)stageTable.findViewById(R.id.stage_worker_value);
                    TextView stageTimeDesc = (TextView)stageTable.findViewById(R.id.stage_time_desc);
                    TextView stageTimeValue = (TextView)stageTable.findViewById(R.id.stage_time_value);
                    ImageView stageStatusIcon = (ImageView)stageTable.findViewById(R.id.stage_status_icon);
                    if(stage.getStageStatus() == 2){
                        stageStatusIcon.setBackgroundResource(R.drawable.task_waiting);
                        stageWorkerDesc.setText(AcceptOngoingTaskActivity.this.getString(R.string.detail_worker_num));
                        stageWorkerValue.setText(String.valueOf(stage.getWorkerNum()));
                        stageTimeDesc.setText(AcceptOngoingTaskActivity.this.getString(R.string.stage_ddl));
                        stageTimeValue.setText(Global.getTime(stage.getDdl()));
                    } else if(stage.getStageStatus() == 1){
                        stageStatusIcon.setBackgroundResource(R.drawable.task_finished);
                        stageWorkerDesc.setText(AcceptOngoingTaskActivity.this.getString(R.string.detail_worker));
                        stageWorkerValue.setText(listToString(stage.getWorkerNames()));
                        stageTimeDesc.setVisibility(View.GONE);
                        stageTimeValue.setVisibility(View.GONE);
                    } else if(stage.getStageStatus() == 0){
                        stageStatusIcon.setBackgroundResource(R.drawable.my_task_ongoing);
                        stageWorkerDesc.setText(AcceptOngoingTaskActivity.this.getString(R.string.detail_worker));
                        stageWorkerValue.setText(listToString(stage.getWorkerNames()));
                        stageTimeDesc.setVisibility(View.GONE);
                        stageTimeValue.setVisibility(View.GONE);
                    }

                    //start location
                    final QueryTaskInfoServlet.ResponseBO.LocationBO start = stage.getLocations().get(0);
                    if(start.getType() != -1){
                        LinearLayout startTitle = (LinearLayout)stageTable.findViewById(R.id.start_title);
                        startTitle.setVisibility(View.VISIBLE);

                        TextView startAddress = (TextView)stageTable.findViewById(R.id.start_location_address);
                        startAddress.setText(start.getAddress().replace("=", "\n " + AcceptOngoingTaskActivity.this.getString(R.string.detail_address)));

                        if(null != start.getInputs() && start.getInputs().size() > 0){
                            TableLayout startExtra  = (TableLayout)stageTable.findViewById(R.id.start_extra_information);
                            startExtra.setVisibility(View.VISIBLE);
                            for(QueryTaskInfoServlet.ResponseBO.InputBO input : start.getInputs()){
                                TextView t1 = new TextView(AcceptOngoingTaskActivity.this);
                                t1.setText(input.getDesc()+": ");

                                TextView t2 = new TextView(AcceptOngoingTaskActivity.this);
                                if(showType == 0){
                                    t2.setText("*****");
                                } else {
                                    t2.setText(input.getValue());
                                }

                                TableRow row = new TableRow(AcceptOngoingTaskActivity.this);
                                row.addView(t1);
                                row.addView(t2);
                                startExtra.addView(row, new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            }
                        }

                        if(null !=  start.getOutputs() && start.getOutputs().size() > 0){
                            TableLayout startFeedback  = (TableLayout)stageTable.findViewById(R.id.start_feedback);
                            startFeedback.setVisibility(View.VISIBLE);
                            for(QueryTaskInfoServlet.ResponseBO.OutputBO output : start.getOutputs()){
                                if(output.getType() == 0){
                                    TextView t1 = (TextView)startFeedback.findViewById(R.id.start_feedback_pic_desc);
                                    t1.setVisibility(View.VISIBLE);
                                    t1.setText("# "+output.getDesc());
                                    if(null != output.getValue()){
                                        ImageView imageView = (ImageView) startFeedback.findViewById(R.id.start_picture);
                                        imageView.setVisibility(View.VISIBLE);
                                        String imageUrl = output.getValue();
                                        Glide.with(AcceptOngoingTaskActivity.this).load(imageUrl).into(imageView);
                                    }
                                } else {
                                    TextView t2 = new TextView(AcceptOngoingTaskActivity.this);
                                    t2.setText("# "+output.getDesc());
                                    startFeedback.addView(t2);
                                    if(null != output.getValue()){
                                        TextView t3 = new TextView(AcceptOngoingTaskActivity.this);
                                        t3.setText(output.getValue());
                                        startFeedback.addView(t3);
                                    }
                                }
                            }
                        }
                    }

                    //end location
                    final QueryTaskInfoServlet.ResponseBO.LocationBO end = stage.getLocations().get(1);
                    if(end.getType() != -1) {
                        LinearLayout endTitle = (LinearLayout) stageTable.findViewById(R.id.end_title);
                        endTitle.setVisibility(View.VISIBLE);

                        TextView endAddress = (TextView) stageTable.findViewById(R.id.end_location_address);
                        endAddress.setText(end.getAddress().replace("=",  "\n " + AcceptOngoingTaskActivity.this.getString(R.string.detail_address)));

                        if(null != end.getInputs() && end.getInputs().size() > 0){
                            TableLayout endExtra  = (TableLayout)stageTable.findViewById(R.id.end_extra_information);
                            endExtra.setVisibility(View.VISIBLE);

                            for(QueryTaskInfoServlet.ResponseBO.InputBO input : end.getInputs()){
                                TextView t1 = new TextView(AcceptOngoingTaskActivity.this);
                                t1.setText(input.getDesc()+": ");

                                TextView t2 = new TextView(AcceptOngoingTaskActivity.this);
                                if(showType == 0){
                                    t2.setText("*****");
                                } else {
                                    t2.setText(input.getValue());
                                }
                                TableRow row = new TableRow(AcceptOngoingTaskActivity.this);
                                row.addView(t1);
                                row.addView(t2);
                                endExtra.addView(row, new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            }
                        }

                        if (null != end.getOutputs() && end.getOutputs().size() > 0) {
                            TableLayout endFeedback = (TableLayout) stageTable.findViewById(R.id.end_feedback);
                            endFeedback.setVisibility(View.VISIBLE);
                            for (QueryTaskInfoServlet.ResponseBO.OutputBO output : end.getOutputs()) {
                                if (output.getType() == 0) {
                                    TextView t1 = (TextView) endFeedback.findViewById(R.id.end_feedback_pic_desc);
                                    t1.setVisibility(View.VISIBLE);
                                    t1.setText("# "+output.getDesc());
                                    if(output.getValue() != null){
                                        ImageView imageView = (ImageView) endFeedback.findViewById(R.id.end_picture);
                                        imageView.setVisibility(View.VISIBLE);
                                        String imageUrl = output.getValue();
                                        Glide.with(AcceptOngoingTaskActivity.this).load(imageUrl).into(imageView);
                                    }

                                } else {
                                    TextView t2 = new TextView(AcceptOngoingTaskActivity.this);
                                    t2.setText("# "+output.getDesc());
                                    endFeedback.addView(t2);
                                    if(null != output.getValue()){
                                        TextView t3 = new TextView(AcceptOngoingTaskActivity.this);
                                        t3.setText(output.getValue());
                                        endFeedback.addView(t3);
                                    }
                                }
                            }
                        }
                    }
                    taskDetailLl.addView(stageTable);
                }

                builder.setView(sv);
                builder.setNegativeButton(AcceptOngoingTaskActivity.this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
    }

    //将workerName列表拼接成字符串
    public String listToString(List<String> stringList){
        if (stringList==null) {
            return null;
        }
        StringBuilder result=new StringBuilder();
        boolean flag=false;
        for (String string : stringList) {
            if (flag) {
                result.append(",");
            }else {
                flag=true;
            }
            result.append(string);
        }
        return result.toString();
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

    }


    @Override
    public void onDestroy() {
        if (mSearch != null) {
            mSearch.destroy();
        }
        super.onDestroy();
    }
    @Override
    public void onResume() {
        super.onResume();
        //注册广播，接收service中启动的线程发送过来的信息，同时更新UI
        registerReceiver(receiver, filter);
    }
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

}
