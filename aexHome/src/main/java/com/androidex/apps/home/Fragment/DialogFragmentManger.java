package com.androidex.apps.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import com.androidex.apps.home.R;
import com.androidex.apps.home.utils.DisplayUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyp on 16/12/1.
 */

public class DialogFragmentManger extends DialogFragment{

    List<Fragment> list = new ArrayList();
    private DisplayMetrics displayMetrics = new DisplayMetrics();
    /**
     * 弹窗的宽高比
     */
    private float widthPerHeight = 0.75f;
    private int padding = 44;
    private RelativeLayout parentLayout;

   public DialogFragmentManger(List<Fragment> list){
       this.list=list;
   }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_manger, container);
        parentLayout = (RelativeLayout) view.findViewById(R.id.parentLayout);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        //setRootContainerHeight();//设置dialog的宽高比
        return view;
    }



    public DialogFragmentManger setWidthPerHeight(float widthPerHeight) {
        this.widthPerHeight = widthPerHeight;

        return this;
    }

    public DialogFragmentManger setPadding(int padding) {
        this.padding = padding;

        return this;
    }

    private void setRootContainerHeight() {

        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthPixels = displayMetrics.widthPixels;
        int totalPadding = DisplayUtil.dip2px(getActivity(), padding * 2);
        int width = widthPixels - totalPadding;
        final int height = (int) (width / widthPerHeight);
        ViewGroup.LayoutParams params = parentLayout.getLayoutParams();
        params.height = height;
    }

    class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }

}
