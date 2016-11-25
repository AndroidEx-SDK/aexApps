package com.androidex.apps.home.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.androidex.apps.home.R;


public class OtherCard extends Fragment implements View.OnClickListener{
    public static final String TAG = "OtherCard";

    private View mView = null;

    private Button finish ;

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
        finish = (Button)mView.findViewById(R.id.bt_finish);
        finish.setOnClickListener(this);
        // Inflate the layout for this fragment

        return mView;
    }



    public static final String action = "com.androidex.othercard.finish";
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case  R.id.bt_finish :
            {
                Intent intent = new Intent();
                intent.setAction(action);
                getActivity().sendBroadcast(intent);
            }
            default:
                return;
        }
    }

    public static final String action_back = "com.androidex.othercard.back";

}
