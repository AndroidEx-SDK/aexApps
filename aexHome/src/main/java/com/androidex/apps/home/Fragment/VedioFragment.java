package com.androidex.apps.home.fragment;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.androidex.apps.home.R;
import com.androidex.apps.home.view.CustomVideoView;

import static com.androidex.apps.home.FullscreenActivity.action_start_network_text;

/**
 * Created by liyp on 16/12/10.
 */

public class VedioFragment extends LazyLoadFragment implements View.OnClickListener {
    private ImageView iv_close;
    private CustomVideoView customVideoView;
    private static VedioFragment fragment;

    public VedioFragment() {

    }

    @Override
    protected void lazyLoad() {
        iniView();
        /**获取参数，根据不同的参数播放不同的视频**/
        Uri uri= Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.a002);
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

    public static VedioFragment Instance(){
        if (fragment==null){
            fragment = new VedioFragment();
        }
        return fragment;
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_vedio;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                //dissMissDialog();
                break;
            case R.id.btn_OK:
                com.androidex.logger.Log.d(TAG, "视频播放正常");
                sendBrocast();
                dissMissDialog();
                break;
            case R.id.btn_NG:
                com.androidex.logger.Log.d(TAG, "视频播放失败");
                sendBrocast();
                dissMissDialog();

                break;
        }
    }
    public VedioFragment dissMissDialog() {
        if (fragment.isVisible()) {
            fragment.dismiss();
        }
        return this;
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
