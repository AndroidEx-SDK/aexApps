package com.androidex.face;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidex.face.utils.InitUtil;
import com.kongqw.interfaces.OnFaceDetectorListener;
import com.kongqw.interfaces.OnOpenCVInitListener;
import com.kongqw.util.FaceUtil;
import com.kongqw.view.CameraFaceDetectionView;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

/**
 * 人脸识别示例
 * Created by cts on 17/5/26.
 */
public class MainActivity extends Activity implements OnFaceDetectorListener {
    private static final String TAG = "FaceActivity";
    private CameraFaceDetectionView mCameraFaceDetectionView;
    private TextView tv_newFace;//
    private ImageView mImageViewFace1;
    private TextView mCmpPic;
    private TextView face_time;//识别时间
    private Mat newMat;
    private static final int MSG_UPDATE = 0X01;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATE:

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitUtil.initPermissionManager(MainActivity.this);//初始化权限管理类
        initView();
    }

    public void initView() {
        initDetectionView();//初始化检测人脸的View
        //身份证信息view
        tv_newFace = (TextView) findViewById(R.id.tv_newFace);//
        mImageViewFace1 = (ImageView) findViewById(R.id.face1);//捕捉到的人脸
        face_time = (TextView) findViewById(R.id.face_time);//识别时间
        mCmpPic = (TextView) findViewById(R.id.text_view);//相似度
    }

    /**
     * 检测人脸是否存在，相似度大于60%为存在，不予存储
     * Utils.bitmapToMat(bitmap, ma);
     * Imgproc.cvtColor(ma, ma1, Imgproc.COLOR_BGR2GRAY);
     * double cmp = FaceUtil.comPareHist(orb, newMat);//比较两个矩阵的相似度
     */
    public boolean checkIsSave() {
        String[] jsonTime = InitUtil.getJsonTime();
        for (int i = 0; i < jsonTime.length; i++) {
            Mat face = getFace(jsonTime[i]);
            Mat orb = FaceUtil.extractORB(face);//提取特征
            if (newMat != null && orb != null) {
                long startTime = System.currentTimeMillis();
                final double cmp = FaceUtil.match(orb, newMat);//计算相似度
                long afterTime = System.currentTimeMillis();
                final long time = afterTime - startTime;
                Log.d("MainActivity", "cmp=" + cmp);
                if (cmp > 50) {//不存入,返回false
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            face_time.setText("识别时间:" + time + "ms");
                            mCmpPic.setText(String.format("相似度 :  %.2f%%", cmp));
                        }
                    });
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 初始化检测人脸的View
     */
    public void initDetectionView() {
        // 检测人脸的View
        mCameraFaceDetectionView = (CameraFaceDetectionView) findViewById(R.id.cameraFaceDetectionView);
        if (mCameraFaceDetectionView != null) {
            mCameraFaceDetectionView.setOnFaceDetectorListener(MainActivity.this);
            mCameraFaceDetectionView.setOnOpenCVInitListener(new OnOpenCVInitListener() {
                @Override
                public void onLoadSuccess() {
                    Log.i(TAG, "onLoadSuccess: ");
                }

                @Override
                public void onLoadFail() {
                    Log.i(TAG, "onLoadFail: ");
                }

                @Override
                public void onMarketError() {
                    Log.i(TAG, "onMarketError: ");
                }

                @Override
                public void onInstallCanceled() {
                    Log.i(TAG, "onInstallCanceled: ");
                }

                @Override
                public void onIncompatibleManagerVersion() {
                    Log.i(TAG, "onIncompatibleManagerVersion: ");
                }

                @Override
                public void onOtherError() {
                    Log.i(TAG, "onOtherError: ");
                }
            });
            mCameraFaceDetectionView.loadOpenCV(getApplicationContext());
        }
    }

    /**
     * 检测到人脸后的回调
     *
     * @param mat  //检测到的人脸
     * @param rect //
     */
    @Override
    public void onFace(final Mat mat, Rect rect) {
        Log.e(TAG, "检测到人脸");
        Mat matGray = FaceUtil.grayChange(mat, rect);//将检测的人脸置灰且变成固定大小，100x100
        newMat = FaceUtil.extractORB(matGray);//拿着检测到的人脸去提取图片特征
        if (checkIsSave()) {//存入
            long millis = System.currentTimeMillis();
            FaceUtil.saveMat(this, mat, rect, millis + ""); //存入特征
            InitUtil.saveJsonTimes(millis + "");//存储名字
            Log.e(TAG, "录入完成");
        } else {//不存入
            return;
        }
    }

    /**
     * 返回经过灰度处理的mat
     *
     * @param name
     * @return
     */
    public Mat getFace(String name) {
        return FaceUtil.getMat(name);
    }
}
