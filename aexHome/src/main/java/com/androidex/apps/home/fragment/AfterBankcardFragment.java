package com.androidex.apps.home.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidex.apps.home.FullscreenActivity;
import com.androidex.apps.home.R;

/**
 * 后插入银行卡
 */

public class AfterBankcardFragment extends Fragment implements View.OnClickListener {

    private View mView = null;

    public AfterBankcardFragment() {
        // Required empty public constructor
    }

    public TextView tv_above;
    public TextView tv_exit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_after_bankcard, container, false);
        }
        tv_above = (TextView) mView.findViewById(R.id.tv_above);
        tv_exit = (TextView) mView.findViewById(R.id.tv_exit);

        tv_above.setOnClickListener(this);
        tv_exit.setOnClickListener(this);
        // Inflate the layout for this fragment
        return mView;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.tv_above://上一步
                intent.putExtra("page", 0);
                intent.setAction(FullscreenActivity.action_back);
                getActivity().sendBroadcast(intent);
                break;
            case R.id.tv_exit://退出
                intent.setAction(FullscreenActivity.action_cancle);
                getActivity().sendBroadcast(intent);
            default:
                break;
        }
    }
}
