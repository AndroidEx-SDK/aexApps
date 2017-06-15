package com.androidex.face;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;

import static com.kongqw.view.CameraFaceDetectionView.mJavaDetector;

/**
 * 人证识别示例
 */
public class FaceCardActivity extends AppCompatActivity implements OnFaceDetectorListener, IdCardUtil.BitmapCallBack {
    private static final String TAG = "FaceCardActivity";
    public static final String DOOR_ACTION = "com.androidex.door";
    private static final String FACE1 = "face1";
    private static final String FACE2 = "face2";
    private static final int MINCMP = 60;
    private static boolean isGettingFace = true;
    private static boolean isSaveFace = false;
    private boolean isShow = false;
    private Bitmap mBitmapFace1;
    private Bitmap mBitmapFace2;
    private Bitmap mBitmapFace3;
    private ImageView mImageViewFace1;
    private ImageView mImageViewFace2;
    private TextView textViewName, textViewSex, textViewNation, textViewBirthday, textViewPIDNo, textViewAddress;//身份证信息
    private ImageView imageViewPhoto;
    private TextView face_time;//识别时间
    private TextView mCmpPic;
    private double mMaxCmp = 0.0;
    private double cmp = 0.0;
    private double cmp1 = 0.0;
    private double cmp2 = 0.0;
    private double cmpTemp;
    private long startTime, afterTime; //比对的前后时间
    private CameraFaceDetectionView mCameraFaceDetectionView;
    private PermissionsManager mPermissionsManager;

    private IdCardUtil mIdCardUtil;
    public MatOfRect matFace;
    public Mat matFace1;
    private IDCard idCard;

    public FaceDao faceDao;
    private Button bt_look;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facecard);

        faceDao = FaceDao.getInstance(this);
        //loadFaceImg = new LoadFaceImg(this);
        initDetectView();//初始化人脸检测View
        // 显示的View
        mImageViewFace1 = (ImageView) findViewById(R.id.face1);
        mImageViewFace2 = (ImageView) findViewById(R.id.face2);
        mCmpPic = (TextView) findViewById(R.id.text_view);
        face_time = (TextView) findViewById(R.id.face_time);
        Button bn_get_face = (Button) findViewById(R.id.bn_get_face);
        //身份证信息view
        textViewName = (TextView) findViewById(R.id.textViewName);
        textViewSex = (TextView) findViewById(R.id.textViewSex);
        textViewBirthday = (TextView) findViewById(R.id.textViewBirthday);
        textViewNation = (TextView) findViewById(R.id.textViewNation);
        textViewAddress = (TextView) findViewById(R.id.textViewAddress);
        textViewPIDNo = (TextView) findViewById(R.id.textViewPIDNo);
        imageViewPhoto = (ImageView) findViewById(R.id.imageViewPhoto);
        InitUtil.initPermissionManager(FaceCardActivity.this) ;//初始化权限管理类
        //查看已经录入的人脸信息
        bt_look = (Button) findViewById(R.id.bt_look);
        bt_look.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FaceCardActivity.this, ImageListActivity.class);
                startActivity(intent);
            }
        });
        // 存入人脸信息
        bn_get_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSaveFace = true;
            }
        });
        Button switch_camera = (Button) findViewById(R.id.switch_camera);
        // 切换摄像头（如果有多个）
        switch_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 切换摄像头
                boolean isSwitched = mCameraFaceDetectionView.switchCamera();
                Toast.makeText(getApplicationContext(), isSwitched ? "摄像头切换成功" : "摄像头切换失败", Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void initDetectView() {
        //获取屏幕的宽和高
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        // 检测人脸的View
        mCameraFaceDetectionView = (CameraFaceDetectionView) findViewById(R.id.cameraFaceDetectionView);
        //动态设置宽和高
        RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams) mCameraFaceDetectionView.getLayoutParams(); //取控件textView当前的布局参数
        linearParams.height = height * 2;
        //linearParams.width = width/2;
        mCameraFaceDetectionView.setLayoutParams(linearParams); //使设置好的布局参数应用到控件
        if (mCameraFaceDetectionView != null) {
            mCameraFaceDetectionView.setOnFaceDetectorListener(this);
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
    protected void onResume() {
        super.onResume();
        //打开阅读器
        if (mIdCardUtil == null) {
            mIdCardUtil = new IdCardUtil(this, this);
        }
        mIdCardUtil.openIdCard();
        mIdCardUtil.readIdCard();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_1) {
            //录入人脸
            isSaveFace = true;
        } else if (keyCode == KeyEvent.KEYCODE_2) {
            //查看
            Intent intent = new Intent(FaceCardActivity.this, ImageListActivity.class);
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 设置应用权限
     *
     * @param view view
     */
    public void setPermissions(View view) {
        PermissionsManager.startAppSettings(getApplicationContext());
    }

    /**
     * 检测到人脸
     *
     * @param mat  Mat
     * @param rect Rect
     */
    @Override
    public void onFace(final Mat mat, final Rect rect) {
        //是否录入人脸判断
        if (isSaveFace) {
            isSaveFace = false;
            //和文件里面已经存入的人脸做对比，有相同的则不存储，
            //查询数据库
            ArrayList<UserInfo> userInfoArrayList = faceDao.getUserinfo();
            if (userInfoArrayList != null && userInfoArrayList.size() > 0) {
                for (int i = 0; i < userInfoArrayList.size(); i++) {
                    UserInfo users = userInfoArrayList.get(i);
                    Bitmap bitmap = BitmapFactory.decodeFile(users.getFacepath());
                    if (bitmap != null) {
                        Mat matFinal = FaceUtil.grayChange(mat, rect);
                        Mat ma = new Mat();
                        Mat ma1 = new Mat();
                        Utils.bitmapToMat(bitmap, ma);

                        Imgproc.cvtColor(ma, ma1, Imgproc.COLOR_BGR2GRAY);

                        cmp1 = FaceUtil.comPareHist(matFinal, ma1);//
                        Log.d(TAG, "onFace: cmp1=" + cmp1);
                        if (cmp1 > MINCMP) {//有相同的，不存入
                            Log.d(TAG, "onFace: 已经存入过");
                            break;
                        }
                    }
                }
            }
            if (cmp1 < MINCMP) {
                Log.d(TAG, "onFace: 没有存入过");
                String time = System.currentTimeMillis() + "";
                //存入数据
                FaceUtil.saveImage(this, mat, rect, time);
                faceDao.insertUserinfo(null, getApplicationContext().getFilesDir().getPath() + time + ".jpg");
                isShow = true;
            }
        }
        if (isGettingFace) {
            mBitmapFace1 = null;
            cmpTemp = 0.0;
            Mat m = FaceUtil.extractORB(mat);//拿着检测到的人脸去提取图片特征
            if (idCard != null && matFace1 != null) {//读取到身份证照片才做对比
                startTime = System.currentTimeMillis();
                cmp = FaceUtil.match(m, matFace1);//计算相似度
                afterTime = System.currentTimeMillis();
                if (mMaxCmp < cmp) {
                    mMaxCmp = cmp;
                    UpdateFaceResult(mat, rect, mMaxCmp);
                }
            }
          /*  else{
                ArrayList<UserInfo> userInfoArrayList = new ArrayList<UserInfo>();
                userInfoArrayList = faceDao.getUserinfo();
               // FaceUtil.saveImage(this,mat,rect,FACE1);
                if (userInfoArrayList!=null&&userInfoArrayList.size()>0){
                    for (int i= 0;i<userInfoArrayList.size();i++){
                        UserInfo users = new UserInfo();
                        users = userInfoArrayList.get(i);
                        *//*cmp = FaceUtil.compare(this,users.getFacepath(),FACE1);
                        if (cmp>MINCMP){//
                            break;
                        }*//*
                        Bitmap bitmap = BitmapFactory.decodeFile(users.getFacepath());
                        if (bitmap!=null){
                            Mat matFinal = FaceUtil.grayChange(mat,rect);
                            Mat ma = new Mat();
                            Mat ma1 = new Mat();
                            Utils.bitmapToMat(bitmap,ma);
                            Imgproc.cvtColor(ma,ma1,Imgproc.COLOR_BGR2GRAY);
                            //ma1 = FaceUtil.extractORB(ma1);
                            //cmp = FaceUtil.match(m,ma1);
                            cmp = FaceUtil.comPareHist(matFinal,ma1);
                            if (cmp>MINCMP){//
                                break;
                            }
                        }
                    }
                }
            }*/
        }
    }

    private void UpdateFaceResult(final Mat mat, final Rect rect, final double lcmp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (idCard != null) {
                    if (null == mat) {
                        mImageViewFace1.setImageResource(R.mipmap.ic_contact_picture);
                    } else {
                        face_time.setText("识别时间:" + (afterTime - startTime) + "ms");

                        if (lcmp > 50) {
                            FaceUtil.saveImage(FaceCardActivity.this, mat, rect, FACE1);
                            mBitmapFace1 = FaceUtil.getImage(FaceCardActivity.this, FACE1);
                            mImageViewFace1.setImageBitmap(mBitmapFace1);
                            mCmpPic.setText(String.format("相似度 :  高（%.2f%%）", cmp));
                        } else if (lcmp >= 40 && lcmp <= 50) {
                            //FaceUtil.saveImage(FaceCardActivity.this,mat,rect,FACE1);
                            //mBitmapFace1 = FaceUtil.getImage(FaceCardActivity.this,FACE1);
                            //mImageViewFace1.setImageBitmap(mBitmapFace1);
                            //mCmpPic.setText("相似度 :  中");
                        } else {
                            //mCmpPic.setText("相似度 :  低");
                        }

                    }
                } else {
                    mCmpPic.setText("相似度 :    ");
                    face_time.setText("识别时间:  ");
                    mImageViewFace1.setImageResource(R.mipmap.ic_contact_picture);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionsManager.recheckPermissions(requestCode, permissions, grantResults);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIdCardUtil.close();
    }

    //身份证回调
    @Override
    public void callBack(int a) {
        if (a == IdCardUtil.READ) {
            idCard = mIdCardUtil.getIdCard();
            if (idCard != null) {

                Log.e(TAG,"====身份证有信息");
                if (matFace == null) matFace = new MatOfRect();
                if (matFace1 == null) matFace1 = new Mat();
                Mat mat2 = new Mat();
                mBitmapFace2 = idCard.getPhoto();
                mBitmapFace3 = FaceUtil.getSizeBmp(FaceUtil.grey(mBitmapFace2));
                Utils.bitmapToMat(mBitmapFace3, mat2);
                if (mJavaDetector != null) {
                    //Log.e(TAG, "onCreate: 级联容器加载成功");
                    mJavaDetector.detectMultiScale(mat2, matFace);
                    //取出身份证人脸部分
                    Rect[] rects = matFace.toArray();
                    for (Rect rect : rects) {
                        Core.rectangle(mat2, new Point(rect.x, rect.y), new Point(rect.x
                                + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
                        matFace1 = FaceUtil.grayChange(mat2, rect);//灰度处理
                        matFace1 = FaceUtil.extractORB(matFace1);//提取图片特征

                    }
                } else {
                    //Log.e(TAG, "onCreate: 级联容器加载失败");
                }
            } else {
                // isGettingFace = true;
                matFace = null;
                matFace1 = null;
                mMaxCmp = 0;
                UpdateFaceResult(null, null, 0);
                Log.e(TAG,"====身份证无信息");
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (null == mBitmapFace2 || idCard == null) {
                        textViewName.setText("姓名:");
                        textViewSex.setText("性别");
                        textViewBirthday.setText("生日:");
                        textViewNation.setText("名族:");
                        textViewAddress.setText("住址：");
                        textViewPIDNo.setText("身份证号码:");
                        imageViewPhoto.setImageResource(R.drawable.photo);
                        mImageViewFace2.setImageResource(R.mipmap.ic_contact_picture);
                    } else {
                        textViewName.setText("姓名:" + idCard.getName());
                        textViewSex.setText("性别:" + idCard.getSex());
                        textViewBirthday.setText("生日:" + idCard.getBirthday());
                        textViewNation.setText("名族:" + idCard.getNation());
                        textViewAddress.setText("住址:" + idCard.getAddress());
                        textViewPIDNo.setText("身份证号码:" + idCard.getIDCardNo());
                        imageViewPhoto.setImageBitmap(mBitmapFace2);
                        mImageViewFace2.setImageBitmap(mBitmapFace2);
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
                    imageViewPhoto.setImageResource(R.drawable.photo);
                    mImageViewFace2.setImageResource(R.mipmap.ic_contact_picture);
                    matFace = null;
                    matFace1 = null;
                    mMaxCmp = 0;
                    UpdateFaceResult(null, null, 0);
                }
            });
        }
    }


}
