package com.androidex.aexkk30.fragment;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.androidex.aexkk30.OneKeyTextActivity;
import com.androidex.aexkk30.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by cts on 16/12/10.
 */

public class CameraFragment extends DialogFragment implements SurfaceHolder.Callback, View.OnClickListener {
    public static final String TAG = "CameraFragment";
    private View rootView;
    private OneKeyTextActivity activity;
    public boolean isCancelable = false;
    private Button btn_NG;
    private Button btn_OK;
    private Camera mCamera;
    private SurfaceView sv;
    private SurfaceHolder sh;
    private static CameraFragment cameraFragment;
    private ImageView iv_close;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        setCancelable(isCancelable);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity = (OneKeyTextActivity) getActivity();
        iniView();
        return rootView;
    }

    public void iniView() {
        sv = (SurfaceView) rootView.findViewById(R.id.sv);
        // 初始化SurfaceHolder
        sh = sv.getHolder();
        sh.addCallback(this);

        btn_NG = (Button) rootView.findViewById(R.id.btn_NG);
        btn_OK = (Button) rootView.findViewById(R.id.btn_OK);
        iv_close = (ImageView) rootView.findViewById(R.id.iv_close);
        btn_NG.setOnClickListener(this);
        btn_OK.setOnClickListener(this);
        iv_close.setOnClickListener(this);

        if (mCamera == null) { // 在activity运行时绑定
            mCamera = getcCamera();
            if (sh != null) {
                showViews(mCamera, sh);
            }
        }

    }

    public static CameraFragment instance() {
        if (cameraFragment == null) {
            cameraFragment = new CameraFragment();
        }
        return cameraFragment;
    }

    private Camera.PictureCallback pc = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // data为完整数据
            File file = new File("/sdcard/photo.png");
            // 使用流进行读写
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(data);
                // 关闭流
                fos.close();
//                // 查看图片
//                Intent intent = new Intent(CameraActivity.this,
//                        PhotoActivity.class);
//                // 传递路径
//                intent.putExtra("path", file.getAbsolutePath());
//                startActivity(intent);
//                CameraActivity.this.finish();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public CameraFragment dissMissDialog() {
        if (cameraFragment.isVisible()) {
            cameraFragment.dismiss();
        }
        clearCamera();
        rootView=null;
        return this;
    }

    /**
     * 获取系统相机
     *
     * @return
     */
    private Camera getcCamera() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            camera = null;
        }
        return camera;
    }

    /**
     * 与SurfaceView传播图像
     */
    private void showViews(Camera camera, SurfaceHolder holder) {
        // 预览相机,绑定
        try {
            camera.setPreviewDisplay(holder);
            // 系统相机默认是横屏的，我们要旋转90°
            //camera.setDisplayOrientation(90);
            // 开始预览
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放相机的内存
     */
    private void clearCamera() {
        // 释放hold资源
        if (mCamera != null) {
            // 停止预览
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            // 释放相机资源
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 开始预览
        showViews(mCamera, sh);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // 重启功能
        mCamera.stopPreview();
        showViews(mCamera, sh);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 释放
        clearCamera();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                dissMissDialog();
                break;
            case R.id.btn_OK:
//                // 获取当前相机参数
//                Camera.Parameters parameters = mCamera.getParameters();
//                // 设置相片格式
//                parameters.setPictureFormat(ImageFormat.JPEG);
//                // 设置预览大小
//                parameters.setPreviewSize(800, 800);
//                // 设置对焦方式，这里设置自动对焦
//                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//                mCamera.autoFocus(new Camera.AutoFocusCallback() {
//
//                    @Override
//                    public void onAutoFocus(boolean success, Camera camera) {
//                        // 判断是否对焦成功
//                        if (success) {
//                            // 拍照 第三个参数为拍照回调
//                            mCamera.takePicture(null, null, pc);
//                        }
//                    }
//                });
                VedioFragment.Instance().show(activity.getSupportFragmentManager(), "vediofragment");
                OneKeyTextActivity.setTextResult("相机测试OK", false);
                Log.d(TAG, "相机测试OK");
                dissMissDialog();
                break;
            case R.id.btn_NG:
                VedioFragment.Instance().show(activity.getSupportFragmentManager(), "vediofragment");
                OneKeyTextActivity.setTextResult("相机测试不良", true);
                Log.d(TAG, "相机测试不良");
                dissMissDialog();
                break;
        }
    }
}
