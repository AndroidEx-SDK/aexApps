package com.androidex.apps.home;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

public class aexLogFragment extends Fragment {
    public aexLogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.aexhome_log, container, false);
        initializeLogging();
        return v;
    }

    /** Create a chain of targets that will receive log data
     *
     * */
    public void initializeLogging() {
        // On screen logging via a fragment with a TextView.
        FragmentManager fm = getActivity().getSupportFragmentManager();
        LogFragment lf = (LogFragment) fm.findFragmentById(R.id.log_fragment);
        if(lf != null) {
            LogWrapper logWrapper = (LogWrapper) Log.getLogNode();
            MessageOnlyLogFilter msgFilter = (MessageOnlyLogFilter)logWrapper.getNext();
            msgFilter.setNext(lf.getLogView());
        }
    }

}
