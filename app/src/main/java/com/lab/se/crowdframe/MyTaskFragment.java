package com.lab.se.crowdframe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lab.se.crowdframe.entity.SysApplication;
import com.lab.se.crowdframe.http.HttpUtil;
import com.lab.se.crowdframe.http.JSONUtils;
import com.lab.se.crowdframe.http.ServletResponseData;
import com.lab.se.crowdframe.servlet.GetBonusPublishCreditServlet;
import com.lab.se.crowdframe.servlet.GetTemplateCollectionServlet;
import com.lab.se.crowdframe.servlet.GetUserInfoServlet;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyTaskFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyTaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyTaskFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static int VISITED = 0;
    CrowdFrameApplication app;
    // TODO: Rename and change types of parameters
    private String mParam1;

    private OnFragmentInteractionListener mListener;
    LinearLayout userInfoLL,l1, l2, l3, l4, l5, l6;
    TextView userCredit;
    FloatingActionButton loginGift;

    public MyTaskFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment MyTaskFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyTaskFragment newInstance(String param1) {
        MyTaskFragment fragment = new MyTaskFragment();
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
        View v = inflater.inflate(R.layout.fragment_my_task, container, false);
        app = (CrowdFrameApplication)getActivity().getApplication();
        VISITED = 1;

        TextView userName = (TextView)v.findViewById(R.id.user_name);
        userCredit = (TextView)v.findViewById(R.id.user_credit);
        userName.setText(app.getUserName());
        userCredit.setText(String.valueOf(app.getCreditPublish() + "(" + app.getCreditWithdraw() + ")"));

        userInfoLL = (LinearLayout)v.findViewById(R.id.user_info_ll);
        l1 = (LinearLayout)v.findViewById(R.id.my_task_accept_ongoing);
        l2 = (LinearLayout)v.findViewById(R.id.my_task_accept_history);
        l3 = (LinearLayout)v.findViewById(R.id.my_task_publish_ongoing);
        l4 = (LinearLayout)v.findViewById(R.id.my_task_publish_history);
        l5 = (LinearLayout)v.findViewById(R.id.logout);
        l6 = (LinearLayout)v.findViewById(R.id.about);

        userInfoLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SettingActivity.class);
                startActivity(i);
            }
        });

        l1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), TaskListActivity.class);
                i.putExtra("type",1);
                startActivity(i);
            }
        });
        l2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), TaskListActivity.class);
                i.putExtra("type",2);
                startActivity(i);
            }
        });
        l3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), TaskListActivity.class);
                i.putExtra("type",3);
                startActivity(i);
            }
        });
        l4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), TaskListActivity.class);
                i.putExtra("type",4);
                startActivity(i);
            }
        });
        l5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            new AlertDialog.Builder(getActivity()).setTitle(getActivity().getString(R.string.confirm)).setMessage(getActivity().getString(R.string.confirm_logout))
                .setPositiveButton(getActivity().getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences preferences = getActivity().getSharedPreferences("crowdframe", 0);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.remove("username");
                        editor.remove("password");
                        editor.commit();
                        SysApplication.getInstance().exit();
                    }
                })
                .setNegativeButton(getActivity().getString(R.string.cancel),new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
            }
        });

        l6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AboutActivity.class);
                startActivity(i);
            }
        });

        loginGift = (FloatingActionButton)v.findViewById(R.id.user_login_gift);
        loginGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final HashMap<String,String> postData = new HashMap<String, String>();
                final GetBonusPublishCreditServlet.RequestBO requestBO = new GetBonusPublishCreditServlet.RequestBO();
                requestBO.setUserId(app.getUserId());
                postData.put("data", JSONUtils.toJSONString(requestBO));
                String servlet = "GetBonusPublishCreditServlet";
                HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            ServletResponseData responseData = JSONUtils.toBean(response.body().string(), ServletResponseData.class);
                            int result = responseData.getResult();
                            if(result == 1){
                                GetBonusPublishCreditServlet.ResponseBO responseBO = JSONUtils.toBean(responseData.getData(),
                                        GetBonusPublishCreditServlet.ResponseBO.class);
                                app.setCreditPublish(responseBO.getCreditPublish());
                                app.setCreditWithdraw(responseBO.getCreditWithdraw());
                                app.setUserFlag(1);
                                refreshCreditAndFlag(0);
                            }
                        }catch (Exception e) {
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
        });

        return v;
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
        if(isVisibleToUser && VISITED==1){
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    requestUserInfo();
                }
            }, 500);
        }
    }

    private void requestUserInfo(){
        final HashMap<String,String> postData = new HashMap<String, String>();
        final GetUserInfoServlet.RequestBO requestBO = new GetUserInfoServlet.RequestBO();
        requestBO.setUserId(app.getUserId());
        postData.put("data", JSONUtils.toJSONString(requestBO));
        String servlet = "GetUserInfoServlet";
        HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    ServletResponseData responseData = JSONUtils.toBean(
                                        response.body().string(), ServletResponseData.class);
                    int result = responseData.getResult();
                    if(result == 1){
                        Log.d("MyTask","success");
                        GetUserInfoServlet.ResponseBO responseBO = JSONUtils.toBean(responseData.getData(),
                                GetUserInfoServlet.ResponseBO.class);
                        app.setCreditPublish(responseBO.getCreditPublish());
                        app.setCreditWithdraw(responseBO.getCreditWithdraw());
                        app.setUserFlag(responseBO.getLoginFlag());
                        app.setUserTag(responseBO.getUserTag());
                        refreshCreditAndFlag(1);
                        Log.d("MyTask",responseBO.getCreditPublish()+"");
                    }
                }catch (Exception e) {
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

    //type:0为点击领取登录礼物刷新，1为切换页面刷新
    public void refreshCreditAndFlag(final int type){
        ((Activity)getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                userCredit.setText(String.valueOf(app.getCreditPublish() + "(" + app.getCreditWithdraw() + ")"));
                if(app.getUserFlag() == 0){
                    loginGift.setVisibility(View.VISIBLE);
                } else {
                    loginGift.setVisibility(View.GONE);
                }
                if(type == 0){
                    new AlertDialog.Builder(getActivity()).setTitle("登录领取积分奖励").setMessage("恭喜您成功领取奖励！")
                        .setPositiveButton(getActivity().getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                               dialog.dismiss();
                            }
                        })
                        .create().show();
                }
            }
        });
    }

    private void showFailed(final int result) {
        ((Activity)getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                if(result == -1){
                    Toast.makeText(getContext(),getActivity().getString(R.string.internal_server_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
