package com.lab.se.crowdframe;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.lab.se.crowdframe.entity.OngoingTask;
import com.bumptech.glide.Glide;
import com.lab.se.crowdframe.entity.SysApplication;
import com.lab.se.crowdframe.http.HttpUtil;
import com.lab.se.crowdframe.http.JSONUtils;
import com.lab.se.crowdframe.http.ServletResponseData;
import com.lab.se.crowdframe.servlet.CancelTaskServlet;
import com.lab.se.crowdframe.servlet.GetAcceptedTaskServlet;
import com.lab.se.crowdframe.servlet.GetCompletedTaskServlet;
import com.lab.se.crowdframe.servlet.GetPublishedTaskServlet;
import com.lab.se.crowdframe.servlet.GiveCreditServlet;
import com.lab.se.crowdframe.servlet.QueryTaskInfoServlet;
import com.lab.se.crowdframe.util.Global;


import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

public class TaskListActivity extends AppCompatActivity {

    List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();
    ListView taskListView;
    Toolbar mToolbar;
    BaseAdapter sa;
    TextView noData;
    Map<Integer,OngoingTask> taskList = new HashMap<Integer,OngoingTask>();

    CrowdFrameApplication app;

    //点击的是哪个task的哪个stage
//    int stageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        SysApplication.getInstance().addActivity(this);

        app = (CrowdFrameApplication)getApplication();
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

        taskListView = (ListView)findViewById(R.id.task_listview);
        noData = (TextView)findViewById(R.id.noData);


        int type = getIntent().getIntExtra("type",0);
        switch (type){
            case 1:
                initAcceptedOngoingTask();
                break;
            case 2:
                initAcceptedHistoryTask();
                break;
            case 3:
                initPubllishedOngoingTask();
                break;
            case 4:
                initPubllishedHistoryTask();
                break;
            default:
                Toast.makeText(this, "error!",Toast.LENGTH_SHORT);
                break;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    private void initAcceptedOngoingTask(){
        /*已接受的正在进行中的任务*/
        final HashMap<String,String> postData = new HashMap<String, String>();
        final GetAcceptedTaskServlet.RequestBO requestBO = new GetAcceptedTaskServlet.RequestBO();
        requestBO.setUserId(app.getUserId());
        postData.put("data", JSONUtils.toJSONString(requestBO));

        String servlet = "GetAcceptedTaskServlet";
        HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    // response只能call一次
                    ServletResponseData responseData = JSONUtils
                            .toBean(response.body().string(), ServletResponseData.class);
                    int result = responseData.getResult();
                    if(result == 1){
                        GetAcceptedTaskServlet.ResponseBO responseBO = JSONUtils.toBean(responseData.getData(),
                                GetAcceptedTaskServlet.ResponseBO.class);
                        initTaskMap(responseBO.getTasks());
                        //任务升序排序
                        Collections.sort(responseBO.getTasks(), new Comparator<GetAcceptedTaskServlet.ResponseBO.TaskBO>() {
                            @Override
                            public int compare(GetAcceptedTaskServlet.ResponseBO.TaskBO o1, GetAcceptedTaskServlet.ResponseBO.TaskBO o2) {
                                Timestamp t1 = o1.getContract();
                                Timestamp t2 = o2.getContract();
                                Long time1 = t1.getTime();
                                Long time2 = t2.getTime();
                                return time1.compareTo(time2);
                            }
                        });
                        sa = new AcceptedOngoingAdapter(TaskListActivity.this, responseBO.getTasks());
                        showList();
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

    public void initTaskMap(List<GetAcceptedTaskServlet.ResponseBO.TaskBO> list){
        for(GetAcceptedTaskServlet.ResponseBO.TaskBO bo: list){
            GetAcceptedTaskServlet.ResponseBO.LocationBO s = bo.getLocations().get(0);
            List<OngoingTask.Input> inputs = new ArrayList<OngoingTask.Input>();
            if(null != s.getInputs() && s.getInputs().size() > 0){
                for(GetAcceptedTaskServlet.ResponseBO.InputBO in : s.getInputs()){
                    OngoingTask.Input input = new OngoingTask.Input(in.getId(),in.getActionId(),
                            in.getType(), in.getDesc(), in.getValue());
                    inputs.add(input);
                }
            }

            List<OngoingTask.Output> outputs = new ArrayList<OngoingTask.Output>();
            if(null != s.getOutputs() && s.getOutputs().size() > 0){
                for(GetAcceptedTaskServlet.ResponseBO.OutputBO out : s.getOutputs()){
                    OngoingTask.Output output = new OngoingTask.Output(out.getId(),out.getActionId(),
                            out.getType(), out.getDesc(), out.getValue(), out.isActive(), out.getIntervalValue(),
                            out.getUpBound(), out.getLowBound(), out.getEntries(), out.getAggregaMethod());
                    outputs.add(output);
                }
            }

            OngoingTask.Location start = new OngoingTask.Location(s.getId(), s.getStageId(), s.getAddress(),
                    s.getLongitude(), s.getLatitude(), s.getType(), inputs, outputs);
            Log.i("111","start1:"+s.getType());

            GetAcceptedTaskServlet.ResponseBO.LocationBO e = bo.getLocations().get(1);
            List<OngoingTask.Input> inputs2 = new ArrayList<OngoingTask.Input>();
            if(null != e.getInputs() && e.getInputs().size() > 0){
                for(GetAcceptedTaskServlet.ResponseBO.InputBO in : e.getInputs()){
                    OngoingTask.Input input = new OngoingTask.Input(in.getId(),in.getActionId(),
                            in.getType(), in.getDesc(), in.getValue());
                    inputs2.add(input);
                }
            }

            List<OngoingTask.Output> outputs2 = new ArrayList<OngoingTask.Output>();
            if(null != e.getOutputs() && e.getOutputs().size() > 0){
                for(GetAcceptedTaskServlet.ResponseBO.OutputBO out : e.getOutputs()){
                    OngoingTask.Output output = new OngoingTask.Output(out.getId(),out.getActionId(),
                            out.getType(), out.getDesc(), out.getValue(), out.isActive(),
                            out.getIntervalValue(), out.getUpBound(), out.getLowBound(),
                            out.getEntries(), out.getAggregaMethod());
                    outputs2.add(output);
                }
            }

            OngoingTask.Location end = new OngoingTask.Location(e.getId(), e.getStageId(), e.getAddress(),
                    e.getLongitude(), e.getLatitude(), e.getType(), inputs2, outputs2);

            List<OngoingTask.Location> locations = new ArrayList<OngoingTask.Location>();
            locations.add(start);
            locations.add(end);
            OngoingTask t = new OngoingTask(bo.getTaskId(),bo.getTaskTitle(), bo.getTaskDesc(),
                    bo.getTaskProgress(), bo.getCurrentStage(),
                    bo.getBonusReward(),bo.getTaskDeadline(),
                    bo.getStageId(), bo.getStageName(), bo.getStageDesc(),
                    bo.getReward(), bo.getDdl(), bo.getContract(), locations);
            taskList.put(t.getTaskId(), t);
        }
    }

    //我接受的正在进行中的任务adapter
    private class AcceptedOngoingAdapter extends BaseAdapter {
        private Context mContext;
        private List<GetAcceptedTaskServlet.ResponseBO.TaskBO> mList;


        public  AcceptedOngoingAdapter(Context context, List<GetAcceptedTaskServlet.ResponseBO.TaskBO> list) {
            mContext = context;
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isEmpty(){
            return mList.size() == 0;
        }

        @Override
        public GetAcceptedTaskServlet.ResponseBO.TaskBO getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(mContext).inflate(R.layout.accept_ongoing_task, null);
            }

            final LinearLayout l = (LinearLayout)convertView;
            l.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        OngoingTask t = taskList.get(Integer.valueOf(getItem(position).getTaskId()));
                        Intent i = new Intent(TaskListActivity.this, AcceptOngoingTaskActivity.class);
                        i.putExtra("ongoingTask", t);
                        startActivityForResult(i, 6);
                    } catch (Exception e){
                        Toast.makeText(TaskListActivity.this,
                                "There is something wrong!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            final GetAcceptedTaskServlet.ResponseBO.TaskBO t = getItem(position);

            TextView taskId = (TextView) l.findViewById(R.id.hidden_task_id);
            taskId.setText(String.valueOf(t.getTaskId()));

            TextView stageDesc = (TextView) l.findViewById(R.id.stage_desc);
            stageDesc.setText(t.getStageDesc());

            TextView  currentStage = (TextView) l.findViewById(R.id.current_stage);
            currentStage.setText(t.getTaskProgress());
            TextView tv = (TextView) l.findViewById(R.id.task_title);
            tv.setText(t.getTaskTitle());
            TextView  ddl= (TextView) l.findViewById(R.id.contract_time);
            ddl.setText(Global.getTime(t.getContract()));
            return l;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 6 && resultCode == 7){
            initAcceptedOngoingTask();
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////

    //我接受的已完成的任务
    private void initAcceptedHistoryTask(){
         /*已接受的正在进行中的任务*/
        final HashMap<String,String> postData = new HashMap<String, String>();
        final GetCompletedTaskServlet.RequestBO requestBO = new GetCompletedTaskServlet.RequestBO();
        requestBO.setUserId(app.getUserId());
        postData.put("data", JSONUtils.toJSONString(requestBO));

        String servlet = "GetCompletedTaskServlet";
        HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    // response只能call一次
                    ServletResponseData responseData = JSONUtils
                            .toBean(response.body().string(), ServletResponseData.class);
                    int result = responseData.getResult();
                    if(result == 1){
                        GetCompletedTaskServlet.ResponseBO responseBO = JSONUtils.toBean(responseData.getData(),
                                GetCompletedTaskServlet.ResponseBO.class);
                        //任务降序排序
                        Collections.sort(responseBO.getTasks(), new Comparator<GetCompletedTaskServlet.ResponseBO.TaskBO>() {
                            @Override
                            public int compare(GetCompletedTaskServlet.ResponseBO.TaskBO o1, GetCompletedTaskServlet.ResponseBO.TaskBO o2) {
                                Timestamp t1 = o1.getFinishTime();
                                Timestamp t2 = o2.getFinishTime();
                                Long time1 = t1.getTime();
                                Long time2 = t2.getTime();
                                return time2.compareTo(time1);
                            }
                        });
                        sa = new AcceptedHistoryAdapter(TaskListActivity.this, responseBO.getTasks());
                        showList();
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

    //我接受的已经完成的任务adapter
    private class AcceptedHistoryAdapter extends BaseAdapter {
        private Context mContext;
        private List<GetCompletedTaskServlet.ResponseBO.TaskBO> mList;

        public  AcceptedHistoryAdapter(Context context, List<GetCompletedTaskServlet.ResponseBO.TaskBO> list) {
            mContext = context;
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public GetCompletedTaskServlet.ResponseBO.TaskBO getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean isEmpty(){
            return mList.size() == 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(mContext).inflate(R.layout.accept_history_task, null);
            }
            final GetCompletedTaskServlet.ResponseBO.TaskBO t = getItem(position);
            LinearLayout l = (LinearLayout)convertView;
            TextView  stageDesc = (TextView) l.findViewById(R.id.stage_desc);
            stageDesc.setText(t.getStageDesc());
            LinearLayout clickableLinear = (LinearLayout)l.findViewById(R.id.clickableLinear);
            clickableLinear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getTaskDetail(t.getTaskId(), 0);
                }
            });

            TextView stageStatus = (TextView)l.findViewById(R.id.stage_status);
            TextView finishTime = (TextView)l.findViewById(R.id.finish_time);
            TextView finishTimeLabel = (TextView)l.findViewById(R.id.finish_time_label);
            if(t.getStageStatus() == 1){
                stageStatus.setText(TaskListActivity.this.getString(R.string.finished));
                finishTime.setVisibility(View.VISIBLE);
                finishTimeLabel.setVisibility(View.VISIBLE);
                finishTime.setText(Global.getTime(t.getFinishTime()));
            } else if(t.getStageStatus() == -1){
                stageStatus.setText(TaskListActivity.this.getString(R.string.expired));
                finishTime.setVisibility(View.GONE);
                finishTimeLabel.setVisibility(View.GONE);
            } else if(t.getStageStatus() == 5){
                stageStatus.setText(TaskListActivity.this.getString(R.string.credit_given));
                finishTime.setVisibility(View.VISIBLE);
                finishTimeLabel.setVisibility(View.VISIBLE);
                finishTime.setText(Global.getTime(t.getFinishTime()));
            }

            TextView reward = (TextView)l.findViewById(R.id.reward);
            reward.setText(String.valueOf(t.getReward()));

            TextView taskProgress = (TextView)l.findViewById(R.id.task_progress);
            taskProgress.setText(t.getTaskProgress());

            return l;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    //我发布的正在进行任务
    private void initPubllishedOngoingTask(){
        final HashMap<String,String> postData = new HashMap<String, String>();
        final GetPublishedTaskServlet.RequestBO requestBO = new GetPublishedTaskServlet.RequestBO();
        requestBO.setUserId(app.getUserId());
        postData.put("data", JSONUtils.toJSONString(requestBO));

        String servlet = "GetPublishedTaskServlet";
        HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    // response只能call一次
                    ServletResponseData responseData = JSONUtils
                            .toBean(response.body().string(), ServletResponseData.class);
                    int result = responseData.getResult();
                    if(result == 1){
                        GetPublishedTaskServlet.ResponseBO responseBO = JSONUtils.toBean(responseData.getData(),
                                GetPublishedTaskServlet.ResponseBO.class);
                        List<GetPublishedTaskServlet.ResponseBO.TaskBO> list = new ArrayList<GetPublishedTaskServlet.ResponseBO.TaskBO>();
                        for(GetPublishedTaskServlet.ResponseBO.TaskBO task : responseBO.getTasks()){
                            if(task.getStatus() == 0){
                                list.add(task);
                            }
                        }
                        //任务降序排序
                        Collections.sort(list, new Comparator<GetPublishedTaskServlet.ResponseBO.TaskBO>() {
                            @Override
                            public int compare(GetPublishedTaskServlet.ResponseBO.TaskBO o1, GetPublishedTaskServlet.ResponseBO.TaskBO o2) {
                                Timestamp t1 = o1.getPublishTime();
                                Timestamp t2 = o2.getPublishTime();
                                Long time1 = t1.getTime();
                                Long time2 = t2.getTime();
                                return time2.compareTo(time1);
                            }
                        });
                        sa = new PublishedOngoingAdapter(TaskListActivity.this, list);
                        showList();
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

    //我发布的正在进行中的任务adapter
    private class PublishedOngoingAdapter extends BaseAdapter {
        private Context mContext;
        private List<GetPublishedTaskServlet.ResponseBO.TaskBO> mList;

        public  PublishedOngoingAdapter(Context context, List<GetPublishedTaskServlet.ResponseBO.TaskBO> list) {
            mContext = context;
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isEmpty(){
            return mList.size() == 0;
        }

        @Override
        public GetPublishedTaskServlet.ResponseBO.TaskBO getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(mContext).inflate(R.layout.publish_ongoing_task, null);
            }
            final GetPublishedTaskServlet.ResponseBO.TaskBO t = getItem(position);
            LinearLayout l = (LinearLayout)convertView;
            TextView  taskTitle = (TextView) l.findViewById(R.id.task_title);

            taskTitle.setText(t.getTitle());
            LinearLayout clickableLinear = (LinearLayout)l.findViewById(R.id.clickableLinear);
            clickableLinear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getTaskDetail(t.getId(), 1);
                }
            });

            TextView taskDesc = (TextView)l.findViewById(R.id.task_desc);
            taskDesc.setText(t.getDescription());
            TextView progress = (TextView)l.findViewById(R.id.progress);
            progress.setText(t.getProgress());
            TextView publishTime = (TextView)l.findViewById(R.id.publish_time);
            publishTime.setText(Global.getTime(t.getPublishTime()));

            //requester 终止任务
            Button btn = (Button)l.findViewById(R.id.cancel_task);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(TaskListActivity.this);//创建对话框
                    builder.setMessage(TaskListActivity.this.getString(R.string.requester_abort_task));
                    builder.setTitle(TaskListActivity.this.getString(R.string.abort_task));
                    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final HashMap<String,String> postData = new HashMap<String, String>();
                            final CancelTaskServlet.RequestBO requestBO = new CancelTaskServlet.RequestBO();
                            requestBO.setUserId(app.getUserId());
                            requestBO.setTaskId(t.getId());
                            postData.put("data", JSONUtils.toJSONString(requestBO));
                            Log.i("111","==========before the post===========");
                            String servlet = "CancelTaskServlet";
                            HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    try {
                                        // response只能call一次
                                        ServletResponseData responseData = JSONUtils
                                                .toBean(response.body().string(), ServletResponseData.class);
                                        int result = responseData.getResult();
                                        Log.i("111","get the result="+result);
                                        if(result == 1){
                                            showReturnMessage(TaskListActivity.this.getString(R.string.cancel_task_success));
                                            finish();
                                        } else if(result == 2){
                                            showReturnMessage(TaskListActivity.this.getString(R.string.cannot_cancel_task));
                                        } else {
                                            showReturnMessage(TaskListActivity.this.getString(R.string.cancel_task_error));
                                        }
                                    } catch (Exception e) {
                                        Log.i("111","==========exception===========");
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Log.i("111","==========on failure===========");
                                }
                            });
                        }
                    });
                    builder.create().show();
                }
            });

            return l;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    //我发布的已完成的任务
    private void initPubllishedHistoryTask(){
        final HashMap<String,String> postData = new HashMap<String, String>();
        final GetPublishedTaskServlet.RequestBO requestBO = new GetPublishedTaskServlet.RequestBO();
        requestBO.setUserId(app.getUserId());
        postData.put("data", JSONUtils.toJSONString(requestBO));

        String servlet = "GetPublishedTaskServlet";
        HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    // response只能call一次
                    ServletResponseData responseData = JSONUtils
                            .toBean(response.body().string(), ServletResponseData.class);
                    int result = responseData.getResult();
                    if(result == 1){
                        GetPublishedTaskServlet.ResponseBO responseBO = JSONUtils.toBean(responseData.getData(),
                                GetPublishedTaskServlet.ResponseBO.class);
                        List<GetPublishedTaskServlet.ResponseBO.TaskBO> list = new ArrayList<GetPublishedTaskServlet.ResponseBO.TaskBO>();
                        for(GetPublishedTaskServlet.ResponseBO.TaskBO task : responseBO.getTasks()){
                            if(task.getStatus() != 0){
                                list.add(task);
                            }
                        }
                        //任务降序排序
                        Collections.sort(list, new Comparator<GetPublishedTaskServlet.ResponseBO.TaskBO>() {
                            @Override
                            public int compare(GetPublishedTaskServlet.ResponseBO.TaskBO o1, GetPublishedTaskServlet.ResponseBO.TaskBO o2) {
                                Timestamp t1 = o1.getPublishTime();
                                Timestamp t2 = o2.getPublishTime();
                                Long time1 = t1.getTime();
                                Long time2 = t2.getTime();
                                return time2.compareTo(time1);
                            }
                        });
                        sa = new PublishedHistoryAdapter(TaskListActivity.this, list);
                        showList();
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


    //我发布的已完成的任务adapter
    private class PublishedHistoryAdapter extends BaseAdapter {
        private Context mContext;
        private List<GetPublishedTaskServlet.ResponseBO.TaskBO> mList;

        public  PublishedHistoryAdapter(Context context, List<GetPublishedTaskServlet.ResponseBO.TaskBO> list) {
            mContext = context;
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isEmpty(){
            return mList.size() == 0;
        }

        @Override
        public GetPublishedTaskServlet.ResponseBO.TaskBO getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(mContext).inflate(R.layout.publish_history_task, null);
            }
            final GetPublishedTaskServlet.ResponseBO.TaskBO t = getItem(position);
            LinearLayout l = (LinearLayout)convertView;
            TextView  taskTitle = (TextView) l.findViewById(R.id.task_title);
            taskTitle.setText(t.getTitle());
            LinearLayout clickableLinear = (LinearLayout)l.findViewById(R.id.clickableLinear);
            clickableLinear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getTaskDetail(t.getId(), 1);
                }
            });

            TextView taskDesc = (TextView)l.findViewById(R.id.task_desc);
            taskDesc.setText(t.getDescription());
            TextView taskStatus = (TextView)l.findViewById(R.id.task_status);
            if(t.getStatus() == 5){
                taskStatus.setText(TaskListActivity.this.getString(R.string.requester_give_credit));
            } else if(t.getStatus() == -1){
                taskStatus.setText(TaskListActivity.this.getString(R.string.expired));
            }  else if(t.getStatus() == 1){
                taskStatus.setText(TaskListActivity.this.getString(R.string.finished));
                Button bn = (Button)l.findViewById(R.id.give_credit);
                bn.setVisibility(View.VISIBLE);
                bn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(TaskListActivity.this);//创建对话框
                        builder.setMessage(TaskListActivity.this.getString(R.string.confirm_give_credit));
                        builder.setTitle(TaskListActivity.this.getString(R.string.give_credit));
                        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final HashMap<String,String> postData = new HashMap<String, String>();
                                final GiveCreditServlet.RequestBO requestBO = new GiveCreditServlet.RequestBO();
                                requestBO.setUserId(app.getUserId());
                                requestBO.setTaskId(t.getId());
                                postData.put("data", JSONUtils.toJSONString(requestBO));
                                Log.i("111","==========before the post===========");
                                String servlet = "GiveCreditServlet";
                                HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        try {
                                            // response只能call一次
                                            ServletResponseData responseData = JSONUtils
                                                    .toBean(response.body().string(), ServletResponseData.class);
                                            int result = responseData.getResult();
                                            Log.i("111","get the result="+result);
                                            if(result == 1){
                                                showReturnMessage(TaskListActivity.this.getString(R.string.give_credit_success));
                                                finish();
                                            } else {
                                                showReturnMessage(TaskListActivity.this.getString(R.string.give_credit_error));
                                            }
                                        } catch (Exception e) {
                                            Log.i("111","==========exception===========");
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        Log.i("111","==========on failure===========");
                                    }
                                });
                            }
                        });
                        builder.create().show();
                    }
                });
            }


            return l;
        }
    }



    //////////////////////////list items all use these methods///////////////////////////////////////////////

    private void showList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                taskListView.setAdapter(sa);
                if(sa.isEmpty()){
                    taskListView.setVisibility(View.GONE);
                    noData.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    //安卓不允许在子线程中进行UI操作，所以要借助于runOnUiThread方法
    private void showReturnMessage(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                Toast.makeText(TaskListActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });
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
                    Log.d("lwh912","The error message is " + e.getMessage());
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
                AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TaskListActivity.this);//创建对话框
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
                        stageWorkerDesc.setText(TaskListActivity.this.getString(R.string.detail_worker_num));
                        stageWorkerValue.setText(String.valueOf(stage.getWorkerNum()));
                        if(null != listToString(stage.getWorkerNames())){
                            TableRow r = (TableRow)stageTable.findViewById(R.id.has_worker_row);
                            r.setVisibility(View.VISIBLE);
                            TextView hasWorkerValue = (TextView)stageTable.findViewById(R.id.stage_has_worker_value);
                            hasWorkerValue.setText(listToString(stage.getWorkerNames()));
                        }
                        stageTimeDesc.setText(TaskListActivity.this.getString(R.string.stage_ddl));
                        stageTimeValue.setText(Global.getTime(stage.getDdl()));
                    } else if(stage.getStageStatus() == 1){
                        stageStatusIcon.setBackgroundResource(R.drawable.task_finished);
                        stageWorkerDesc.setText(TaskListActivity.this.getString(R.string.detail_worker));
                        stageWorkerValue.setText(listToString(stage.getWorkerNames()));
                        stageTimeDesc.setVisibility(View.GONE);
                        stageTimeValue.setVisibility(View.GONE);

                    } else if(stage.getStageStatus() == 0){
                        stageStatusIcon.setBackgroundResource(R.drawable.my_task_ongoing);
                        stageWorkerDesc.setText(TaskListActivity.this.getString(R.string.detail_worker));
                        stageWorkerValue.setText(listToString(stage.getWorkerNames()));
                        stageTimeDesc.setVisibility(View.GONE);
                        stageTimeValue.setVisibility(View.GONE);
                    } else if(stage.getStageStatus() == -1){
                        stageStatusIcon.setBackgroundResource(R.drawable.expired);
                        stageWorkerDesc.setVisibility(View.GONE);
                        stageWorkerValue.setVisibility(View.GONE);
                        stageTimeDesc.setVisibility(View.GONE);
                        stageTimeValue.setVisibility(View.GONE);
                    }

                    //start location
                    final QueryTaskInfoServlet.ResponseBO.LocationBO start = stage.getLocations().get(0);
                    if(start.getType() != -1){
                        LinearLayout startTitle = (LinearLayout)stageTable.findViewById(R.id.start_title);
                        startTitle.setVisibility(View.VISIBLE);

                        TextView startAddress = (TextView)stageTable.findViewById(R.id.start_location_address);
                        startAddress.setText(start.getAddress().replace("=", "\n" + TaskListActivity.this.getString(R.string.detail_address)));

                        if(null != start.getInputs() && start.getInputs().size() > 0){
                            TableLayout startExtra  = (TableLayout)stageTable.findViewById(R.id.start_extra_information);
                            startExtra.setVisibility(View.VISIBLE);
                            for(QueryTaskInfoServlet.ResponseBO.InputBO input : start.getInputs()){
                                TextView t1 = new TextView(TaskListActivity.this);
                                t1.setText(input.getDesc()+": ");

                                TextView t2 = new TextView(TaskListActivity.this);
                                if(showType == 0){
                                    t2.setText("*****");
                                } else {
                                    t2.setText(input.getValue());
                                }
                                TableRow row = new TableRow(TaskListActivity.this);
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
                                    t1.setText("# " + output.getDesc());
                                    if(output.getValue() != null){
                                        ImageView imageView = (ImageView) startFeedback.findViewById(R.id.start_picture);
                                        imageView.setVisibility(View.VISIBLE);
                                        String imageUrl = output.getValue();
                                        Glide.with(TaskListActivity.this).load(imageUrl).into(imageView);
                                    }
                                } else {
                                    TextView t2 = new TextView(TaskListActivity.this);
                                    t2.setText("# " + output.getDesc());
                                    startFeedback.addView(t2);
                                    if(null != output.getValue()){
                                        TextView t3 = new TextView(TaskListActivity.this);
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
                        endAddress.setText(end.getAddress().replace("=", "\n" + TaskListActivity.this.getString(R.string.detail_address)));

                        if(null != end.getInputs() && end.getInputs().size() > 0){
                            TableLayout endExtra  = (TableLayout)stageTable.findViewById(R.id.end_extra_information);
                            endExtra.setVisibility(View.VISIBLE);

                            for(QueryTaskInfoServlet.ResponseBO.InputBO input : end.getInputs()){
                                TextView t1 = new TextView(TaskListActivity.this);
                                t1.setText(input.getDesc()+": ");

                                TextView t2 = new TextView(TaskListActivity.this);
                                if(showType == 0){
                                    t2.setText("*****");
                                } else {
                                    t2.setText(input.getValue());
                                }
                                TableRow row = new TableRow(TaskListActivity.this);
                                row.addView(t1);
                                row.addView(t2);
                                endExtra.addView(row, new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            }
                        }

                        if (null != end.getOutputs() && end.getOutputs().size() > 0) {
                            TableLayout endFeedback = (TableLayout) stageTable.findViewById(R.id.end_feedback);
                            endFeedback.setVisibility(View.VISIBLE);

                            //用于展示多张照片
                            GridView gridView = (GridView)endFeedback.findViewById(R.id.end_gridview);
                            List<HashMap<String,Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();

                            for (QueryTaskInfoServlet.ResponseBO.OutputBO output : end.getOutputs()) {
                                if (output.getType() == 0) {
                                    if(output.getValue() == null){
                                        TextView t1 = (TextView) endFeedback.findViewById(R.id.end_feedback_pic_desc);
                                        t1.setVisibility(View.VISIBLE);
                                        t1.setText("# " + output.getDesc());
                                    }
                                    else{
                                        //将照片添加
                                        HashMap<String,Object> map = new HashMap<String, Object>();
                                        map.put("ItemImage", output.getValue());
                                        map.put("ItemText",output.getDesc());
                                        lstImageItem.add(map);
//                                        ImageView imageView = (ImageView) endFeedback.findViewById(R.id.end_picture);
//                                        imageView.setVisibility(View.VISIBLE);
//                                        String imageUrl = output.getValue();
//                                        Glide.with(TaskListActivity.this).load(imageUrl).into(imageView);
                                    }
                                }else {
                                    TextView t2 = new TextView(TaskListActivity.this);
                                    t2.setText("# " + output.getDesc());
                                    endFeedback.addView(t2);
                                    if(null != output.getValue()){
                                        TextView t3 = new TextView(TaskListActivity.this);
                                        t3.setText(output.getValue());
                                        endFeedback.addView(t3);
                                    }
                                }
                            }
                            //展示照片
                            if(lstImageItem.size() > 0){
                                //生成适配器的ImageItem <====> 动态数组的元素，两者一一对应
                                SimpleAdapter pictureAdapter = new SimpleAdapter(TaskListActivity.this, //没什么解释
                                        lstImageItem,//数据来源
                                        R.layout.picture_output_item,//picture_output_item的XML实现
                                        //动态数组与ImageItem对应的子项
                                        new String[] {"ItemImage","ItemText"},
                                        //ImageItem的XML文件里面的一个ImageView,两个TextView ID
                                        new int[] {R.id.ItemImage,R.id.ItemText});

                                pictureAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                                    @Override
                                    public boolean setViewValue(View view, Object data, String textRepresentation) {
                                        //判断是否为我们要处理的对象
                                        if(view instanceof ImageView){
                                            ImageView iv = (ImageView) view;
                                            String imageUrl = (String)data;
                                            Glide.with(TaskListActivity.this).load(imageUrl).into(iv);
                                            return true;
                                        }
                                        else
                                            return false;
                                    }
                                });
                                //添加并且显示
                                gridView.setAdapter(pictureAdapter);
                                gridView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    taskDetailLl.addView(stageTable);
                }

                builder.setView(sv);
                builder.setNegativeButton(TaskListActivity.this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
        if (stringList == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        boolean flag = false;
        for (String string : stringList) {
            if (flag) {
                result.append(",");
            }else {
                flag = true;
            }
            result.append(string);
        }
        return result.toString();
    }

}
