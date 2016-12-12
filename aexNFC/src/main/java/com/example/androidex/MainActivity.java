package com.example.androidex;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

/**
 *
 */
public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback{
    private static final String TAG= "MainActivity";
    public static int READER_FLAGS =
            NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK ;

    //public LoyaltyCardReader mLoyaltyCardReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableReaderMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disenableReaderMode();
    }

    private void enableReaderMode() {
        Log.i("", "启用读卡模式");
        if(Build.VERSION.SDK_INT >= 19)
        {
            Activity activity = this;
            NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
            if (nfc != null) {
                //nfc.enableReaderMode(activity, , READER_FLAGS, null);
            }
        }
    }

    private void disenableReaderMode(){
        Log.i("","禁用读卡模式");
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter!=null){
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
                nfcAdapter.disableReaderMode(this);
            }
        }
    }

    @Override
    public void onTagDiscovered(Tag tag) {

    }
}
