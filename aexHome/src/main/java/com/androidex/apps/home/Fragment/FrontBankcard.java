package com.androidex.apps.home.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.androidex.apps.home.R;


public class FrontBankcard extends Fragment implements View.OnClickListener{
    private static final String TAG = "FrontBankcard";
    private View mView = null;
    private Button next;
    public FrontBankcard() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(mView==null){
            mView = inflater.inflate(R.layout.fragment_front_bankcard, container, false);
        }
        next = (Button) mView.findViewById(R.id.btn_next);
        next.setOnClickListener(this);

        // Inflate the layout for this fragment
        return mView;
    }


    public static final String action_fb_back= "com.androidex.frontbankcard_back";
    public static final String str = "com.androidex.frontbankcard";
    @Override
    public void onClick(View v) {
       switch (v.getId()){
           case R.id.btn_next:
           {
                Intent intent = new Intent();
                intent.setAction(str);
                getActivity().sendBroadcast(intent);
           }

           default:
               return;
       }
    }
}
