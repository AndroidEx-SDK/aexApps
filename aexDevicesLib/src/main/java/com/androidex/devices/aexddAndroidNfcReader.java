package com.androidex.devices;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcF;
import android.os.Build;

import com.androidex.devices.tech.FelicaReader;
import com.androidex.devices.tech.pboc.StandardPboc;
import com.androidex.logger.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by yangjun on 2016/11/6.
 */

@TargetApi(Build.VERSION_CODES.KITKAT)
public class aexddAndroidNfcReader extends aexddNfcReader implements NfcAdapter.ReaderCallback {
    private static final String TAG = "AndroidNfc";
    // Recommend NfcAdapter flags for reading from other Android devices. Indicates that this
    // activity is interested in NFC-A devices (including other Android devices), and that the
    // system should not check for the presence of NDEF-formatted data (e.g. Android Beam).
    public static int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    // AID for our loyalty card service.
    protected static final String SAMPLE_LOYALTY_CARD_AID = "F222222222";
    // ISO-DEP command HEADER for selecting an AID.
    // Format: [Class | Instruction | Parameter 1 | Parameter 2]
    protected static final String SELECT_APDU_HEADER = "00A40400";
    // "OK" status word sent in response to SELECT AID command (0x9000)
    protected static final byte[] SELECT_OK_SW = {(byte) 0x90, (byte) 0x00};
    public static final String START_ACTION = "com.androidex.apps.home.transationactivity.isdep";
    public static final String NFCF = "com.androidex.apps.home.transationactivity.nfcf";
    private static aexddAndroidNfcReader mAexddAndroidNfcReader;

    public static aexddAndroidNfcReader getInstance(Context ctx) {
        if (mAexddAndroidNfcReader == null) {
            mAexddAndroidNfcReader = new aexddAndroidNfcReader(ctx);
        }
        return mAexddAndroidNfcReader;
    }

    private aexddAndroidNfcReader(Context ctx) {
        super(ctx);
        if ((ctx != null) && (ctx instanceof AccountCallback)) {
            AccountCallback listener = (AccountCallback) ctx;
            mAccountCallback = new WeakReference<AccountCallback>(listener);
        }
    }

    public aexddAndroidNfcReader(Context ctx, JSONObject args) {
        super(ctx, args);
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

    /**
     * Callback when a new tag is discovered by the system.
     * <p>
     * <p>Communication with the card should take place here.
     *
     * @param tag Discovered tag
     */
    @Override
    public void onTagDiscovered(Tag tag) {
        Log.i(TAG, "发现新卡");
        android.util.Log.d(TAG, "onTagDiscovered: "+tag.toString());
        //发送广播
        Intent intent = new Intent();
        intent.setAction(START_ACTION);
        //intent.putExtra("cardinfo",r.toString());
        mContext.sendBroadcast(intent);
        // Android's Host-based Card Emulation (HCE) feature implements the ISO-DEP (ISO 14443-4)
        // protocol.
        //
        // In order to communicate with a device using HCE, the discovered tag should be processed
        // using the IsoDep class.
        /*
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep != null) {
            try {
                // Connect to the remote NFC device
                isoDep.connect();
                // Build SELECT AID command for our loyalty card service.
                // This command tells the remote device which service we wish to communicate with.
                Log.i(TAG, "请求远端AID: " + SAMPLE_LOYALTY_CARD_AID);
                byte[] command = BuildSelectApdu(SAMPLE_LOYALTY_CARD_AID);
                // Send command to remote device
                Log.i(TAG, "发送: " + ByteArrayToHexString(command));
                byte[] result = isoDep.transceive(command);
                // If AID is successfully selected, 0x9000 is returned as the status word (last 2
                // bytes of the result) by convention. Everything before the status word is
                // optional payload, which is used here to hold the account number.
                int resultLength = result.length;
                byte[] statusWord = {result[resultLength-2], result[resultLength-1]};
                byte[] payload = Arrays.copyOf(result, resultLength-2);
                if (Arrays.equals(SELECT_OK_SW, statusWord)) {
                    // The remote NFC device will immediately respond with its stored account number
                    String accountNumber = new String(payload, "UTF-8");
                    Log.i(TAG, "接收: " + accountNumber);
                    // Inform CardReaderFragment of received account number
                    mAccountCallback.get().onAccountReceived(accountNumber);
                }
            } catch (IOException e) {
                Log.e(TAG, "与卡通讯发生错误: " + e.toString());
            }
        }
        */

        final IsoDep isodep = IsoDep.get(tag);
        if (isodep != null) {
            try {
                JSONObject r = StandardPboc.readCard(isodep);
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
            }
        } else {
            Log.d(TAG, "卡内无信息");
        }

        final NfcF nfcf = NfcF.get(tag);
        if (nfcf != null) {
            try {
                JSONObject r = FelicaReader.readCard(nfcf);
                Intent intent1 = new Intent();
                intent.setAction(NFCF);
                mContext.sendBroadcast(intent1);
                try {
                    Log.d(TAG, r.toString(4));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Build APDU for SELECT AID command. This command indicates which service a reader is
     * interested in communicating with. See ISO 7816-4.
     *
     * @param aid Application ID (AID) to select
     * @return APDU for SELECT AID command
     */
    public static byte[] BuildSelectApdu(String aid) {
        // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
        return HexStringToByteArray(SELECT_APDU_HEADER + String.format("%02X", aid.length() / 2) + aid);
    }

    /**
     * Utility class to convert a byte array to a hexadecimal string.
     *
     * @param bytes Bytes to convert
     * @return String, containing hexadecimal representation.
     */
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

    /**
     * Utility class to convert a hexadecimal string to a byte string.
     * <p>
     * <p>Behavior with input strings containing non-hexadecimal characters is undefined.
     *
     * @param s String containing hexadecimal characters to convert
     * @return Byte array generated from input
     */
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
