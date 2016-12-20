package com.androidex.apps.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidex.common.LogFragment;
import com.androidex.common.OnMultClickListener;
import com.androidex.logger.Log;
import com.androidex.logger.LogView;
import com.androidex.logger.LogWrapper;
import com.androidex.logger.MessageOnlyLogFilter;

import static com.androidex.apps.home.AdvertFragment.ONCLICKTIMES;

/**
 * Created by yangjun on 2016/11/7.
 */

public class aexLogFragment extends LogFragment implements OnMultClickListener {
    public static final String TAG = "LOG";
    public NotyBroadCast mNotyBroadcast;
    public CallBackValue mCallBackValue;

    public aexLogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //注册广播
        mNotyBroadcast = new NotyBroadCast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PRINT_ACTION);
        getActivity().registerReceiver(mNotyBroadcast,intentFilter);
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

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mNotyBroadcast);
        super.onDestroy();
    }

    /**
     * 得到logview对象
     * @return
     */
    @Override
    public LogView getLogView() {
        return super.getLogView();
    }
    public String getPrintLog(){
        String printLog = getLogView().getText().toString();
        if (printLog!=null){
            return printLog;
        }else{
            return null;
        }
    }
    /**
     * 广播接收器
     * 打印的广播
     */
    public class NotyBroadCast extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(PRINT_ACTION)){//需要打印
                //将需要打印的数据回传给fullscreenactivity
                //一行行的传过去打印
                String [] ss = getPrintLog().split("\n");
                for (int i = 0;i<ss.length;i++){
                    mCallBackValue.sendMessageValue(ss[i],ss.length-1,i);
                }
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallBackValue = (CallBackValue) getActivity();
    }

    //回调接口
    public interface CallBackValue{
         void sendMessageValue(String printLog,int totalLength,int length);
    }
    public static final String PRINT_ACTION = "com.androidex.apps.home.paction";
}
