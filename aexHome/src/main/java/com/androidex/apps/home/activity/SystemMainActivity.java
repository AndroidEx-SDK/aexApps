package com.androidex.apps.home.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidex.apps.home.R;
import com.androidex.apps.home.fragment.AboutLocalFragment;
import com.androidex.apps.home.fragment.NetWorkSettingFragment;
import com.androidex.apps.home.fragment.StartSettingFragment;
import com.androidex.apps.home.fragment.SystemSettingFragment;
import com.androidex.common.AndroidExActivityBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyp on 16/11/25.
 */

public class SystemMainActivity extends AndroidExActivityBase {
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
      private ViewPager viewPager;

      @Override
      protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_system_main);
            initTablayoutAndViewPager();
            // viewPager.setOffscreenPageLimit(1);//预加载的页数
            PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(pagerAdapter);
            tabLayout.setupWithViewPager(viewPager);
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                  tabLayout.getTabAt(i).setCustomView(addTab(this, i));
            }
            //tabLayout.getTabAt(0).getCustomView().setSelected(true);
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

            viewPager = (ViewPager) findViewById(R.id.viewpager);
            tabLayout = (TabLayout) findViewById(R.id.tablayout);
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
