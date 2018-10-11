package com.lab.se.crowdframe;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.lab.se.crowdframe.entity.EnumOutput;
import com.lab.se.crowdframe.entity.NumericalOutput;
import com.lab.se.crowdframe.entity.PictureOutput;
import com.lab.se.crowdframe.entity.StageContent;
import com.lab.se.crowdframe.entity.StageInfo;
import com.lab.se.crowdframe.entity.SysApplication;
import com.lab.se.crowdframe.entity.TemplateInfo;
import com.lab.se.crowdframe.entity.TextOutput;
import com.lab.se.crowdframe.http.HttpUtil;
import com.lab.se.crowdframe.http.JSONUtils;
import com.lab.se.crowdframe.http.ServletResponseData;
import com.lab.se.crowdframe.servlet.PublishTaskServlet;
import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

public class CustomizeTaskActivity extends AppCompatActivity {
    private static final String TAG = "CustomizeTaskTEST";
    private static final int EDIT_TASK_STAGE = 1;
    CrowdFrameApplication app;
    private static String uri;
    private static String templateId;
    TemplateInfo templateInfo;
    SimpleAdapter adapter;
    Toolbar mToolbar;
    private ListView list;
    private String[] mDetail;
    private ArrayList<Map<String, Object>> mData = new  ArrayList<Map<String, Object>>();

    EditText et1;
    EditText et2;
    EditText et3;
    ImageView refreshImageView;
    private HashMap<Integer,StageContent> scmap;
    private List<StageContent> stageContents = new ArrayList<StageContent>();

    Button finishButton;
    //获取日期格式器对象
    DateFormat fmtDate = new java.text.SimpleDateFormat("yyyy-MM-dd");
    DateFormat fmtTime = new java.text.SimpleDateFormat("HH:mm:ss");
    //定义一个TextView控件对象
    TextView txtDate = null;
    TextView txtTime = null;
    //获取一个日历对象
    Calendar dateAndTime = Calendar.getInstance(Locale.CHINA);

    //保证所有的stage都被编辑过了，防止程序崩溃
    private HashMap<Integer,Integer> stageClicked;

    //当点击DatePickerDialog控件的设置按钮时，调用该方法
    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener()
    {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            //修改日历控件的年，月，日
            //这里的year,monthOfYear,dayOfMonth的值与DatePickerDialog控件设置的最新值一致
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            //将页面TextView的显示更新为最新时间
            upDateDate();
        }
    };

    TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {

        //同DatePickerDialog控件
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dateAndTime.set(Calendar.MINUTE, minute);
            upDateTime();
        }
    };

    //stage
    TextView currentLocationView;
    //user scope
    Spinner userScope;

    //avoid the user repeatedly click the finish button
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_task);
        SysApplication.getInstance().addActivity(this);

        app = (CrowdFrameApplication) CustomizeTaskActivity.this.getApplication();
        scmap = new HashMap<Integer, StageContent>();
        stageClicked = new HashMap<Integer,Integer>();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(CustomizeTaskActivity.this).setTitle(CustomizeTaskActivity.this.getString(R.string.confirm)).setMessage(CustomizeTaskActivity.this.getString(R.string.confirm_edit_stage))
                        .setPositiveButton(CustomizeTaskActivity.this.getString(R.string.confirm_yes), new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CustomizeTaskActivity.this.finish();
                            }
                        }).show();
            }
        });

        progressDialog = new ProgressDialog(CustomizeTaskActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("上传数据中...");

        et1 = (EditText)findViewById(R.id.ts_title);
        et2 = (EditText)findViewById(R.id.ts_desc);
        et3 = (EditText)findViewById(R.id.ts_bonus);
        //user scope
        userScope = (Spinner)findViewById(R.id.user_scope_entry);

        txtDate = (TextView)findViewById(R.id.set_date_1);
        txtDate.setClickable(true);
        txtDate.setFocusable(true);
        txtTime = (TextView)findViewById(R.id.set_time_1);
        txtTime.setClickable(true);
        txtTime.setFocusable(true);

        txtDate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                DatePickerDialog  dateDlg = new DatePickerDialog(CustomizeTaskActivity.this,
                        d,
                        dateAndTime.get(Calendar.YEAR),
                        dateAndTime.get(Calendar.MONTH),
                        dateAndTime.get(Calendar.DAY_OF_MONTH));
                dateDlg.show();
            }
        });
        txtTime.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                TimePickerDialog timeDlg = new TimePickerDialog(CustomizeTaskActivity.this,
                        t,
                        dateAndTime.get(Calendar.HOUR_OF_DAY),
                        dateAndTime.get(Calendar.MINUTE),
                        true);
                timeDlg.show();
            }
        });
        upDateDate();
        upDateTime();

        templateId = getIntent().getStringExtra("templateId");
        uri = getIntent().getStringExtra("uri");

        Log.d(TAG,String.format("The uri is %s", uri));

        //下载模板文件并解析
        downloadTemplateAndParse(uri);

    }

    //下载模板文件并解析
    private void downloadTemplateAndParse(final String templateUrl) {
        //反序列化xml为对象
        final XStream xstream = new XStream();
        xstream.alias("template",TemplateInfo.class);
        xstream.alias("stage",StageInfo.class);
        xstream.alias("location",StageInfo.LocationInformation.class);
        xstream.alias("pictureOutput",PictureOutput.class);
        xstream.alias("textOutput",TextOutput.class);
        xstream.alias("numericalOutput",NumericalOutput.class);
        xstream.alias("enumOutput",EnumOutput.class);

        final String templateDir = this.getApplicationContext().getFilesDir().toString() + File.separator + "template/";
        final String templateName = templateUrl.substring(templateUrl.lastIndexOf('/') + 1);
        final File templateFile = new File(templateDir,templateName);
        if(!templateFile.getParentFile().exists()){
            templateFile.getParentFile().mkdirs();
        }
        Log.d(TAG,String.format("The template name is %s", templateName));

        if(templateFile.exists()){
            Log.d(TAG,"The file already exist");
            Log.d("lwhych", templateFile.getParentFile().getAbsolutePath());
            templateInfo = (TemplateInfo)xstream.fromXML(templateFile);
            Log.d(TAG,String.format("The template title is %s", templateInfo.getName()));
            //完成解析以后进行操作
            showTaskContent();
        }
        else{
            HttpUtil.downloadFile(templateUrl, new okhttp3.Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    InputStream is = null;
                    byte[] buf = new byte[2048];
                    int len = 0;
                    FileOutputStream fos = null;
                    try {
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            if(!templateFile.getParentFile().exists()){
                                templateFile.getParentFile().mkdirs();
                            }
                            long total = response.body().contentLength();
                            long current = 0;
                            is = response.body().byteStream();
                            //不能输出，否则就会导致流关闭的问题One thing to remember with stream : Reading or printing the outputStream will close it.
                            //Log.d(TAG,response.body().string());
                            Log.d(TAG, templateFile.getParentFile().getAbsolutePath());
                            fos = new FileOutputStream(templateFile);
                            while ((len = is.read(buf)) != -1) {
                                fos.write(buf, 0, len);
                            }
                            fos.flush();
                            fos.close();
                            is.close();
                            templateInfo = (TemplateInfo)xstream.fromXML(templateFile);
                            Log.d(TAG,String.format("The template title is %s", templateInfo.getName()));
                            //完成解析以后进行操作
                            showTaskContent();
                        }
                        else{
                            Log.d(TAG,String.format("模板下载失败，SD卡无效！"));
                        }
                    }catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {

                }
            });
        }

    }

    //在解析完模板以后展示内容并发布任务
    private void showTaskContent(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG,templateInfo.getStages().size()+"");
                list = (ListView)findViewById(R.id.stage_list_in_customize_task);

                //重新获取并设置模板内容
                mData.clear();
                for(int i=0; i < templateInfo.getStages().size(); i++){
                    Map<String,Object> item = new HashMap<String,Object>();
                    item.put("index",i+1);
                    item.put("detail",templateInfo.getStages().get(i).getStageDescription());
                    mData.add(item);
                }
                TextView tv = (TextView)findViewById(R.id.set_task_title);
                tv.setText(templateInfo.getName());
                et1.setText(templateInfo.getName());
                et2.setText(templateInfo.getDescription());
                et3.setText("0");

                adapter = new SimpleAdapter(getApplicationContext(),mData,R.layout.stage_in_edit_temp2,
                        new String[]{"index","detail"},new int[]{R.id.stage_index,R.id.customize_stage_detail}){
                    @Override
                    public View getView(final int position, View convertView, ViewGroup parent) {
                        final int p = position;
                        final View view = super.getView(position, convertView, parent);

                        final ImageView iv = (ImageView)view.findViewById(R.id.stage_edit);
                        iv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(CustomizeTaskActivity.this,CustomizeStageActivity.class);

                                int index = p + 1;
                                StageInfo stageInfo = templateInfo.getStages().get(p);
                                StageContent stageContent = scmap.get(index);

                                intent.putExtra("index",index);
                                intent.putExtra("stageInfo",stageInfo);
                                intent.putExtra("stageContent",stageContent);
                                Log.d(TAG,String.format("The index of the stage is %d",p + 1));
//                                Log.d(TAG,String.format("The stage's worker number is %d",stageContent.getWorkerNum()));
                                startActivityForResult(intent,EDIT_TASK_STAGE);
                            }
                        });
                        return view;
                    }
                };
                list.setAdapter(adapter);

                //finish按钮
                finishButton = (Button) findViewById(R.id.finish_edit_template);
                finishButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        app = (CrowdFrameApplication)getApplication();
                        Double total = 0.0;
                        if(et3.getText()!=null && !et3.getText().toString().equals("")){
                            total += Double.parseDouble(et3.getText().toString());
                        }
                        for(int i=0;i<scmap.size();i++){
                                total += scmap.get(i+1).getReward()*scmap.get(i+1).getWorkerNum();
                        }
                        Log.d("fzj3",total+"");
                        Log.d("fzj4","creditPublish="+app.getCreditPublish());
                        if(total > app.getCreditPublish()){
                            Toast.makeText(CustomizeTaskActivity.this,CustomizeTaskActivity.this.getString(R.string.not_enough_credit),Toast.LENGTH_LONG).show();
                            return;
                        }
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                        String timeStr = format.format(currentTime);
                        currentTime = Timestamp.valueOf(timeStr);
                        //设置ddl
                        String timestr = txtDate.getText().toString()+" "+txtTime.getText().toString();
                        Timestamp ddl = Timestamp.valueOf(timestr);
                        timeStr = format.format(ddl);
                        ddl = Timestamp.valueOf(timeStr);

                        long diff = ddl.getTime() - currentTime.getTime();
                        if(diff <= 300000){
                            Toast.makeText(CustomizeTaskActivity.this,CustomizeTaskActivity.this.getString(R.string.premature_ddl),Toast.LENGTH_LONG).show();
                            return;
                        }

                        int stageDeadlineCorrectFlag = 1;
                        List<Timestamp> list = new ArrayList<Timestamp>();
                        for(int i=0;i<scmap.size();i++){
                            //Log.d("testtime","loop");
                            if(scmap.get(i+1).isDeadlineActive()){
                                list.add(scmap.get(i+1).getDdl());
                                if(scmap.get(i+1).getDdl().getTime() - currentTime.getTime() <=0){
                                    stageDeadlineCorrectFlag =0;
                                }
                                if(ddl.getTime() - scmap.get(i+1).getDdl().getTime() <= 0){
                                    stageDeadlineCorrectFlag = 0;
                                }
                            }
                        }

                        for(int i=1;i<list.size();i++){
                            if(list.get(i).getTime() - list.get(i-1).getTime() <=0){
                                stageDeadlineCorrectFlag = 0;
                            }
                        }

                        if(stageDeadlineCorrectFlag == 0){
                            Toast.makeText(CustomizeTaskActivity.this,CustomizeTaskActivity.this.getString(R.string.wrong_stage_ddl),Toast.LENGTH_LONG).show();
                            return;
                        }
                        //Toast.makeText(CustomizeTaskActivity.this,dateAndTime.getTime().toString(),Toast.LENGTH_LONG).show();
                        showFinishInfo();
                    }
                });

            }
        });
    }


    public void showFinishInfo() {
        new android.support.v7.app.AlertDialog.Builder(CustomizeTaskActivity.this).setTitle(CustomizeTaskActivity.this.getString(R.string.confirm)).setMessage(CustomizeTaskActivity.this.getString(R.string.confirm_publish))
                .setPositiveButton(CustomizeTaskActivity.this.getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                publish();
                            }
                        }).show();
    }

    //获取编辑好的stage信息
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == EDIT_TASK_STAGE){
            if(resultCode > 0){
                StageContent sc = (StageContent) data.getSerializableExtra("stageContent");
                int stageIndex = resultCode;
                Log.d(TAG,String.format("The index of corresponding stage is %d", stageIndex));
                Log.d(TAG,String.format("The address is %s and the corresponding longitude is %f, latitude is %f",sc.getLocations().get(0).getAddress(),
                        sc.getLocations().get(0).getLongitude(),sc.getLocations().get(0).getLatitude()));
                scmap.put(stageIndex,sc);
                stageClicked.put(stageIndex,1);

                //更新stage的描述
                int mDataSize = mData.size();
                for(int i = 0; i < mDataSize; i++) {
                    int mIndex = (Integer) mData.get(i).get("index");
                    if(mIndex == stageIndex){
                        mData.get(i).put("detail",sc.getDesc());
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
            else{
                Log.d(TAG,String.format("You have canceld editing the stage"));
            }
        }
    }

    //发布任务
    private void publish(){
        //显示进度条，按钮不可用
        progressDialog.show();
        //构建请求体
        final HashMap<String,String> postData = new HashMap<String, String>();

        PublishTaskServlet.RequestBO requestBO = new PublishTaskServlet.RequestBO();
        requestBO.setTemplateId(Integer.parseInt(templateId));
        requestBO.setRequesterId(app.getUserId());

        if(stageClicked.size() < templateInfo.getStages().size()){
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(),CustomizeTaskActivity.this.getString(R.string.stages_remain_unmodified), Toast.LENGTH_LONG).show();
            return;
        }

        if(et1.getText() != null && !"".equals(et1.getText().toString()))
            requestBO.setTitle(et1.getText().toString());
        else{
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(),CustomizeTaskActivity.this.getString(R.string.input_title), Toast.LENGTH_LONG).show();
            return;
        }
        if(et2.getText() != null && !"".equals(et2.getText().toString()))
            requestBO.setDescription(et2.getText().toString());
        else{
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(),CustomizeTaskActivity.this.getString(R.string.input_desc), Toast.LENGTH_LONG).show();
            return;
        }
        if(et3.getText() != null && !"".equals(et3.getText().toString()))
            requestBO.setBonusReward(Double.parseDouble(et3.getText().toString()));
        else
            requestBO.setBonusReward(0);
        //获得当前时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String timeStr = format.format(currentTime);
        currentTime = Timestamp.valueOf(timeStr);
        requestBO.setPublishTime(currentTime);
        //设置ddl
        String timestr = txtDate.getText().toString()+" "+txtTime.getText().toString();
        Timestamp ddl = Timestamp.valueOf(timestr);
        timeStr = format.format(ddl);
        ddl = Timestamp.valueOf(timeStr);
        requestBO.setDeadline(ddl);
        //设置user scope
        if(userScope.getSelectedItem().toString().equals("所有用户可见")){
            requestBO.setUserScope(0);
        } else {
            requestBO.setUserScope(1);
        }


        //设置stage的信息
        List<PublishTaskServlet.RequestBO.StageBO> stages = new ArrayList<PublishTaskServlet.RequestBO.StageBO>();
        //设置新的deadline
        //最后一个stage的ddl如果没有设置，则视为和task的ddl一致
        //Log.d("fzj","scmap length"+scmap.size());
        if(!scmap.get(scmap.size()).isDeadlineActive()){
            scmap.get(scmap.size()).setDdl(ddl);
        }
        //之前所有的stage依次推导
        if(scmap.size() > 2){
            for(int i = scmap.size()-1; i >=1;i--){
                if(!scmap.get(i).isDeadlineActive()){
                    scmap.get(i).setDdl(new Timestamp(Math.round(scmap.get(i+1).getDdl().getTime()-scmap.get(i+1).getStageDuration()*60*1000)));
                }
            }
        }else if(scmap.size() == 2){
            scmap.get(1).setDdl(new Timestamp(Math.round(scmap.get(2).getDdl().getTime()-scmap.get(2).getStageDuration()*60*1000)));
        }


        //获得stages的信息,防止用户不按正常顺序编辑stage
        int iteratorIndex = 1;
        while(scmap.get(iteratorIndex) != null){
            stageContents.add(scmap.get(iteratorIndex));
            iteratorIndex++;
        }
        for(StageContent stageContent : stageContents){
            PublishTaskServlet.RequestBO.StageBO stage = new PublishTaskServlet.RequestBO.StageBO();
            setStageInformation(stageContent,stage);
            stages.add(stage);
        }
        requestBO.setStages(stages);

        postData.put("data", JSONUtils.toJSONString(requestBO));
        Log.d("fzj",postData.get("data"));
        String servlet = "PublishTaskServlet";
        HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    ServletResponseData responseData = JSONUtils.toBean(response.body().string(), ServletResponseData.class);
                    int result = responseData.getResult();
                    if(result == 1){
                        Log.d(TAG,"success");
                        progressDialog.dismiss();
                        showMessage(CustomizeTaskActivity.this.getString(R.string.publish_task_successfully));
                        CustomizeTaskActivity.this.finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    showMessage(CustomizeTaskActivity.this.getString(R.string.publish_task_fail_exception));

                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                progressDialog.dismiss();
                showMessage(CustomizeTaskActivity.this.getString(R.string.publish_task_fail_post));
            }

        });
    }

    //根据stageContent的信息设置stage的信息
    private void setStageInformation(StageContent stageContent, PublishTaskServlet.RequestBO.StageBO stage){
        stage.setName(stageContent.getName());
        stage.setDesc(stageContent.getDesc());
        stage.setReward(stageContent.getReward());
        stage.setStageDuration(stageContent.getStageDuration());//新增stage duration
        stage.setStageIndex(stageContent.getStageIndex());
        stage.setDdl(stageContent.getDdl());
        stage.setWorkerNum(stageContent.getWorkerNum());
        stage.setAggregateMethod(stageContent.getAggregateMethod());
        stage.setRestrictions(stageContent.getRestrictions());
        List<PublishTaskServlet.RequestBO.LocationBO> locations = new ArrayList<PublishTaskServlet.RequestBO.LocationBO>();

        PublishTaskServlet.RequestBO.LocationBO dest = new PublishTaskServlet.RequestBO.LocationBO();
        dest.setType(CustomizeStageActivity.LOCATION_DEST);
        StageContent.LocationContent destContent = stageContent.getLocations().get(0);
        dest.setDuration(destContent.getDuration());//新增location duration，代表在此location上的action所需要的时间
        dest.setAddress(destContent.getAddress());
        dest.setLatitude(destContent.getLatitude());
        dest.setLongitude(destContent.getLongitude());
        //input信息
        List<PublishTaskServlet.RequestBO.InputBO> inputList = new ArrayList<PublishTaskServlet.RequestBO.InputBO>();
        List<StageContent.InputContent> stageContentInputList = destContent.getInputs();
        Log.d(TAG,String.format("The size of input is %d", stageContentInputList.size()));
        for(StageContent.InputContent inputContent : stageContentInputList){
            PublishTaskServlet.RequestBO.InputBO input = new PublishTaskServlet.RequestBO.InputBO();
            input.setType(inputContent.getType());
            input.setDesc(inputContent.getDesc());
            input.setValue(inputContent.getValue());
            Log.d(TAG,String.format("The input type is %d, the description is %s and the value is %s", input.getType(),input.getDesc(), input.getValue()));
            inputList.add(input);
        }
        //output信息
        List<PublishTaskServlet.RequestBO.OutputBO> outputList = new ArrayList<PublishTaskServlet.RequestBO.OutputBO>();
        List<StageContent.OutputContent> stageContentOutputList = destContent.getOutputs();
        if(stageContentOutputList != null){
            for(StageContent.OutputContent outputContent : stageContentOutputList) {
                PublishTaskServlet.RequestBO.OutputBO output = new PublishTaskServlet.RequestBO.OutputBO();
                output.setType(outputContent.getType());
                output.setDesc(outputContent.getDesc());
                output.setValue(outputContent.getValue());
                switch (outputContent.getType()){
                    case 0:
                        output.setActive(outputContent.isActive());
                        break;
                    case 1:
                        break;
                    case 2:
                        output.setIntervalValue(outputContent.getIntervalValue());
                        output.setLowBound(outputContent.getLowBound());
                        output.setUpBound(outputContent.getUpBound());
                        output.setAggregaMethod(outputContent.getAggregaMethod());
                        break;
                    case 3:
                        output.setEntries(outputContent.getEntries());
                        break;
                    default:
                }
                Log.d(TAG, String.format("The output type is %d and the desc is %s", output.getType(), output.getDesc()));
                outputList.add(output);
            }
        }
        dest.setInputs(inputList);
        dest.setOutputs(outputList);
        locations.add(dest);

        stage.setLocations(locations);
    }

    //显示提示信息
    private void showMessage(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void upDateDate() {

        txtDate.setText(fmtDate.format(dateAndTime.getTime()));

    }

    private void upDateTime() {

        txtTime.setText(fmtTime.format(dateAndTime.getTime()).substring(0,fmtTime.format(dateAndTime.getTime()).length()-3)+":00");

    }

}
