package com.androidex.apps.home.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.androidex.apps.home.FullscreenActivity;
import com.androidex.apps.home.R;

import org.json.JSONException;
import org.json.JSONObject;

import static com.androidex.apps.home.FullscreenActivity.action_finish;

/**
 * 插入其他银行卡
 */
public class OtherCardFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = "OtherCardFragment";

    private View mView = null;
    private EditText et_input;
    private double money;
    private TextView tv_back;       //上一步
    private TextView tv_ok;         //完成
    private TextView tv_cardinfo;  //显示卡的信息

    public OtherCardFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_other_card, container, false);
        }
        initView();//初始化

        // Inflate the layout for this fragment

        return mView;
    }

    private void initView() {
        et_input = (EditText) mView.findViewById(R.id.et_input);
        tv_back = (TextView) mView.findViewById(R.id.tv_back);
        tv_ok = (TextView) mView.findViewById(R.id.tv_ok);
        tv_cardinfo = (TextView) mView.findViewById(R.id.tv_cardinfo);

        tv_ok.setOnClickListener(this);
        tv_back.setOnClickListener(this);

        String r = FullscreenActivity.cardInfo;
        if (r != null) {
            getString(r);
            tv_cardinfo.setText(cardInfoStr);//显示卡的信息
        }
        String result = et_input.getText().toString().trim();
        if (!result.isEmpty()) {
            money = Double.parseDouble(result);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.tv_back://上一步
                intent.putExtra("page", 1);
                intent.setAction(FullscreenActivity.action_back);
                getActivity().sendBroadcast(intent);
                break;
            case R.id.tv_ok:
                intent.setAction(action_finish);
                getActivity().sendBroadcast(intent);
                break;
            default:
                break;
        }
    }

    private StringBuilder cardInfoStr = new StringBuilder();

    private void getString(String str) {

        JSONObject jsb = null;
        try {
            jsb = new JSONObject(str);
            String id = jsb.optString("id");
            JSONObject jsb_b = jsb.getJSONObject("balance");
            String balance = jsb_b.optString("balance");
            cardInfoStr.append("类型:" + id + "\n" + "余额:" + balance + "\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
