package com.androidex.apps.home.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.androidex.apps.home.FullscreenActivity;
import com.androidex.apps.home.R;

/**
 * 关于本机
 * Created by liyp on 16/11/24.
 */

public class AboutLocalFragment extends Fragment implements  View.OnClickListener {
    private static final String TAG = "aboutlocalfragment";
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_aboutlocal, container, false);
        ImageView close = (ImageView) rootView.findViewById(R.id.iv_close);
        close.setOnClickListener(this);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                Intent intent = new Intent(FullscreenActivity.action_Viewpager_gone);
                getContext().sendBroadcast(intent);
                break;
        }
    }
}
