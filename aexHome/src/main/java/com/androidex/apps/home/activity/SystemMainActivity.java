package com.androidex.apps.home.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
            initActionBar(R.id.toolbar);
            // viewPager.setOffscreenPageLimit(1);//预加载的页数
            PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(pagerAdapter);
            tabLayout.setupWithViewPager(viewPager);
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                  tabLayout.getTabAt(i).setCustomView(addTab(this, i));
            }
            //tabLayout.getTabAt(0).getCustomView().setSelected(true);
            setFullScreen(true);
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
            textView.setTextColor(Color.WHITE);
            imageView.setImageResource(tabIcs[index]);
            return view;
      }

      @Override
      public void initActionBar(int resId) {
            Toolbar toolbar = (Toolbar) findViewById(resId);
            super.initActionBar(resId);
            if (toolbar != null) {
                  toolbar.setLogo(com.androidex.aexapplibs.R.drawable.androidex);      //设置logo图片
                  toolbar.setNavigationIcon(com.androidex.aexapplibs.R.drawable.back);     //设置导航按钮
                  toolbar.setTitle(R.string.app_name);          //设置标题
                  toolbar.setSubtitle(R.string.app_subtitle);   //设置子标题
                  setSupportActionBar(toolbar);
            }
      }

      @Override
      public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.main, menu);
            MenuItem m4 = menu.add(R.string.str_quit);
            m4.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                  public boolean onMenuItemClick(MenuItem item) {
                        showExitDialog();
                        return true;
                  }
            });
            return super.onCreateOptionsMenu(menu);
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
