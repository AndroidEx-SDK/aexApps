package com.androidex.apps.home.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.androidex.apps.home.FullscreenActivity;
import com.androidex.apps.home.R;

/**
 * 设置UUID的FragMent
 * Created by liyp on 16/12/1.
 */

public class SetUUIDFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "setuuidfragment";
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {

            rootView = inflater.inflate(R.layout.set_uuid, container, false);
        }
        iniView();
        return rootView;
    }

    public void iniView() {

        EditText uuid = (EditText) rootView.findViewById(R.id.et_uuid);
        Button finish = (Button) rootView.findViewById(R.id.finish);
        finish.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finish:
                Intent intent = new Intent();
                intent.putExtra("page",1);
                intent.setAction(FullscreenActivity.action_next);
                getActivity().sendBroadcast(intent);
                break;
        }
    }
}
