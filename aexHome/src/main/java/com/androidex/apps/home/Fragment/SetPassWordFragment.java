package com.androidex.apps.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidex.apps.home.R;

/**
 * 设置密码的FragMent
 * Created by liyp on 16/12/1.
 */

public class SetPassWordFragment extends Fragment {
    private static final String TAG = "setpassword";
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.set_password, container, false);
        return rootView;
    }
}
