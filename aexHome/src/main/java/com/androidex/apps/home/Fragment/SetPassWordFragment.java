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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.set_password, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        initView();
        return rootView;
    }

    public void initView() {
        EditText password = (EditText) rootView.findViewById(R.id.et_password);
        Button cancle = (Button) rootView.findViewById(R.id.btn_cancle);
        Button ok = (Button) rootView.findViewById(R.id.btn_ok);
        cancle.setOnClickListener(this);
        ok.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.btn_cancle:

                break;
            case R.id.btn_ok:
              
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
