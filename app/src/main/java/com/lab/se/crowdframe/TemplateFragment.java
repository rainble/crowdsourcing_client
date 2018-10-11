package com.lab.se.crowdframe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.lab.se.crowdframe.entity.EnumOutput;
import com.lab.se.crowdframe.entity.NumericalOutput;
import com.lab.se.crowdframe.entity.PictureOutput;
import com.lab.se.crowdframe.entity.StageInfo;
import com.lab.se.crowdframe.entity.TemplateInfo;
import com.lab.se.crowdframe.entity.TextOutput;
import com.lab.se.crowdframe.http.HttpUtil;
import com.lab.se.crowdframe.http.JSONUtils;
import com.lab.se.crowdframe.http.ServletResponseData;
import com.lab.se.crowdframe.servlet.AcceptTaskServlet;
import com.lab.se.crowdframe.servlet.CollectTemplateServlet;
import com.lab.se.crowdframe.servlet.GetAllTemplateServlet;
import com.lab.se.crowdframe.servlet.GetTaskInfoServlet;
import com.lab.se.crowdframe.servlet.GetTemplateCollectionServlet;
import com.lab.se.crowdframe.servlet.SearchTemplateServlet;
import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TemplateFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TemplateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TemplateFragment extends Fragment{
    private static int VISITED = 0;
    private static final String TAG = "TemplateFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final int EDIT_TEMPLATE = 1, COLLECT_TEMPLATE = 1,COLLECT_SUCCESS = 1, COLLECT_FAIL = -1;
    private String mParam1;
    List<String> detailList;
    TemplateInfo templateInfo;
    private Map<String,Object> item;
    List<GetAllTemplateServlet.ResponseBO.TemplateBO> tList;
    List<SearchTemplateServlet.ResponseBO.TemplateBO> tList2;
    private ArrayList<Map<String, Object>> templates = new  ArrayList<Map<String, Object>>();

    CrowdFrameApplication app;
    private OnFragmentInteractionListener mListener;
    SimpleAdapter adapter;
    ListView list;
    ImageView refreshImageView;
    Button addButton;
    private SearchView mSearchView;
    private ListView mListView;
    TextView noData;
    ProgressDialog progressDialog;


    public TemplateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment TemplateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TemplateFragment newInstance(String param1) {
        TemplateFragment fragment = new TemplateFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        app = (CrowdFrameApplication)getActivity().getApplication();
        View view = inflater.inflate(R.layout.fragment_template, null);
        progressDialog = new ProgressDialog(getActivity(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("请稍候...");
        VISITED = 1;

        //这是一个隐藏的控件，在初始化fragment时获得焦点，防止每次点进去时焦点都在editText而弹出软键盘
        TextView textView = (TextView)view.findViewById(R.id.text_notuse);
        textView.requestFocus();
        noData = (TextView)view.findViewById(R.id.noData);

        refreshImageView = (ImageView)view.findViewById(R.id.refresh);
        refreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestList();
                Toast.makeText(getActivity(),((Activity)getContext()).getString(R.string.template_update),Toast.LENGTH_SHORT).show();
            }
        });

        mSearchView = (SearchView) view.findViewById(R.id.searchView);
        mListView = (ListView) view.findViewById(R.id.searchListView);

        // 设置搜索文本监听
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                requestSearchList(query);
                return false;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)){
                    mListView.setFilterText(newText);
                }else{
                    mListView.clearTextFilter();
                    requestList();
                }
                return false;
            }
        });

        list = (ListView)view.findViewById(R.id.template_list);

        requestList();

        //重写SimpleAdapter方法，以实现对list中的每一个item分别进行监听器绑定
       adapter = new SimpleAdapter(getActivity(), templates,R.layout.fragment_template_item,
                new String[]{"title","publisher","desc","stages","date","used"},new int[]{R.id.template_title,R.id.template_publisher,R.id.template_desc
                ,R.id.template_stage,R.id.template_number,R.id.template_used}){
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                final int p = position;
                final View view = super.getView(position, convertView, parent);

                //编号
                final TextView tv=(TextView) view.findViewById(R.id.template_number);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(),((Activity)getContext()).getString(R.string.template_release_time),Toast.LENGTH_LONG).show();

                    }
                });
                final ImageView iv = (ImageView)view.findViewById(R.id.sketch_1);
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(),((Activity)getContext()).getString(R.string.template_release_time),Toast.LENGTH_LONG).show();
                    }
                });

                //步骤数
                final TextView tv2=(TextView) view.findViewById(R.id.template_stage);
                tv2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(),((Activity)getContext()).getString(R.string.number_of_stages),Toast.LENGTH_LONG).show();

                    }
                });
                final ImageView iv2 = (ImageView)view.findViewById(R.id.sketch_2);
                iv2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(),((Activity)getContext()).getString(R.string.number_of_stages),Toast.LENGTH_LONG).show();
                    }
                });

                //热度

                final TextView tv3=(TextView) view.findViewById(R.id.template_used);
                tv3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(),((Activity)getContext()).getString(R.string.template_heat),Toast.LENGTH_LONG).show();

                    }
                });
                final ImageView iv3 = (ImageView)view.findViewById(R.id.sketch_3);
                iv3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(),((Activity)getContext()).getString(R.string.template_heat),Toast.LENGTH_LONG).show();
                    }
                });

                //任务详情
                LinearLayout llDetail = (LinearLayout)view.findViewById(R.id.template_detail);
                llDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(getContext(),templates.get(p).get("uri").toString()+"||"+templates.get(p).get("uri").toString(),Toast.LENGTH_LONG).show();
                        //Log.d("fzj",templates.size()+"");
                        downloadTemplateAndParse(templates.get(p).get("uri").toString());

                    }
                });

                //收藏模板按钮
                Button buttonAdopt = (Button) view.findViewById(R.id.collectTemp);
                buttonAdopt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //收藏模板
                        showAddInfo(position);
                    }
                });
                return view;
            }
        };

        list.setAdapter(adapter);

        addButton = (Button)view.findViewById(R.id.add_temp);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),EditTempActivity.class);
                startActivityForResult(intent,EDIT_TEMPLATE);
            }
        });
        return view;
    }

    public void showAddInfo(final int p) {
        new android.support.v7.app.AlertDialog.Builder(getContext()).setTitle(((Activity)getContext()).getString(R.string.confirm)).setMessage(((Activity)getContext()).getString(R.string.confirm_collect_template))
                .setPositiveButton(((Activity)getContext()).getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        progressDialog.show();
                        int templateId = (Integer)templates.get(p).get("templateId");
                        Log.d(TAG,String.format("The templateId is %d",templateId));

                        /*发送post请求*/
                        final HashMap<String,String> postData = new HashMap<String, String>();
                        CollectTemplateServlet.RequestBO requestBO = new CollectTemplateServlet.RequestBO();
                        requestBO.setUserId(app.getUserId());
                        requestBO.setTemplateId(templateId);requestBO.setIndicator(COLLECT_TEMPLATE);
                        postData.put("data", JSONUtils.toJSONString(requestBO));

                        String servlet = "CollectTemplateServlet";
                        HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    ServletResponseData responseData = JSONUtils.toBean(response.body().string(), ServletResponseData.class);

                                    int result = responseData.getResult();

                                    if (result == 1) {
                                        showCollectResult(COLLECT_SUCCESS);
                                    }
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                    Log.d(TAG,"Exception");
                                    showCollectResult(COLLECT_FAIL);
                                }
                            }

                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.d(TAG,"Failure");
                                showCollectResult(COLLECT_FAIL);
                            }
                        });
                    }
                }).show();
    }

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

        final String templateDir = this.getActivity().getApplicationContext().getFilesDir().toString() + File.separator + "template/";
        final String templateName = templateUrl.substring(templateUrl.lastIndexOf('/') + 1);
        final File templateFile = new File(templateDir,templateName);
        if(!templateFile.getParentFile().exists()){
            templateFile.getParentFile().mkdirs();
        }
        Log.d(TAG,String.format("The template name is %s", templateName));

        if(templateFile.exists()){
            Log.d(TAG,"The file already exist");
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

    private void showTaskContent(){
        ((Activity)getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());//创建对话框
                ScrollView sv = (ScrollView) getActivity().getLayoutInflater()
                                .inflate(R.layout.task_template_detail,null);
                TextView taskTitle = (TextView)sv.findViewById(R.id.task_template_title);
                taskTitle.setText(templateInfo.getName());
                TextView taskDesc = (TextView)sv.findViewById(R.id.task_template_desc);
                taskDesc.setText(templateInfo.getDescription());

                LinearLayout taskDetailLl = (LinearLayout)sv.findViewById(R.id.task_template_detail_ll);
                for(int i= 0; i < templateInfo.getStages().size(); i++){
                    StageInfo stage = templateInfo.getStages().get(i);
                    TableLayout stageTable = (TableLayout)getActivity().getLayoutInflater()
                            .inflate(R.layout.stage_template_detail, null);
                    TextView stageName = (TextView)stageTable.findViewById(R.id.stage_name);
                    stageName.setText(stage.getStageName());
                    TextView stageDesc = (TextView)stageTable.findViewById(R.id.stage_desc);
                    stageDesc.setText(stage.getStageDescription());

                    TextView stageWorkerStrategy = (TextView)stageTable.findViewById(R.id.stage_worker_strategy);
                    stageWorkerStrategy.setText(stage.getWorkerStrategy());
                    TextView stageMultiWorker = (TextView)stageTable.findViewById(R.id.stage_multi_worker);
                    TableRow workerNumRow = (TableRow)stageTable.findViewById(R.id.worker_number_row);
                    TextView multiWorkerNumber = (TextView)stageTable.findViewById(R.id.multi_worker_number);
                    if (stage.isAllowMultiWorker()) {
                        stageMultiWorker.setText("允许");
                        workerNumRow.setVisibility(View.VISIBLE);
                        multiWorkerNumber.setText(String.valueOf(stage.getWorkerNumber()));
                    } else {
                        stageMultiWorker.setText("不允许");
                    }
                    TextView resultAggregationMethod = (TextView)stageTable.findViewById(R.id.result_aggregation_method);
                    resultAggregationMethod.setText(stage.getAggregateMethod());

                    //start location
                    StageInfo.LocationInformation start = stage.getSrc();
                    TextView startLocationSelectMethod = (TextView)stageTable.findViewById(R.id.start_location_select_method);
                    startLocationSelectMethod.setText(start.getLocationStrategy());
                    if(! start.getLocationStrategy().equals("不需要")){
                        if(null != start.getInputs() && start.getInputs().size() > 0){
                            TableRow startInputRow = (TableRow)stageTable.findViewById(R.id.start_input_row);
                            startInputRow.setVisibility(View.VISIBLE);
                            TextView startInput  = (TextView) stageTable.findViewById(R.id.start_input);
                            startInput.setText(listToString(start.getInputs()));
                        }

                        TableLayout startFeedback  = (TableLayout)stageTable.findViewById(R.id.start_feedback);
                        if(null !=  start.getPictureOutput()){
                            if(null != start.getPictureOutput().getOutputDesc()){
                                startFeedback.setVisibility(View.VISIBLE);
                                TextView t2 = new TextView(getActivity());
                                t2.setText("图片：" + start.getPictureOutput().getOutputDesc());
                                startFeedback.addView(t2);
                            }
                        }
                        if (start.getTextOutputs().size() + start.getEnumOutputs().size() +
                                start.getNumericalOutputs().size() > 0) {
                            startFeedback.setVisibility(View.VISIBLE);
                            for (TextOutput to : start.getTextOutputs()) {
                                TextView t2 = new TextView(getActivity());
                                t2.setText("文本：" + start.getPictureOutput().getOutputDesc());
                                startFeedback.addView(t2);
                            }
                            for (EnumOutput eo : start.getEnumOutputs()) {
                                TextView t2 = new TextView(getActivity());
                                t2.setText("枚举：" + start.getPictureOutput().getOutputDesc());
                                startFeedback.addView(t2);
                            }
                            for (NumericalOutput no : start.getNumericalOutputs()) {
                                TextView t2 = new TextView(getActivity());
                                t2.setText("数字：" + start.getPictureOutput().getOutputDesc());
                                startFeedback.addView(t2);
                            }
                        }
                    }

                    //end location
                    StageInfo.LocationInformation end = stage.getDest();
                    TextView endLocationSelectMethod = (TextView)stageTable.findViewById(R.id.end_location_select_method);
                    endLocationSelectMethod.setText(end.getLocationStrategy());
                    if(! end.getLocationStrategy().equals("不需要")){
                        if(null != end.getInputs() && end.getInputs().size() > 0){
                            TableRow endInputRow = (TableRow)stageTable.findViewById(R.id.end_input_row);
                            endInputRow.setVisibility(View.VISIBLE);
                            TextView endInput  = (TextView) stageTable.findViewById(R.id.end_input);
                            endInput.setText(listToString(end.getInputs()));
                        }

                        TableLayout endFeedback  = (TableLayout)stageTable.findViewById(R.id.end_feedback);
                        if(null !=  end.getPictureOutput()){
                            if(null != end.getPictureOutput().getOutputDesc()){
                                endFeedback.setVisibility(View.VISIBLE);
                                TextView t2 = new TextView(getActivity());
                                t2.setText("图片：" + end.getPictureOutput().getOutputDesc());
                                endFeedback.addView(t2);
                            }
                        }
                        if (end.getTextOutputs().size() + end.getEnumOutputs().size() +
                                end.getNumericalOutputs().size() > 0) {
                            endFeedback.setVisibility(View.VISIBLE);
                            for (TextOutput to : end.getTextOutputs()) {
                                TextView t2 = new TextView(getActivity());
                                t2.setText("文本：" + to.getOutputDesc());
                                endFeedback.addView(t2);
                            }
                            for (EnumOutput eo : end.getEnumOutputs()) {
                                TextView t2 = new TextView(getActivity());
                                t2.setText("枚举：" + eo.getOutputDesc());
                                endFeedback.addView(t2);
                            }
                            for (NumericalOutput no : end.getNumericalOutputs()) {
                                TextView t2 = new TextView(getActivity());
                                t2.setText("数字：" + no.getOutputDesc());
                                endFeedback.addView(t2);
                            }
                        }

                    }

                    taskDetailLl.addView(stageTable);
                }

                builder.setView(sv);
                builder.setPositiveButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
//                detailList = new ArrayList<String>();
//                detailList.add(((Activity)getContext()).getString(R.string.template_overview_desc) + templateInfo.getDescription());
//                for (int i = 0; i < templateInfo.getStages().size(); i++) {
//                    detailList.add( ((Activity)getContext()).getString(R.string.template_overview_stage)+ (i + 1) + " -------------------------------------");
//                    detailList.add(((Activity)getContext()).getString(R.string.template_overview_stage_desc) + templateInfo.getStages().get(i).getStageDescription());
//                    detailList.add(((Activity)getContext()).getString(R.string.template_overview_stage_worker_strategy) + templateInfo.getStages().get(i).getWorkerStrategy());
//                    if (templateInfo.getStages().get(i).isAllowMultiWorker()) {
//                        detailList.add(((Activity)getContext()).getString(R.string.template_overview_stage_multiworker) + ((Activity)getContext()).getString(R.string.template_stage_allow_multiple_worker));
//                        detailList.add(((Activity)getContext()).getString(R.string.template_overview_worker_number_limit) + templateInfo.getStages().get(i).getWorkerNumber());
//                    } else {
//                        detailList.add(((Activity)getContext()).getString(R.string.template_overview_stage_multiworker) + ((Activity)getContext()).getString(R.string.template_stage_not_allow_multiple_worker));
//                    }
//                    detailList.add(((Activity)getContext()).getString(R.string.template_overview_stage_aggregate_method) + templateInfo.getStages().get(i).getAggregateMethod());
//                    detailList.add(((Activity)getContext()).getString(R.string.start_location_strategy) + templateInfo.getStages().get(i).getSrc().getLocationStrategy());
//                    if (templateInfo.getStages().get(i).getSrc().getInputs().size() > 0) {
//                        String temp = ((Activity)getContext()).getString(R.string.start_location_inputs);
//                        for (String s : templateInfo.getStages().get(i).getSrc().getInputs()) {
//                            temp += s;
//                            temp += ", ";
//                        }
//                        temp = temp.substring(0, temp.length() - 2);
//                        detailList.add(temp);
//                    }
//
//                    if (templateInfo.getStages().get(i).getSrc().getPictureOutput().getOutputDesc() != null) {
//                        detailList.add(((Activity)getContext()).getString(R.string.start_picture_output) + templateInfo.getStages().get(i).getSrc().getPictureOutput().getOutputDesc());
//                    }
//                    if (templateInfo.getStages().get(i).getSrc().getTextOutputs().size() +
//                            templateInfo.getStages().get(i).getSrc().getEnumOutputs().size() +
//                            templateInfo.getStages().get(i).getSrc().getNumericalOutputs().size() > 0) {
//                        detailList.add(((Activity)getContext()).getString(R.string.start_other_output));
//                        for (TextOutput to : templateInfo.getStages().get(i).getSrc().getTextOutputs()) {
//                            detailList.add("    " + to.getOutputDesc());
//                        }
//                        for (EnumOutput eo : templateInfo.getStages().get(i).getSrc().getEnumOutputs()) {
//                            detailList.add("    " + eo.getOutputDesc());
//                        }
//                        for (NumericalOutput no : templateInfo.getStages().get(i).getSrc().getNumericalOutputs()) {
//                            detailList.add("    " + no.getOutputDesc());
//                        }
//                    }
//
//                    detailList.add(((Activity)getContext()).getString(R.string.end_location_strategy) + templateInfo.getStages().get(i).getDest().getLocationStrategy());
//                    if (templateInfo.getStages().get(i).getDest().getInputs().size() > 0) {
//                        String temp = ((Activity)getContext()).getString(R.string.end_location_inputs);
//                        for (String s : templateInfo.getStages().get(i).getDest().getInputs()) {
//                            temp += s;
//                            temp += ", ";
//
//                        }
//                        temp = temp.substring(0, temp.length() - 2);
//                        detailList.add(temp);
//
//                    }
//
//                    if (templateInfo.getStages().get(i).getDest().getPictureOutput().getOutputDesc() != null) {
//                        detailList.add(((Activity)getContext()).getString(R.string.end_picture_output) + templateInfo.getStages().get(i).getDest().getPictureOutput().getOutputDesc());
//                    }
//                    if (templateInfo.getStages().get(i).getDest().getTextOutputs().size() +
//                            templateInfo.getStages().get(i).getDest().getEnumOutputs().size() +
//                            templateInfo.getStages().get(i).getDest().getNumericalOutputs().size() > 0) {
//                        detailList.add(((Activity)getContext()).getString(R.string.end_other_output));
//                        for (TextOutput to : templateInfo.getStages().get(i).getDest().getTextOutputs()) {
//                            detailList.add("    " + to.getOutputDesc());
//                        }
//                        for (EnumOutput eo : templateInfo.getStages().get(i).getDest().getEnumOutputs()) {
//                            detailList.add("    " + eo.getOutputDesc());
//                        }
//                        for (NumericalOutput no : templateInfo.getStages().get(i).getDest().getNumericalOutputs()) {
//                            detailList.add("    " + no.getOutputDesc());
//                        }
//                    }
//                }
//                String[] itemString = detailList.toArray(new String[detailList.size()]);
//                showListDialog(templateInfo.getName(), itemString);
//            }
        });
    }

//    private void showListDialog(String title,String[] items) {
//
//        AlertDialog.Builder listDialog =
//                new AlertDialog.Builder(getContext());
//        listDialog.setTitle(title);
//        listDialog.setItems(items,null);
//        listDialog.show();
//    }

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

    private void showCollectResult(int result){
        progressDialog.dismiss();
        final int resultFinal = result;
        ((Activity)getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                if(resultFinal == COLLECT_FAIL){
                    Toast.makeText(getContext(),((Activity)getContext()).getString(R.string.collect_template_fail), Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getContext(),((Activity)getContext()).getString(R.string.collect_template_successfully), Toast.LENGTH_SHORT).show();
                    requestList();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_TEMPLATE) {
            if (resultCode > 0) {
                Toast.makeText(getContext(),((Activity)getContext()).getString(R.string.create_template_successfully), Toast.LENGTH_LONG).show();
                requestList();
            }
        }
    }

    private void requestList(){
        templates.clear();
        if(adapter != null)
            adapter.notifyDataSetChanged();
        /*发送post请求*/
        final HashMap<String,String> postData = new HashMap<String, String>();
        GetAllTemplateServlet.RequestBO requestBO = new GetAllTemplateServlet.RequestBO();
        requestBO.setUserId(app.getUserId());
        postData.put("data", JSONUtils.toJSONString(requestBO));
        String servlet = "GetAllTemplateServlet";
        HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    ServletResponseData responseData = JSONUtils.toBean(response.body().string(),
                                                ServletResponseData.class);

                    int result = responseData.getResult();
                    if(result == 1){
                        GetAllTemplateServlet.ResponseBO responseBO = JSONUtils.toBean(responseData.getData(),
                                GetAllTemplateServlet.ResponseBO.class);
                       tList = responseBO.getTemplates();
                        Collections.reverse(tList);
                        changeList();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showFailed(-1);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                showFailed(-1);

            }
        });
    }

    private void requestSearchList(String queryText){
        templates.clear();
        if(adapter != null)
            adapter.notifyDataSetChanged();
        /*发送post请求*/
        final HashMap<String,String> postData = new HashMap<String, String>();
        SearchTemplateServlet.RequestBO requestBO = new SearchTemplateServlet.RequestBO();
        requestBO.setUserId(app.getUserId());
        requestBO.setTemplateName(queryText);
        Log.d(TAG,queryText);
        postData.put("data", JSONUtils.toJSONString(requestBO));

        String servlet = "SearchTemplateServlet";
        HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    ServletResponseData responseData = JSONUtils.toBean(response.body().string(), ServletResponseData.class);

                    int result = responseData.getResult();
                    Log.d(TAG,result+"");
                    if(result == 1){

                        SearchTemplateServlet.ResponseBO responseBO = JSONUtils.toBean(responseData.getData(),
                                SearchTemplateServlet.ResponseBO.class);
                       tList2 = responseBO.getTemplates();
                        Collections.reverse(tList2);
                        Log.d(TAG,tList.size()+"");

                        changeList2();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showFailed(-1);

                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                showFailed(-1);

            }

        });
    }


    private void showFailed(final int result) {
        ((Activity)getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上

                if(result == -1){
                    Toast.makeText(getContext(),"Internal server error, please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void changeList() {
        ((Activity)getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上

                for(GetAllTemplateServlet.ResponseBO.TemplateBO bo : tList){
                    // Log.d("test",bo.getName());
                    item = new HashMap<String,Object>();
                    item.put("uri",bo.getUri());
                    item.put("templateId",bo.getTemplateId());
                    item.put("title", bo.getName());
                    item.put("publisher", bo.getCreater());
                    item.put("desc",bo.getDescription());
                    item.put("stages",bo.getTotalStageNum());
                    item.put("date", bo.getCreateTime().toString().substring(0,10));
                    item.put("used",bo.getHeat());
                    //Log.d(TAG,bo.getDescription());
                    templates.add(item);
                }
                if(templates.size() == 0){
                    noData.setVisibility(View.VISIBLE);
                }else{
                    noData.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();

            }
        });
    }

    private void changeList2() {
        ((Activity)getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上

                for( SearchTemplateServlet.ResponseBO.TemplateBO bo : tList2){
                    Log.d(TAG,bo.getName());
                    item = new HashMap<String,Object>();
                    item.put("uri",bo.getUri());
                    item.put("templateId",bo.getTemplateId());
                    item.put("title", bo.getName());
                    item.put("publisher", bo.getCreater());
                    item.put("desc",bo.getDescription());
                    item.put("stages",bo.getTotalStageNum());
                    item.put("date", bo.getCreateTime().toString().substring(0,10));
                    item.put("used",bo.getHeat());
                    //Log.d(TAG,bo.getDescription());
                    templates.add(item);
                }
                if(templates.size() == 0){
                    noData.setVisibility(View.VISIBLE);
                }else{
                    noData.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();

            }
        });
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

    public void setUserVisibleHint(boolean isVisibleToUser){
            if(isVisibleToUser ){
                //延时刷新 防止执行两次onCreateView
                new Handler().postDelayed(new Runnable(){
                    public void run() {
                        requestList();
                    }
                }, 200);
            }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onResume(){
        super.onResume();
            Log.d("fzj","template fragment onResumeCalled");
    }

    public void onStart(){
        super.onStart();
        Log.d(TAG,"onStartCalled");
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
}
