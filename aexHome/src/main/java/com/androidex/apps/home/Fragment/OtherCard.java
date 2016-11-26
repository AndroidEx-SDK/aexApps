package com.androidex.apps.home.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.androidex.apps.home.R;
import com.androidex.apps.home.view.CustomDialog;


public class OtherCard extends Fragment implements View.OnClickListener{
    public static final String TAG = "OtherCard";

    private View mView = null;
    private EditText et_input ;
    private double money ;
    private TextView tv_sure;   //确认
    private TextView tv_back; //退出
    private TextView tv_before; //上一步

    public OtherCard() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView==null){
            mView = inflater.inflate(R.layout.fragment_other_card,container,false);
        }
        initView();//初始化

        // Inflate the layout for this fragment

        return mView;
    }

    private void initView(){
        et_input = (EditText) mView.findViewById(R.id.et_input);
        tv_sure = (TextView)mView.findViewById(R.id.tv_sure);
        tv_back = (TextView) mView.findViewById(R.id.tv_back);
        tv_before = (TextView)mView.findViewById(R.id.tv_before);

        tv_before.setOnClickListener(this);
        tv_sure.setOnClickListener(this);
        tv_back.setOnClickListener(this);

        String result = et_input.getText().toString().trim();
        if(!result.isEmpty()){
            money = Double.parseDouble(result);
        }
    }

    public static final String action = "com.androidex.othercard.finish";
    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()){

            case R.id.tv_sure:
            {
                //弹出对话框提示信息
                showDalig("确认为xxxx卡充值"+money+"金额？","提示",true);
            }
            break;
            case R.id.tv_back ://退出
            {
                intent.setAction(FrontBankcard.action_fb_back);
                getActivity().sendBroadcast(intent);
            }
            break;
            case R.id.tv_before :
            {
                intent.setAction(action_back);
                getActivity().sendBroadcast(intent);
            }
            break;
            default:
                break;
        }
    }

    private void showDalig(String message ,String title,boolean flag){
        CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
        builder.setMessage(message);
        builder.setTitle(title);
        if(flag){
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    showDalig("正在充值，请稍后...","",false);
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        builder.create().show();

    }

    public static final String action_back = "com.androidex.othercard.back";

}
