package com.androidex.face;

import android.app.Activity;
import android.graphics.Bitmap;
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

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

/**
 * 人脸识别示例
 * Created by cts on 17/5/26.
 */
public class MainActivity extends Activity implements OnFaceDetectorListener {
    private static final String TAG = "MainActivity";
    private CameraFaceDetectionView mCameraFaceDetectionView;
    private TextView tv_newFace;//
    private ImageView mImageViewFace1;
    private TextView mCmpPic;
    private TextView tv_num;
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
        tv_num = (TextView) findViewById(R.id.tv_num);//相似度
    }

    /**
     * 检测人脸是否存在，相似度大于60%为存在，不予存储
     * Utils.bitmapToMat(bitmap, ma);
     * Imgproc.cvtColor(ma, ma1, Imgproc.COLOR_BGR2GRAY);
     * double cmp = FaceUtil.comPareHist(orb, newMat);//比较两个矩阵的相似度
     */
    public boolean checkIsSave() {
        final String[] jsonTime = InitUtil.getJsonTime(MainActivity.this);
        Log.d("MainActivity", "jsonTime=" + jsonTime.length);
        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < jsonTime.length; i++) {
            Mat face = getFace(jsonTime[i]);
            if (face!=null){
                if (newMat != null && face != null) {
                    final double cmp = FaceUtil.match(face, newMat);//计算相似度
                    Log.d("MainActivity", "cmp=" + cmp);
                    if (cmp > 90) {//不存入,返回false
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                long afterTime = System.currentTimeMillis();
                                final long time = afterTime - startTime;
                                face_time.setText("识别时间:" + time + "ms");
                                tv_num.setText("比对个数: "+jsonTime.length);
                                mCmpPic.setText(String.format("相似度 :  %.2f%%", cmp));
                                tv_newFace.setText("是同一个人");
                            }
                        });
                        return false;
                    }
                }
            }else {
                Log.d("MainActivity", "没有读取到人脸特征" );
            }
        }
        long afterTime = System.currentTimeMillis();
        final long time = afterTime - startTime;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_num.setText("比对个数: "+jsonTime.length);
                tv_newFace.setText("添加到末尾");
                face_time.setText("识别时间:" + time + "ms");
            }
        });
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
            boolean b = FaceUtil.saveMat(this, mat, rect, millis+"" );//存入特征
            Log.e(TAG, "b=="+b);
            if (b){
                InitUtil.saveJsonTimes(millis+"" ,MainActivity.this);//存储名字
            }else{
                Log.e(TAG, "录入失败"+millis);
                return;
            }
            Log.e(TAG, "录入完成"+millis);
        } else {//不存入
            Log.e(TAG, "不录入");
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
       // return FaceUtil.getMat(name);
        Mat mat = new Mat();
        Bitmap bitmap = FaceUtil.getImage(MainActivity.this, name);
        Utils.bitmapToMat(bitmap, mat);
        //提取图片特征//从存储的信息中取出的图片
        mat = FaceUtil.extractORB(mat);
        return mat;
    }
}
