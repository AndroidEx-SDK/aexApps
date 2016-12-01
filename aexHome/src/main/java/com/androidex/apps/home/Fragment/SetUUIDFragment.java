package com.androidex.apps.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.androidex.apps.home.R;

/**
 * 设置UUID的FragMent
 * Created by liyp on 16/12/1.
 */

public class SetUUIDFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "setuuidfragment";
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.set_uuid, container, false);
        return rootView;
    }
    public void iniView(){

        Button finish = (Button) rootView.findViewById(R.id.finish);

        finish.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.finish:

                break;
        }
    }
}
