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
import com.synjones.idcard.IDCard;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private MatOfRect matFace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitUtil.initPermissionManager(MainActivity.this);//初始化权限管理类
        initView();
        initDao();//初始化数据库
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
     * 初始化数据库
     */
    public void initDao() {
        mFaceDao = FaceDao.getInstance(this);
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
                        return false;
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
        Mat mCharacteristic = FaceUtil.extractORB(matGray);//拿着检测到的人脸去提取图片特征
        saveFace(mat, rect);//自动存储人脸信息
        FaceUtil.saveImage(MainActivity.this, mat, rect, "face");
        head = FaceUtil.getImage(MainActivity.this, "face");

        if (newMat != null && mCharacteristic != null) {
            long startTime = System.currentTimeMillis();
            double cmp = FaceUtil.match(mCharacteristic, newMat);//计算相似度
            long afterTime = System.currentTimeMillis();
            long time = afterTime - startTime;
            UpdateFaceResult(mat, rect, cmp, time);
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
     * 根据身份证获取存储的信息
     */
    public Map<String, String> getMap(String idnum) {
        List<Map<String, String>> maps = null;
        try {
            maps = InitUtil.parseJson(idnum);

            for (int i = 0; i < maps.size(); i++) {
                Map<String, String> map = maps.get(i);
                if (map.get("idnum").equals(idnum)) {
                    Log.e(TAG, "====idnum: " + idnum);
                    return map;
                } else {
                    Log.e(TAG, "====idnum: null");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bitmap getBitmap(String path) {
        File file = new File(path);
        if (file.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(path);
            return bm;
        }
        return null;

    }

    /**
     * jsonObject.put("name", idCard.getName());
     * jsonObject.put("photo", idCard.getPhoto());
     * jsonObject.put("sex", idCard.getSex());
     * jsonObject.put("nation", idCard.getNation());
     * jsonObject.put("birthday", idCard.getBirthday());
     * jsonObject.put("address", idCard.getAddress());
     * jsonObject.put("idnum", idCard.getIDCardNo());
     * jsonObject.put("head", bmp);
     *
     * @param idCard
     */
    private void saveCardInfo(IDCard idCard, Bitmap head) {
        Bitmap photo = idCard.getPhoto();//身份证照片
        long millis = System.currentTimeMillis();
        InitUtil.saveBitmap("/sdcard/face/", millis + ".png", photo);
        long millis_head = System.currentTimeMillis();
        InitUtil.saveBitmap("/sdcard/face/", millis_head + ".png", head);

        String[] strArray = new String[8];
        strArray[0] = idCard.getName();
        strArray[1] = "/sdcard/face/" + millis + ".png";
        strArray[2] = idCard.getSex();
        strArray[3] = idCard.getNation();
        strArray[4] = idCard.getBirthday();
        strArray[5] = idCard.getAddress();
        strArray[6] = idCard.getIDCardNo();
        strArray[7] = "/sdcard/face/" + millis_head + ".png";//检测到的人脸路径

        InitUtil.saveJsonStringArray(strArray);
    }

    /**
     * 存入人脸,将bitmap保存至固定路径下
     */
    public void saveFace(Mat mat, final Rect rect) {
        if (checkIsSave(mat, rect)) {//为真表示存入
            long millis = System.currentTimeMillis();
            //存入数据
            FaceUtil.saveImage(this, mat, rect, millis + ".png");

            Log.e(TAG, "录入完成");
        } else {
            Log.e(TAG, "重复录入");

        }
    }
}
