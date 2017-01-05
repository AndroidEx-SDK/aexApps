package com.wificonnect;

/**
 * Created by xinshuhao on 16/7/17.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.devicedemo.R;

import java.util.ArrayList;

public class WifiConnListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<WifiElement> mArr;

    public WifiConnListAdapter(Context context, ArrayList<WifiElement> list) {
        this.inflater = LayoutInflater.from(context);
        this.mArr = list;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mArr.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mArr.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View view = inflater.inflate(R.layout.widget_wifi_conn_lv, null);
        TextView ssid = (TextView) view.findViewById(R.id.wifi_conn_name);
        TextView wpe = (TextView) view.findViewById(R.id.wifi_conn_wpe);
        ImageView level = (ImageView) view.findViewById(R.id.wifi_conn_level);
        ssid.setText(mArr.get(position).getSsid());
        wpe.setText("加密类型:" + mArr.get(position).getCapabilities());
        int i = abs(mArr.get(position).getLevel());
        if (i <= 50) {
            level.setBackgroundResource(R.mipmap.wifi_05);
        } else if (i > 50 && i <= 65) {
            level.setBackgroundResource(R.mipmap.wifi_04);
        } else if (i > 65 && i <= 75) {
            level.setBackgroundResource(R.mipmap.wifi_03);
        } else if (i > 75 && i <= 90) {
            level.setBackgroundResource(R.mipmap.wifi_02);
        } else {
            level.setBackgroundResource(R.mipmap.wifi_01);
        }
        // level.setText(String.valueOf(mArr.get(position).getLevel()));
        return view;
    }

    /**
     * 绝对值
     *
     * @param num
     * @return
     */
    private int abs(int num) {
        return num * (1 - ((num >>> 31) << 1));
    }


}
