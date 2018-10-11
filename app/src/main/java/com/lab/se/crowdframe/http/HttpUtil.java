package com.lab.se.crowdframe.http;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by lwh on 2017/3/13.
 */

public class HttpUtil {
    private static final String ip = "120.79.72.242";
//private static final String ip = "localhost";

    private static final String port="8080";

    //运行值
//    private static final String resource_name = "MobiCrowdsourcing";
    //测试值
    private static final String resource_name = "MobiCrowdsourcingTest";
//    private static final String resource_name = "crowd_server";

    private static final String UPLOAD_FILE_SERVLET = "UploadFile";

    private static final String UPLOAD_IMAGE_SERVLET = "UploadImage";
    private static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
    private static final MediaType MEDIA_TYPE_XML = MediaType.parse("text/xml; charset=utf-8");

    //Get请求，只获取数据，不发送数据
    public static void sendOkHttpRequestByGet(String servlet,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(String.format("http://%s:%s/%s/%s", ip, port, resource_name, servlet))
                .build();
        client.newCall(request).enqueue(callback);
    }

    //Post请求，发送数据的同时获取并返回数据
    public static void doPost(String servlet, HashMap<String,String> map, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder builder = new FormBody.Builder();
        //添加要发送的数据
        for(Map.Entry<String, String> entry: map.entrySet()){
            builder.add(entry.getKey(),entry.getValue());
        }
        RequestBody body = builder.build();
        //发送请求
        Request request = new Request.Builder()
                .url(String.format("http://%s:%s/%s/%s", ip, port, resource_name, servlet))
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    //上传文件
    public static void uploadFile(String filePath,HashMap<String,String> map, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        String fileName = map.get("fileName");

        //构建请求体
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("fileName",fileName);
        builder.addFormDataPart("upload", filePath,
                RequestBody.create(MEDIA_TYPE_XML, new File(filePath)));
        RequestBody body = builder.build();

        //发送请求
        Request request = new Request.Builder()
                .url(String.format("http://%s:%s/%s/%s", ip, port, resource_name,UPLOAD_FILE_SERVLET))
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    //上传图片
    public static void uploadImage(HashMap<String,String> map, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        String imageName = map.get("imageName");
        String startImagePath = map.get("startImagePath");
        String endImagePath = map.get("endImagePath");

        //构建请求体
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("imageName", imageName);

        //start location需要上传图片
        if(startImagePath != ""){
            builder.addFormDataPart("startImagePath", startImagePath,
                    RequestBody.create(MEDIA_TYPE_JPEG, new File(startImagePath)));
        }
        //start location不需要上传图片
        else{
            builder.addFormDataPart("startImagePath", "");
        }
        
        //end location需要上传图片
        if(endImagePath != ""){
            builder.addFormDataPart("endImagePath", endImagePath,
                    RequestBody.create(MEDIA_TYPE_JPEG, new File(endImagePath)));
        }
        //end location不需要上传图片
        else{
            builder.addFormDataPart("endImagePath", "");
        }

        RequestBody body = builder.build();

        //发送请求
        Request request = new Request.Builder()
                .url(String.format("http://%s:%s/%s/%s", ip, port, resource_name, UPLOAD_IMAGE_SERVLET))
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    //下载文件
    public static void downloadFile(String fileUrl,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        //发送请求
        Request request = new Request.Builder()
                .url(fileUrl)
                .build();
        client.newCall(request).enqueue(callback);

    }
}
