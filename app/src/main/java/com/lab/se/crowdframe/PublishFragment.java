package com.lab.se.crowdframe;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.lab.se.crowdframe.http.HttpUtil;
import com.lab.se.crowdframe.http.JSONUtils;
import com.lab.se.crowdframe.http.ServletResponseData;
import com.lab.se.crowdframe.servlet.CollectTemplateServlet;
import com.lab.se.crowdframe.servlet.GetTemplateCollectionServlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PublishFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PublishFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PublishFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static int CREATED = 0;
    private static final String TAG = "PublishFragment";
    private static final int CUSTOMIZE_TASK = 1,CANCEL_COLLECT_TEMPLATE = -1,CANCEL_SUCCESS = 1, CANCEL_FAIL = -1;
    private static final int REFRESH_COMPLETE = 0X110;
    private SwipeRefreshLayout mSwipeLayout;
    CrowdFrameApplication app;
    private static final String ARG_PARAM1 = "param1";
    private String[] template_name;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private Map<String,Object> item;
    private OnFragmentInteractionListener mListener;
    SimpleAdapter adapter;

    ListView templateListView;
    TextView noData;
    ArrayList<Map<String, Object>> templates = new ArrayList<Map<String, Object>>();

    public PublishFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment PublishFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PublishFragment newInstance(String param1) {
        PublishFragment fragment = new PublishFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }
    private Handler mHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            switch (msg.what)
            {
                case REFRESH_COMPLETE:
                    requestList();
                    mSwipeLayout.setRefreshing(false);
                    break;

            }
        };
    };
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
        Log.d("fzj3","onCreateView called");

        app = (CrowdFrameApplication)getActivity().getApplication();


        View v = inflater.inflate(R.layout.fragment_publish, container, false);
        templateListView = (ListView)v.findViewById(R.id.publish_template_listview);
        noData = (TextView)v.findViewById(R.id.noData);
        templateListView.setEmptyView(noData);

        mSwipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.id_swipe_ly);

        mSwipeLayout.setOnRefreshListener(this);

        adapter = new SimpleAdapter(getActivity(),templates,R.layout.fragment_publish_item,
                new String[]{"name"},new int[]{R.id.publish_template_item_name}){
            @Override
            public View getView(int position, View convertView,ViewGroup parent) {
                final int p = position;
                final View view = super.getView(position, convertView, parent);
                final LinearLayout ll = (LinearLayout) view.findViewById(R.id.publish_template_item_linear);
                ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Log.d(TAG,templates.get(p).get("uri").toString());
                        Intent it = new Intent(getActivity(),CustomizeTaskActivity.class);
                        it.putExtra("templateId",templates.get(p).get("templateId").toString());
                        it.putExtra("uri",templates.get(p).get("uri").toString());
                        startActivityForResult(it,CUSTOMIZE_TASK);
                    }
                });

                ll.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showDeleteInfo(p);
                        return true;
                    }
                });



                return view;
            }
        };

        templateListView.setAdapter(adapter);
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void onRefresh()
    {
        // Log.e("xxx", Thread.currentThread().getName());
        // UI Thread

        mHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE, 1000);

    }


    public void showDeleteInfo(final int position) {
        new AlertDialog.Builder(getContext()).setTitle(((Activity)getContext()).getString(R.string.confirm)).setMessage(((Activity)getContext()).getString(R.string.confirm_delete_template))
                .setPositiveButton(((Activity)getContext()).getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int templateId = (Integer)templates.get(position).get("templateId");
                        Log.d(TAG,String.format("The template id is %d", templateId));
                        //取消收藏模板
                        cancelCollectTemplate(templateId);
                    }
                }).show();
    }

    private void cancelCollectTemplate(int templateId){
        /*发送post请求*/
        final HashMap<String,String> postData = new HashMap<String, String>();
        CollectTemplateServlet.RequestBO requestBO = new CollectTemplateServlet.RequestBO();
        requestBO.setUserId(app.getUserId());
        requestBO.setTemplateId(templateId);requestBO.setIndicator(CANCEL_COLLECT_TEMPLATE);
        postData.put("data", JSONUtils.toJSONString(requestBO));

        String servlet = "CollectTemplateServlet";
        HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    ServletResponseData responseData = JSONUtils.toBean(response.body().string(), ServletResponseData.class);

                    int result = responseData.getResult();

                    if (result == 1) {
                        showCollectResult(CANCEL_SUCCESS);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG,"Exception");
                    showCollectResult(CANCEL_FAIL);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"Failure");
                showCollectResult(CANCEL_FAIL);
            }
        });
    }

    private void showCollectResult(int result){
        final int resultFinal = result;
        ((Activity)getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                if(resultFinal == CANCEL_FAIL){
                    Toast.makeText(getContext(),((Activity)getContext()).getString(R.string.cancel_collection_fail), Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getContext(),((Activity)getContext()).getString(R.string.cancel_collection_successfully), Toast.LENGTH_LONG).show();
                    requestList();
                }
            }
        });
    }

    private void requestList(){
        templates.clear();
        if(adapter != null)
            adapter.notifyDataSetChanged();
        /*发送post请求*/
        final HashMap<String,String> postData = new HashMap<String, String>();
        GetTemplateCollectionServlet.RequestBO requestBO = new GetTemplateCollectionServlet.RequestBO();
        requestBO.setUserId(app.getUserId());
        postData.put("data", JSONUtils.toJSONString(requestBO));

        String servlet = "GetTemplateCollectionServlet";
        HttpUtil.doPost(servlet, postData, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    ServletResponseData responseData = JSONUtils.toBean(response.body().string(), ServletResponseData.class);

                    int result = responseData.getResult();
                    //Log.d(TAG,"1");
                    if(result == 1){

                        GetTemplateCollectionServlet.ResponseBO responseBO = JSONUtils.toBean(responseData.getData(),
                                GetTemplateCollectionServlet.ResponseBO.class);
                        List<GetTemplateCollectionServlet.ResponseBO.TemplateBO> tList = responseBO.getTemplates();
                        Collections.reverse(tList);
                        for(GetTemplateCollectionServlet.ResponseBO.TemplateBO bo : tList){
                            item = new HashMap<String,Object>();
                            item.put("name", bo.getName());
                            item.put("uri",bo.getUri());
                            item.put("templateId",bo.getTemplateId());
                            //Log.d(TAG,bo.getUri());
                            templates.add(item);
                        }

                        changeList(templates.size());

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
                    Toast.makeText(getContext(),((Activity)getContext()).getString(R.string.internal_server_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }




    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d("fzj","publish fragment onResume called");

    }




    public void setUserVisibleHint(boolean isVisibleToUser){
        if(isVisibleToUser){
            Log.d("fzj3","publish");
            requestList();
        }
    }

    private void changeList(final int number) {
        ((Activity)getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                if(number == 0){
                    noData.setVisibility(View.VISIBLE);
                }else{
                    noData.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();

            }
        });
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
