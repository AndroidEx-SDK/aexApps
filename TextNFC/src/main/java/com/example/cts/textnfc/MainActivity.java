package com.example.cts.textnfc;

import android.app.LocalActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {
    private static final String TAG = "MainActivityxxxxxx";
    protected LocalActivityManager mLocalActivityManager;
    private aexddAndroidNfcReader androidNfcReader;
    public static final String ACTION_NFC_CARDINFO="com.example.cts.textnfc.cardinfo";
    private TextView mNFC_cardinfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mLocalActivityManager = new LocalActivityManager(this, true);
        Bundle states = savedInstanceState != null?savedInstanceState.getBundle("android:states"):null;
        this.mLocalActivityManager.dispatchCreate(states);
        setContentView(R.layout.activity_main);
        androidNfcReader = aexddAndroidNfcReader.getInstance(this);
        enableReaderMode();
        initView();
        Receive receive = new Receive();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NFC_CARDINFO);
        registerReceiver(receive, intentFilter);
    }

    public void initView(){
        mNFC_cardinfo = (TextView) findViewById(R.id.tv_hello);
    }
    @Override
    protected void onDestroy() {
        disableReaderMode();
        super.onDestroy();
    }

    /**
     * 此函数实现NfcAdapter.ReaderCallback接口，这里调用NFC Reader类的接口来实现该函数的功能。
     * 这里只是为了把这个调用映射到此Activity而已。
     *
     * @param tag
     */
    @Override
    public void onTagDiscovered(Tag tag) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if ((androidNfcReader != null) && (androidNfcReader instanceof NfcAdapter.ReaderCallback)) {
                NfcAdapter.ReaderCallback nfcReader = (NfcAdapter.ReaderCallback) androidNfcReader;
                nfcReader.onTagDiscovered(tag);
            }
        }
    }

    /**
     * 启用NFC读卡
     */
    public void enableReaderMode() {
        Log.e(TAG, "启用读卡模式");
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (this instanceof NfcAdapter.ReaderCallback) {
                    nfc.enableReaderMode(this, androidNfcReader, aexddAndroidNfcReader.READER_FLAGS, null);
                }
            }
        }
    }

    /**
     * 禁用NFC读卡
     */
    public void disableReaderMode() {
        Log.i(TAG, "禁用读卡模式");
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                nfc.disableReaderMode(this);
            }
        }
    }

    /**
     * 当NFC读卡器读到AID后调用此函数事件通知此Activity。
     *
     * @param account
     */
    public void onAccountReceived(final String account) {
        // This callback is run on a background thread, but updates to UI elements must be performed
        // on the UI thread.
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mAccountField.setText(account);
                Log.i(TAG, String.format("NFC:%s", account));
            }
        });
    }

    class Receive extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ACTION_NFC_CARDINFO:
                    mNFC_cardinfo.setText(intent.getStringExtra("cardinfo"));
                    break;
            }
        }
    }
}
