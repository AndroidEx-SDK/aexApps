package com.androidex.apps.home.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.androidex.apps.home.R;
import com.androidex.apps.home.brocast.CardInfoBrocast;

import org.json.JSONException;
import org.json.JSONObject;

import static com.androidex.apps.home.fragment.FrontBankcard.action_fb_back;


public class OtherCard extends Fragment implements View.OnClickListener{
    public static final String TAG = "OtherCard";

    private View mView = null;
    private EditText et_input ;
    private double money ;
    private TextView tv_back; //退出
    private TextView tv_before; //上一步
    private TextView tv_cardinfo ; //显示卡的信息

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
        tv_back = (TextView) mView.findViewById(R.id.tv_back);
        tv_before = (TextView)mView.findViewById(R.id.tv_before);
        tv_cardinfo = (TextView)mView.findViewById(R.id.tv_cardinfo);

        tv_before.setOnClickListener(this);
        tv_back.setOnClickListener(this);

        CardInfoBrocast cifb = new CardInfoBrocast();
        String r =cifb.cardInfo;
        if (!r.isEmpty()){
            getString(r);
            tv_cardinfo.setText(cardInfoStr);//显示卡的信息
        }

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


            case R.id.tv_back ://退出or下一步
            {
                intent.setAction(FrontBankcard.str);
                getActivity().sendBroadcast(intent);
            }
            break;
            case R.id.tv_before :
            {
                intent.setAction(action_fb_back);
                getActivity().sendBroadcast(intent);
            }
            break;
            default:
                break;
        }
    }

    private StringBuilder cardInfoStr = new StringBuilder();
    private void getString(String str){

        JSONObject jsb = null;
        try {
            jsb = new JSONObject(str);
            String id = jsb.optString("id");
            String balance = jsb.optString("balance");
            cardInfoStr.append("类型:"+id+"\n"+"余额:"+balance+"\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static final String action_back = "com.androidex.othercard.back";

}
