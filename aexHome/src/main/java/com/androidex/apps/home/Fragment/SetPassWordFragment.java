package com.androidex.apps.home.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidex.apps.home.FullscreenActivity;
import com.androidex.apps.home.R;

/**
 * 设置密码的FragMent
 * <p>
 * Created by liyp on 16/12/1.
 */

public class SetPassWordFragment extends DialogFragment implements View.OnClickListener {
    private static final String TAG = "setpassword";
    private View rootView;
    private static SetPassWordFragment setPassWordFragment;
    private FullscreenActivity activity;
    public boolean isCancelable = false;
    private EditText et_password;
    private TextView tv_title;
    private String pass;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.set_password, container, false);
        setCancelable(isCancelable);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity = (FullscreenActivity) getActivity();
        initView();
        return rootView;
    }

    public void initView() {
        et_password = (EditText) rootView.findViewById(R.id.et_password);
        tv_title = (TextView) rootView.findViewById(R.id.tv_title);
        Button cancle = (Button) rootView.findViewById(R.id.btn_cancle);
        Button ok = (Button) rootView.findViewById(R.id.btn_ok);
        cancle.setOnClickListener(this);
        ok.setOnClickListener(this);
        pass = activity.hwservice.get_pass();
        if (pass.equals("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")) {
            Toast.makeText(getContext(), "请设置密码", Toast.LENGTH_LONG).show();
            android.util.Log.e("原始密码：", pass);
            tv_title.setText("请设置密码");
        } else {
            Toast.makeText(getContext(), "请输入密码", Toast.LENGTH_LONG).show();
            tv_title.setText("请输入密码");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancle:
                dissMissDialog();
                Intent intent = new Intent(FullscreenActivity.ActionControlBar);
                intent.putExtra("flag", "hide");
                intent.putExtra("bar", true);
                getContext().sendBroadcast(intent);
                break;
            case R.id.btn_ok:
                String newPassWork = et_password.getText().toString().trim();
                /********密码********/
                String pass1 = activity.hwservice.get_pass();
                Toast.makeText(getContext(), "原始密码: " + pass1, Toast.LENGTH_LONG).show();
                android.util.Log.e("原始密码: ", pass1);

                activity.hwservice.set_pass(newPassWork);
                String pass2 = activity.hwservice.get_pass();
                if (pass2.equals("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")) {
                    Toast.makeText(getContext(), "密码修改失败: " + pass2, Toast.LENGTH_LONG).show();
                    android.util.Log.e("密码修改失败: ", pass2);

                } else {
                    Toast.makeText(getContext(), "新的密码：" + pass2, Toast.LENGTH_LONG).show();
                    android.util.Log.e("新的密码：", pass2);
                }
                break;

            case R.id.tv_skip:
                if (activity.hwservice.get_pass().equals("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")){
                    android.util.Log.e("跳过密码设置", "");
                    Intent intent_show= new Intent(FullscreenActivity.ActionControlBar);
                    intent_show.putExtra("flag", "show");
                    intent_show.putExtra("bar", true);
                    getContext().sendBroadcast(intent_show);
                    dissMissDialog();
                }else {
                    Toast.makeText(getContext(), "请输入密码", Toast.LENGTH_LONG).show();
                    Intent intent_hide= new Intent(FullscreenActivity.ActionControlBar);
                    intent_hide.putExtra("flag", "hide");
                    intent_hide.putExtra("bar", true);
                    getContext().sendBroadcast(intent_hide);
                }
                break;
        }
    }

    public static SetPassWordFragment instance() {
        if (setPassWordFragment == null) {
            setPassWordFragment = new SetPassWordFragment();
        }
        return setPassWordFragment;
    }


    public SetPassWordFragment dissMissDialog() {
        if (setPassWordFragment.isVisible()) {
            setPassWordFragment.dismiss();
        }
        return this;
    }
}
