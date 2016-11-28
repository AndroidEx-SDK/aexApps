package com.androidex.apps.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidex.common.LogFragment;
import com.androidex.logger.Log;
import com.androidex.logger.LogWrapper;
import com.androidex.logger.MessageOnlyLogFilter;

/**
 * Created by yangjun on 2016/11/7.
 */

public class aexLogFragment extends LogFragment {

      private View logView;

      public aexLogFragment() {
      }

      @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            return super.getLogView();
      }

      /**
       * Create a chain of targets that will receive log data
       */
      public void initializeLogging() {
            // On screen logging via a fragment with a TextView.
            LogWrapper logWrapper = (LogWrapper) Log.getLogNode();
            MessageOnlyLogFilter msgFilter = (MessageOnlyLogFilter) logWrapper.getNext();
            msgFilter.setNext(this.getLogView());
      }

}
