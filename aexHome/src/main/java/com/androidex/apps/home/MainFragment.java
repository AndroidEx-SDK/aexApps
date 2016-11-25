package com.androidex.apps.home;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidex.apps.home.fragment.AboutLocalFragment;
import com.androidex.apps.home.fragment.NetWorkSettingFragment;
import com.androidex.apps.home.fragment.StartSettingFragment;
import com.androidex.apps.home.fragment.SystemSettingFragment;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by yangjun on 2016/11/7.
 */

public class MainFragment extends Fragment {
      private View view;
      private ViewPager viewPager;
      private static final String TAB_NAME_1 = "关于本机";
      private static final String TAB_NAME_2 = "系统设置";
      private static final String TAB_NAME_3 = "网络设置";
      private static final String TAB_NAME_4 = "启动设置";
      private static final Integer[] tabIcs = {R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher};
      private List<String> tabNames;
      private static List<Fragment> fragments;

      private AboutLocalFragment mAboutLocalFragment;
      private SystemSettingFragment mSystemSettingFragment;
      private NetWorkSettingFragment mNetWorkSettingFragment;
      private StartSettingFragment mStartSettingFragment;
      public TabLayout tabLayout;

      public MainFragment() {
      }

      @Override
      public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
      }

      @Nullable
      @Override
      public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            view = inflater.inflate(R.layout.fragment_main, null);
            initTablayoutAndViewPager();
            return view;
      }


      private void initTablayoutAndViewPager() {
            tabNames = new ArrayList<>();
            fragments = new ArrayList<>();
            tabNames.add(TAB_NAME_1);
            tabNames.add(TAB_NAME_2);
            tabNames.add(TAB_NAME_3);
            tabNames.add(TAB_NAME_4);

            mAboutLocalFragment = new AboutLocalFragment();
            mSystemSettingFragment = new SystemSettingFragment();
            mNetWorkSettingFragment = new NetWorkSettingFragment();
            mStartSettingFragment = new StartSettingFragment();
            fragments.add(mAboutLocalFragment);
            fragments.add(mSystemSettingFragment);
            fragments.add(mNetWorkSettingFragment);
            fragments.add(mStartSettingFragment);

            viewPager = (ViewPager) view.findViewById(R.id.viewpager);
            tabLayout = (TabLayout) view.findViewById(R.id.tablayout);
            // viewPager.setOffscreenPageLimit(1);//预加载的页数
            PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager());
            viewPager.setAdapter(pagerAdapter);
            tabLayout.setupWithViewPager(viewPager);
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                  tabLayout.getTabAt(i).setCustomView(addTab(getActivity(), i));
            }
            //tabLayout.getTabAt(0).getCustomView().setSelected(true);
      }

      public View addTab(Context context, int index) {
            View view = View.inflate(context, R.layout.fragment_main_tabitem, null);
            TextView textView = (TextView) view.findViewById(R.id.tv_tabitem);
            ImageView imageView = (ImageView) view.findViewById(R.id.iv_tabitem);
            //textView.setText(getResources().getStringArray(R.array.indexpage_text_tabs)[index]);
            textView.setText(tabNames.get(index));
            imageView.setImageResource(tabIcs[index]);
            return view;
      }

      @Override
      public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
      }

      @Nullable
      @Override
      public View getView() {
            return super.getView();
      }

      @Override
      public void onDestroyView() {
            super.onDestroyView();
      }

      @Override
      public void onDestroy() {
            super.onDestroy();
      }

      static class PagerAdapter extends FragmentPagerAdapter {

            public PagerAdapter(FragmentManager fm) {
                  super(fm);
            }

            @Override
            public Fragment getItem(int position) {
                  return fragments.get(position);
            }

            @Override
            public int getCount() {
                  return fragments == null ? 0 : fragments.size();
            }
      }
}
