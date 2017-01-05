package com.androidex.apps.home.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidex.aexlibs.hwService;
import com.androidex.apps.home.FullscreenActivity;
import com.androidex.apps.home.R;
import com.androidex.logger.Log;

import java.io.UnsupportedEncodingException;

/**
 * 设置UUID的FragMent
 * Created by liyp on 16/12/1.
 */

public class SetUUIDFragment extends DialogFragment implements View.OnClickListener {
    private static final String TAG = "setuuidfragment";
    private static SetUUIDFragment setUUIDFragment;
    private View rootView;
    private FullscreenActivity activity;
    private EditText et_uuid;
    private boolean flag = false;
    public boolean isCancelable = false;
    /**
     * 8 位 UCS 转换格式
     */
    public static final String UTF_8 = "UTF-8";
    private TextView tv_remind;
    private boolean isSimpleSetUUID=true;//为true表示进入测试

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.set_uuid, container, false);
        }
        setCancelable(isCancelable);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
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

    //为true表示进入测试
    public SetUUIDFragment isStartText(boolean flag) {
        isSimpleSetUUID = flag;
        return this;
    }

    public SetUUIDFragment dissMissDialog() {
        if (setUUIDFragment.isVisible()) {
            setUUIDFragment.dismiss();
        }
        return this;
    }

    public void iniView() {
        et_uuid = (EditText) rootView.findViewById(R.id.et_uuid);
        Button finish = (Button) rootView.findViewById(R.id.finish);
        ImageView clear = (ImageView) rootView.findViewById(R.id.iv_clear);
        tv_remind = (TextView) rootView.findViewById(R.id.tv_remind);
        finish.setOnClickListener(this);
        clear.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finish:
                tv_remind.setText("正在写入...");
                /********uuid********/
                String uuid = et_uuid.getText().toString().trim();
                if (uuid.length() >= 32) {
                    // String uuid = Base16.encode(uuid.getBytes());
                    //activity.hwservice.writeHex(hwService.aexp_uuid, uuid);
                    activity.hwservice.setAndroidExParameter(hwService.aexp_uuid, uuid);
                } else {
                    Toast.makeText(getContext(), "请输入正确的UUID", Toast.LENGTH_LONG).show();
                    tv_remind.setText("请输入正确的UUID");
                    return;
                }

                if (activity.hwservice.get_uuid().equals("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF")) {
                    Toast.makeText(getContext(), "写入UUID失败：" + activity.hwservice.get_uuid(), Toast.LENGTH_LONG).show();
                    Log.e("写入UUID失败:", activity.hwservice.get_uuid());
                    tv_remind.setTextColor(Color.RED);
                    tv_remind.setText("UUID写入失败");
                } else {
                    Log.d(TAG, "写入UUID成功");
                    Toast.makeText(getContext(), "写入UUID成功", Toast.LENGTH_LONG).show();
                    tv_remind.setTextColor(Color.BLACK);
                    tv_remind.setText("UUID写入成功");
                    //activity.hwservice.runReboot();
                    if (isSimpleSetUUID){
                        Intent intent = new Intent(FullscreenActivity.action_start_text);
                        getContext().sendBroadcast(intent);
                    }
                    dissMissDialog();
                }
                break;
            case R.id.iv_clear:
                et_uuid.setText(null);
                break;
        }
    }

    /********
     * 密码
     ********/
    private void setPassword() {
        String pass1 = activity.hwservice.get_pass();
        android.util.Log.e("原始密码: ", pass1);
        activity.hwservice.set_pass("123456789");
        String pass2 = activity.hwservice.get_pass();
        if (pass2.equals("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")) {
            Log.e("密码修改失败: ", pass2);
        } else {
            Log.d("新的密码：", pass2);
        }
    }

    public String toUTF_8(String str) throws UnsupportedEncodingException {
        return this.changeCharset(str, UTF_8);
    }

    /**
     * 字符串编码转换的实现方法
     *
     * @param str        待转换的字符串
     * @param newCharset 目标编码
     */
    public String changeCharset(String str, String newCharset)
            throws UnsupportedEncodingException {
        if (str != null) {
            // 用默认字符编码解码字符串。与系统相关，中文windows默认为GB2312
            byte[] bs = str.getBytes();
            return new String(bs, newCharset); // 用新的字符编码生成字符串
        }
        return null;
    }
}
