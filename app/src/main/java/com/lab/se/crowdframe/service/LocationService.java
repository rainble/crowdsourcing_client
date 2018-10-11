package com.lab.se.crowdframe.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.lab.se.crowdframe.CheckMapActivity;

import java.util.List;


public class LocationService extends Service {

    private LocationClient locationClient;
    private BDLocationListener locationListener=new MyLocationListener();
    private double lat = 0, lng = 0;
    private float radius = 0;
    private String address = "";
    private MyBinder binder= new MyBinder();


    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

       return binder;
    }

    //create
    @Override
    public void onCreate(){
        super.onCreate();
    }


    //start service
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationClient=new LocationClient(this);
        locationClient.registerLocationListener(locationListener);
        initLocation();
        locationClient.start();
        return super.onStartCommand(intent, flags, startId);
    }

    //销毁service
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if(locationClient!=null){
            locationClient.stop();
        }
        super.onDestroy();
    }


    /**配置定位SDK参数**/
    private void initLocation(){
        LocationClientOption option=new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(2000);// 多长时间进行一次请求
        option.setNeedDeviceDirect(true);        // 返回的定位结果包含手机机头的方向
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);
        option.setIgnoreKillProcess(false);
        option.setEnableSimulateGps(false);
        locationClient.setLocOption(option);

    }

    class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation){// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }

            lat = location.getLatitude();
            lng = location.getLongitude();
            address = location.getAddrStr();
            radius = location.getRadius();
            Intent intent = new Intent("com.lab.se.updateLocation");
            intent.putExtra("address",address);
            intent.putExtra("lat",lat);
            intent.putExtra("lng",lng);
            intent.putExtra("radius",radius);
            sendBroadcast(intent);

        }
    }

    public class MyBinder extends Binder {
        public double getLat(){
            return lat;
        }

        public double getLng(){
            return lng;
        }

        public String getAddress(){
            return address;
        }

        public double getRadius(){
            return radius;
        }
    }


}
