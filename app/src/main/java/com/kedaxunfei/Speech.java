package com.kedaxunfei;

import android.content.Context;
import android.content.Intent;

import com.androidex.DoorLock;
import com.google.gson.Gson;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import java.util.ArrayList;

/**
 * Created by cts on 16/12/29.
 * 科大讯飞的语音听写的类
 */

public class Speech {
    /**
     * 初始化语音识别
     */
    public static void initSpeech(final Context context) {
        //1.创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(context, null);
        //2.设置accent、language等参数
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        //3.设置回调接口
        mDialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean isLast) {
                if (!isLast) {
                    //解析语音
                    String result = parseVoice(recognizerResult.getResultString());
                    if (result.contains("开门")&& !result.contains("不开门")){
                        int status = 2;
                        Intent ds_intent = new Intent();
                        ds_intent.setAction(DoorLock.DoorLockOpenDoor);
                        ds_intent.putExtra("index",0);
                        ds_intent.putExtra("status",status);
                        context.sendBroadcast(ds_intent);
                    }
                }
            }

            @Override
            public void onError(SpeechError speechError) {

            }
        });
        //4.显示dialog，接收语音输入
        mDialog.show();
    }

    /**
     * 解析语音json
     */
    private static  String parseVoice(String resultString) {
        Gson gson = new Gson();
        Speech.Voice voiceBean = gson.fromJson(resultString, Speech.Voice.class);

        StringBuffer sb = new StringBuffer();
        ArrayList<Speech.Voice.WSBean> ws = voiceBean.ws;
        for (Speech.Voice.WSBean wsBean : ws) {
            String word = wsBean.cw.get(0).w;
            sb.append(word);
        }
        return sb.toString();
    }

    /**
     * 语音对象封装
     */
    public class Voice {

        public ArrayList<WSBean> ws;

        public class WSBean {
            public ArrayList<CWBean> cw;
        }

        public class CWBean {
            public String w;
        }
    }
}
