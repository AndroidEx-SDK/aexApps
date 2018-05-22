package com.androidex.face;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidex.face.idcard.util.IdCardUtil;
import com.androidex.face.utils.InitUtil;
import com.synjones.idcard.IDCard;

/**
 * 身份证阅读示例
 */
public class FaceCardActivity extends AppCompatActivity implements IdCardUtil.BitmapCallBack {
    private static final String TAG = "FaceCardActivity";
    private Bitmap mBitmapFace2;
    private ImageView mImageViewFace2;
    private TextView textViewName, textViewSex, textViewNation, textViewBirthday, textViewPIDNo, textViewAddress;//身份证信息
    private ImageView imageViewPhoto;

    private IdCardUtil mIdCardUtil;
    private IDCard idCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facecard);

        mImageViewFace2 = (ImageView) findViewById(R.id.face2);
        //身份证信息view
        textViewName = (TextView) findViewById(R.id.textViewName);
        textViewSex = (TextView) findViewById(R.id.textViewSex);
        textViewBirthday = (TextView) findViewById(R.id.textViewBirthday);
        textViewNation = (TextView) findViewById(R.id.textViewNation);
        textViewAddress = (TextView) findViewById(R.id.textViewAddress);
        textViewPIDNo = (TextView) findViewById(R.id.textViewPIDNo);
        imageViewPhoto = (ImageView) findViewById(R.id.imageViewPhoto);
        InitUtil.initPermissionManager(FaceCardActivity.this);//初始化权限管理类
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
        } else if (keyCode == KeyEvent.KEYCODE_2) {

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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

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

                Log.e(TAG, "====身份证有信息");

                mBitmapFace2 = idCard.getPhoto();

            } else {
                Log.e(TAG, "====身份证无信息");
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
                }
            });
        }
    }
}
