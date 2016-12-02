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
 * 设置密码的FragMent
 *
 * Created by liyp on 16/12/1.
 */

public class SetPassWordFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "setpassword";
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.set_password, container, false);
        initView();
        return rootView;
    }

    public void initView() {
        EditText password = (EditText) rootView.findViewById(R.id.et_password);
        Button cancle = (Button) rootView.findViewById(R.id.btn_cancle);
        Button ok = (Button) rootView.findViewById(R.id.btn_ok);

        cancle.setOnClickListener(this);
        ok.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.btn_cancle:
                intent.setAction(FullscreenActivity.action_cancle);
                getActivity().sendBroadcast(intent);
                break;
            case R.id.btn_ok:
                intent.setAction(FullscreenActivity.action_finish);
                getActivity().sendBroadcast(intent);
                break;
        }
    }
}
