package com.androidex.apps.home.utils;

import android.content.Context;
import android.content.DialogInterface;

import com.androidex.apps.home.view.CustomDialog;

/**
 * Created by cts on 16/11/28.
 * 类似web提示框
 */

public class ShowDialog {
    public static  void showDalig(final Context context, String message , String title, boolean flag){
        CustomDialog.Builder builder = new CustomDialog.Builder(context);
        builder.setMessage(message);
        builder.setTitle(title);
        if(flag){
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    showDalig(context,"正在充值，请稍后...","",false);
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        builder.create().show();

    }
}
