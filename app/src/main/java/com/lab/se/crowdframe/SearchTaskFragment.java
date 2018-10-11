package com.lab.se.crowdframe;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.lab.se.crowdframe.http.HttpUtil;
import com.lab.se.crowdframe.http.JSONUtils;
import com.lab.se.crowdframe.http.ServletResponseData;
import com.lab.se.crowdframe.servlet.AcceptTaskServlet;
import com.lab.se.crowdframe.servlet.GetRecommendedTaskServlet;
import com.lab.se.crowdframe.servlet.GetTaskInfoServlet;
import com.lab.se.crowdframe.util.Global;


import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Response;


public class SearchTaskFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private OnFragmentInteractionListener mListener;

    // 百度地图控件
    private MapView mMapView = null;

    // 百度地图对象
    private BaiduMap bdMap;
    private double latitude, longitude;
    private String addressLocation,currentCity;
    private GeoCoder search=null;
    private TextView currentAddress;//当前定位地点
    private Button selectLocation;
    private boolean initLocationFlag;//是否已经更新地图位置和周边任务信息
    double radomRange = 0.0001;
    //location service
    LocationBroadCaseReceiver receiver;
    //marker
    List<Marker> markerList = new ArrayList<Marker>();
    BitmapDescriptor unselectedIcon = BitmapDescriptorFactory.fromResource(R.drawable.location_icon_unselected);
    BitmapDescriptor selectedIcon = BitmapDescriptorFactory.fromResource(R.drawable.location_icon_selected);

    GetRecommendedTaskServlet.ResponseBO.TaskBO currentTask;//当前显示的卡片任务
    LinearLayout taskCardLayout;
    private TextView taskCardTitle, taskCardDesc, stageReward, requesterName;//当前的卡片任务相关信息
    List<GetRecommendedTaskServlet.ResponseBO.TaskBO> taskList;
    private Button taskDetail;

    CrowdFrameApplication app;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    //获取日期格式器对象
    DateFormat fmtDate = new java.text.SimpleDateFormat("yyyy-MM-dd");
    DateFormat fmtTime = new java.text.SimpleDateFormat("HH:mm:ss");
    //获取一个日历对象
    Calendar dateAndTime = Calendar.getInstance(Locale.CHINA);

    public SearchTaskFragment() {
        // Required empty public constructor
    }


    public static SearchTaskFragment newInstance(String param1) {
        SearchTaskFragment fragment = new SearchTaskFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initLocationFlag = false;
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_search_task, container, false);
        app = (CrowdFrameApplication)getActivity().getApplication();

        preferences = getActivity().getSharedPreferences("crowdframe", 0);
        editor = preferences.edit();

        //请求授予地理位置权限
        if(ContextCompat.checkSelfPermission((Activity)getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true
            if(ActivityCompat.shouldShowRequestPermissionRationale((Activity)getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder((Activity)getContext()).setTitle("Confirm").setMessage("You denied the location permission.To use the application normally, you need to assign manually!")
                        .show();
            }
            else
                ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        //baidu map
        currentAddress=(TextView) v.findViewById(R.id.current_address);
        selectLocation=(Button) v.findViewById(R.id.select_location);
        mMapView = (MapView) v.findViewById(R.id.bmapview);
        bdMap = mMapView.getMap();
        // 隐藏百度的LOGO
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
        }
        // 不显示地图上比例尺
        mMapView.showScaleControl(false);
        // 不显示地图缩放控件（按钮控制栏）
        mMapView.showZoomControls(false);
        //修改放大比例
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(19);
        bdMap.setMapStatus(msu);
        bdMap.setMyLocationEnabled(true);//只能set一次，否则会导致图标加一次消失


        //下方显示任务内容的卡片
        taskCardLayout = (LinearLayout)v.findViewById(R.id.task_card_ll);
        taskCardTitle = (TextView)v.findViewById(R.id.task_card_title);
        taskCardDesc = (TextView)v.findViewById(R.id.task_card_desc);
        stageReward = (TextView)v.findViewById(R.id.stage_reward);
        requesterName = (TextView)v.findViewById(R.id.requester_name);
        taskDetail = (Button)v.findViewById(R.id.task_detail);
        taskDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTaskDetail();
            }
        });

        //点击任务点触发
        bdMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //获得marker中的taskId,request ongoingTask detail from server
                for(Marker m: markerList){
                    m.setIcon(unselectedIcon);
                }
                marker.setIcon(selectedIcon);
                currentTask = taskList.get(marker.getExtraInfo().getInt("taskIndex"));
                stageReward.setText(String.valueOf((int)currentTask.getStageReward()));
                requesterName.setText(currentTask.getRequester());
                taskCardTitle.setText(currentTask.getTaskTitle());
                taskCardDesc.setText(currentTask.getStageDesc());
                taskCardLayout.setVisibility(View.VISIBLE);
                return true;
            }
        });

        //点击地图其他地方，任务信息卡片消失
        bdMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                for(Marker m: markerList){
                    m.setIcon(unselectedIcon);
                }
                taskCardLayout.setVisibility(View.GONE);
            }
        });

        /**跳转到搜索地址页面**/
        v.findViewById(R.id.select_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("city", "上海");
                startActivityForResult(intent, 0);
            }
        });

        return v;
    }

    //click the "DETAIL" button and get the ongoingTask detail from the server
    public void getTaskDetail(){
        /*获取任务详情*/
        final HashMap<String,String> postData = new HashMap<String, String>();
        final GetTaskInfoServlet.RequestBO requestBO = new GetTaskInfoServlet.RequestBO();
        requestBO.setTaskId(currentTask.getId());
        requestBO.setLatitude(latitude);
        requestBO.setLongitude(longitude);
        postData.put("data", JSONUtils.toJSONString(requestBO));

        String servlet = "GetTaskInfoServlet";
        HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    // response只能call一次
                    ServletResponseData responseData = JSONUtils
                            .toBean(response.body().string(), ServletResponseData.class);
                    int result = responseData.getResult();
                    if(result == 1){
                        GetTaskInfoServlet.ResponseBO responseBO =
                                JSONUtils.toBean(responseData.getData(), GetTaskInfoServlet.ResponseBO.class);
                        showTaskDetail(responseBO);
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

    // show the task detail dialog
    private void showTaskDetail(final GetTaskInfoServlet.ResponseBO responseBO){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                AlertDialog.Builder builder = new Builder(getActivity());//创建对话框
                ScrollView sv = (ScrollView) getActivity().getLayoutInflater().inflate(R.layout.task_detail,null);
                TextView taskTitle = (TextView)sv.findViewById(R.id.task_title);
                taskTitle.setText(responseBO.getTitle());
                TextView taskDesc = (TextView)sv.findViewById(R.id.task_desc);
                taskDesc.setText(responseBO.getDescription());
                TextView taskProgress = (TextView)sv.findViewById(R.id.task_progress);
                taskProgress.setText(responseBO.getProgress());
                TextView taskBonus = (TextView)sv.findViewById(R.id.task_detail_bonus);
                taskBonus.setText(String.valueOf(responseBO.getBonusReward()));
                TextView taskTotalReward = (TextView)sv.findViewById(R.id.task_detail_totalReward);

                //计算总奖励
                List<GetTaskInfoServlet.ResponseBO.StageBO> stages = responseBO.getStages();
                double totalReward = responseBO.getBonusReward();
                for(GetTaskInfoServlet.ResponseBO.StageBO stage : stages){
                    totalReward += stage.getReward();
                }

                taskTotalReward.setText(String.valueOf(totalReward));
                TextView taskDdl = (TextView)sv.findViewById(R.id.task_ddl);
                taskDdl.setText(Global.getTime(responseBO.getDeadline()));
                //task contract time
                Timestamp contractTime = null, wantedDdl = null;


                LinearLayout taskDetailLl = (LinearLayout)sv.findViewById(R.id.task_detail_ll);
                for(int i= 0; i < stages.size(); i++){
                    GetTaskInfoServlet.ResponseBO.StageBO stage = stages.get(i);
                    TableLayout stageTable = (TableLayout)getActivity().getLayoutInflater()
                                                .inflate(R.layout.stage_detail, null);
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
                        if(null != stage.getContractTime()){
                            stageStatusIcon.setBackgroundResource(R.drawable.task_wanted);
                            stageWorkerDesc.setText(getActivity().getString(R.string.detail_worker_num));
                            stageWorkerValue.setText(String.valueOf(stage.getWorkerNum()));
                            stageTimeDesc.setText(getActivity().getString(R.string.contract));
                            stageTimeDesc.setTextColor(Color.RED);
                            stageTimeValue.setText(Global.getTime(stage.getContractTime()));
                            stageTimeValue.setTextColor(Color.RED);
                            contractTime = stage.getContractTime();
                            wantedDdl = stage.getDdl();
                        } else{
                            stageStatusIcon.setBackgroundResource(R.drawable.task_waiting);
                            stageWorkerDesc.setText(getActivity().getString(R.string.detail_worker_num));
                            stageWorkerValue.setText(String.valueOf(stage.getWorkerNum()));
                            stageTimeDesc.setText(getActivity().getString(R.string.stage_ddl));
                            stageTimeValue.setText(stage.getDdl().toString());
                        }

                    } else if(stage.getStageStatus() == 1){
                        stageStatusIcon.setBackgroundResource(R.drawable.task_finished);
                        stageWorkerDesc.setText(getActivity().getString(R.string.detail_worker));
                        stageWorkerValue.setText(listToString(stage.getWorkerNames()));
                        stageTimeDesc.setVisibility(View.GONE);
                        stageTimeValue.setVisibility(View.GONE);
                    } else if(stage.getStageStatus() == 0){
                        stageStatusIcon.setBackgroundResource(R.drawable.my_task_ongoing);
                        stageWorkerDesc.setText(getActivity().getString(R.string.detail_worker));
                        stageWorkerValue.setText(listToString(stage.getWorkerNames()));
                        stageTimeDesc.setVisibility(View.GONE);
                        stageTimeValue.setVisibility(View.GONE);
                    }

                    //start location
                    final GetTaskInfoServlet.ResponseBO.LocationBO start = stage.getLocations().get(0);
                    if(start.getType() != -1){
                        LinearLayout startTitle = (LinearLayout)stageTable.findViewById(R.id.start_title);
                        startTitle.setVisibility(View.VISIBLE);

                        TextView startAddress = (TextView)stageTable.findViewById(R.id.start_location_address);
                        startAddress.setText(start.getAddress().replace("=", "\n" + getActivity().getString(R.string.detail_address)));

                        if(null != start.getInputs() && start.getInputs().size() > 0){
                            TableLayout startExtra  = (TableLayout)stageTable.findViewById(R.id.start_extra_information);
                            startExtra.setVisibility(View.VISIBLE);
                            for(GetTaskInfoServlet.ResponseBO.InputBO input : start.getInputs()){
                                TextView t1 = new TextView(getActivity());
                                t1.setText(input.getDesc()+": ");
                                TextView t2 = new TextView(getActivity());
                                t2.setText("*****");
                                TableRow row = new TableRow(getActivity());
                                row.addView(t1);
                                row.addView(t2);
                                startExtra.addView(row, new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            }
                        }

                        if(null !=  start.getOutputs() && start.getOutputs().size() > 0){
                            TableLayout startFeedback  = (TableLayout)stageTable.findViewById(R.id.start_feedback);
                            startFeedback.setVisibility(View.VISIBLE);
                            for(GetTaskInfoServlet.ResponseBO.OutputBO output : start.getOutputs()){
                                if(output.getType() == 0){ //picture
                                    TextView t1 = (TextView)startFeedback.findViewById(R.id.start_feedback_pic_desc);
                                    t1.setVisibility(View.VISIBLE);
                                    t1.setText("# " + output.getDesc());
                                } else { //text/num/enum
                                    TextView t2 = new TextView(getActivity());
                                    switch(output.getType()){
                                        case 1:
                                            t2.setText(getActivity().getString(R.string.feedback_text)+output.getDesc());
                                            break;
                                        case 2:
                                            t2.setText(getActivity().getString(R.string.feedback_number)+output.getDesc());
                                            break;
                                        case 3:
                                            t2.setText(getActivity().getString(R.string.feedback_enum)+output.getDesc());
                                            break;
                                    }
                                    startFeedback.addView(t2);
                                }
                            }
                        }
                    }

                    //end location
                    final GetTaskInfoServlet.ResponseBO.LocationBO end = stage.getLocations().get(1);
                    if(end.getType() != -1) {
                        LinearLayout endTitle = (LinearLayout) stageTable.findViewById(R.id.end_title);
                        endTitle.setVisibility(View.VISIBLE);

                        TextView endAddress = (TextView) stageTable.findViewById(R.id.end_location_address);
                        endAddress.setText(end.getAddress().replace("=",  "\n" + getActivity().getString(R.string.detail_address)));

                        if(null != end.getInputs() && end.getInputs().size() > 0){
                            TableLayout endExtra  = (TableLayout)stageTable.findViewById(R.id.end_extra_information);
                            endExtra.setVisibility(View.VISIBLE);

                            for(GetTaskInfoServlet.ResponseBO.InputBO input : end.getInputs()){
                                TextView t1 = new TextView(getActivity());
                                t1.setText(input.getDesc()+": ");
                                TextView t2 = new TextView(getActivity());
                                t2.setText("*****");
                                TableRow row = new TableRow(getActivity());
                                row.addView(t1);
                                row.addView(t2);
                                endExtra.addView(row, new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            }
                        }

                        if (null != end.getOutputs() && end.getOutputs().size() > 0) {
                            TableLayout endFeedback = (TableLayout) stageTable.findViewById(R.id.end_feedback);
                            endFeedback.setVisibility(View.VISIBLE);
                            for (GetTaskInfoServlet.ResponseBO.OutputBO output : end.getOutputs()) {
                                if (output.getType() == 0) {
                                    TextView t1 = (TextView) endFeedback.findViewById(R.id.end_feedback_pic_desc);
                                    t1.setVisibility(View.VISIBLE);
                                    t1.setText("# " + output.getDesc());

                                } else {//text/num/enum
                                    TextView t2 = new TextView(getActivity());
                                    switch(output.getType()){
                                        case 1:
                                            t2.setText(getActivity().getString(R.string.feedback_text)+output.getDesc());
                                            break;
                                        case 2:
                                            t2.setText(getActivity().getString(R.string.feedback_number)+output.getDesc());
                                            break;
                                        case 3:
                                            t2.setText(getActivity().getString(R.string.feedback_enum)+output.getDesc());
                                            break;
                                    }
                                    endFeedback.addView(t2);
                                }
                            }
                        }
                    }

                    taskDetailLl.addView(stageTable);
                }

                final Timestamp finalContract = contractTime;
                final Timestamp finalDdl = wantedDdl;

                builder.setView(sv);
                builder.setPositiveButton(((Activity)getContext()).getString(R.string.accept_task), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {

                        LinearLayout selectContract = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.select_contract_time,null);
                        final TextView txtDate =(TextView)selectContract.findViewById(R.id.set_date);
                        txtDate.setClickable(true);
                        txtDate.setFocusable(true);
                        final TextView txtTime =(TextView)selectContract.findViewById(R.id.set_time);
                        txtTime.setClickable(true);
                        txtTime.setFocusable(true);
                        txtDate.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v){
                                DatePickerDialog dateDlg = new DatePickerDialog(getActivity(),
                                        new DatePickerDialog.OnDateSetListener(){
                                            @Override
                                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                                //修改日历控件的年，月，日
                                                //这里的year,monthOfYear,dayOfMonth的值与DatePickerDialog控件设置的最新值一致
                                                dateAndTime.set(Calendar.YEAR, year);
                                                dateAndTime.set(Calendar.MONTH, monthOfYear);
                                                dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                                txtDate.setText(fmtDate.format(dateAndTime.getTime()));
                                            }
                                        },
                                        dateAndTime.get(Calendar.YEAR),
                                        dateAndTime.get(Calendar.MONTH),
                                        dateAndTime.get(Calendar.DAY_OF_MONTH));
                                dateDlg.show();
                            }
                        });

                        txtTime.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v){
                                TimePickerDialog timeDlg = new TimePickerDialog(getActivity(),
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                                dateAndTime.set(Calendar.MINUTE, minute);
                                                txtTime.setText(fmtTime.format(dateAndTime.getTime()).substring(0,fmtTime.format(dateAndTime.getTime()).length()-3)+":00");
                                            }
                                        },
                                        dateAndTime.get(Calendar.HOUR_OF_DAY),
                                        dateAndTime.get(Calendar.MINUTE),
                                        true);
                                timeDlg.show();
                            }
                        });
                        txtDate.setText(finalContract.toString().split(" ")[0]);
                        txtTime.setText(finalContract.toString().split(" ")[1]);

                        new AlertDialog.Builder(getContext())
                                .setView(selectContract)
                                .setPositiveButton(getActivity().getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog2, int which) {
                                        if(Timestamp.valueOf(txtDate.getText()+" "+txtTime.getText()).getTime() <= finalDdl.getTime()){
                                            //to accept the task
                                            final HashMap<String,String> postData = new HashMap<String, String>();
                                            final AcceptTaskServlet.RequestBO requestBO = new AcceptTaskServlet.RequestBO();
                                            requestBO.setUserId(app.getUserId());
                                            requestBO.setTaskId(currentTask.getId());
                                            requestBO.setCurrentStage(currentTask.getCurrentStage());
                                            requestBO.setStartTime(new Timestamp(new Date().getTime()));
                                            requestBO.setContractTime(Timestamp.valueOf(txtDate.getText()+" "+txtTime.getText()));//make contract
                                            postData.put("data", JSONUtils.toJSONString(requestBO));

                                            String servlet = "AcceptTaskServlet";
                                            HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
                                                @Override
                                                public void onResponse(Call call, Response response) throws IOException {
                                                    try {
                                                        // response只能call一次
                                                        ServletResponseData responseData = JSONUtils
                                                                .toBean(response.body().string(), ServletResponseData.class);
                                                        int result = responseData.getResult();
                                                        acceptTaskTip(result);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                @Override
                                                public void onFailure(Call call, IOException e) {

                                                }
                                            });
                                            dialog2.dismiss();

                                        } else {
                                            new AlertDialog.Builder(getContext()).setMessage(((Activity)getContext()).getString(R.string.wrong_contract)).show();
//                                            Toast.makeText(getActivity(), ((Activity)getContext()).getString(R.string.wrong_contract), Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                })
                                .setNegativeButton(((Activity)getContext()).getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog2, int which) {
                                        dialog2.dismiss();
                                    }
                                }).create().show();
                    }
                });
                builder.setNegativeButton(((Activity)getContext()).getString(R.string.cancel), new DialogInterface.OnClickListener() {
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


    //show the user whether he accept the ongoingTask successfully
    private void acceptTaskTip(final int result){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(result == 1){
                    taskCardLayout.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), getActivity().getString(R.string.accept_successfully), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.accept_fail), Toast.LENGTH_SHORT).show();
                }
                getTaskList();
            }
        });
    }

    /**经纬度地址动画显示在屏幕中间**/
    private void location(double latitude,double longitude){
        LatLng ll = new LatLng(latitude, longitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(ll);
        bdMap.animateMapStatus(msu);
        //设置现在所在的地址
        currentAddress.setText(addressLocation);

        //获取任务列表
        getTaskList();
    }

    //获取任务列表
    private  void getTaskList(){
        /*get任务列表*/
        final HashMap<String,String> postData = new HashMap<String, String>();
        GetRecommendedTaskServlet.RequestBO requestBO = new GetRecommendedTaskServlet.RequestBO();
        requestBO.setUserId(app.getUserId());
        requestBO.setLatitude(latitude);
        requestBO.setLongitude(longitude);
        postData.put("data", JSONUtils.toJSONString(requestBO));

        String servlet = "GetRecommendedTaskServlet";
        HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    // response只能call一次
                    ServletResponseData responseData = JSONUtils
                            .toBean(response.body().string(), ServletResponseData.class);

                    int result = responseData.getResult();
                    if(result == 1){
                        GetRecommendedTaskServlet.ResponseBO responseBO =
                                JSONUtils.toBean(responseData.getData(), GetRecommendedTaskServlet.ResponseBO.class);
                        taskList = responseBO.getTasks();
                        addTaskMarker();
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


    //安卓不允许在子线程中进行UI操作，所以要借助于runOnUiThread方法
    private void addTaskMarker() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                bdMap.clear();
                LatLng latLng = null;
                OverlayOptions overlayOptions = null;
                Random r = new Random();
                for (int i = 0; null != taskList && i < taskList.size(); i++) {
                    GetRecommendedTaskServlet.ResponseBO.TaskBO task = taskList.get(i);
                    double lat = task.getLatitude();
                    double lng = task.getLongitude();
                    //random (-0.5 ~ 0.5)* randomRange ratio
                    lat += (r.nextDouble() - 0.5) * radomRange;
                    lng += (r.nextDouble() - 0.5) * radomRange;
                    // 位置
                    latLng = new LatLng(lat, lng);
                    // 图标
                    overlayOptions = new MarkerOptions().position(latLng)
                            .icon(unselectedIcon);
                    Marker marker = (Marker) (bdMap.addOverlay(overlayOptions));
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("taskIndex", i);
                    marker.setExtraInfo(bundle);
                    markerList.add(marker);
                }
            }
        });
    }


    //receive the location message
    class LocationBroadCaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            latitude = intent.getDoubleExtra("lat",0);
            longitude = intent.getDoubleExtra("lng",0);
            editor.putFloat("latitude",(float)latitude);
            editor.putFloat("longitude",(float)longitude);
            editor.commit();

            addressLocation=intent.getStringExtra("address");
            LatLng ll = new LatLng(latitude, longitude);
            MyLocationData locationData=new MyLocationData.Builder()
                    .accuracy(intent.getFloatExtra("radius",0))
                    .direction(100).latitude(latitude)
                    .longitude(longitude).build();
            bdMap.setMyLocationData(locationData);

            if(initLocationFlag == false){//是否刷新，只有第一次启动或者周围任务点数据变化hasRefresh才设为False
                location(latitude, longitude);
                initLocationFlag = true;
            }

        }
    }

    /**根据搜索页面地名的经纬度定位**/
    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==0){
            if(resultCode==1){
                addressLocation=data.getStringExtra("address");
                location(Double.parseDouble(data.getStringExtra("latitude")),Double.parseDouble(data.getStringExtra("longitude")));
            }
        }
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    //百度地图生命周期管理
    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        //注册广播，接收service中启动的线程发送过来的信息，同时更新UI
        IntentFilter filter = new IntentFilter("com.lab.se.updateLocation");
        receiver = new LocationBroadCaseReceiver();
        getActivity().registerReceiver(receiver, filter);
    }

    public static void onResume2(){
        Log.d("fzj2","template fragment onResume2Called");
    }
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
        mMapView.onPause();
    }

}