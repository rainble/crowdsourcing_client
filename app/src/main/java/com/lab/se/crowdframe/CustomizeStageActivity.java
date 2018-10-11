package com.lab.se.crowdframe;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.baidu.mapapi.map.Text;
import com.lab.se.crowdframe.entity.EnumOutput;
import com.lab.se.crowdframe.entity.ExtraMap;
import com.lab.se.crowdframe.entity.ExtraName;
import com.lab.se.crowdframe.entity.ExtraValue;
import com.lab.se.crowdframe.entity.ListViewUtility;
import com.lab.se.crowdframe.entity.MyCustomAdapter;
import com.lab.se.crowdframe.entity.NumericalOutput;
import com.lab.se.crowdframe.entity.PictureOutput;
import com.lab.se.crowdframe.entity.StageContent;
import com.lab.se.crowdframe.entity.StageInfo;
import com.lab.se.crowdframe.entity.SysApplication;
import com.lab.se.crowdframe.entity.TextOutput;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomizeStageActivity extends AppCompatActivity {
    private static final String TAG = "CustomizeStageActivity";
    public static final int EDIT_TASK_STAGE_CANCEL = -1, LOCATION_DEST = 1,TYPE_INPUTBOX = 1,OUT_PICTURE = 0, OUTPUT_TEXT = 1;
    List<EditText> editTextList;
    StageInfo receivedStageInfo;
    StageContent receivedStageContent;
    String address, aggreMethod;
    HashMap<String,String> extraMap;
    private String longitude = null;
    private String latitude = null;
    EditText descEditText,rewardEditText,detailAddressEditText,workerNumText;

    Toolbar mToolbar;
    LinearLayout wrapper;
    EditText endLocationDuration;
    CheckBox isDeadline;
    LinearLayout pickerLayout;
    TextView indexTextView,workerStrategy,stageOutput,resultMethod;
    ListView stageOutputList;
    //获取日期格式器对象
    DateFormat fmtDate = new java.text.SimpleDateFormat("yyyy-MM-dd");

    DateFormat fmtTime = new java.text.SimpleDateFormat("HH:mm:ss");

    //定义一个TextView控件对象
    TextView txtDate = null;
    TextView txtTime = null;
    //获取一个日历对象
    Calendar dateAndTime = Calendar.getInstance(Locale.CHINA);

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_customize_stage);
        SysApplication.getInstance().addActivity(this);
        extraMap = new HashMap<String,String>();
        TextView title_show = (TextView) findViewById(R.id.stageName);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //返回按钮
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.app.AlertDialog.Builder(CustomizeStageActivity.this).setTitle(CustomizeStageActivity.this.getString(R.string.confirm)).setMessage(CustomizeStageActivity.this.getString(R.string.confirm_edit_stage))
                        .setPositiveButton(CustomizeStageActivity.this.getString(R.string.confirm_yes), new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                setResult(EDIT_TASK_STAGE_CANCEL, intent);
                                CustomizeStageActivity.this.finish();
                            }
                        }).show();
            }
        });
        wrapper = (LinearLayout)findViewById(R.id.wrapper);
        descEditText = (EditText)findViewById(R.id.cus_desc);
        rewardEditText = (EditText)findViewById(R.id.cus_reward);
        detailAddressEditText = (EditText)findViewById(R.id.cus_detail_address);
        workerStrategy = (TextView)findViewById(R.id.worker_strategy);
        //stageOutput = (TextView)findViewById(R.id.stage_output);
        workerNumText = (EditText)findViewById(R.id.worker_num_text);
        resultMethod = (TextView)findViewById(R.id.result_method);

        endLocationDuration = (EditText)findViewById(R.id.cus_end_duration);
        isDeadline = (CheckBox)findViewById(R.id.isDeadline_check_box);
        pickerLayout = (LinearLayout)findViewById(R.id.date_time_picker_layout);
        pickerLayout.setVisibility(View.GONE);
        isDeadline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                   pickerLayout.setVisibility(View.VISIBLE);
                } else {
                    pickerLayout.setVisibility(View.GONE);
                }
            }
        });

        txtDate =(TextView)findViewById(R.id.set_date_stage1);
        txtDate.setClickable(true);
        txtDate.setFocusable(true);
        txtTime =(TextView)findViewById(R.id.set_time_stage1);
        txtTime.setClickable(true);
        txtTime.setFocusable(true);

        txtDate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                DatePickerDialog  dateDlg = new DatePickerDialog(CustomizeStageActivity.this,
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
                TimePickerDialog timeDlg = new TimePickerDialog(CustomizeStageActivity.this,
                        t,
                        dateAndTime.get(Calendar.HOUR_OF_DAY),
                        dateAndTime.get(Calendar.MINUTE),
                        true);
                timeDlg.show();
            }
        });
        upDateDate();
        upDateTime();

        //edit location
        editLocation();

        //动态生成页面
        final int index = getIntent().getIntExtra("index",-1);
        indexTextView = (TextView)findViewById(R.id.cusName);

        receivedStageInfo =(StageInfo)getIntent().getSerializableExtra("stageInfo");
        receivedStageContent = (StageContent)getIntent().getSerializableExtra("stageContent");
        Log.d(TAG,String.format("The stage content is %s", receivedStageContent));
        title_show.setText(receivedStageInfo.getStageName());
        descEditText.setText(receivedStageInfo.getStageDescription());
        workerStrategy.setText(receivedStageInfo.getWorkerStrategy());

        stageOutputList = (ListView)findViewById(R.id.stage_output_listview);
        MyCustomAdapter adapter = new MyCustomAdapter(this);
        if(receivedStageInfo.getDest().getPictureOutput().getOutputDesc()!=null){
            adapter.addSeparatorItem("PICTURE");
            adapter.addItem((receivedStageInfo.getDest().getPictureOutput().getOutputDesc()));
        }
        if(receivedStageInfo.getDest().getNumericalOutputs().size() > 0){
            adapter.addSeparatorItem("NUMERICAL");
            for(int i = 0; i < receivedStageInfo.getDest().getNumericalOutputs().size(); i++){
                adapter.addItem(receivedStageInfo.getDest().getNumericalOutputs().get(i).getOutputDesc());
            }
        }

        if(receivedStageInfo.getDest().getEnumOutputs().size() > 0){
            adapter.addSeparatorItem("ENUMERATED");
            for(int i = 0; i < receivedStageInfo.getDest().getEnumOutputs().size(); i++){
                String str = receivedStageInfo.getDest().getEnumOutputs().get(i).getOutputDesc()+":";
                for(int j = 0;j < receivedStageInfo.getDest().getEnumOutputs().get(i).getEntries().size();j++){
                    str += receivedStageInfo.getDest().getEnumOutputs().get(i).getEntries().get(j)+", ";
                }

                adapter.addItem( str.substring(0,str.length()-2));
            }
        }

        if(receivedStageInfo.getDest().getTextOutputs().size() > 0){
            adapter.addSeparatorItem("TEXTUAL");
            for(int i = 0; i < receivedStageInfo.getDest().getTextOutputs().size(); i++){
                adapter.addItem(receivedStageInfo.getDest().getTextOutputs().get(i).getOutputDesc());
            }
        }

        stageOutputList.setAdapter(adapter);
        // 调整listView的高度，否则嵌套在scrollView中的listView将无法正常显示
       ListViewUtility.setListViewHeightBasedOnChildren(stageOutputList);

//        stageOutput.setText(outputText);
        if(receivedStageInfo.getWorkerNumber() <= 1){
            workerNumText.setKeyListener(null);//设置不可编辑
            workerNumText.setBackgroundResource(R.drawable.worker_number_edit_text);
        }
        workerNumText.setText(receivedStageInfo.getWorkerNumber() + "");
        resultMethod.setText(receivedStageInfo.getAggregateMethod());

        //动态生成额外输入
        if(receivedStageInfo.getDest().getInputs().size() > 0){
            for(int i = 0; i < receivedStageInfo.getDest().getInputs().size();i++){
                createView(receivedStageInfo.getDest().getInputs().get(i));
            }
        }

        //如果receivedStageContent不是null，则是修改
        if(receivedStageContent != null)
            setRelatedInfo();

        //finish按钮
        Button finishButton = (Button)findViewById(R.id.finish_edit_stage);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                /*
                构建返回的stagecontent信息
                 */
                //首先是stage本身的信息
                StageContent sc = new StageContent();
                String desc = descEditText.getText().toString();
                sc.setDesc(desc);
                if(rewardEditText.getText() != null && !rewardEditText.getText().toString().equals("")){
                    sc.setReward(Double.parseDouble(rewardEditText.getText().toString()));
                }
                else{
                    Toast.makeText(CustomizeStageActivity.this, "请输入报酬值！" ,Toast.LENGTH_SHORT).show();
                    return;
                }
                sc.setStageIndex(index);
                if(isDeadline.isChecked()){
                    sc.setDeadlineActive(true);

                    String timestr = txtDate.getText().toString()+" "+txtTime.getText().toString();
                    Timestamp ddl = Timestamp.valueOf(timestr);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String timeStr = format.format(ddl);
                    ddl = Timestamp.valueOf(timeStr);
                    sc.setDdl(ddl);
                    Log.d(TAG,String.format("The ddl is %s", ddl));
                }else{
                    sc.setDeadlineActive(false);
                }


                //设置location信息，目前只有终点
                if(!endLocationDuration.getText().toString().equals("") && endLocationDuration.getText()!= null){
                    sc.setEndDuration(Double.parseDouble(endLocationDuration.getText().toString()));
                }else{
                    sc.setEndDuration(0);
                }
                sc.setStageDuration(sc.getEndDuration()+sc.getStartDuration());

                StageContent.LocationContent dest = new StageContent.LocationContent();
                dest.setType(LOCATION_DEST);
                //检查是否选择终点位置，必填选项
                if(address == null){
                    Toast.makeText(CustomizeStageActivity.this, "终点位置必须点击选择！" ,Toast.LENGTH_SHORT).show();
                    return;
                }
                String addressDetail = address + "=" +  detailAddressEditText.getText().toString();
                dest.setAddress(addressDetail);
                if(longitude != null){
                    dest.setLongitude(Double.parseDouble(longitude));
                }
                else{
                    dest.setLongitude(0);
                }

                if(latitude != null){
                    dest.setLatitude(Double.parseDouble(latitude));
                }
                else{
                    dest.setLatitude(0);
                }
                dest.setDuration(sc.getEndDuration());
                //设置location的input信息
                List<StageContent.InputContent> inputList = new ArrayList<StageContent.InputContent>();
                //遍历extraMap获得
                Log.d(TAG,String.format("The size of input is %d",extraMap.size()));
                Iterator iter = extraMap.entrySet().iterator();
                while(iter.hasNext()){
                    StageContent.InputContent inputContent = new StageContent.InputContent();
                    Map.Entry entry = (Map.Entry)iter.next();
                    String inputName = (String)entry.getKey();
                    String inputValue = (String)entry.getValue();
                    Log.d(TAG,String.format("The label is %s and the value is %s", inputName,inputValue));
                    inputContent.setType(TYPE_INPUTBOX);
                    inputContent.setDesc(inputName);
                    inputContent.setValue(inputValue);
                    inputList.add(inputContent);
                }

                StageInfo stageInfo = receivedStageInfo;

                //设置location的output信息
                List<StageContent.OutputContent> outputList = new ArrayList<StageContent.OutputContent>();

                //图片
                if(stageInfo.getDest().getPictureOutput()!=null){
                    PictureOutput po = stageInfo.getDest().getPictureOutput();
                    StageContent.OutputContent output1 = new StageContent.OutputContent();
                    output1.setType(0);
                    output1.setDesc(po.getOutputDesc());
                    output1.setActive(po.isActive());
                    outputList.add(output1);
                }


                if(stageInfo.getDest().getNumericalOutputs()!=null){
                    for(NumericalOutput no : stageInfo.getDest().getNumericalOutputs()){
                        StageContent.OutputContent output = new StageContent.OutputContent();
                        output.setType(2);
                        output.setDesc(no.getOutputDesc());
                        output.setIntervalValue(no.getInterval());
                        output.setUpBound(no.getUpBound());
                        output.setLowBound(no.getLowBound());
                        output.setAggregaMethod(no.getNumberAggregate());
                        outputList.add(output);
                    }
                }

                if(stageInfo.getDest().getEnumOutputs()!=null){
                    for(EnumOutput eo : stageInfo.getDest().getEnumOutputs()){
                        StageContent.OutputContent output = new StageContent.OutputContent();
                        output.setType(3);
                        output.setDesc(eo.getOutputDesc());
                        String result = "";
                        if(eo.getEntries()!=null){
                            for(String s : eo.getEntries()){
                                result += s+";";
                            }
                        }

                        output.setEntries(result);
                        outputList.add(output);
                    }
                }

                if(stageInfo.getDest().getTextOutputs()!=null){
                    for(TextOutput to : stageInfo.getDest().getTextOutputs()){
                        StageContent.OutputContent output = new StageContent.OutputContent();
                        output.setType(1);
                        output.setDesc(to.getOutputDesc());
                        outputList.add(output);
                    }
                }

                dest.setInputs(inputList);
                dest.setOutputs(outputList);

                List<StageContent.LocationContent> locationList = new ArrayList<StageContent.LocationContent>();
                locationList.add(dest);//添加终点信息，目前只有终点
                sc.setLocations(locationList);

                //将template本身具有的stage信息进行设置,其中worker number>1 会由任务发布者自行设置数量

                sc.setName(stageInfo.getStageName());

                if(workerNumText.getText() != null && !workerNumText.getText().toString().equals("")){
                    try{
                        int n = Integer.parseInt(workerNumText.getText().toString());
                        if( n < 1 ){
                            Toast.makeText(CustomizeStageActivity.this, "参与者人数格式错误！" ,Toast.LENGTH_SHORT).show();
                            return;
                        }
                        sc.setWorkerNum(n);
                    } catch (Exception e){
                        Toast.makeText(CustomizeStageActivity.this, "参与者人数格式错误！" ,Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(CustomizeStageActivity.this, "参与者人数必填！" ,Toast.LENGTH_SHORT).show();
                    return;
                }

                //worker数量大于1时，设定聚合方法
                int aggregateMethod = 1;
                if(sc.getWorkerNum() > 1) {
                    String aggregateMethodTemp = stageInfo.getAggregateMethod();
                    if (aggregateMethodTemp.equals(CustomizeStageActivity.this.getString(R.string.result_most)))
                        aggregateMethod = 2;
                    else if (aggregateMethodTemp.equals(CustomizeStageActivity.this.getString(R.string.result_select)))
                        aggregateMethod = 3;
                }
                sc.setAggregateMethod(aggregateMethod);

                //设定约束，目前只有worker strategy算约束
                int workerStrategy;
                String workerStrategyTemp = stageInfo.getWorkerStrategy();
                if(workerStrategyTemp.equals(CustomizeStageActivity.this.getString(R.string.worker_strategy_same)))
                    workerStrategy = 1;
                else if(workerStrategyTemp.equals(CustomizeStageActivity.this.getString(R.string.worker_strategy_new)))
                    workerStrategy = 2;
                else
                    workerStrategy = 3;

                sc.setRestrictions(workerStrategy);
                intent.putExtra("stageContent",sc);
                setResult(index,intent);
                finish();
            }
        });
    }

    //根据之前的编辑内容恢复页面
    private void setRelatedInfo(){
        descEditText.setText(receivedStageContent.getDesc());
        if(receivedStageContent.getReward() == 0.0)
            rewardEditText.setText("");
        else
            rewardEditText.setText(receivedStageContent.getReward() + "");
        if(receivedStageContent.getEndDuration() == 0.0)
            endLocationDuration.setText("");
        else
            endLocationDuration.setText(receivedStageContent.getEndDuration()+"");
        if(receivedStageContent.getWorkerNum() == 0.0){
            workerNumText.setText(receivedStageInfo.getWorkerNumber()+"");
        } else {
            workerNumText.setText(receivedStageContent.getWorkerNum()+"");
        }
        if(receivedStageContent.getLocations() != null){
            StageContent.LocationContent dest = receivedStageContent.getLocations().get(0);
            String allAddress = dest.getAddress();
            if(allAddress != null){
                String[] addresses = allAddress.split("=");

                currentLocationView = (TextView)findViewById(R.id.cus_end_location);

                if(addresses.length > 0){
                    Log.d(TAG,String.format("The addresses[0] is %s", addresses[0]));
                    if(!addresses[0].equals("null")){
                        currentLocationView.setText(addresses[0]);
                        address = addresses[0];
                    }
                    else{
                        currentLocationView.setText("");
                        address = "";
                    }
                }
                if(addresses.length > 1) {
                    Log.d(TAG, String.format("The end location is %s and the detail address is %s", addresses[0], addresses[1]));
                    detailAddressEditText.setText(addresses[1]);
                }
            }
            longitude = dest.getLongitude() + "";
            latitude = dest.getLatitude() + "";
            //恢复时间信息

            if(receivedStageContent.isDeadlineActive()){
                isDeadline.setChecked(true);
                Timestamp ddl = receivedStageContent.getDdl();
                if(ddl != null){
                    String ddlString = ddl.toString();
                    Log.d(TAG,String.format("The ddl is %s",ddlString));
                    String[] ddlStrings = ddlString.split(" ");
                    Log.d(TAG,String.format("The ddl date is %s and the ddl time is %s", ddlStrings[0], ddlStrings[1]));
                    txtDate.setText(ddlStrings[0]);
                    txtTime.setText(ddlStrings[1]);
            }else{
                    isDeadline.setChecked(false);
                }

            }
            //恢复input信息
            List<StageContent.InputContent> inputList = dest.getInputs();
            for(StageContent.InputContent input : inputList){
                extraMap.put(input.getDesc(),input.getValue());
            }
        }
    }

    private void createView(final String viewName){

        LinearLayout linearLayout =(LinearLayout) getLayoutInflater().inflate(R.layout.input_item,null);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.setMargins(100,0,100,0);
        TextView textView = (TextView) linearLayout.findViewById(R.id.inputName);
        textView.setText(viewName+":");
        final EditText editText = (EditText)linearLayout.findViewById(R.id.inputValue);

        //设置input的内容
        if(receivedStageContent != null){
            List<StageContent.InputContent> inputList = receivedStageContent.getLocations().get(0).getInputs();
            for(StageContent.InputContent input : inputList){
                Log.d(TAG,String.format("The viewName is %s and the input desc is %s",viewName,input.getDesc()));
                if(input.getDesc().equals(viewName)){
                    editText.setText(input.getValue());
                }
            }
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                extraMap.put(viewName,editText.getText().toString());
            }
        });

        wrapper.addView(linearLayout,params1);
    }

    private void upDateDate() {
        txtDate.setText(fmtDate.format(dateAndTime.getTime()));
    }

    private void upDateTime() {

        txtTime.setText(fmtTime.format(dateAndTime.getTime()).substring(0,fmtTime.format(dateAndTime.getTime()).length()-3)+":00");

    }

    private void editLocation(){
        TextView stage1end = (TextView)findViewById(R.id.cus_end_location);
        stage1end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLocationView = (TextView)v;
                Intent intent = new Intent(CustomizeStageActivity.this, SearchActivity.class);
                intent.putExtra("city", "上海");
                startActivityForResult(intent, 0);
            }
        });
    }

    /**根据搜索页面地名的经纬度定位**/
    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==0){
            if(resultCode==1){
                currentLocationView.setText(data.getStringExtra("poi"));
                address = data.getStringExtra("poi");
                longitude = data.getStringExtra("longitude");
                latitude = data.getStringExtra("latitude");
            }
        }
    }



}
