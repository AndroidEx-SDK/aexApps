package com.androidex.apps.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.androidex.apps.home.FullscreenActivity;
import com.androidex.apps.home.R;

/**
 * Created by cts on 16/12/10.
 */

public class CameraFragment extends DialogFragment {
    private View rootView;
    private FullscreenActivity activity;
    public boolean isCancelable = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        }
        setCancelable(isCancelable);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity = (FullscreenActivity) getActivity();
        iniView();
        return rootView;
    }
    public void iniView(){

    }
}
