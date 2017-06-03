package com.androidex.aexkk30;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.KeyListener;
import android.text.method.NumberKeyListener;
import android.text.method.TextKeyListener;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.androidex.aexkk30.bean.AssistBean;
import com.androidex.aexkk30.bean.ComBean;
import com.androidex.aexkk30.bean.MyFunc;
import com.androidex.aexkk30.bean.SerialPortFinder;
import com.androidex.common.AndroidExActivityBase;
import com.androidex.plugins.kkserial;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by cts on 17/6/3.
 */

public class Text485Activity extends AndroidExActivityBase implements View.OnClickListener {
    private static String TAG = "Text485Activity";
    public static String PORT_ADDR = "/dev/ttyS4,19200,N,1,8";
    private kkserial serial;
    private DispQueueThread dispQueueThread;
    protected int mSerialFd;
    private ReadThread mReadThread;
    private SendThread mSendThread;
    private int iDelay = 500;
    private byte[] _bLoopData = new byte[]{0x30};

    private EditText editTextCOMA;
    private EditText editTextTimeCOMA;
    private RadioButton radioButtonTxt;
    private RadioButton radioButtonHex;
    private CheckBox checkBoxAutoCOMA;
    private ToggleButton toggleButtonCOMA;
    SerialPortFinder mSerialPortFinder;//串口设备搜索
    private Spinner spinnerCOMA;
    private Spinner spinnerBaudRateCOMA;
    AssistBean AssistData;//用于界面数据序列化和反序列化
    private EditText editTextRecDisp;
    private EditText editTextLines;
    int iRecLines = 0;//接收区行数
    private CheckBox checkBoxAutoClear;
    private ComBean comBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text485);
        AssistData = getAssistData();
        if (serial == null) {
            serial = new kkserial(this);
        }
        initView();
    }

    private void initStartConfig() {
        mSerialFd = serial.serial_open(PORT_ADDR);
        Log.e("xxxmSerialFd", mSerialFd + "");

        if (mSerialFd > 0) {
            Log.e("MainActivity", "xxx串口打开成功");
            showMessage("串口打开成功");
        } else {
            Log.e("MainActivity", "xxx串口打开失败");
            showMessage("串口打开失败");
            return;
        }
        if (dispQueueThread == null) {
            dispQueueThread = new DispQueueThread();
            dispQueueThread.start();
        }
        mReadThread = new ReadThread();
        mReadThread.start();
        mSendThread = new SendThread();
        mSendThread.setSuspendFlag();
        mSendThread.start();
    }


    private void initView() {
        editTextRecDisp = (EditText) findViewById(R.id.editTextRecDisp);
        editTextLines = (EditText) findViewById(R.id.editTextLines);
        editTextCOMA = (EditText) findViewById(R.id.editTextCOMA);
        editTextTimeCOMA = (EditText) findViewById(R.id.editTextTimeCOMA);

        checkBoxAutoClear = (CheckBox) findViewById(R.id.checkBoxAutoClear);
        checkBoxAutoCOMA = (CheckBox) findViewById(R.id.checkBoxAutoCOMA);

        Button ButtonClear = (Button) findViewById(R.id.ButtonClear);
        Button ButtonSendCOMA = (Button) findViewById(R.id.ButtonSendCOMA);

        Button btn_serialText = (Button) findViewById(R.id.btn_serialText);
        Button btn_queryType = (Button) findViewById(R.id.btn_queryType);
        Button btn_queryVersion = (Button) findViewById(R.id.btn_queryVersion);
        Button btn_parameter = (Button) findViewById(R.id.btn_parameter);

        ButtonClear.setOnClickListener(this);
        ButtonSendCOMA.setOnClickListener(this);
        btn_serialText.setOnClickListener(this);
        btn_queryType.setOnClickListener(this);
        btn_queryVersion.setOnClickListener(this);
        btn_parameter.setOnClickListener(this);

        toggleButtonCOMA = (ToggleButton) findViewById(R.id.toggleButtonCOMA);
        spinnerCOMA = (Spinner) findViewById(R.id.SpinnerCOMA);

        spinnerBaudRateCOMA = (Spinner) findViewById(R.id.SpinnerBaudRateCOMA);

        radioButtonTxt = (RadioButton) findViewById(R.id.radioButtonTxt);
        radioButtonHex = (RadioButton) findViewById(R.id.radioButtonHex);

        editTextCOMA.setOnEditorActionListener(new EditorActionEvent());
        editTextTimeCOMA.setOnEditorActionListener(new EditorActionEvent());
        editTextCOMA.setOnFocusChangeListener(new FocusChangeEvent());
        editTextTimeCOMA.setOnFocusChangeListener(new FocusChangeEvent());

        radioButtonTxt.setOnClickListener(new radioButtonClickEvent());
        radioButtonHex.setOnClickListener(new radioButtonClickEvent());
        checkBoxAutoCOMA.setOnCheckedChangeListener(new CheckBoxChangeEvent());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.baudrates_value, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerBaudRateCOMA.setAdapter(adapter);
        spinnerBaudRateCOMA.setSelection(12);

        mSerialPortFinder = new SerialPortFinder();
        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
        List<String> allDevices = new ArrayList<String>();
        for (int i = 0; i < entryValues.length; i++) {
            allDevices.add(entryValues[i]);
        }
        ArrayAdapter<String> aspnDevices = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, allDevices);
        aspnDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCOMA.setAdapter(aspnDevices);
        if (allDevices.size() > 0) {
            spinnerCOMA.setSelection(0);
        }

        toggleButtonCOMA.setOnCheckedChangeListener(new ToggleButtonCheckedChangeEvent());
        spinnerCOMA.setOnItemSelectedListener(new ItemSelectedEvent());
        spinnerBaudRateCOMA.setOnItemSelectedListener(new ItemSelectedEvent());

        DispAssistData(AssistData);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ButtonClear:
                editTextRecDisp.setText("");
                break;
            case R.id.ButtonSendCOMA:
                sendPortData(editTextCOMA.getText().toString());
                break;
            case R.id.btn_serialText:
                sendPortData("$001,01&");
                break;
            case R.id.btn_queryType:
                sendPortData("$001,02&");
                break;
            case R.id.btn_queryVersion:
                sendPortData("$001,03&");
                break;
            case R.id.btn_parameter:
                sendPortData("$001,04&");
                break;
        }
    }

    //----------------------------------------------------打开关闭串口"/dev/ttyS4,19200,N,1,8";
    class ToggleButtonCheckedChangeEvent implements ToggleButton.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView == toggleButtonCOMA) {
                if (isChecked) {
                    PORT_ADDR=spinnerCOMA.getSelectedItem().toString()+","+spinnerBaudRateCOMA.getSelectedItem().toString()+",N,1,8";
                    showMessage(PORT_ADDR);
                    initStartConfig();

                } else {
                    CloseComPort();
                    checkBoxAutoCOMA.setChecked(false);
                }
            }
        }
    }

    //----------------------------------------------------串口发送
    private void sendPortData(String str) {
        if (mSerialFd > 0) {
            if (radioButtonTxt.isChecked()) {
                byte[] bytes = str.getBytes();
                serial.serial_write(mSerialFd, bytes, 20);
            } else if (radioButtonHex.isChecked()) {
                serial.serial_writeHex(mSerialFd, str);
            }
        } else {
            showMessage("请先打开串口");
        }
    }


    private void DispRecData(ComBean ComRecData) {
        StringBuilder sMsg = new StringBuilder();
        sMsg.append(ComRecData.sRecTime);
        sMsg.append("[");
        sMsg.append(ComRecData.sComPort);
        sMsg.append("]");
        if (radioButtonTxt.isChecked()) {
            sMsg.append("[Txt] ");
            sMsg.append(new String(ComRecData.bRec));
        } else if (radioButtonHex.isChecked()) {
            sMsg.append("[Hex] ");
            sMsg.append(MyFunc.ByteArrToHex(ComRecData.bRec));
        }
        sMsg.append("\r\n");
        editTextRecDisp.append(sMsg);
        Log.e("xxx显示数据：", sMsg.toString());
        iRecLines++;
        editTextLines.setText(String.valueOf(iRecLines));
        if ((iRecLines > 500) && (checkBoxAutoClear.isChecked())) {//达到500项自动清除
            editTextRecDisp.setText("");
            editTextLines.setText("0");
            iRecLines = 0;
        }
    }

    //----------------------------------------------------
    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                try {
                    byte[] data = serial.serial_read(mSerialFd, 20, 3 * 1000);
                    if (data == null) continue;
                    if (data.length > 0) {
                        comBean = new ComBean(PORT_ADDR, data, data.length);
                        runOnUiThread(new Runnable()//直接刷新显示
                        {
                            public void run() {
                                DispRecData(comBean);
                            }
                        });
                    }
                    try {
                        Thread.sleep(50);//延时50ms
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    //----------------------------------------------------
    private class SendThread extends Thread {
        public boolean suspendFlag = true;// 控制线程的执行

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                synchronized (this) {
                    while (suspendFlag) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                sendPortData(editTextCOMA.getText().toString().trim());
                try {
                    Thread.sleep(iDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //线程暂停
        public void setSuspendFlag() {
            this.suspendFlag = true;
        }

        //唤醒线程
        public synchronized void setResume() {
            this.suspendFlag = false;
            notify();
        }
    }

    //----------------------------------------------------刷新显示线程
    private class DispQueueThread extends Thread {
        private Queue<ComBean> QueueList = new LinkedList<ComBean>();

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                final ComBean ComData;
                while ((ComData = QueueList.poll()) != null) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            DispRecData(ComData);
                        }
                    });
                    try {
                        Thread.sleep(100);//显示性能高的话，可以把此数值调小。
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        public synchronized void AddQueue(ComBean ComData) {
            QueueList.add(ComData);
        }
    }

    //----------------------------------------------------关闭串口
    private void CloseComPort() {
        if (serial != null) {
            serial.serial_close(mSerialFd);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CloseComPort();
    }

    //----------------------------------------------------刷新界面数据
    private void DispAssistData(AssistBean AssistData) {
        if (AssistData.isTxt()) {
            radioButtonTxt.setChecked(true);
        } else {
            radioButtonHex.setChecked(true);
        }
        editTextTimeCOMA.setText(AssistData.sTimeA);
        setDelayTime(editTextTimeCOMA);
    }

    //----------------------------------------------------
    private AssistBean getAssistData() {
        SharedPreferences msharedPreferences = getSharedPreferences("text485", Context.MODE_PRIVATE);
        AssistBean AssistData = new AssistBean();
        try {
            String personBase64 = msharedPreferences.getString("AssistData", "");
            byte[] base64Bytes = Base64.decode(personBase64.getBytes(), 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            AssistData = (AssistBean) ois.readObject();
            return AssistData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AssistData;
    }

    //----------------------------------------------------自动发送
    class CheckBoxChangeEvent implements CheckBox.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView == checkBoxAutoCOMA) {
                if (!toggleButtonCOMA.isChecked() && isChecked) {
                    buttonView.setChecked(false);
                    return;
                }
                SetLoopData(editTextCOMA.getText().toString());
                SetAutoSend(isChecked);
            }
        }
    }

    //----------------------------------------------------串口号或波特率变化时，关闭打开的串口
    class ItemSelectedEvent implements Spinner.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if ((arg0 == spinnerCOMA) || (arg0 == spinnerBaudRateCOMA)) {
                CloseComPort();
                checkBoxAutoCOMA.setChecked(false);
                toggleButtonCOMA.setChecked(false);
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {

        }

    }

    //----------------------------------------------------
    public void setTxtLoopData(String sTxt) {
        this._bLoopData = sTxt.getBytes();
    }

    //----------------------------------------------------
    public void setHexLoopData(String sHex) {
        this._bLoopData = MyFunc.HexToByteArr(sHex);
    }

    public byte[] getbLoopData() {
        return _bLoopData;
    }

    //----------------------------------------------------设置自动发送数据
    private void SetLoopData(String sLoopData) {
        if (radioButtonTxt.isChecked()) {
            setTxtLoopData(sLoopData);
        } else if (radioButtonHex.isChecked()) {
            setHexLoopData(sLoopData);
        }
    }

    //----------------------------------------------------设置自动发送模式开关
    private void SetAutoSend(boolean isAutoSend) {
        if (isAutoSend) {
            startSend();
        } else {
            stopSend();
        }
    }

    public void startSend() {
        if (mSendThread != null) {
            mSendThread.setResume();
        }
    }

    //----------------------------------------------------
    public void stopSend() {
        if (mSendThread != null) {
            mSendThread.setSuspendFlag();
        }
    }

    //----------------------------------------------------编辑框完成事件
    class EditorActionEvent implements EditText.OnEditorActionListener {
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (v == editTextCOMA) {
                setSendData(editTextCOMA);
            } else if (v == editTextTimeCOMA) {
                setDelayTime(editTextTimeCOMA);
            }
            return false;
        }
    }

    //----------------------------------------------------编辑框焦点转移事件
    class FocusChangeEvent implements EditText.OnFocusChangeListener {
        public void onFocusChange(View v, boolean hasFocus) {
            if (v == editTextCOMA) {
                setSendData(editTextCOMA);
            } else if (v == editTextTimeCOMA) {
                setDelayTime(editTextTimeCOMA);
            }
        }
    }

    //----------------------------------------------------设置自动发送数据
    private void setSendData(TextView v) {
        AssistData.setSendA(v.getText().toString());
        SetLoopData(v.getText().toString());
    }

    //----------------------------------------------------设置自动发送延时
    private void SetiDelayTime(String sTime) {
        setiDelay(Integer.parseInt(sTime));
    }

    public int getiDelay() {
        return iDelay;
    }

    //----------------------------------------------------
    public void setiDelay(int iDelay) {
        this.iDelay = iDelay;
    }

    //----------------------------------------------------设置自动发送延时
    private void setDelayTime(TextView v) {
        AssistData.sTimeA = v.getText().toString();
        SetiDelayTime(v.getText().toString());
    }

    //----------------------------------------------------Txt、Hex模式选择
    class radioButtonClickEvent implements RadioButton.OnClickListener {
        public void onClick(View v) {
            if (v == radioButtonTxt) {
                KeyListener TxtkeyListener = new TextKeyListener(TextKeyListener.Capitalize.NONE, false);
                editTextCOMA.setKeyListener(TxtkeyListener);
                AssistData.setTxtMode(true);
            } else if (v == radioButtonHex) {
                KeyListener HexkeyListener = new NumberKeyListener() {
                    public int getInputType() {
                        return InputType.TYPE_CLASS_TEXT;
                    }

                    @Override
                    protected char[] getAcceptedChars() {
                        return new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                                'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F'};
                    }
                };
                editTextCOMA.setKeyListener(HexkeyListener);
                AssistData.setTxtMode(false);
            }
            editTextCOMA.setText(AssistData.getSendA());

            //setSendData(editTextCOMA);
        }
    }

    public void showMessage(String str) {
        if (str != null) {
            Toast.makeText(Text485Activity.this, str, Toast.LENGTH_SHORT).show();
            Log.e("xxxToast", str);
        } else {
            Log.e("xxxToast", "Toast is not null");
        }
    }

}
