package com.lab.se.crowdframe;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.lab.se.crowdframe.entity.EnumOutput;
import com.lab.se.crowdframe.entity.NumericalOutput;
import com.lab.se.crowdframe.entity.PictureOutput;
import com.lab.se.crowdframe.entity.StageInfo;
import com.lab.se.crowdframe.entity.TemplateInfo;
import com.lab.se.crowdframe.entity.SysApplication;
import com.lab.se.crowdframe.entity.TextOutput;
import com.lab.se.crowdframe.http.HttpUtil;
import com.lab.se.crowdframe.http.JSONUtils;
import com.lab.se.crowdframe.http.ServletResponseData;
import com.lab.se.crowdframe.servlet.SaveTemplateServlet;
import com.lab.se.crowdframe.servlet.UploadFile;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.FieldDictionary;
import com.thoughtworks.xstream.converters.reflection.SortableFieldKeySorter;
import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Response;

public class EditTempActivity extends AppCompatActivity {
    private static final int CREATE_TEMPLATE = -1, INIT_HEAT = 0;
    CrowdFrameApplication app;
    SimpleAdapter adapter;
    Toolbar mToolbar;
    private static final String TAG = "EditTempActivity";
    private ListView list;
    private static final int EDIT_STAGE = 1,EDIT_TEMPLATE_FINISH = 1, EDIT_TEMPLATE_CANCEL = -1;
    String mDetail;
    private ArrayList<Map<String, Object>> mData = new  ArrayList<Map<String, Object>>();
    private Map<Integer,StageInfo> stageMap = new HashMap<Integer,StageInfo>();
    private List<StageInfo> stages = new ArrayList<StageInfo>();
    private TemplateInfo template = new TemplateInfo();
    private Map<Integer, Integer> stageClicked = new HashMap<Integer, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_temp);
        SysApplication.getInstance().addActivity(this);
        app = (CrowdFrameApplication)getApplication();
        mDetail = this.getString(R.string.default_stage_desc);
        //返回按钮
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.app.AlertDialog.Builder(EditTempActivity.this).setTitle(EditTempActivity.this.getString(R.string.confirm)).setMessage(EditTempActivity.this.getString(R.string.confirm_edit_template))
                        .setPositiveButton(EditTempActivity.this.getString(R.string.confirm_yes), new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                setResult(EDIT_TEMPLATE_CANCEL, intent);
                                EditTempActivity.this.finish();
                            }
                        }).show();
            }

        });

        list = (ListView)findViewById(R.id.stage_list);
        Map<String,Object> item = new HashMap<String,Object>();
        item.put("index", 1);
        item.put("detail", mDetail);
        mData.add(item);

        adapter = new SimpleAdapter(getApplicationContext(),mData,R.layout.stage_in_edit_temp,
                new String[]{"index","detail"},new int[]{R.id.stage_index,R.id.stage_detail}){
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                final int p = position;
                final View view = super.getView(position, convertView, parent);
                ImageView iv = (ImageView) view.findViewById(R.id.stage_delete);
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDeleteInfo(p);
                    }
                });

                ImageView iv2 = (ImageView) view.findViewById(R.id.stage_setting);
                iv2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(EditTempActivity.this,EditStageActivity.class);

                        //Get the index of stage and then construct the name of stage
                        int index = (Integer)mData.get(p).get("index");
                        String stageName = String.format("Stage%s",index);
                        StageInfo si = stageMap.get(p+1);
                        if(si != null){
                            intent.putExtra("isFirst",0);
                            intent.putExtra("content",si);
                        }
                        else{
                            intent.putExtra("isFirst",1);
                        }
                        //Set the needed info and then start activity for result
                        intent.putExtra("title",stageName);
                        intent.putExtra("index",index);
                        startActivityForResult(intent,EDIT_STAGE);
                    }
                });
                return view;
            }
        };

        list.setAdapter(adapter);
        
        Button buttonAddStage = (Button)findViewById(R.id.add_stage);
        buttonAddStage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int mDataSize = mData.size();
                Map<String,Object> item = new HashMap<String,Object>();
                item.put("index", mDataSize+1);
                item.put("detail", EditTempActivity.this.getString(R.string.new_stage_desc));
                mData.add(item);
                list.setAdapter(adapter);

                Toast.makeText(getApplicationContext(),EditTempActivity.this.getString(R.string.add_stage_successful),Toast.LENGTH_SHORT).show();
            }
        });

        Button buttonFinishEdit = (Button)findViewById(R.id.finish_edit_template);
        buttonFinishEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.app.AlertDialog.Builder(EditTempActivity.this).setTitle(EditTempActivity.this.getString(R.string.confirm)).setMessage(EditTempActivity.this.getString(R.string.confirm_finish))
                        .setPositiveButton(EditTempActivity.this.getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(validate()){

                                    //获得模板的名字和描述信息
                                    EditText name_text = (EditText)findViewById(R.id.template_name);
                                    String templateName = name_text.getText().toString();
                                    EditText desc_text = (EditText)findViewById(R.id.template_description);
                                    String templateDesc = desc_text.getText().toString();
                                    Log.d(TAG,String.format("The template name is %s and the template description is %s",templateName,templateDesc));
                                    //获得stages的信息
                                    int iteratorIndex = 1;
                                    while(stageMap.get(iteratorIndex) != null){
                                        stages.add(stageMap.get(iteratorIndex));
                                        iteratorIndex++;
                                    }
                                    for(StageInfo stage: stages){
                                        Log.d(TAG,String.format("The stage name is %s", stage.getStageName()));
                                    }
                                    //设置template的信息
                                    template.setName(templateName);
                                    template.setDescription(templateDesc);
                                    template.setStages(stages);

                                    //写入到xml文件
                                    XStream xstream = new XStream();
                                    //设置XML里内容的顺序
                                    SortableFieldKeySorter sorter = new SortableFieldKeySorter();
                                    String[] templateOrder = new String[] { "name", "description", "stages" };
                                    String[] stageOrder = new String[] { "stageName","stageDescription", "workerStrategy","allowMultiWorker","workerNumber","aggregateMethod","src","dest" };
                                    String[] locationOrder = new String[]{"locationStrategy","inputs","pictureOutput","textOutputs","numericalOutputs","enumOutputs"};
                                    String[] pictureOrder = new String[]{"outputType","outputDesc","outputValue","isActive"};
                                    String[] textOrder = new String[]{"outputType","outputDesc","outputValue"};
                                    String[] numericalOrder = new String[]{"outputType","outputDesc","outputValue","interval","upBound","lowBound","numberAggregate"};
                                    String[] enumOrder = new String[]{"outputType","outputDesc","outputValue","entries","enumAggregate"};
                                    sorter.registerFieldOrder(TemplateInfo.class, templateOrder);
                                    sorter.registerFieldOrder(StageInfo.class, stageOrder);
                                    sorter.registerFieldOrder(StageInfo.LocationInformation.class, locationOrder);
                                    sorter.registerFieldOrder(PictureOutput.class, pictureOrder);
                                    sorter.registerFieldOrder(TextOutput.class, textOrder);
                                    sorter.registerFieldOrder(NumericalOutput.class, numericalOrder);
                                    sorter.registerFieldOrder(EnumOutput.class, enumOrder);
                                    //开始写入
                                    xstream = new XStream(new Sun14ReflectionProvider(new FieldDictionary(sorter)));
                                    //设置别名
                                    xstream.alias("template",TemplateInfo.class);
                                    xstream.alias("stage",StageInfo.class);
                                    xstream.alias("location",StageInfo.LocationInformation.class);
                                    xstream.alias("pictureOutput",PictureOutput.class);
                                    xstream.alias("textOutput",TextOutput.class);
                                    xstream.alias("numericalOutput",NumericalOutput.class);
                                    xstream.alias("enumOutput",EnumOutput.class);
                                    String xml = xstream.toXML(template);

                                /*
                                 写入到XML文件之中
                                 */
                                    //设置存储路径和文件名
                                    String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                                    //存储文件名去除用户输入的模板名字，防止中文乱码
                                    String name = app.getUserId() + "-" + time;
                                    String fileNmae = EditTempActivity.this.getApplicationContext().getFilesDir().toString() + File.separator + "template/" + name + ".xml";
                                    String xmlPath = fileNmae;
                                    Log.d(TAG,String.format("The path of xml is %s ", xmlPath));
                                    File xmlFile = new File(xmlPath);
                                    try {
                                        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                                            if(!xmlFile.getParentFile().exists()){
                                                xmlFile.getParentFile().mkdirs();
                                            }
                                            //将xml内容写入之前指定的路径和文件名
                                            BufferedOutputStream bos;
                                            bos = new BufferedOutputStream(new FileOutputStream(xmlFile));
                                            byte[] bytes = xml.getBytes();
                                            bos.write(bytes);
                                            bos.flush();
                                            bos.close();
//                                        Toast toast= Toast.makeText(EditTempActivity.this, EditTempActivity.this.getString(R.string.create_template_successfully), Toast.LENGTH_SHORT);
//                                        toast.setGravity(Gravity.CENTER, 0, 0);
//                                        toast.show();

                                            //上传到服务器端
                                            final HashMap<String,String> postData = new HashMap<String, String>();
                                            postData.put("fileName",name);
                                            HttpUtil.uploadFile(xmlPath,postData, new okhttp3.Callback() {
                                                @Override
                                                public void onResponse(Call call, Response response) throws IOException {
                                                    try{
                                                        ServletResponseData responseData = JSONUtils.toBean(response.body().string(), ServletResponseData.class);
                                                        //判断是否上传成功，如果成功则存储url
                                                        int result = responseData.getResult();
                                                        if(result == 1){
                                                            UploadFile.ResponseBO responseBO = JSONUtils.toBean(responseData.getData(),UploadFile.ResponseBO.class);
                                                            String templateUri = responseBO.getUri();
                                                            Log.d(TAG,String.format("The result from the server is %s",templateUri));

                                                            //发布模板
                                                            final HashMap<String,String> postData = new HashMap<String, String>();

                                                            SaveTemplateServlet.RequestBO requestBO = new SaveTemplateServlet.RequestBO();
                                                            requestBO.setTemplateId(CREATE_TEMPLATE);
                                                            requestBO.setUserId(app.getUserId());
                                                            requestBO.setName(template.getName());
                                                            requestBO.setDescription(template.getDescription());
                                                            requestBO.setHeat(INIT_HEAT);
                                                            requestBO.setUri(templateUri);
                                                            //获得当前时间
                                                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                                                            String timeStr = format.format(currentTime);
                                                            currentTime = Timestamp.valueOf(timeStr);
                                                            Log.d(TAG,String.format("The current timestamp is %s",currentTime));

                                                            requestBO.setCreateTime(currentTime);
                                                            requestBO.setTotalStageNum(template.getStages().size());

                                                            postData.put("data", JSONUtils.toJSONString(requestBO));

                                                            String servlet = "SaveTemplateServlet";
                                                            HttpUtil.doPost(servlet, postData, new okhttp3.Callback() {
                                                                @Override
                                                                public void onResponse(Call call, Response response) throws IOException {
                                                                    try {
                                                                        ServletResponseData responseData = JSONUtils.toBean(response.body().string(), ServletResponseData.class);
                                                                        //判断是否发布成功
                                                                        int result = responseData.getResult();
                                                                        if (result == 1) {
                                                                            Log.d(TAG,"You have created the template successfully!");
                                                                            Intent intent = new Intent();
                                                                            setResult(EDIT_TEMPLATE_FINISH, intent);
                                                                            EditTempActivity.this.finish();
                                                                        } else {
                                                                            Log.d(TAG,"Create the template fail!");
                                                                        }
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Call call, IOException e) {
                                                                    Log.d(TAG,"OKhttp FAIL");
                                                                }
                                                            });

                                                        }
                                                    }
                                                    catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                @Override
                                                public void onFailure(Call call, IOException e) {
                                                    e.printStackTrace();
                                                }
                                            });

                                        }else{
                                            Toast toast= Toast.makeText(EditTempActivity.this, "Fail to reate the template! The SD card is invalid.", Toast.LENGTH_SHORT);
                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                            toast.show();
                                        }
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    Log.d(TAG,xml);
                                }
                                else{
                                    new android.app.AlertDialog.Builder(EditTempActivity.this).setTitle(EditTempActivity.this.getString(R.string.warn)).setMessage(EditTempActivity.this.getString(R.string.warn_left_stage))
                                            .setPositiveButton(EditTempActivity.this.getString(R.string.confirm_yes), new DialogInterface.OnClickListener(){
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            }).show();
                                }
                            }
                        }).show();
            }
        });
    }

    private boolean validate(){
        boolean isValid = true;
        if(stageClicked.size() < mData.size()){
            isValid = false;
        }
        return isValid;
    }

    public void showDeleteInfo(final int position) {
        new AlertDialog.Builder(this).setTitle(EditTempActivity.this.getString(R.string.confirm)).setMessage(EditTempActivity.this.getString(R.string.confirm_delete))
                .setPositiveButton(EditTempActivity.this.getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mData.remove(position);
                        int mDataSize = mData.size();
                        for(int i = position; i < mDataSize; i++) {
                            int mIndex = (Integer) mData.get(i).get("index");
                            mIndex--;
                            mData.get(i).put("index", mIndex);
                        }

                        list.setAdapter(adapter);
                    }
                }).show();
    }

    //获取填好的stage信息
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == EDIT_STAGE){
            if(resultCode > 0){
                Log.d(TAG,String.format("The resultcode is %d",resultCode));
                int stageIndex = resultCode;
                StageInfo stage = (StageInfo)data.getSerializableExtra("stage");
                Log.d(TAG,String.format("The stage description is %s", stage.getStageDescription()));
                int mDataSize = mData.size();
                for(int i = 0; i < mDataSize; i++) {
                    int mIndex = (Integer) mData.get(i).get("index");
                    if(mIndex == stageIndex){
                        mData.get(i).put("detail",stage.getStageDescription());
                        break;
                    }
                }
                list.setAdapter(adapter);
                stageMap.put(stageIndex,stage);
                stageClicked.put(stageIndex,1);
            }
            else{
                Log.d(TAG,"You have cancelled the modification!");
            }
        }
    }
}
