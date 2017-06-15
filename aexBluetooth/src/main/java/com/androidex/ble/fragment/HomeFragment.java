package com.androidex.ble.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidex.ble.R;

/**
 * Created by cts on 17/6/5.
 * 主界面
 */

public class HomeFragment extends Fragment{
    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view==null){
            view = inflater.inflate(R.layout.home_fragment,container,false);

        }
        return view;
    }
}
