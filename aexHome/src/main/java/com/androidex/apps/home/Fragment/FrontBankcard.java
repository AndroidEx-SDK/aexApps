package com.androidex.apps.home.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidex.apps.home.R;


public class FrontBankcard extends Fragment implements View.OnClickListener{
    private static final String TAG = "FrontBankcard";
    private View mView = null;
    private TextView next;
    private TextView back;//退出

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
        next = (TextView) mView.findViewById(R.id.tv_next);
        next.setOnClickListener(this);

        back = (TextView) mView.findViewById(R.id.tv_finish) ;
        back.setOnClickListener(this);

        initSpinner(R.id.spinner);
        // Inflate the layout for this fragment
        return mView;
    }

    /**
     * 设置spinner
     * @param spinner
     */
    private void initSpinner(int spinner) {
        final Spinner mSpinner = (Spinner) mView .findViewById(spinner);
        mSpinner.setBackground(getResources().getDrawable(R.drawable.wp_spinner_bg));

        String [] mItems = getResources().getStringArray(R.array.select_type);

        ArrayAdapter <String> mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,mItems);

        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        mSpinner.setAdapter(mAdapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    public static final String action_fb_back= "com.androidex.frontbankcard_back";
    public static final String str = "com.androidex.frontbankcard";
    @Override
    public void onClick(View v) {
       switch (v.getId()){
           case R.id.tv_next:
           {
                Intent intent = new Intent();
                intent.setAction(str);
                getActivity().sendBroadcast(intent);
           }
           break;
           case R.id.tv_finish:
           {
               Log.d(TAG,"send finish broadcast");
               Intent intent = new Intent();
               intent.setAction(action_fb_back);
               getActivity().sendBroadcast(intent);
           }
            break;
           default:
                break;
       }
    }
}
