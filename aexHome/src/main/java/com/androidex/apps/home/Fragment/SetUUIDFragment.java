package com.androidex.apps.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidex.apps.home.FullscreenActivity;
import com.androidex.apps.home.R;

/**
 * 设置UUID的FragMent
 * Created by liyp on 16/12/1.
 */

public class SetUUIDFragment extends BaseDialogFragment implements View.OnClickListener {
    private static final String TAG = "setuuidfragment";
    private static final String UUID_PATH = "/sys/class/androidex_parameters/androidex/uuid";
    private static SetUUIDFragment setUUIDFragment;
    private View rootView;
    private FullscreenActivity activity;
    private EditText et_uuid;
    private boolean flag = false;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.set_uuid, container, false);
        }
        activity = (FullscreenActivity) getActivity();
        iniView();
        return rootView;
    }

    public static SetUUIDFragment instance() {
        if (setUUIDFragment == null) {
            setUUIDFragment = new SetUUIDFragment();
        }
        return setUUIDFragment;
    }

    @Override
    public BaseDialogFragment dissMissDialog() {
        if (setUUIDFragment.isVisible()) {
            setUUIDFragment.dismiss();
        }
        return this;
    }

    public void iniView() {
        et_uuid = (EditText) rootView.findViewById(R.id.et_uuid);
        Button write = (Button) rootView.findViewById(R.id.btn_write);
        Button finish = (Button) rootView.findViewById(R.id.finish);
        ImageView clear = (ImageView) rootView.findViewById(R.id.iv_clear);
        finish.setOnClickListener(this);
        write.setOnClickListener(this);
        clear.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_write:
                /********uuid********/
                String newuuid = et_uuid.getText().toString().trim();
                //String newuuid = uuid.replaceAll("-", "");
                if (newuuid.length() >=32) {
                    activity.hwservice.setAndroidExParameter(UUID_PATH, newuuid);
                    flag = true;
                    Toast.makeText(getContext(), newuuid, Toast.LENGTH_LONG).show();
                    android.util.Log.e("======newnewuuid++", newuuid);
                } else {
                    Toast.makeText(getContext(), "请输入正确的UUID", Toast.LENGTH_LONG).show();
                }

                if (activity.hwservice.getAndroidExParameter(UUID_PATH).equals("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF")) {
                    Toast.makeText(getContext(), "原始UUID："+activity.hwservice.get_uuid(), Toast.LENGTH_LONG).show();
                    android.util.Log.e("原始UUID:", activity.hwservice.get_uuid());
                } else {
                    Toast.makeText(getContext(), "newnewuuid："+activity.hwservice.get_uuid(), Toast.LENGTH_LONG).show();
                    android.util.Log.e("======newnewuuid++", activity.hwservice.get_uuid());
                }

                /****用户信息*******/
                String userInfo1= activity.hwservice.getUserInfo();
                Toast.makeText(getContext(), "原始用户信息："+userInfo1, Toast.LENGTH_LONG).show();
                android.util.Log.e("原始用户信息：", userInfo1);
                activity.hwservice.setUserInfo(newuuid);
                String userInfo2 = activity.hwservice.getUserInfo();
                if (userInfo2.equals(userInfo1)) {
                    Toast.makeText(getContext(), "用户信息写入失败："+activity.hwservice.getUserInfo(), Toast.LENGTH_LONG).show();
                    android.util.Log.e("用户信息写入失败：=====", activity.hwservice.getUserInfo());
                }else {
                    Toast.makeText(getContext(), "用户信息写入成功："+activity.hwservice.getUserInfo(), Toast.LENGTH_LONG).show();
                    android.util.Log.e("用户信息写入成功：", activity.hwservice.getUserInfo());
                }

                /********密码********/
                String pass1 = activity.hwservice.get_pass();
                Toast.makeText(getContext(), "原始密码: "+pass1, Toast.LENGTH_LONG).show();
                android.util.Log.e("原始密码: ", pass1);

                activity.hwservice.set_pass("123456789");
                String pass2 = activity.hwservice.get_pass();
                if (pass2.equals("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")) {
                    Toast.makeText(getContext(), "密码修改失败: "+pass2, Toast.LENGTH_LONG).show();
                    android.util.Log.e("密码修改失败: ", pass2);

                }else {
                    Toast.makeText(getContext(), "新的密码："+pass2, Toast.LENGTH_LONG).show();
                    android.util.Log.e("新的密码：", pass2);
                }
                break;

            case R.id.finish:
                if (flag) {
                    //activity.hwservice.runReboot();
                    Toast.makeText(getContext(), flag + "", Toast.LENGTH_LONG).show();
                    android.util.Log.e("======重启了", "重启了");
                } else {
                    Toast.makeText(getContext(), "请先输入UUID", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.iv_clear:
                et_uuid.setText(null);
                break;
        }
    }
}
