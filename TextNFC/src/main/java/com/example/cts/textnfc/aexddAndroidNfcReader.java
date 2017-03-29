package com.example.cts.textnfc;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.util.Log;

import com.androidex.devices.aexddNfcReader;
import com.androidex.devices.tech.FelicaReader;
import com.androidex.devices.tech.pboc.StandardPboc;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yangjun on 2016/11/6.
 */

@TargetApi(Build.VERSION_CODES.KITKAT)
public class aexddAndroidNfcReader extends aexddNfcReader implements NfcAdapter.ReaderCallback {
    private static final String TAG = "AndroidNfc";
    public static int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    protected static final String SAMPLE_LOYALTY_CARD_AID = "F222222222";
    protected static final String SELECT_APDU_HEADER = "00A40400";
    protected static final byte[] SELECT_OK_SW = {(byte) 0x90, (byte) 0x00};
    public static final String START_ACTION = "com.androidex.apps.home.transationactivity.isdep";
    public static final String NFCF = "com.androidex.apps.home.transationactivity.nfcf";
    private static aexddAndroidNfcReader mAexddAndroidNfcReader;
    private Context mContext;
    public int times_succeed = 0;//读卡有数据次数
    public int times_fail = 0;//读有数据卡失败次数
    public int times = 0;//刷卡数总计
    public int times_write = 0;//写卡次数
    private Intent intent;
    private Timer timer;

    public static aexddAndroidNfcReader getInstance(Context ctx) {
        if (mAexddAndroidNfcReader == null) {
            mAexddAndroidNfcReader = new aexddAndroidNfcReader(ctx);
        }
        return mAexddAndroidNfcReader;
    }

    private aexddAndroidNfcReader(Context ctx) {
        super(ctx);
        mContext = ctx;
        if ((ctx != null) && (ctx instanceof AccountCallback)) {
            AccountCallback listener = (AccountCallback) ctx;
            mAccountCallback = new WeakReference<AccountCallback>(listener);
        }
    }

    public aexddAndroidNfcReader(Context ctx, JSONObject args) {
        super(ctx, args);
        mContext = ctx;
        if ((ctx != null) && (ctx instanceof AccountCallback)) {
            AccountCallback listener = (AccountCallback) ctx;
            mAccountCallback = new WeakReference<AccountCallback>(listener);
        }
    }
    
    // Weak reference to prevent retain loop. mAccountCallback is responsible for exiting
    // foreground mode before it becomes invalid (e.g. during onPause() or onStop()).
    private WeakReference<AccountCallback> mAccountCallback;

    public interface AccountCallback {
        public void onAccountReceived(String account);
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        final byte[] cmdCardInfo80 = {(byte) 0x00, (byte) 0xb0, (byte) 0x9f, (byte) 0x05, (byte) 0x80};
        final byte[] cmdCardInfo = {(byte) 0x04, (byte) 0xd6, (byte) 0x81, (byte) 0x00, (byte) 0x31, (byte) 0x49};//0x04,0xd6,0x81,0x00,0x31
        Log.i(TAG, "发现新卡");

        Intent intent1 = new Intent();
        intent1.setAction(MainActivity.ACTION_NFC_TIMES);

        intent1.putExtra("times", times);
        times++;
        mContext.sendBroadcast(intent1);

        final IsoDep isodep = IsoDep.get(tag);
        //79返回数据
//        String  str="4ABE19515A7972453095D57B685F1026A10689FE7321FF169A0CDE96D368F42E3181EFE3CEECE3E8B92B8E7B4F080050D0374CF52277BF8A155AAA4B839A9A452EB0F101435257E000BD5A2F1FF32018915E319417BA5595C4D87461BBCB1193B4F198052B95249CF02F790C074AB5C62A36216179A196A3B09000";
//        //80应该返回的数据
//        String str1="4ABE19515A7972453095D57B685F1026A10689FE7321FF169A0CDE96D368F42E3181EFE3CEECE3E8B92B8E7B4F080050D0374CF52277BF8A155AAA4B839A9A452EB0F101435257E000BD5A2F1FF32018915E319417BA5595C4D87461BBCB1193B4F198052B95249CF02F790C074AB5C62A36216179A196A3B00550E91BC9F6BA9000";
//        int len=str.length();
//        int len1=str1.length();

//        Log.d(TAG, "79返回数据的长度xxxx:"+len);
//        Log.d(TAG, "80应该返回数据的长度xxxx:"+len1);

//        // 1.
        // 初始化定时器
        if (timer == null) {
            timer = new Timer();
            if (intent == null) {
                intent = new Intent();
                intent.setAction(MainActivity.ACTION_NFC_WRITEDATA_TIMES);
            }
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    otherSend(cmdCardInfo, isodep);//另外一种发送命令的方式
                    times_write++;
                    intent.putExtra("times", times_write);
                    mContext.sendBroadcast(intent);
                    Log.e("writetimesxxxxxx:", times_write + "");
//                Toast.makeText(mContext,"writetimes"+times_write,Toast.LENGTH_LONG).show();
                }
            }, 1000, 1000);
        }

//        //2.发送80字节
//        send80(isodep, cmdCardInfo80);

//        //3.发送79字节长度的命令
//        final byte[] cmdCardInfo79 = {(byte) 0x00, (byte) 0xb0, (byte) 0x9f, (byte) 0x05, (byte) 0x79};
//        send79(isodep, cmdCardInfo79);

        //4.发送String格式的命令
        // sendStringCmd(isodep,"00b09f0580");

        if (isodep != null) {
            JSONObject r = null;
            try {
                r = StandardPboc.readCard(isodep);
                try {
                    Log.d(TAG, r.toString(4));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Intent intent = new Intent();
                intent.setAction(MainActivity.ACTION_NFC_CARDINFO);
                if (r != null) {
                    intent.putExtra("cardinfo", r.toString());
                    times_succeed++;
                } else {
                    intent.putExtra("cardinfo", "获取卡信息失败");
                    times_fail++;
                }
                intent.putExtra("times", times_succeed);
                intent.putExtra("times_fail", times_fail);
                mContext.sendBroadcast(intent);
            }
        } else {
            Log.d(TAG, "卡内无信息");
        }

        final NfcF nfcf = NfcF.get(tag);
        if (nfcf != null) {
            JSONObject r = null;
            try {
                r = FelicaReader.readCard(nfcf);
                Intent intent = new Intent();
                intent.setAction(NFCF);
                mContext.sendBroadcast(intent);
                try {
                    Log.d(TAG, r.toString(4));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Intent intent = new Intent();
                intent.setAction(MainActivity.ACTION_NFC_CARDINFO);
                if (r != null) {
                    intent.putExtra("cardinfo", r.toString());
                    times_succeed++;
                } else {
                    intent.putExtra("cardinfo", "获取卡信息失败");
                    times_fail++;
                }
                intent.putExtra("times", times_succeed);
                intent.putExtra("times_fail", times_fail);
                mContext.sendBroadcast(intent);
            }
        }
    }

    private void otherSend(byte[] cmdCardInfo80, IsoDep isodep) {
        String s = null;
        Iso7816.StdTag stdTag = new Iso7816.StdTag(isodep);
        try {
            byte[] tagTransceive = stdTag.transceive(cmdCardInfo80);
            s = ByteArrayToHexString(tagTransceive);
            Log.e("s:xxxx", s);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Intent intent = new Intent();
            intent.setAction(MainActivity.ACTION_NFC_WRITEDATA);
            if (s != null) {
                intent.putExtra("writedata", s);
            } else {
                intent.putExtra("writedata", "获取卡信息失败");
            }
            mContext.sendBroadcast(intent);
        }
    }

    private void sendStringCmd(IsoDep isodep, String cmd) {
        byte[] bytes = HexStringToByteArray(cmd);
        if (isodep != null) {
            Log.e("bytes:xxxx", ByteArrayToHexString(bytes));
            try {
                if (!isodep.isConnected()) {
                    isodep.connect();
                    isodep.setTimeout(5000);
                }
                byte[] transceive = isodep.transceive(bytes);
                String s = ByteArrayToHexString(transceive);
                Log.e("s:xxxx", s);
                if (isodep.isConnected()) {
                    isodep.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void send79(IsoDep isodep, byte[] cmdCardInfo79) {
        String s = null;
        if (isodep != null) {
            Iso7816.StdTag stdTag = new Iso7816.StdTag(isodep);
            try {
                Log.e("cmdCardInfo79:", "xxxx:" + ByteArrayToHexString(cmdCardInfo79));
                if (!isodep.isConnected()) {
                    isodep.connect();
                    isodep.setTimeout(5000);
                }
                byte[] tagTransceive = stdTag.transceive(cmdCardInfo79);
                s = ByteArrayToHexString(tagTransceive);
                Log.e("aexddAndroidNfcRead:", "xxxx:" + s);
                if (isodep.isConnected()) {
                    isodep.close();
                }
            } catch (IOException e) {
                Log.e("aexddAndroidNfcRead:", "xxxx:" + "异常1" + e.toString());
                e.printStackTrace();
            } finally {
                Intent intent = new Intent();
                intent.setAction(MainActivity.ACTION_NFC_CARDINFO);
                if (s != null) {
                    intent.putExtra("cardinfo", s);
                } else {
                    intent.putExtra("cardinfo", "获取卡信息失败");
                }
                mContext.sendBroadcast(intent);
            }

        }
    }

    private void send80(IsoDep isodep, byte[] cmdCardInfo80) {
        String s = null;
        if (isodep != null) {
            try {
                Log.e("cmdCardInfo80:", "xxxx:" + ByteArrayToHexString(cmdCardInfo80));
                if (!isodep.isConnected()) {
                    isodep.connect();
                    isodep.setTimeout(5000);
                }
                byte[] transceive = isodep.transceive(cmdCardInfo80);
                Log.e("transceive:xxxx", transceive.toString());
                s = ByteArrayToHexString(transceive);
                Log.e("s:xxxx", s);

                if (isodep.isConnected()) {
                    isodep.close();
                }
            } catch (IOException e) {
                Log.e("aexddAndroidNfcRead:", "xxxx80异常:" + e.toString());
                e.printStackTrace();
            } finally {
                Intent intent = new Intent();
                intent.setAction(MainActivity.ACTION_NFC_CARDINFO);
                if (s != null) {
                    intent.putExtra("cardinfo", s);
                } else {
                    intent.putExtra("cardinfo", "获取卡信息失败");
                }
                mContext.sendBroadcast(intent);
            }
        }
    }

    public static byte[] BuildSelectApdu(String aid) {
        // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
        return HexStringToByteArray(SELECT_APDU_HEADER + String.format("%02X", aid.length() / 2) + aid);
    }

    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] HexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}
