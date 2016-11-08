package com.androidex.apps.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidex.common.WebviewFragment;

/**
 * Created by yangjun on 2016/11/7.
 */

public class AboutFragment extends WebviewFragment {
    private static final String TAG = "AboutPage";
    public AboutFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        loadUrl("file:///android_asset/about.html");
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void loadUrl(String url) {
        super.loadUrl(url);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDeviceEvent(int _code, String args) {
        super.onDeviceEvent(_code, args);
    }

    @Override
    public void onSendJavaScript(String jscode) {
        super.onSendJavaScript(jscode);
    }

    @Override
    public void sendJavaScript(String jscode) {
        super.sendJavaScript(jscode);
    }
}
