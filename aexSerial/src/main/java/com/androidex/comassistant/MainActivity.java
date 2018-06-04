package com.androidex.comassistant;

/**
 * Created by cts on 17/3/31.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.KeyListener;
import android.text.method.NumberKeyListener;
import android.text.method.TextKeyListener;
import android.util.Base64;
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

import com.androidex.bean.AssistBean;
import com.androidex.bean.ComBean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author liyp
 *  安卓工控专用串口助手
 *  程序载入时可自动搜索串口设备
 *  n,8,1，没得选
 */
public class MainActivity extends Activity {
    EditText editTextRecDisp, editTextLines, editTextCOMA;
    EditText editTextTimeCOMA;
    CheckBox checkBoxAutoClear, checkBoxAutoCOMA;
    Button ButtonClear, ButtonSendCOMA;
    ToggleButton toggleButton_startTimingA,toggleButtonCOMA;
    Spinner SpinnerCOMA;
    Spinner SpinnerBaudRateCOMA;
    RadioButton radioButtonTxt, radioButtonHex;
    SerialControl ComA;
    DispQueueThread DispQueue;//刷新显示线程
    SerialPortFinder mSerialPortFinder;//串口设备搜索
    AssistBean AssistData;//用于界面数据序列化和反序列化
    int iRecLines = 0;//接收区行数

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ComA = new SerialControl(this);
        DispQueue = new DispQueueThread();
        DispQueue.start();
        AssistData = getAssistData();
        setControls();
    }

    @Override
    public void onDestroy() {
        saveAssistData(AssistData);
        CloseComPort(ComA);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        CloseComPort(ComA);
        setContentView(R.layout.main);
        setControls();
    }

    private void setControls() {
        String appName = getString(R.string.app_name);
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo("com.androidex.comassistant", PackageManager.GET_CONFIGURATIONS);
            String versionName = pinfo.versionName;
            setTitle(appName + " V" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        editTextRecDisp = (EditText) findViewById(R.id.editTextRecDisp);
        editTextLines = (EditText) findViewById(R.id.editTextLines);
        editTextCOMA = (EditText) findViewById(R.id.editTextCOMA);
        editTextTimeCOMA = (EditText) findViewById(R.id.editTextTimeCOMA);

        checkBoxAutoClear = (CheckBox) findViewById(R.id.checkBoxAutoClear);
        checkBoxAutoCOMA = (CheckBox) findViewById(R.id.checkBoxAutoCOMA);

        ButtonClear = (Button) findViewById(R.id.ButtonClear);
        ButtonSendCOMA = (Button) findViewById(R.id.ButtonSendCOMA);

        toggleButton_startTimingA = (ToggleButton) findViewById(R.id.toggleButton_startTimingA);
        toggleButtonCOMA = (ToggleButton) findViewById(R.id.toggleButtonCOMA);
        SpinnerCOMA = (Spinner) findViewById(R.id.SpinnerCOMA);
        SpinnerBaudRateCOMA = (Spinner) findViewById(R.id.SpinnerBaudRateCOMA);
        radioButtonTxt = (RadioButton) findViewById(R.id.radioButtonTxt);
        radioButtonHex = (RadioButton) findViewById(R.id.radioButtonHex);

        editTextCOMA.setOnEditorActionListener(new EditorActionEvent());
        editTextTimeCOMA.setOnEditorActionListener(new EditorActionEvent());
        editTextCOMA.setOnFocusChangeListener(new FocusChangeEvent());
        editTextTimeCOMA.setOnFocusChangeListener(new FocusChangeEvent());

        radioButtonTxt.setOnClickListener(new radioButtonClickEvent());
        radioButtonHex.setOnClickListener(new radioButtonClickEvent());
        ButtonClear.setOnClickListener(new ButtonClickEvent());
        ButtonSendCOMA.setOnClickListener(new ButtonClickEvent());
        toggleButton_startTimingA.setOnCheckedChangeListener(new ToggleButtonStartTimingListener());
        toggleButtonCOMA.setOnCheckedChangeListener(new ToggleButtonCheckedChangeEvent());
        checkBoxAutoCOMA.setOnCheckedChangeListener(new CheckBoxChangeEvent());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.baudrates_value, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpinnerBaudRateCOMA.setAdapter(adapter);
        SpinnerBaudRateCOMA.setSelection(12);

        mSerialPortFinder = new SerialPortFinder();
        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
        List<String> allDevices = new ArrayList<String>();
        for (int i = 0; i < entryValues.length; i++) {
            allDevices.add(entryValues[i]);
        }
        ArrayAdapter<String> aspnDevices = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, allDevices);
        aspnDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpinnerCOMA.setAdapter(aspnDevices);
        if (allDevices.size() > 0) {
            SpinnerCOMA.setSelection(0);
        }
        SpinnerCOMA.setOnItemSelectedListener(new ItemSelectedEvent());
        SpinnerBaudRateCOMA.setOnItemSelectedListener(new ItemSelectedEvent());
        DispAssistData(AssistData);
    }

    //串口号或波特率变化时，关闭打开的串口
    class ItemSelectedEvent implements Spinner.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if ((arg0 == SpinnerCOMA) || (arg0 == SpinnerBaudRateCOMA)) {
                CloseComPort(ComA);
                checkBoxAutoCOMA.setChecked(false);
                toggleButtonCOMA.setChecked(false);
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {

        }
    }

    //编辑框焦点转移事件
    class FocusChangeEvent implements EditText.OnFocusChangeListener {
        public void onFocusChange(View v, boolean hasFocus) {
            if (v == editTextCOMA) {
                setSendData(editTextCOMA);
            } else if (v == editTextTimeCOMA) {
                setDelayTime(editTextTimeCOMA);
            }
        }
    }

    //编辑框完成事件
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

    //Txt、Hex模式选择
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
            setSendData(editTextCOMA);
        }
    }

    //自动发送
    class CheckBoxChangeEvent implements CheckBox.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView == checkBoxAutoCOMA) {
                if (!toggleButtonCOMA.isChecked() && isChecked) {
                    buttonView.setChecked(false);
                    return;
                }
                SetLoopData(ComA, editTextCOMA.getText().toString());
                SetAutoSend(ComA, isChecked);
            }
        }
    }

    //清除按钮、发送按钮
    class ButtonClickEvent implements View.OnClickListener {
        public void onClick(View v) {
            if (v == ButtonClear) {
                editTextRecDisp.setText("");
            } else if (v == ButtonSendCOMA) {
                sendPortData(ComA, editTextCOMA.getText().toString());
            }
        }
    }


    /**
     * 自动控制串口操作，每间隔x s，循环发送数据，然后关闭，然后再间隔x s，循环发送数据，然后再关闭，循环执行。
     */
    class ToggleButtonStartTimingListener implements ToggleButton.OnCheckedChangeListener {

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView == toggleButton_startTimingA) {
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "开启串口自动控制状态", Toast.LENGTH_LONG).show();
                    //ComA.startTime();
                    toggleButton_startTimingA.setChecked(true);
                } else {
                    Toast.makeText(MainActivity.this, "关闭串口自动控制状态", Toast.LENGTH_LONG).show();
                    //ComA.stopTime();
                    toggleButton_startTimingA.setChecked(false);
                }
            }
        }
    }

    /**
     * 打开关闭串口
     */
    class ToggleButtonCheckedChangeEvent implements ToggleButton.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView == toggleButtonCOMA) {
                if (isChecked) {
                    ComA.setPort(SpinnerCOMA.getSelectedItem().toString());
                    ComA.setBaudRate(SpinnerBaudRateCOMA.getSelectedItem().toString());
                    OpenComPort(ComA);
                } else {
                    CloseComPort(ComA);
                    checkBoxAutoCOMA.setChecked(false);
                    toggleButtonCOMA.setChecked(false);
                }
            }
        }
    }

    /**
     * 串口控制类
     */
    public class SerialControl extends SerialHelper {

        public SerialControl(Context context) {
            super(context);
        }

        @Override
        protected void onDataReceived(final ComBean ComRecData) {
            //数据接收量大或接收时弹出软键盘，界面会卡顿,可能和6410的显示性能有关
            //直接刷新显示，接收数据量大时，卡顿明显，但接收与显示同步。
            //用线程定时刷新显示可以获得较流畅的显示效果，但是接收数据速度快于显示速度时，显示会滞后。
            //最终效果差不多_，线程定时刷新稍好一些。
            //DispQueue.AddQueue(ComRecData);//线程定时刷新显示(推荐)
            runOnUiThread(new Runnable()//直接刷新显示
            {
                public void run() {
                    DispRecData(ComRecData);
                }
            });
        }

        @Override
        protected void onLog(final String msg){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    log(msg);
                }
            });
        }

        @Override
        protected void onClearMessage(){
            editTextRecDisp.setText("");
        }
    }

    /**
     * 刷新显示线程
     */
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
                        Thread.sleep(200);//显示性能高的话，可以把此数值调小。
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

    /**
     * 刷新界面数据
     */
    private void DispAssistData(AssistBean AssistData) {
        editTextCOMA.setText(AssistData.getSendA());
        setSendData(editTextCOMA);
        if (AssistData.isTxt()) {
            radioButtonTxt.setChecked(true);
        } else {
            radioButtonHex.setChecked(true);
        }
        editTextTimeCOMA.setText(AssistData.sTimeA);
        setDelayTime(editTextTimeCOMA);
    }

    /**
     * 保存、获取界面数据
     */
    private void saveAssistData(AssistBean AssistData) {
        AssistData.sTimeA = editTextTimeCOMA.getText().toString();
        SharedPreferences msharedPreferences = getSharedPreferences("comassistant", Context.MODE_PRIVATE);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(AssistData);
            String sBase64 = new String(Base64.encode(baos.toByteArray(), 0));
            SharedPreferences.Editor editor = msharedPreferences.edit();
            editor.putString("AssistData", sBase64);
            editor.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取缓存的数据
     *
     * @return
     */
    private AssistBean getAssistData() {
        SharedPreferences msharedPreferences = getSharedPreferences("comassistant", Context.MODE_PRIVATE);
        AssistBean AssistData = new AssistBean();
        try {
            String personBase64 = msharedPreferences.getString("AssistData", "");
            byte[] base64Bytes = Base64.decode(personBase64.getBytes(), 0);
            if (base64Bytes != null) {
                ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
                ObjectInputStream ois = new ObjectInputStream(bais);
                AssistData = (AssistBean) ois.readObject();
            }
            return AssistData;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AssistData;
    }

    //设置自动发送延时
    private void setDelayTime(TextView v) {
        if (v == editTextTimeCOMA) {
            AssistData.sTimeA = v.getText().toString();
            SetiDelayTime(ComA, v.getText().toString());
        }
    }

    //设置自动发送数据
    private void setSendData(TextView v) {
        if (v == editTextCOMA) {
            AssistData.setSendA(v.getText().toString());
            SetLoopData(ComA, v.getText().toString());
        }
    }

    //设置自动发送延时
    private void SetiDelayTime(SerialHelper ComPort, String sTime) {
        ComPort.setiDelay(Integer.parseInt(sTime));
    }

    //设置自动发送数据
    private void SetLoopData(SerialHelper ComPort, String sLoopData) {
        if (radioButtonTxt.isChecked()) {
            ComPort.setTxtLoopData(sLoopData);
        } else if (radioButtonHex.isChecked()) {
            ComPort.setHexLoopData(sLoopData);
        }
    }

    //显示接收数据
    private void DispRecData(ComBean ComRecData) {
        StringBuilder sMsg = new StringBuilder();
        if (ComRecData.bRec[0] == 0X24) {
            try {
                //sMsg.append("[Str] ");
                sMsg.append(new String(ComRecData.bRec, "UTF8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            if (radioButtonTxt.isChecked()) {
                try {
                    sMsg.append(new String(ComRecData.bRec, "UTF8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else if (radioButtonHex.isChecked()) {
                sMsg.append("[Hex] ");
                sMsg.append(MyFunc.ByteArrToHex(ComRecData.bRec));
            }
        }
        editTextRecDisp.append(sMsg);
        iRecLines++;
        editTextLines.setText(String.valueOf(iRecLines));
        if ((iRecLines > 500) && (checkBoxAutoClear.isChecked())) {//达到500项自动清除
            editTextRecDisp.setText("");
            editTextLines.setText("0");
            iRecLines = 0;
        }
    }

    private void log(String msg){
        editTextRecDisp.append(String.format("\r\n%s\r\n",msg));
    }

    //设置自动发送模式开关
    private void SetAutoSend(SerialHelper ComPort, boolean isAutoSend) {
        if (isAutoSend) {
            //ComPort.startSend();
        } else {
            //ComPort.stopSend();
        }
    }

    //串口发送
    private void sendPortData(SerialHelper ComPort, String sOut) {
        if (ComPort != null && ComPort.isOpen()) {
            if (radioButtonTxt.isChecked()) {
                ComPort.sendTxt(sOut);
            } else if (radioButtonHex.isChecked()) {
                ComPort.sendHex(sOut);
            }
        }
    }

    //关闭串口
    private void CloseComPort(SerialHelper ComPort) {
        if (ComPort != null) {
            ComPort.close();
        }
    }

    //打开串口
    private void OpenComPort(SerialHelper ComPort) {
        int mSerialFd = ComPort.open();
        if (mSerialFd > 0) {
            ShowMessage("打开串口成功！");
            ComPort.startReadSerial();
        } else {
            checkBoxAutoCOMA.setChecked(false);
            toggleButtonCOMA.setChecked(false);
            ShowMessage("打开串口失败！请查看串口是否存在或是否有权限");
        }
    }

    //显示消息
    private void ShowMessage(String sMsg) {
        log(sMsg);
    }
}
