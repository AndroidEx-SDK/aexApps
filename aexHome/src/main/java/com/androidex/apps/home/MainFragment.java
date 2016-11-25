package com.androidex.apps.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by yangjun on 2016/11/7.
 */

public class MainFragment extends Fragment {
      private View view;


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

}
