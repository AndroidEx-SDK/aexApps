package com.androidex.apps.home.fragment;

import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.androidex.apps.home.R;
import com.androidex.apps.home.utils.NetWork;
import com.androidex.apps.home.view.CustomVideoView;

/**
 * Created by liyp on 16/12/10.
 */

public class VedioFragment extends  LazyLoadFragment implements View.OnClickListener {
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
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
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
                com.androidex.logger.Log.d(TAG, "视频播放OK");
                NetWork.wifiManger(getActivity());
                dissMissDialog();
                break;
            case R.id.btn_NG:
                com.androidex.logger.Log.d(TAG, "视频播放失败");
                NetWork.wifiManger(getActivity());
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

    @Override
    protected void stopLoad() {
        super.stopLoad();
        if (customVideoView != null) {
            customVideoView.stopPlayback();
            customVideoView.setVisibility(View.GONE);
        }
    }
}
