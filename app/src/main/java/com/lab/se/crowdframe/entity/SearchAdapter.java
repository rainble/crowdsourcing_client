package com.lab.se.crowdframe.entity;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.baidu.mapapi.search.core.PoiInfo;
import com.lab.se.crowdframe.R;

import java.util.List;

/**
 * Created by lenovo2013 on 2017/3/10.
 */

public class SearchAdapter extends BaseAdapter {

    private List<PoiInfo> list = null;
    private Context ct = null;

    public SearchAdapter(Context context, List<PoiInfo> poiInfos){
        this.list = poiInfos;
        this.ct = context;
    }


    @Override
    public int getCount() {
        if(list != null && list.size() > 0){
            return list.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout line = new LinearLayout(this.ct);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setPadding(10,15,0,15);
        ImageView image = new ImageView(this.ct);
        image.setImageResource(R.drawable.vector_drawable__location__sfont);
        image.setLayoutParams(new ViewGroup.LayoutParams(50,50));
        line.addView(image);

        TextView tv = new TextView(this.ct);
        tv.setText(list.get(position).name+"\n"+list.get(position).address);
        line.addView(tv);
        return line;
    }
}
