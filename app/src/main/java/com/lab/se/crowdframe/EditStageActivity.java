package com.lab.se.crowdframe;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterViewFlipper;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ZoomControls;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Text;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.lab.se.crowdframe.entity.EnumOutput;
import com.lab.se.crowdframe.entity.NumericalOutput;
import com.lab.se.crowdframe.entity.OutputParent;
import com.lab.se.crowdframe.entity.PictureOutput;
import com.lab.se.crowdframe.entity.SearchAdapter;
import com.lab.se.crowdframe.entity.StageInfo;
import com.lab.se.crowdframe.entity.SysApplication;
import com.lab.se.crowdframe.entity.TagCloudLayout;
import com.lab.se.crowdframe.entity.TextOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class EditStageActivity extends AppCompatActivity {

    private static final String TAG = "EditStageTEST";
    private List<String> messageFeedbackList1 = null;
    private List<String> messageFeedbackList2 = null;
    private static final int TYPE_PICT = 0;
    private static final int TYPE_TEXT = 1;
    private static final int TYPE_NUMBER = 2;
    private static final int TYPE_ENUM = 3;
    private static final int CANCEL = -1;
    StageInfo si;//用于接收数据
    StageInfo stageInfo = new StageInfo();//用于返回数据
    private Toolbar mToolbar;
    //worker选择策略
    //worker数量和结果聚合方式
    CheckBox workerNumCheckbox;
    LinearLayout multiWorkerShow;
    ImageButton workerNumMinus, workerNumPlus;
    EditText workerNumText;
    Spinner resultAggregationSpinner;
    List<String> aggregationMethods = new ArrayList<String>();
    ArrayAdapter<String> aggregationAdapter;
    int workerNum;
    String aggreMethod,title;

    //起点终点地图
    private RadioGroup rg;
    private RadioGroup startRg, endRg;
    private LinearLayout startView, endView;//起点终点的view和选择地图部分
    int locationFlag = 1;//1为显示起点，2为显示终点
    //添加input标签
    LinearLayout labelll1, labelll2;
    List<String> labelList1 = new ArrayList<String>();
    List<String> labelList2 = new ArrayList<String>();
    TagCloudLayout showLabel1, showLabel2;
    EditText labelText1, labelText2;
    Button addLabelButton1, addLabelButton2;
    private TagBaseAdapter tagAdapter1, tagAdapter2;
    //反馈方式
    CheckBox needLabel1, needLabel2, picCheckbox1, picCheckbox2, messageCheckbox1, messageCheckbox2;
    EditText picDesc1, picDesc2;
    CheckBox needFeedback1, needFeedback2;
    LinearLayout feedbackll1, feedbackll2;

    LinearLayout feedbackMessageArea1,feedbackMessageArea2;
    EditText feedbackMessageInput1,feedbackMessageInput2;
    Button feedbackMessageAddButton1,feedbackMessageAddButton2;
    Spinner feedbackMessageType1,feedbackMessageType2;
    //finish按钮
    Button finishButton;
    //储存起点的output信息
    PictureOutput src_pictureOutput = new PictureOutput();
    List<TextOutput> src_textOutputs = new ArrayList<TextOutput>();
    List<NumericalOutput> src_numericalOutputs = new ArrayList<NumericalOutput>();
    List<EnumOutput> src_enumOutputs = new ArrayList<EnumOutput>();
    //储存终点的output信息
    PictureOutput dest_pictureOutput = new PictureOutput();
    List<TextOutput> dest_textOutputs = new ArrayList<TextOutput>();
    List<NumericalOutput> dest_numericalOutputs = new ArrayList<NumericalOutput>();
    List<EnumOutput> dest_enumOutputs = new ArrayList<EnumOutput>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_stage);
        SysApplication.getInstance().addActivity(this);

        messageFeedbackList1 = new ArrayList<String>();
        messageFeedbackList2 = new ArrayList<String>();

        //获取从template传过来的数据:stageName和index

        //si =(StageInfo)getIntent().getSerializableExtra("content");
        final int isFirst = getIntent().getIntExtra("isFirst",-1);

        if(isFirst == 0){
            si =(StageInfo)getIntent().getSerializableExtra("content");
            Log.d(TAG,si.getStageDescription());
            Log.d(TAG+" here",si.getWorkerStrategy());
        }


        title = getIntent().getStringExtra("title");
        final int index = getIntent().getIntExtra("index",-1);
        Log.d(TAG,String.format("The index of the stage is: %d",index));

        //返回按钮
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        TextView title_show = (TextView) findViewById(R.id.stageName);
        title_show.setText(title);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //居然同时设定了点击返回物理按键的事件！！！
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(EditStageActivity.this).setTitle(EditStageActivity.this.getString(R.string.confirm)).setMessage(EditStageActivity.this.getString(R.string.confirm_edit_stage))
                        .setPositiveButton(EditStageActivity.this.getString(R.string.confirm_yes), new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                setResult(CANCEL, intent);
                                EditStageActivity.this.finish();
                            }
                        }).show();
            }
        });

        //初始化起点终点定位的view
        initLocation();

        //worker选择策略,worker数量选择和结果聚合方式
        initWorkerStrategy();




        //状态保存
        if(isFirst == 0) {
            EditText et = (EditText) findViewById(R.id.stage_description);
            et.setText(si.getStageDescription());

            RadioButton rb1 = (RadioButton) findViewById(R.id.same_worker);
            RadioButton rb2 = (RadioButton) findViewById(R.id.new_worker);
            RadioButton rb3 = (RadioButton) findViewById(R.id.no_strategy);


            // Log.d(TAG+"1",si.getWorkerStrategy());
            if (si.getWorkerStrategy().equals(this.getString(R.string.worker_strategy_same))) {
                //Log.d(TAG+"1","sss");
                rb1.setChecked(true);
            } else if (si.getWorkerStrategy().equals(this.getString(R.string.worker_strategy_new))) {
                rb2.setChecked(true);
            } else {
                rb3.setChecked(true);
            }
           // Log.d(TAG + "1", "worker number=" + si.getWorkerNumber());
            //Log.d(TAG + "1", "aggregate=" + si.getAggregateMethod());

            CheckBox cb = (CheckBox) findViewById(R.id.worker_num_checkbox);
            EditText et2 = (EditText) findViewById(R.id.worker_num_text);
            Spinner sp = (Spinner) findViewById(R.id.result_aggregation_spinner);
            if (si.isAllowMultiWorker()) {
                cb.setChecked(true);
                et2.setText(si.getWorkerNumber() + "");
                workerNum = si.getWorkerNumber();
                if (si.getAggregateMethod().equals(this.getString(R.string.result_first))) {
                    sp.setSelection(0, true);
                } else if (si.getAggregateMethod().equals(this.getString(R.string.result_most))) {
                    sp.setSelection(1, true);
                } else {
                    sp.setSelection(2, true);
                }


            }

            //Start location
            RadioButton label1 = (RadioButton) findViewById(R.id.end_location_no_requirement_1);
            RadioButton label2 = (RadioButton) findViewById(R.id.requester_location_1);
            RadioButton label3 = (RadioButton) findViewById(R.id.other_location_1);
            CheckBox label4 = (CheckBox) findViewById(R.id.need_label_1);

            StageInfo.LocationInformation src = si.getSrc();
            if (src.getLocationStrategy().equals(this.getString(R.string.location_no_requirement))) {
                label1.setChecked(true);
            } else if (src.getLocationStrategy().equals(this.getString(R.string.location_request_location))) {
                label2.setChecked(true);
            } else {
                label3.setChecked(true);
            }

            if(src.getInputs().size() > 0){

                label4.setChecked(true);
                labelList1.clear();
                for(int i = 0; i < src.getInputs().size(); i++){
                    labelList1.add(src.getInputs().get(i)) ;
                    //Log.d(TAG+"2",src.getInputs().get(i));
                    //Log.d(TAG+"2",labelList1.get(i));
                }
                tagAdapter1.notifyDataSetChanged();
            }
//
//
            CheckBox feedback1 = (CheckBox)findViewById(R.id.need_feedback_1);
            CheckBox picture1 = (CheckBox)findViewById(R.id.feedback_pic_checkbox__1);
            CheckBox message1 = (CheckBox)findViewById(R.id.feedback_message_checkbox_1);
            EditText pic_desc1 = (EditText)findViewById(R.id.feedback_pic_desc_1);
            EditText feedback_message_input1 = (EditText)findViewById(R.id.feedback_message_input_1);
//            EditText message_desc = (EditText)findViewById(R.id.feecback_message_desc_1);
            if(src.getPictureOutput().isActive()){
                feedback1.setChecked(true);
                picture1.setChecked(true);
                pic_desc1.setText(src.getPictureOutput().getOutputDesc());
            }

            if(src.getNumericalOutputs().size()>0 || src.getEnumOutputs().size()>0 || src.getTextOutputs().size()>0){
                feedback1.setChecked(true);
                message1.setChecked(true);
                int srcTotalNumber = src.getNumericalOutputs().size() + src.getEnumOutputs().size() +  src.getTextOutputs().size();
                //feedback_message_input1.setText(srcTotalNumber+"");
                View addedMessage;
                //number类型的output的状态保存（起点）
                for(int i = 0; i < src.getNumericalOutputs().size(); i++){
                    addedMessage = getLayoutInflater().inflate(R.layout.feedback_message_number, null);
                    TextView numberDesc = (TextView)addedMessage.findViewById(R.id.number_desc);
                    numberDesc.setText(src.getNumericalOutputs().get(i).getOutputDesc());
                    final LinearLayout intervalLl = (LinearLayout)addedMessage.findViewById(R.id.number_interval_ll);
                    CheckBox needInterval = (CheckBox)addedMessage.findViewById(R.id.number_interval);
                    needInterval.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(isChecked){
                                intervalLl.setVisibility(View.VISIBLE);
                            } else {
                                intervalLl.setVisibility(View.GONE);
                            }
                        }
                    });
                    int isIntervalActive = src.getNumericalOutputs().get(i).getInterval();
                    if(isIntervalActive == 1){
                        needInterval.setChecked(true);
                        intervalLl.setVisibility(View.VISIBLE);
                    }else{
                        needInterval.setChecked(false);
                        intervalLl.setVisibility(View.GONE);
                    }
                    EditText lowBoundET = (EditText)addedMessage.findViewById(R.id.number_min);
                    EditText upBoundET = (EditText)addedMessage.findViewById(R.id.number_max);
                    lowBoundET.setText(src.getNumericalOutputs().get(i).getLowBound()+"");
                    upBoundET.setText(src.getNumericalOutputs().get(i).getUpBound()+"");
                    Spinner spinner = (Spinner)addedMessage.findViewById(R.id.number_strategy);
                    spinner.setSelection(src.getNumericalOutputs().get(i).getNumberAggregate());

                    feedbackMessageArea1.addView(addedMessage);
                    messageFeedbackList1.add("NUMBER");
                    //Log.d("feedbacknumber",feedbackMessageArea1.getChildCount()+"");
                }
                //enum类型的状态保存（起点）
                for(int i = 0; i < src.getEnumOutputs().size(); i++){
                    addedMessage = getLayoutInflater().inflate(R.layout.feedback_message_enumeration, null);
                    TextView enumDesc = (TextView)addedMessage.findViewById(R.id.enum_desc);
                    enumDesc.setText(src.getEnumOutputs().get(i).getOutputDesc());
                    final EditText enumInput = (EditText)addedMessage.findViewById(R.id.enum_entry_input);
                    Button enumButton = (Button)addedMessage.findViewById(R.id.enum_add_button);
                    final LinearLayout enumLl = (LinearLayout)addedMessage.findViewById(R.id.enum_ll);
                   for(int j = 0; j < src.getEnumOutputs().get(i).getEntries().size(); j++){
                       View label = getLayoutInflater().inflate(R.layout.stage_output_label, null);
                       TextView labelText = (TextView)label.findViewById(R.id.one_label_text);
                       labelText.setText(src.getEnumOutputs().get(i).getEntries().get(j));
                       enumLl.addView(label);
                   }
                    enumButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(! TextUtils.isEmpty(enumInput.getText())){
                                View label = getLayoutInflater().inflate(R.layout.stage_output_label, null);
                                TextView labelText = (TextView)label.findViewById(R.id.one_label_text);
                                labelText.setText(enumInput.getText().toString());
                                enumLl.addView(label);
                                enumInput.setText("");
                            } else {
                                Toast.makeText(EditStageActivity.this,
                                        "You need fill the input", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    feedbackMessageArea1.addView(addedMessage);
                    messageFeedbackList1.add("ENUM");
                }
                //text类型的状态保存（起点）
                for(int i = 0; i < src.getTextOutputs().size();i++){
                    addedMessage = getLayoutInflater().inflate(R.layout.feedback_message_text, null);
                    TextView text = (TextView)addedMessage.findViewById(R.id.feedback_message_text_desc);
                    text.setText(src.getTextOutputs().get(i).getOutputDesc());
                    feedbackMessageArea1.addView(addedMessage);
                    messageFeedbackList1.add("TEXT");
                }
            }

//
            //End location
            RadioButton label1_r = (RadioButton)findViewById(R.id.end_location_no_requirement_2);
            RadioButton label2_r = (RadioButton)findViewById(R.id.requester_location_2);
            RadioButton label3_r = (RadioButton)findViewById(R.id.other_location_2);
            CheckBox label4_r = (CheckBox) findViewById(R.id.need_label_2);

            StageInfo.LocationInformation dest = si.getDest();
            if(dest.getLocationStrategy().equals(this.getString(R.string.location_no_requirement))){
                label1_r.setChecked(true);
            }else if(dest.getLocationStrategy().equals(this.getString(R.string.location_request_location))){
                label2_r.setChecked(true);
            }else{
                label3_r.setChecked(true);
            }

            if(dest.getInputs().size() > 0){

                label4_r.setChecked(true);
                labelList2.clear();
                for(int i = 0; i < dest.getInputs().size(); i++){
                    labelList2.add(dest.getInputs().get(i)) ;

                }
                tagAdapter2.notifyDataSetChanged();
            }


            CheckBox feedback2 = (CheckBox)findViewById(R.id.need_feedback_2);
            CheckBox picture2 = (CheckBox)findViewById(R.id.feedback_pic_checkbox__2);
            CheckBox message2 = (CheckBox)findViewById(R.id.feedback_message_checkbox_2);
            EditText pic_desc2 = (EditText)findViewById(R.id.feedback_pic_desc_2);
            EditText feedback_message_input2 = (EditText)findViewById(R.id.feedback_message_input_2);
            if(dest.getPictureOutput().isActive()){
                feedback2.setChecked(true);
                picture2.setChecked(true);
                pic_desc2.setText(dest.getPictureOutput().getOutputDesc());
            }

            if(dest.getNumericalOutputs().size()>0 || dest.getEnumOutputs().size()>0 || dest.getTextOutputs().size()>0){
                feedback2.setChecked(true);
                message2.setChecked(true);
                int destTotalNumber = dest.getNumericalOutputs().size() + dest.getEnumOutputs().size() +  dest.getTextOutputs().size();
                //feedback_message_input1.setText(srcTotalNumber+"");
                View addedMessage;
                //number类型的output的状态保存（终点）
                for(int i = 0; i < dest.getNumericalOutputs().size(); i++){
                    addedMessage = getLayoutInflater().inflate(R.layout.feedback_message_number, null);
                    TextView numberDesc = (TextView)addedMessage.findViewById(R.id.number_desc);
                    numberDesc.setText(dest.getNumericalOutputs().get(i).getOutputDesc());
                    final LinearLayout intervalLl = (LinearLayout)addedMessage.findViewById(R.id.number_interval_ll);
                    CheckBox needInterval = (CheckBox)addedMessage.findViewById(R.id.number_interval);
                    needInterval.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(isChecked){
                                intervalLl.setVisibility(View.VISIBLE);
                            } else {
                                intervalLl.setVisibility(View.GONE);
                            }
                        }
                    });
                    int isIntervalActive = dest.getNumericalOutputs().get(i).getInterval();
                    if(isIntervalActive == 1){
                        needInterval.setChecked(true);
                        intervalLl.setVisibility(View.VISIBLE);
                    }else{
                        needInterval.setChecked(false);
                        intervalLl.setVisibility(View.GONE);
                    }
                    EditText lowBoundET = (EditText)addedMessage.findViewById(R.id.number_min);
                    EditText upBoundET = (EditText)addedMessage.findViewById(R.id.number_max);
                    lowBoundET.setText(dest.getNumericalOutputs().get(i).getLowBound()+"");
                    upBoundET.setText(dest.getNumericalOutputs().get(i).getUpBound()+"");
                    Spinner spinner = (Spinner)addedMessage.findViewById(R.id.number_strategy);
                    spinner.setSelection(dest.getNumericalOutputs().get(i).getNumberAggregate());

                    feedbackMessageArea2.addView(addedMessage);
                    messageFeedbackList2.add("NUMBER");
                    //Log.d("feedbacknumber",feedbackMessageArea1.getChildCount()+"");
                }
                //enum类型的状态保存（终点）
                for(int i = 0; i < dest.getEnumOutputs().size(); i++){
                    addedMessage = getLayoutInflater().inflate(R.layout.feedback_message_enumeration, null);
                    TextView enumDesc = (TextView)addedMessage.findViewById(R.id.enum_desc);
                    enumDesc.setText(dest.getEnumOutputs().get(i).getOutputDesc());
                    final EditText enumInput = (EditText)addedMessage.findViewById(R.id.enum_entry_input);
                    Button enumButton = (Button)addedMessage.findViewById(R.id.enum_add_button);
                    final LinearLayout enumLl = (LinearLayout)addedMessage.findViewById(R.id.enum_ll);
                    for(int j = 0; j < dest.getEnumOutputs().get(i).getEntries().size(); j++){
                        View label = getLayoutInflater().inflate(R.layout.stage_output_label, null);
                        TextView labelText = (TextView)label.findViewById(R.id.one_label_text);
                        labelText.setText(dest.getEnumOutputs().get(i).getEntries().get(j));
                        enumLl.addView(label);
                    }
                    enumButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(! TextUtils.isEmpty(enumInput.getText())){
                                View label = getLayoutInflater().inflate(R.layout.stage_output_label, null);
                                TextView labelText = (TextView)label.findViewById(R.id.one_label_text);
                                labelText.setText(enumInput.getText().toString());
                                enumLl.addView(label);
                                enumInput.setText("");
                            } else {
                                Toast.makeText(EditStageActivity.this,
                                        "You need fill the input", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    feedbackMessageArea2.addView(addedMessage);
                    messageFeedbackList2.add("ENUM");
                }
                //text类型的状态保存（终点）
                for(int i = 0; i < dest.getTextOutputs().size();i++){
                    addedMessage = getLayoutInflater().inflate(R.layout.feedback_message_text, null);
                    TextView text = (TextView)addedMessage.findViewById(R.id.feedback_message_text_desc);
                    text.setText(dest.getTextOutputs().get(i).getOutputDesc());
                    feedbackMessageArea2.addView(addedMessage);
                    messageFeedbackList2.add("TEXT");
                }
            }

        }
        finishButton = (Button)findViewById(R.id.stage_finish_button);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(EditStageActivity.this).setTitle(EditStageActivity.this.getString(R.string.confirm)).
                        setMessage(EditStageActivity.this.getString(R.string.confirm_finish))
                        .setPositiveButton(EditStageActivity.this.getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                stageInfo = getTheStageInfo();
                                //阶段描述必填
                                Log.d(TAG,String.format("The value of stage desc is %s", stageInfo.getStageDescription().isEmpty()));
                                if(stageInfo.getStageDescription().isEmpty()){
                                    Toast.makeText(EditStageActivity.this, "必须填写该阶段说明！" ,Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Log.d(TAG+"finish",String.format("The stage info in stageEdit is %s", stageInfo.toString()));
                                intent.putExtra("stage",stageInfo);
                                setResult(index, intent);
                                EditStageActivity.this.finish();
                            }
                        }).show();
            }
        });


    }


    private StageInfo getTheStageInfo(){
        StageInfo stageInfo = new StageInfo();

        //stageName
        String stageName = title;
        Log.d(TAG,String.format("The stage name is %s", stageName));
        //stageDescription
        EditText description_text = (EditText)findViewById(R.id.stage_description);
        String description = description_text.getText().toString();
        Log.d(TAG,String.format("The description content is %s", description));
        //worker strategy
        RadioGroup workerStragetyRG = (RadioGroup)findViewById(R.id.select_worker_strategy);
        int selectedRadioId = workerStragetyRG.getCheckedRadioButtonId();
        RadioButton selectedButton = (RadioButton)findViewById(selectedRadioId);
        String workerStragety = selectedButton.getText().toString();
        Log.d(TAG,String.format("The selected worker strategy is %s", workerStragety));
        //worker number and aggregate method
        boolean allowMultiWorker = workerNumCheckbox.isChecked();
        Log.d(TAG,String.format("Does it allow multiworker? %b", allowMultiWorker));
        int workerNumber = 1;
        String aggregateMethod = this.getString(R.string.result_first);
        if(allowMultiWorker){
            workerNumber = Integer.valueOf(workerNumText.getText().toString());
            aggregateMethod = aggreMethod;
        }
        Log.d(TAG,String.format("The worker number is %d and the aggregate method is %s", workerNumber, aggregateMethod));
        //Get the src location information
        selectedRadioId = startRg.getCheckedRadioButtonId();
        selectedButton = (RadioButton)findViewById(selectedRadioId);
        String src_strategy = selectedButton.getText().toString();
        Log.d(TAG,String.format("The selected src location strategy is %s", src_strategy));
        List<String> src_input = labelList1;
        Log.d(TAG,String.format("The needed input of src location is %s",src_input.toString()));
        boolean src_needFeedback = needFeedback1.isChecked();
        boolean src_needPicture = picCheckbox1.isChecked();
        boolean src_needText = messageCheckbox1.isChecked();
        if(src_needFeedback){
            if(src_needPicture) {
                src_pictureOutput.setOutputDesc(picDesc1.getText().toString());
                src_pictureOutput.setOutputType(TYPE_PICT);
                src_pictureOutput.setActive(true);
            }
        }
        Log.d(TAG,String.format("Does the src need feedback? %b Does it need picture? %b Does it need text? %b",src_needFeedback,src_needPicture,src_needText));
//        Log.d(TAG,String.format("The picture description is %s and the text description is %s", src_picture_desc, src_text_desc));
        //Set the src location information
        StageInfo.LocationInformation src = new StageInfo.LocationInformation();
        src.setLocationStrategy(src_strategy);
        src.setInputs(src_input);
        src.setPictureOutput(src_pictureOutput);
        List<NumericalOutput> nolist1 = new ArrayList<NumericalOutput>();
        List<EnumOutput> eolist1 = new ArrayList<EnumOutput>();
        List<TextOutput> tolist1 = new ArrayList<TextOutput>();


        for(int i = 2; i < feedbackMessageArea1.getChildCount(); i++){
            View child = feedbackMessageArea1.getChildAt(i);
            switch (messageFeedbackList1.get(i-2)){
                case "NUMBER":
                    TextView tv = (TextView) child.findViewById(R.id.number_desc);
                    CheckBox cb = (CheckBox)child.findViewById(R.id.number_interval);
                    EditText et1 = (EditText)child.findViewById(R.id.number_min);
                    EditText et2 = (EditText)child.findViewById(R.id.number_max);
                    Spinner sp = (Spinner)child.findViewById(R.id.number_strategy);
                    NumericalOutput no = new NumericalOutput();
                    if(cb.isChecked()){
                        no.setInterval(1);
                        if(!(et1.getText().toString().equals(""))){ no.setLowBound(Double.parseDouble(et1.getText().toString()));}
                        if(!(et2.getText().toString().equals(""))){ no.setUpBound(Double.parseDouble(et2.getText().toString()));}
                    }else{
                        no.setInterval(0);
                    }
                    switch(sp.getSelectedItem().toString()){
                        case "Max":
                            no.setNumberAggregate(0);
                            break;
                        case "Min":
                            no.setNumberAggregate(1);
                            break;
                        case "Average":
                            no.setNumberAggregate(2);
                            break;
                        case "Mode":
                            no.setNumberAggregate(3);
                            break;
                        default:
                            no.setNumberAggregate(0);
                    }

                    no.setOutputType(2);
                    no.setOutputDesc(tv.getText().toString());

                    nolist1.add(no);
                    break;

                case "ENUM":
                    LinearLayout ll = (LinearLayout)child.findViewById(R.id.enum_ll);
                    TextView desc = (TextView)child.findViewById(R.id.enum_desc);
                    List<String> list = new ArrayList<String>();
                    for(int j=0;j<ll.getChildCount();j++){
                        View gchild = ll.getChildAt(j);
                        TextView gtv = (TextView) gchild.findViewById(R.id.one_label_text);
                        list.add(gtv.getText().toString());
                    }
                    EnumOutput eo = new EnumOutput();
                    eo.setEntries(list);
                    eo.setEnumAggregate(0);
                    eo.setOutputType(3);
                    eo.setOutputDesc(desc.getText().toString());

                    eolist1.add(eo);
                    break;

                case "TEXT":
                    TextView text = (TextView)child.findViewById(R.id.feedback_message_text_desc);
                    TextOutput to = new TextOutput();
                    to.setOutputDesc(text.getText().toString());
                    to.setOutputType(1);

                    tolist1.add(to);
                    break;
            }
        }
        src.setNumericalOutputs(nolist1);
        src.setEnumOutputs(eolist1);
        src.setTextOutputs(tolist1);

        //Get the dest location information
        selectedRadioId = endRg.getCheckedRadioButtonId();
        selectedButton = (RadioButton)findViewById(selectedRadioId);
        String dest_strategy = selectedButton.getText().toString();
        Log.d(TAG,String.format("The selected dest location strategy is %s", dest_strategy));
        List<String> dest_input = labelList2;
        Log.d(TAG,String.format("The needed input of dest location is %s",dest_input.toString()));
        boolean dest_needFeedback = needFeedback2.isChecked();
        boolean dest_needPicture = picCheckbox2.isChecked();
        String dest_picture_desc = "";
        boolean dest_needText = messageCheckbox2.isChecked();
        String dest_text_desc = "";
        if(dest_needFeedback){
            if(dest_needPicture) {
                dest_pictureOutput.setOutputDesc(picDesc2.getText().toString());
                dest_pictureOutput.setOutputType(TYPE_PICT);
                dest_pictureOutput.setActive(true);
            }
        }
        Log.d(TAG,String.format("Does the src need feedback? %b Does it need picture? %b Does it need text? %b",dest_needFeedback,dest_needPicture,dest_needText));
        Log.d(TAG,String.format("The picture description is %s and the text description is %s", dest_picture_desc, dest_text_desc));
        //Set the src location information
        StageInfo.LocationInformation dest = new StageInfo.LocationInformation();
        dest.setLocationStrategy(dest_strategy);
        dest.setInputs(dest_input);
        dest.setPictureOutput(dest_pictureOutput);
        List<NumericalOutput> nolist2 = new ArrayList<NumericalOutput>();
        List<EnumOutput> eolist2 = new ArrayList<EnumOutput>();
        List<TextOutput> tolist2 = new ArrayList<TextOutput>();


        for(int i = 2; i < feedbackMessageArea2.getChildCount(); i++){
            View child = feedbackMessageArea2.getChildAt(i);
            switch (messageFeedbackList2.get(i-2)){
                case "NUMBER":
                    TextView tv = (TextView) child.findViewById(R.id.number_desc);
                    CheckBox cb = (CheckBox)child.findViewById(R.id.number_interval);
                    EditText et1 = (EditText)child.findViewById(R.id.number_min);
                    EditText et2 = (EditText)child.findViewById(R.id.number_max);
                    Spinner sp = (Spinner)child.findViewById(R.id.number_strategy);
                    NumericalOutput no = new NumericalOutput();
                    if(cb.isChecked()){
                        no.setInterval(1);
                        if(!(et1.getText().toString().equals(""))){ no.setLowBound(Double.parseDouble(et1.getText().toString()));}
                        if(!(et2.getText().toString().equals(""))){ no.setUpBound(Double.parseDouble(et2.getText().toString()));}
                    }else{
                        no.setInterval(0);
                    }
                    switch(sp.getSelectedItem().toString()){
                        case "Max":
                            no.setNumberAggregate(0);
                            break;
                        case "Min":
                            no.setNumberAggregate(1);
                            break;
                        case "Average":
                            no.setNumberAggregate(2);
                            break;
                        case "Mode":
                            no.setNumberAggregate(3);
                            break;
                        default:
                            no.setNumberAggregate(0);
                    }

                    no.setOutputType(2);
                    no.setOutputDesc(tv.getText().toString());

                    nolist2.add(no);
                    break;

                case "ENUM":
                    LinearLayout ll = (LinearLayout)child.findViewById(R.id.enum_ll);
                    TextView desc = (TextView)child.findViewById(R.id.enum_desc);
                    List<String> list = new ArrayList<String>();
                    for(int j=0;j<ll.getChildCount();j++){
                        View gchild = ll.getChildAt(j);
                        TextView gtv = (TextView) gchild.findViewById(R.id.one_label_text);
                        list.add(gtv.getText().toString());
                    }
                    EnumOutput eo = new EnumOutput();
                    eo.setEntries(list);
                    eo.setEnumAggregate(0);
                    eo.setOutputType(3);
                    eo.setOutputDesc(desc.getText().toString());

                    eolist2.add(eo);
                    break;

                case "TEXT":
                    TextView text = (TextView)child.findViewById(R.id.feedback_message_text_desc);
                    TextOutput to = new TextOutput();
                    to.setOutputDesc(text.getText().toString());
                    to.setOutputType(1);

                    tolist2.add(to);
                    break;
            }
        }
        dest.setNumericalOutputs(nolist2);
        dest.setEnumOutputs(eolist2);
        dest.setTextOutputs(tolist2);

        //Set the stage information
        stageInfo.setStageName(stageName);
        stageInfo.setStageDescription(description);
        stageInfo.setWorkerStrategy(workerStragety);
        stageInfo.setAllowMultiWorker(allowMultiWorker);
        stageInfo.setWorkerNumber(workerNumber);
        stageInfo.setAggregateMethod(aggregateMethod);
        stageInfo.setSrc(src);
        stageInfo.setDest(dest);

        return stageInfo;
    }

    private void initFeedback(){
        //起点
        needFeedback1 = (CheckBox)findViewById(R.id.need_feedback_1);
        feedbackll1 = (LinearLayout)findViewById(R.id.feedback_ll_1);
        needFeedback1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    feedbackll1.setVisibility(View.VISIBLE);
                } else {
                    feedbackll1.setVisibility(View.GONE);
                }
            }
        });
        picCheckbox1 = (CheckBox)findViewById(R.id.feedback_pic_checkbox__1);
        picDesc1 = (EditText)findViewById(R.id.feedback_pic_desc_1);
        messageCheckbox1 = (CheckBox)findViewById(R.id.feedback_message_checkbox_1);
        feedbackMessageArea1 = (LinearLayout)findViewById(R.id.feedback_message_area_1);
//        messageDesc1 = (EditText)findViewById(R.id.feecback_message_desc_1);
        picCheckbox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    picDesc1.setVisibility(View.VISIBLE);
                } else {
                    picDesc1.setVisibility(View.GONE);
                }
            }
        });
        messageCheckbox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    feedbackMessageArea1.setVisibility(View.VISIBLE);
                } else {
                    feedbackMessageArea1.setVisibility(View.GONE);
                }
            }
        });

        feedbackMessageInput1 = (EditText)findViewById(R.id.feedback_message_input_1);
        feedbackMessageAddButton1 = (Button)findViewById(R.id.feedback_message_add_button_1);
        feedbackMessageType1 = (Spinner)findViewById(R.id.feedback_message_type_1);
        feedbackMessageAddButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(! TextUtils.isEmpty(feedbackMessageInput1.getText())){
                    View addedMessage = null;
                    switch(feedbackMessageType1.getSelectedItem().toString()){
                        case "Number":
                            addedMessage = getLayoutInflater().inflate(R.layout.feedback_message_number, null);
                            TextView numberDesc = (TextView)addedMessage.findViewById(R.id.number_desc);
                            numberDesc.setText(feedbackMessageInput1.getText().toString());
                            final LinearLayout intervalLl = (LinearLayout)addedMessage.findViewById(R.id.number_interval_ll);
                            CheckBox needInterval = (CheckBox)addedMessage.findViewById(R.id.number_interval);
                            needInterval.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if(isChecked){
                                        intervalLl.setVisibility(View.VISIBLE);
                                    } else {
                                        intervalLl.setVisibility(View.GONE);
                                    }
                                }
                            });
                            messageFeedbackList1.add("NUMBER");
                            break;

                        case "Enum":
                            addedMessage = getLayoutInflater().inflate(R.layout.feedback_message_enumeration, null);
                            TextView enumDesc = (TextView)addedMessage.findViewById(R.id.enum_desc);
                            enumDesc.setText(feedbackMessageInput1.getText().toString());
                            final EditText enumInput = (EditText)addedMessage.findViewById(R.id.enum_entry_input);
                            Button enumButton = (Button)addedMessage.findViewById(R.id.enum_add_button);
                            final LinearLayout enumLl = (LinearLayout)addedMessage.findViewById(R.id.enum_ll);
                            enumButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(! TextUtils.isEmpty(enumInput.getText())){
                                        View label = getLayoutInflater().inflate(R.layout.stage_output_label, null);
                                        TextView labelText = (TextView)label.findViewById(R.id.one_label_text);
                                        labelText.setText(enumInput.getText().toString());
                                        enumLl.addView(label);
                                        enumInput.setText("");
                                    } else {
                                        Toast.makeText(EditStageActivity.this,
                                                "You need fill the input", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            messageFeedbackList1.add("ENUM");
                            break;
                        case "Text":
                            addedMessage = getLayoutInflater().inflate(R.layout.feedback_message_text, null);
                            TextView text = (TextView)addedMessage.findViewById(R.id.feedback_message_text_desc);
                            text.setText(feedbackMessageInput1.getText().toString());
                            messageFeedbackList1.add("TEXT");
                            break;
                        default: Toast.makeText(EditStageActivity.this,
                                "the message type is wrong.", Toast.LENGTH_SHORT).show();
                    }
                    if(null != addedMessage){
                        feedbackMessageArea1.addView(addedMessage);
                    }
                    feedbackMessageInput1.setText("");
                } else {
                    Toast.makeText(EditStageActivity.this,
                            "You haven't input anything.", Toast.LENGTH_SHORT).show();
                }
            }
        });




        //终点
        needFeedback2 = (CheckBox)findViewById(R.id.need_feedback_2);
        feedbackll2 = (LinearLayout)findViewById(R.id.feedback_ll_2);
        needFeedback2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    feedbackll2.setVisibility(View.VISIBLE);
                } else {
                    feedbackll2.setVisibility(View.GONE);
                }
            }
        });
        picCheckbox2 = (CheckBox)findViewById(R.id.feedback_pic_checkbox__2);
        picDesc2 = (EditText)findViewById(R.id.feedback_pic_desc_2);
        messageCheckbox2 = (CheckBox)findViewById(R.id.feedback_message_checkbox_2);
        feedbackMessageArea2 = (LinearLayout)findViewById(R.id.feedback_message_area_2);
//        messageDesc1 = (EditText)findViewById(R.id.feecback_message_desc_1);
        picCheckbox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    picDesc2.setVisibility(View.VISIBLE);
                } else {
                    picDesc2.setVisibility(View.GONE);
                }
            }
        });
        messageCheckbox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    feedbackMessageArea2.setVisibility(View.VISIBLE);
                } else {
                    feedbackMessageArea2.setVisibility(View.GONE);
                }
            }
        });

        feedbackMessageInput2 = (EditText)findViewById(R.id.feedback_message_input_2);
        feedbackMessageAddButton2 = (Button)findViewById(R.id.feedback_message_add_button_2);
        feedbackMessageType2 = (Spinner)findViewById(R.id.feedback_message_type_2);
        feedbackMessageAddButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(! TextUtils.isEmpty(feedbackMessageInput2.getText())){
                    View addedMessage = null;
                    switch(feedbackMessageType2.getSelectedItem().toString()){
                        case "Number":
                            addedMessage = getLayoutInflater().inflate(R.layout.feedback_message_number, null);
                            TextView numberDesc = (TextView)addedMessage.findViewById(R.id.number_desc);
                            numberDesc.setText(feedbackMessageInput2.getText().toString());
                            final LinearLayout intervalLl = (LinearLayout)addedMessage.findViewById(R.id.number_interval_ll);
                            CheckBox needInterval = (CheckBox)addedMessage.findViewById(R.id.number_interval);
                            needInterval.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if(isChecked){
                                        intervalLl.setVisibility(View.VISIBLE);
                                    } else {
                                        intervalLl.setVisibility(View.GONE);
                                    }
                                }
                            });
                            messageFeedbackList2.add("NUMBER");
                            break;
                        case "Enum":
                            addedMessage = getLayoutInflater().inflate(R.layout.feedback_message_enumeration, null);
                            TextView enumDesc = (TextView)addedMessage.findViewById(R.id.enum_desc);
                            enumDesc.setText(feedbackMessageInput2.getText().toString());
                            final EditText enumInput = (EditText)addedMessage.findViewById(R.id.enum_entry_input);
                            Button enumButton = (Button)addedMessage.findViewById(R.id.enum_add_button);
                            final LinearLayout enumLl = (LinearLayout)addedMessage.findViewById(R.id.enum_ll);
                            enumButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(! TextUtils.isEmpty(enumInput.getText())){
                                        View label = getLayoutInflater().inflate(R.layout.stage_output_label, null);
                                        TextView labelText = (TextView)label.findViewById(R.id.one_label_text);
                                        labelText.setText(enumInput.getText().toString());
                                        enumLl.addView(label);
                                        enumInput.setText("");
                                    } else {
                                        Toast.makeText(EditStageActivity.this,
                                                "You need fill the input", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            messageFeedbackList2.add("ENUM");
                            break;
                        case "Text":
                            addedMessage = getLayoutInflater().inflate(R.layout.feedback_message_text, null);
                            TextView text = (TextView)addedMessage.findViewById(R.id.feedback_message_text_desc);
                            text.setText(feedbackMessageInput2.getText().toString());

                            messageFeedbackList2.add("TEXT");
                            break;
                        default: Toast.makeText(EditStageActivity.this,
                                "the message type is wrong.", Toast.LENGTH_SHORT).show();
                    }
                    if(null != addedMessage){
                        feedbackMessageArea2.addView(addedMessage);
                    }
                    feedbackMessageInput2.setText("");
                } else {
                    Toast.makeText(EditStageActivity.this,
                            "You haven't input anything.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //动态添加label
    private void addLabel() {
        //起点
        needLabel1 = (CheckBox)findViewById(R.id.need_label_1);
        labelll1 = (LinearLayout)findViewById(R.id.need_label_ll_1);
        showLabel1 = (TagCloudLayout)findViewById(R.id.add_input_label_show_1);
        tagAdapter1= new TagBaseAdapter(this, labelList1);
        showLabel1.setAdapter(tagAdapter1);
        labelText1 = (EditText)findViewById(R.id.add_input_label_text_1);
        addLabelButton1 = (Button)findViewById(R.id.add_input_label_button_1);

        needLabel1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    labelll1.setVisibility(View.VISIBLE);
                } else {
                    labelList1.clear();
                    tagAdapter1.notifyDataSetChanged();
                    labelll1.setVisibility(View.GONE);
                }
            }
        });
        addLabelButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String labelString = labelText1.getText().toString();
                if(labelString!= null && !labelString.isEmpty()){
                    labelList1.add(labelString);
                    labelText1.setText("");
                    tagAdapter1.notifyDataSetChanged();
                }
            }
        });

        //终点
        needLabel2 = (CheckBox)findViewById(R.id.need_label_2);
        labelll2 = (LinearLayout)findViewById(R.id.need_label_ll_2);
        showLabel2 = (TagCloudLayout)findViewById(R.id.add_input_label_show_2);
        tagAdapter2 = new TagBaseAdapter(this, labelList2);
        showLabel2.setAdapter(tagAdapter2);
        labelText2 = (EditText)findViewById(R.id.add_input_label_text_2);
        addLabelButton2 = (Button)findViewById(R.id.add_input_label_button_2);

        needLabel2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    labelll2.setVisibility(View.VISIBLE);
                } else {
                    labelList2.clear();
                    tagAdapter2.notifyDataSetChanged();
                    labelll2.setVisibility(View.GONE);
                }
            }
        });
        addLabelButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String labelString = labelText2.getText().toString();
                if(labelString!= null && !labelString.isEmpty()){
                    labelList2.add(labelString);
                    labelText2.setText("");
                    tagAdapter2.notifyDataSetChanged();
                }
            }
        });

    }

    //标签adapter
    public class TagBaseAdapter extends BaseAdapter {
        private Context mContext;
        private List<String> mList;

        public TagBaseAdapter(Context context, List list) {
            mContext = context;
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public String getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout ll = (LinearLayout)LayoutInflater.from(mContext).inflate(R.layout.stage_input_label, null);
            TextView text = (TextView)ll.findViewById(R.id.one_label_text);
            text.setText(getItem(position));
            final  int p = position;
            ImageButton ib = (ImageButton)ll.findViewById(R.id.input_label_delete);
            ib.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteInfo(p);
                }
            });

            return ll;
        }
    }

    //删除标签弹出框
    public void showDeleteInfo(final int index){
        new AlertDialog.Builder(this).setTitle(this.getString(R.string.confirm)).setMessage(this.getString(R.string.confirm_delete_tag))
                .setPositiveButton(this.getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(locationFlag == 1){ //起点
                            if(index >= 0 && index < labelList1.size()){
                                labelList1.remove(index);
                                tagAdapter1.notifyDataSetChanged();
                            }
                        } else {//终点
                            if(index >= 0 && index < labelList2.size()){
                                labelList2.remove(index);
                                tagAdapter2.notifyDataSetChanged();
                            }
                        }
                    }
                }).show();
    }

    //初始化位置选择控件
    private void initLocation(){
        //起点终点切换
        rg = (RadioGroup)findViewById(R.id.stage_select_location_group);
        startView = (LinearLayout)findViewById(R.id.stage_select_location_module_1);
        endView = (LinearLayout)findViewById(R.id.stage_select_location_module_2);
        startRg = (RadioGroup)findViewById(R.id.location_strategy_1);
        endRg = (RadioGroup)findViewById(R.id.location_strategy_2);

        //判断显示起点地图还是终点地图
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if(checkedId == R.id.start_location_radio){
                    startView.setVisibility(View.VISIBLE);
                    endView.setVisibility(View.GONE);
                    locationFlag = 1;
                } else {
                    startView.setVisibility(View.GONE);
                    endView.setVisibility(View.VISIBLE);
                    locationFlag = 2;
                }
            }
        });

        //动态添加label
        addLabel();
        //反馈方式
        initFeedback();
    }


    private void initWorkerStrategy(){
        workerNumText = (EditText)findViewById(R.id.worker_num_text);
        workerNumText.setSaveEnabled(false);
        workerNum = Integer.valueOf(workerNumText.getText().toString());
        workerNumCheckbox = (CheckBox)findViewById(R.id.worker_num_checkbox);
        multiWorkerShow = (LinearLayout)findViewById(R.id.multi_worker_show);
        workerNumMinus = (ImageButton)findViewById(R.id.worker_num_minus);
        workerNumPlus = (ImageButton)findViewById(R.id.worker_num_plus);
        workerNumCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    multiWorkerShow.setVisibility(View.VISIBLE);
                } else {
                    multiWorkerShow.setVisibility(View.GONE);
                }
            }
        });
        workerNumMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(workerNum > 2){
                    workerNum --;
                    workerNumText.setText(String.valueOf(workerNum));//必须这样写，否则会去找id为这个整数的资源
                }
            }
        });
        workerNumPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workerNum++;
                workerNumText.setText(String.valueOf(workerNum));
            }
        });

        aggregationMethods.add(this.getString(R.string.result_first));
        aggregationMethods.add(this.getString(R.string.result_most));
        aggregationMethods.add(this.getString(R.string.result_select));
        aggregationAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,aggregationMethods);
        aggregationAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        resultAggregationSpinner = (Spinner)findViewById(R.id.result_aggregation_spinner);
        resultAggregationSpinner.setAdapter(aggregationAdapter);
        resultAggregationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aggreMethod = aggregationMethods.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

}
