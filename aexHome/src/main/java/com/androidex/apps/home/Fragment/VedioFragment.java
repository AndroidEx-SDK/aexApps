package com.androidex.apps.home.fragment;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.androidex.apps.home.FullscreenActivity;
import com.androidex.apps.home.R;
import com.androidex.apps.home.view.CustomVideoView;

import static com.androidex.apps.home.FullscreenActivity.action_start_network_text;

/**
 * Created by liyp on 16/12/10.
 */

public class VedioFragment extends LazyLoadFragment implements View.OnClickListener {
    private FullscreenActivity activity;
    private int index;
    private ImageView iv_close;
    private CustomVideoView customVideoView;

    public VedioFragment() {

    }

    @Override
    protected void lazyLoad() {
        iniView();

        activity = (FullscreenActivity) getActivity();
        /**获取参数，根据不同的参数播放不同的视频**/
        index = getArguments().getInt("index");
        Log.d("vediofragment", "开始播放视频=" + index);
        Uri uri;
        if (index == 1) {
            uri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.a002);
        } else {
            uri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.love);
        }
        /**播放视频**/
        customVideoView.setVisibility(View.VISIBLE);
        customVideoView.playVideo(uri);
    }

    public void iniView() {
        iv_close = findViewById(R.id.iv_close);
        Button btn_OK = findViewById(R.id.btn_OK);
        Button btn_NG = findViewById(R.id.btn_NG);
        customVideoView = findViewById(R.id.cv);
        iv_close.setOnClickListener(this);
        btn_OK.setOnClickListener(this);
        btn_NG.setOnClickListener(this);
    }

    public VedioFragment dissMissDialog() {
        dismiss();
        return this;
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_vedio;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                dissMissDialog();
                DialogFragmentManger.instance().dissMissDialog();
                break;
            case R.id.btn_OK:
                com.androidex.logger.Log.d(TAG,"视频播放正常");
                sendBrocast();
                break;
            case R.id.btn_NG:
                com.androidex.logger.Log.d(TAG,"视频播放失败");
                sendBrocast();
                break;
        }
    }

    private void sendBrocast() {
        Intent intent = new Intent(action_start_network_text);
        getContext().sendBroadcast(intent);
        DialogFragmentManger.instance().dissMissDialog();
    }

    @Override
    protected void stopLoad() {
        super.stopLoad();
        if (customVideoView != null) {
            customVideoView.stopPlayback();
            customVideoView.setVisibility(View.GONE);
        }
    }
}
