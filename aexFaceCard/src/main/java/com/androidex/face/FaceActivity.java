package com.androidex.face;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidex.face.db.CardInfoDao;
import com.androidex.face.db.FaceDao;
import com.androidex.face.utils.UserInfo;
import com.androidex.face.idcard.util.IdCardUtil;
import com.androidex.face.utils.InitUtil;
import com.kongqw.interfaces.OnFaceDetectorListener;
import com.kongqw.interfaces.OnOpenCVInitListener;
import com.kongqw.util.FaceUtil;
import com.kongqw.view.CameraFaceDetectionView;
import com.synjones.idcard.IDCard;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.kongqw.view.CameraFaceDetectionView.mJavaDetector;

/**
 * 人脸识别示例
 * Created by cts on 17/5/26.
 */
public class FaceActivity extends AppCompatActivity implements View.OnClickListener, OnFaceDetectorListener, IdCardUtil.BitmapCallBack {

    private static final String TAG = "FaceActivity";
    private CameraFaceDetectionView mCameraFaceDetectionView;
    private FaceDao mFaceDao;
    private CardInfoDao mCardInfoDao;
    private IdCardUtil mIdCardUtil;
    private IDCard idCard;
    private Bitmap photo;
    private Bitmap head;
    private TextView textViewName, textViewSex, textViewNation, textViewBirthday, textViewPIDNo, textViewAddress;//身份证信息
    private ImageView imageViewPhoto;
    private ImageView mImageViewFace1;
    private ImageView mImageViewFace2;
    private TextView mCmpPic;
    private TextView face_time;//识别时间
    private Mat newMat;
    private MatOfRect matFace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);
        InitUtil.initPermissionManager(FaceActivity.this);//初始化权限管理类
        initView();
        initDao();//初始化数据库
    }

    public void initView() {
        initDetectionView();//初始化检测人脸的View
        //身份证信息view
        textViewName = (TextView) findViewById(R.id.textViewName);
        textViewSex = (TextView) findViewById(R.id.textViewSex);
        textViewBirthday = (TextView) findViewById(R.id.textViewBirthday);
        textViewNation = (TextView) findViewById(R.id.textViewNation);
        textViewAddress = (TextView) findViewById(R.id.textViewAddress);
        textViewPIDNo = (TextView) findViewById(R.id.textViewPIDNo);
        imageViewPhoto = (ImageView) findViewById(R.id.imageViewPhoto);
        mImageViewFace1 = (ImageView) findViewById(R.id.face1);
        mImageViewFace2 = (ImageView) findViewById(R.id.face2);
        face_time = (TextView) findViewById(R.id.face_time);
        mCmpPic = (TextView) findViewById(R.id.text_view);

        Button btn_saveface = (Button) findViewById(R.id.btn_saveCardInfo);//存储身份信息
        Button btn_close = (Button) findViewById(R.id.btn_close);//关闭身份证阅读
        Button btn_lookFaceList = (Button) findViewById(R.id.btn_lookFaceList);//查看存储列表
        btn_saveface.setOnClickListener(this);
        btn_close.setOnClickListener(this);
        btn_lookFaceList.setOnClickListener(this);
    }

    /**
     * 初始化数据库
     */
    public void initDao() {
        mFaceDao = FaceDao.getInstance(this);
        mCardInfoDao = CardInfoDao.getInstance(this);
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
        //获取屏幕的宽和高
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        //动态设置宽和高
        RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams) mCameraFaceDetectionView.getLayoutParams(); //取控件textView当前的布局参数
        linearParams.height = height * 2;
        mCameraFaceDetectionView.setLayoutParams(linearParams); //使设置好的布局参数应用到控件

        if (mCameraFaceDetectionView != null) {
            mCameraFaceDetectionView.setOnFaceDetectorListener(FaceActivity.this);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_saveCardInfo://存储身份信息
                if (idCard == null || head == null) {
                    Toast.makeText(this, "身份证信息为空", Toast.LENGTH_LONG).show();
                } else {
                    saveCardInfo(idCard, head);
                    idCard = null;
                    head = null;
                    Toast.makeText(this, "存储成功", Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.btn_lookFaceList://查看人脸列表
                Intent intent = new Intent(FaceActivity.this, ImageListActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_close:
                mIdCardUtil.close();
                mIdCardUtil.closeIdCard();
                mIdCardUtil.closeReadThread();
                break;

            case R.id.btn_startread:
                //打开阅读器
                if (mIdCardUtil == null) {
                    mIdCardUtil = new IdCardUtil(this, this);
                }
                mIdCardUtil.openIdCard();
                mIdCardUtil.readIdCard();

                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //打开阅读器
        if (mIdCardUtil == null) {
            mIdCardUtil = new IdCardUtil(this, this);
        }
        mIdCardUtil.openIdCard();
        mIdCardUtil.readIdCard();
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
        //saveFace(mat, rect);//自动存储人脸信息

        FaceUtil.saveImage(FaceActivity.this, mat, rect, "face");
        head = FaceUtil.getImage(FaceActivity.this, "face");

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
                if (idCard != null) {
                    if (null == mat) {
                        mImageViewFace1.setImageResource(R.mipmap.ic_contact_picture);
                    } else {
                        face_time.setText("识别时间:" + (time) + "ms");
                        mCmpPic.setText(String.format("相似度 :  %.2f%%", lcmp));
                    }
                } else {
                    mCmpPic.setText("相似度 :    ");
                    face_time.setText("识别时间:  ");
                    mImageViewFace1.setImageResource(R.mipmap.ic_contact_picture);
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

    /**
     * 读取身份证回调
     *
     * @param a
     */
    @Override
    public void callBack(int a) {
        if (a == IdCardUtil.READ) {
            idCard = mIdCardUtil.getIdCard();
            if (idCard != null) {//不自动存储
                photo = idCard.getPhoto();
                Mat mat = new Mat();
                Map<String, String> map = getMap(idCard.getIDCardNo());
                if (map != null) {
                    String headPath = map.get("head");
                    if (headPath != null) {
                        Bitmap bitmap = getBitmap(headPath);
                        Utils.bitmapToMat(bitmap, mat);
                        //提取图片特征//从存储的信息中取出的图片
                        newMat = FaceUtil.extractORB(mat);
                        Log.e(TAG, "====headPath: " + headPath);
                    }
                    Log.e(TAG, "====map:  bmp");
                } else {

                    Log.e(TAG, "====map:  null");
                    if (matFace == null) matFace = new MatOfRect();
                    if (newMat == null) newMat = new Mat();
                    Bitmap bmp = FaceUtil.getSizeBmp(FaceUtil.grey(photo));
                    Utils.bitmapToMat(bmp, mat);
                    if (mJavaDetector != null) {
                        //Log.e(TAG, "onCreate: 级联容器加载成功");
                        mJavaDetector.detectMultiScale(mat, matFace);
                        //取出身份证人脸部分
                        Rect[] rects = matFace.toArray();
                        for (Rect rect : rects) {
                            Core.rectangle(mat, new Point(rect.x, rect.y), new Point(rect.x
                                    + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
                            newMat = FaceUtil.grayChange(mat, rect);//灰度处理
                            newMat = FaceUtil.extractORB(newMat);//提取图片特征
                        }
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (idCard == null) {
                            textViewName.setText("姓名:");
                            textViewSex.setText("性别");
                            textViewBirthday.setText("生日:");
                            textViewNation.setText("名族:");
                            textViewAddress.setText("住址：");
                            textViewPIDNo.setText("身份证号码:");
                            face_time.setText("识别时间:");
                            mCmpPic.setText("相似度 :  ");
                            imageViewPhoto.setImageResource(R.drawable.photo);
                            mImageViewFace2.setImageResource(R.mipmap.ic_contact_picture);
                            newMat = null;
                            head = null;

                        } else {
                            textViewName.setText("姓名:" + idCard.getName());
                            textViewSex.setText("性别:" + idCard.getSex());
                            textViewBirthday.setText("生日:" + idCard.getBirthday());
                            textViewNation.setText("名族:" + idCard.getNation());
                            textViewAddress.setText("住址:" + idCard.getAddress());
                            textViewPIDNo.setText("身份证号码:" + idCard.getIDCardNo());

                            imageViewPhoto.setImageBitmap(photo);
                            mImageViewFace2.setImageBitmap(photo);
                        }
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewName.setText("姓名:");
                        textViewSex.setText("性别");
                        textViewBirthday.setText("生日:");
                        textViewNation.setText("名族:");
                        textViewAddress.setText("住址：");
                        textViewPIDNo.setText("身份证号码:");
                        face_time.setText("识别时间:");
                        face_time.setText("识别时间:");
                        mCmpPic.setText("相似度 :  ");
                        imageViewPhoto.setImageResource(R.drawable.photo);
                        mImageViewFace2.setImageResource(R.mipmap.ic_contact_picture);
                        matFace = null;
                        newMat = null;
                        head = null;

                    }
                });
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewName.setText("姓名:");
                    textViewSex.setText("性别");
                    textViewBirthday.setText("生日:");
                    textViewNation.setText("名族:");
                    textViewAddress.setText("住址：");
                    textViewPIDNo.setText("身份证号码:");
                    face_time.setText("识别时间:");
                    face_time.setText("识别时间:");
                    mCmpPic.setText("相似度 :  ");
                    imageViewPhoto.setImageResource(R.drawable.photo);
                    mImageViewFace2.setImageResource(R.mipmap.ic_contact_picture);
                    matFace = null;
                    newMat = null;
                    head = null;
                }
            });
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIdCardUtil.close();
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
