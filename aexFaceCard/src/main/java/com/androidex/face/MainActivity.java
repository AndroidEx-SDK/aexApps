package com.androidex.face;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidex.face.db.FaceDao;
import com.androidex.face.utils.InitUtil;
import com.androidex.face.utils.UserInfo;
import com.kongqw.interfaces.OnFaceDetectorListener;
import com.kongqw.interfaces.OnOpenCVInitListener;
import com.kongqw.util.FaceUtil;
import com.kongqw.view.CameraFaceDetectionView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

/**
 * 人脸识别示例
 * Created by cts on 17/5/26.
 */
public class MainActivity extends Activity implements OnFaceDetectorListener {
    private static final String TAG = "FaceActivity";
    private CameraFaceDetectionView mCameraFaceDetectionView;
    private FaceDao mFaceDao;
    private Bitmap photo;
    private Bitmap head;
    private TextView tv_newFace;//
    private ImageView mImageViewFace1;
    private TextView mCmpPic;
    private TextView face_time;//识别时间
    private Mat newMat;

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
        tv_newFace = (TextView) findViewById(R.id.tv_newFace);
        mImageViewFace1 = (ImageView) findViewById(R.id.face1);
        face_time = (TextView) findViewById(R.id.face_time);
        mCmpPic = (TextView) findViewById(R.id.text_view);
    }

    /**
     * 检测人脸是否存在，相似度大于60%为存在，不予存储
     *
     * @param mat
     * @param rect
     */
    public boolean checkIsSave(Mat mat, final Rect rect) {
        ArrayList<UserInfo> userinfo = mFaceDao.getUserinfo();
        if (userinfo != null && userinfo.size() > 0) {
            for (int i = 0; i < userinfo.size(); i++) {
                UserInfo users = userinfo.get(i);
                Bitmap bitmap = BitmapFactory.decodeFile(users.getFacepath());
                if (bitmap != null) {
                    Mat matFinal = FaceUtil.grayChange(mat, rect);
                    Mat ma = new Mat();
                    Mat ma1 = new Mat();
                    Utils.bitmapToMat(bitmap, ma);
                    Imgproc.cvtColor(ma, ma1, Imgproc.COLOR_BGR2GRAY);
                    double cmp = FaceUtil.comPareHist(matFinal, ma1);//比较两个矩阵的相似度
                    if (cmp > 60) {//不存入,返回false
                        tv_newFace.setText("相似");//同一个人
                        return false;
                    }else {
                        tv_newFace.setText("不像素");//同一个人
                    }
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
    public void onFace(Mat mat, Rect rect) {
        Mat matGray = FaceUtil.grayChange(mat, rect);//将检测的人脸置灰且变成固定大小，100x100
        newMat = FaceUtil.extractORB(matGray);//拿着检测到的人脸去提取图片特征
        saveFace(mat, rect);//自动存储人脸信息
        String[] jsonTime = InitUtil.getJsonTime();
        for (int i=0;i<jsonTime.length;i++){
            Mat face = getFace(jsonTime[i]);
            Mat orb = FaceUtil.extractORB(face);
            if (newMat != null && orb != null) {
                long startTime = System.currentTimeMillis();
                double cmp = FaceUtil.match(orb, newMat);//计算相似度
                long afterTime = System.currentTimeMillis();
                long time = afterTime - startTime;
                UpdateFaceResult(mat, rect, cmp, time);
            }
        }
    }

    private void UpdateFaceResult(final Mat mat, final Rect rect, final double lcmp, final long time) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null == mat) {
                    mImageViewFace1.setImageResource(R.mipmap.ic_contact_picture);
                } else {
                    face_time.setText("识别时间:" + (time) + "ms");
                    mCmpPic.setText(String.format("相似度 :  %.2f%%", lcmp));
                }
            }
        });
    }

    /**
     * 存入人脸,将bitmap保存至固定路径下
     */
    public void saveFace(Mat mat, final Rect rect) {
        if (checkIsSave(mat, rect)) {//为真表示存入
            long millis = System.currentTimeMillis();
            //存入数据
            FaceUtil.saveMat(this, mat, rect, millis+"");
            InitUtil.saveJsonTimes(millis+"");//存储名字
            Log.e(TAG, "录入完成");
        } else {
            Log.e(TAG, "重复录入");
        }
    }

    /**
     * 返回经过灰度处理的mat
     * @param name
     * @return
     */
    public Mat getFace(String name){
       return FaceUtil.getMat(name);
    }
}
