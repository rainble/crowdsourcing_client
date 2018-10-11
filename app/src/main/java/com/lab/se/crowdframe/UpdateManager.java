package com.lab.se.crowdframe;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.lab.se.crowdframe.util.Global;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lwh on 2017/10/15.
 */

public class UpdateManager {
    private Context context;
    private ProgressDialog progressDialog;

    //文件存储
    private String updateDir = null;
    private File updateFile = null;
    // 准备安装新版本应用标记
    private static final int INSTALL_TOKEN = 1;

    //Log日志打印标签
    private static final String TAG = "update_log";

    public UpdateManager(Context context){
        this.context = context;
    }

    /**
     * 显示下载进度对话框
     */
    public void showDownloadDialog() {

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("正在下载...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        new downloadAsyncTask().execute();
    }

    /**
     * 下载新版本应用
     */
    private class downloadAsyncTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            Log.e(TAG, "执行至--onPreExecute");
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {

            Log.e(TAG, "执行至--doInBackground");

            URL url;
            HttpURLConnection connection = null;
            InputStream in = null;
            FileOutputStream out = null;
            try {
                url = new URL(Global.downloadUrl);
                connection = (HttpURLConnection) url.openConnection();

                in = connection.getInputStream();
                long fileLength = connection.getContentLength();
                updateDir = context.getFilesDir().toString() + File.separator + "download/";
                updateFile = new File(updateDir,"CrowdFrame.apk");
                File file_path = new File(updateDir);
                if (!file_path.exists()) {
                    file_path.mkdir();
                }

                out = new FileOutputStream(updateFile);//为指定的文件路径创建文件输出流
                byte[] buffer = new byte[1024 * 1024];
                int len = 0;
                long readLength = 0;

                Log.e(TAG, "执行至--readLength = 0");

                while ((len = in.read(buffer)) != -1) {

                    out.write(buffer, 0, len);//从buffer的第0位开始读取len长度的字节到输出流
                    readLength += len;

                    int curProgress = (int) (((float) readLength / fileLength) * 100);

                    Log.e(TAG, "当前下载进度：" + curProgress);

                    publishProgress(curProgress);

                    if (readLength >= fileLength) {

                        Log.e(TAG, "执行至--readLength >= fileLength");
                        break;
                    }
                }

                out.flush();
                return INSTALL_TOKEN;

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            Log.e(TAG, "异步更新进度接收到的值：" + values[0]);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {

            progressDialog.dismiss();//关闭进度条
            //安装应用
            installApp();
        }
    }

    /**
     * 安装新版本应用
     */
    private void installApp() {
        //下载完成以后自动安装
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        // 由于没有在Activity环境下启动Activity,设置下面的标签
        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //版本在7.0以上是不能直接通过uri访问的
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri = FileProvider.getUriForFile(context, "com.lab.se.crowdframe.fileprovider", updateFile);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        }
        else{
            Uri uri = Uri.fromFile(updateFile);
            installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
        }
        //进行安装
        context.startActivity(installIntent);
    }
}
