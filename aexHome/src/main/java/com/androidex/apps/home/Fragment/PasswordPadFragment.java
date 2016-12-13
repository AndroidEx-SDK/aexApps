package com.androidex.apps.home.fragment;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.androidex.apps.home.R;
import com.androidex.devices.aexddZTC70;
import com.androidex.logger.Log;

/**
 * A simple {@link Fragment} subclass.
 */
public class PasswordPadFragment extends DialogFragment implements View.OnClickListener{

    public  static    aexddZTC70 mAexddZtc70;
    public EditText et_input;
    public Button bt_close;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_password_pad, container, false);
        et_input = (EditText)mView.findViewById(R.id.et_input);
        int i = mAexddZtc70.ReciveDataLoop();
        /*mAexddZtc70.setCallBack(new aexddZTC70.CallBack() {
            @Override
            public void mCallBack(int key) {
                Toast.makeText(getActivity(),key+"",Toast.LENGTH_SHORT).show();
                et_input.setText(key+"");
            }
        });*/

        Log.i("按键：", i + "");
        Toast.makeText(getActivity(),i+"",Toast.LENGTH_SHORT).show();
        return mView;
    }

    private static PasswordPadFragment mPwPf ;
    public   PasswordPadFragment (){

    }

    public static PasswordPadFragment instance (aexddZTC70 aex){
        mAexddZtc70 = aex;
        if(mPwPf==null){
            mPwPf = new PasswordPadFragment();
        }
        return  mPwPf;
    }

    public PasswordPadFragment dismissDialog(){
        if(mPwPf.isVisible()){
            mPwPf.dismissDialog();
        }
        return this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_close:
            {
                mAexddZtc70.Close();
                dismissDialog();
            }
        }
    }
}
