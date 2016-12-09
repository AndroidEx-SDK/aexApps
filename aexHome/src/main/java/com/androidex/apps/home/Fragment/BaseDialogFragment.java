package com.androidex.apps.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by cts on 16/12/6.
 */
public class BaseDialogFragment extends DialogFragment {
    public boolean isCancelable = false;
    public float widthPerHeight = 0.75f;
    public int padding = 44;
    private static BaseDialogFragment baseDialogFragment;

    public BaseDialogFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setCancelable(isCancelable);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public BaseDialogFragment setIsCancelable(boolean isShow) {
        this.isCancelable = isShow;
        return this;
    }

    public BaseDialogFragment setPadding(int padding) {
        this.padding = padding;
        return this;
    }

    public BaseDialogFragment dissMissDialog(){
        if (baseDialogFragment.isVisible()){
            baseDialogFragment.dismiss();
        }
        return this;
    }
}
