package com.androidex.apps.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidex.common.LogFragment;
import com.androidex.common.OnMultClickListener;
import com.androidex.logger.Log;
import com.androidex.logger.LogWrapper;
import com.androidex.logger.MessageOnlyLogFilter;

import static com.androidex.apps.home.AdvertFragment.ONCLICKTIMES;

/**
 * Created by yangjun on 2016/11/7.
 */

public class aexLogFragment extends LogFragment implements OnMultClickListener {
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
        FullscreenActivity.registerMultClickListener(getLogView(), this);
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

    @Override
    public boolean OnMultClick(View view, int times) {
        if (times == ONCLICKTIMES) {
            Intent intent = new Intent(FullscreenActivity.ActionControlBar);
            intent.putExtra("flag", "toggle");
            intent.putExtra("bar", true);
            getLogView().getContext().sendBroadcast(intent);
            return true;
        }
        return false;
    }
}
