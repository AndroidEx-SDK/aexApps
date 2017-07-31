package com.androidex.apps.home.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidex.apps.home.FullscreenActivity;
import com.androidex.apps.home.R;

/**
 * 先插入银行卡的Fragment
 */
public class FrontBankcardFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "FrontBankcardFragment";
    private View mView = null;
    private TextView next;
    private TextView back;//退出

    public FrontBankcardFragment() {
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
            mView = inflater.inflate(R.layout.fragment_front_bankcard, container, false);
        }
        next = (TextView) mView.findViewById(R.id.tv_next);
        back = (TextView) mView.findViewById(R.id.tv_exit);
        next.setOnClickListener(this);
        back.setOnClickListener(this);

        initSpinner(R.id.spinner);
        // Inflate the layout for this fragment
        return mView;
    }

    /**
     * 设置spinner
     *
     * @param spinner
     */
    private void initSpinner(int spinner) {
        final Spinner mSpinner = (Spinner) mView.findViewById(spinner);
        mSpinner.setBackground(getResources().getDrawable(R.drawable.wp_spinner_bg));

        String[] mItems = getResources().getStringArray(R.array.select_type);

        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mItems);

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


    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.tv_next: {
                intent.putExtra("page",1);
                intent.setAction(FullscreenActivity.action_next);
                getActivity().sendBroadcast(intent);
            }
            break;
            case R.id.tv_exit: {

                intent.setAction(FullscreenActivity.action_cancle);
                getActivity().sendBroadcast(intent);
            }
            break;
            default:
                break;
        }
    }
}
