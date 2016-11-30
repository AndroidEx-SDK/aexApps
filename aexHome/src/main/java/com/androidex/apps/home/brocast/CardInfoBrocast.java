package com.androidex.apps.home.brocast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.androidex.apps.home.activity.TransactionActivity;
import com.androidex.devices.aexddAndroidNfcReader;

/**
 * Created by cts on 16/11/28.
 * 读取卡的信息
 */

public class CardInfoBrocast extends BroadcastReceiver{

    public CardInfoBrocast (){
        super();
    }

    public static  String cardInfo;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(aexddAndroidNfcReader.START_ACTION)){
            cardInfo = intent.getStringExtra("cardinfo");
            Intent i = new Intent(context, TransactionActivity.class);
            context.startActivity(i);
        }
    }

    public static String getCardInfo() {
        if (cardInfo!=null)
        return cardInfo;
        else return null;
    }
}
