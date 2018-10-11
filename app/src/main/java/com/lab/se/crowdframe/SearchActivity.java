package com.lab.se.crowdframe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
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
import com.lab.se.crowdframe.entity.SearchAdapter;
import com.lab.se.crowdframe.entity.SysApplication;


import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private EditText etSearch;
    private PoiSearch poiSearch;
    private List<PoiInfo> poiInfos;
    private SearchAdapter adapter;
    private GeoCoder search=null;
    private String city;
    Toolbar mToolbar;
    PoiInfo selectPoi;
    boolean enableSearch = true;
    //地图
    MapView  mMapView;
    BaiduMap bdMap;
    BitmapDescriptor selectedIcon = BitmapDescriptorFactory.fromResource(R.drawable.location_icon_selected);
    Button confirmButton;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        SysApplication.getInstance().addActivity(this);

        preferences = getSharedPreferences("crowdframe", 0);
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
        initView();
    }

    private void initView(){
        //baidu map
        mMapView = (MapView) findViewById(R.id.select_location_mapview);
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
        //定义地图状态
        double latitude = preferences.getFloat("latitude",0);
        double longitude = preferences.getFloat("longitude",0);
        LatLng cenpt = new LatLng(latitude,longitude);

        MapStatus mMapstatus = new MapStatus.Builder()
                .target(cenpt)
                .zoom(18)
                .build();

        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapstatus);
        bdMap.setMapStatus(mMapStatusUpdate);

        //输入文本触发POI搜索
        final TextWatcher doSomething = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("lwh","Something");
                initData(etSearch.getText().toString());
            }
        };

        //选择item后不触发POI搜索
        final TextWatcher doNothing = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("lwh","nothing");
                poiInfos.clear();
                adapter.notifyDataSetChanged();
            }
        };

        etSearch=(EditText) findViewById(R.id.et_search);
        etSearch.setText("");
        etSearch.addTextChangedListener(doSomething);

        //地图点击事件
        bdMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d("search","latlng");
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                Log.d("search","mappoi");
                Log.d("search",mapPoi.getName());
                selectPoi = new PoiInfo();
                selectPoi.name = mapPoi.getName();
                selectPoi.address = "";
                selectPoi.location = mapPoi.getPosition();
                etSearch.addTextChangedListener(doSomething);
                etSearch.setText(selectPoi.name+selectPoi.address);
                return false;
            }
        });

        //确认选择按钮
        confirmButton = (Button)findViewById(R.id.confirm_location);

        if(null != getIntent().getStringExtra("city")){
            city = getIntent().getStringExtra("city");
        } else {
            city = "上海";
        }
        poiSearch=PoiSearch.newInstance();
        poiInfos=new ArrayList<PoiInfo>();
        adapter=new SearchAdapter(this,poiInfos);
        ListView listView=(ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //根据所在城市的地址转换为经纬度
                selectPoi = poiInfos.get(position);
                etSearch.addTextChangedListener(doNothing);
                etSearch.setText(selectPoi.name+"\n"+selectPoi.address);
                enableSearch = false;
                //清空搜索列表
                poiInfos.clear();
                adapter.notifyDataSetChanged();

                //add the marker
                bdMap.clear();
                MarkerOptions overlayOptions = new MarkerOptions().position(selectPoi.location)
                                             .icon(selectedIcon);
                bdMap.addOverlay(overlayOptions);
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(selectPoi.location);
                bdMap.animateMapStatus(msu);
            }
        });

        //点击输入框，重新选择
        etSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = (EditText)v;
                et.setText("");
                selectPoi = null;
                enableSearch = true;

                /**刷新查询数据**/
                etSearch.addTextChangedListener(doSomething);
            }
        });

        //点击确认按钮，返回
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != selectPoi){
                    Intent intent = new Intent();
                    intent.putExtra("poi", selectPoi.name+"\n"+selectPoi.address);
                    intent.putExtra("latitude", selectPoi.location.latitude + "");
                    intent.putExtra("longitude", selectPoi.location.longitude + "");
                    intent.putExtra("address", selectPoi.address + "");
                    setResult(1, intent);
                    SearchActivity.this.finish();
                } else {
                    Toast.makeText(SearchActivity.this, SearchActivity.this.getString(R.string.remind_select_location), Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void initData(String key){
        OnGetPoiSearchResultListener poiSearchResultListener=new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                poiInfos.clear();
                if(poiResult.getAllPoi()!=null){
                    poiInfos.addAll(poiResult.getAllPoi());
                    adapter.notifyDataSetChanged();
                }else{
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        };
        poiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);
        if(enableSearch == true){
            poiSearch.searchInCity((new PoiCitySearchOption())
                    .city(city)
                    .keyword(key));
        }
    }

}
