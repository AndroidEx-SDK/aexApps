package com.androidex.apps.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidex.common.LogFragment;
import com.androidex.logger.Log;
import com.androidex.logger.LogWrapper;
import com.androidex.logger.MessageOnlyLogFilter;

/**
 * Created by yangjun on 2016/11/7.
 */

public class aexLogFragment extends LogFragment {
    public static final String TAG = "LOG";

    public aexLogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    public View inflateViews() {
        return super.inflateViews();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeLogging();
    }

    /** Create a chain of targets that will receive log data
     *
     * */
    public void initializeLogging() {
        // On screen logging via a fragment with a TextView.
        LogWrapper logWrapper = (LogWrapper) Log.getLogNode();
        MessageOnlyLogFilter msgFilter = (MessageOnlyLogFilter)logWrapper.getNext();
        msgFilter.setNext(this.getLogView());
        Log.i(TAG,"就绪");
    }

}
