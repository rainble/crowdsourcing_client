package com.lab.se.crowdframe;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.lab.se.crowdframe.entity.SysApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchTempActivity extends AppCompatActivity {
    Toolbar mToolbar;
    String value;
    ListView list;
    private ArrayList<Map<String, Object>> mData = new  ArrayList<Map<String, Object>>();
    private String[] mTitle = {"Pick-up Delivery"};
    private String[] mPublisher = {"Bruce LEE"};
    private String[] mDesc = {"So I really need somebody to help me get my delivery at Akang, and as I assume, it won't take so long"};
    private String[] mStages = {"1"};
    private String[] mIndex = {"42"};
    private String[] mUsed = {"123"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_temp);
        SysApplication.getInstance().addActivity(this);

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
        Intent intent = getIntent();
        //从Intent当中根据key取得value
        if (intent != null) {
           value = intent.getStringExtra("query");
        }

        list = (ListView)findViewById(R.id.template_result_list);
        for(int i = 0; i < mTitle.length; i ++){
            Map<String,Object> item = new HashMap<String,Object>();
            item.put("title", mTitle[i]);
            item.put("publisher", mPublisher[i]);
            item.put("desc",mDesc[i]);
            item.put("stages",mStages[i]);
            item.put("index",mIndex[i]);
            item.put("used",mUsed[i]);
            mData.add(item);
        }

        //重写SimpleAdapter方法，以实现对list中的每一个item分别进行监听器绑定
        SimpleAdapter adapter = new SimpleAdapter(SearchTempActivity.this,mData,R.layout.fragment_template_item2,
                new String[]{"title","publisher","desc","stages","index","used"},new int[]{R.id.template_title,R.id.template_publisher,R.id.template_desc
                ,R.id.template_stage,R.id.template_number,R.id.template_used}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final int p = position;
                final View view = super.getView(position, convertView, parent);
                return view;
            }
        };
        list.setAdapter(adapter);





    }

}
