package com.tencent.devicedemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.phone.config.DeviceConfig;
import com.phone.utils.Ajax;
import com.phone.utils.HttpUtils;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.phone.service.MainService.httpServerToken;

public class InputCardInfoActivity extends Activity {
    public EditText et_admin;
    public EditText et_password;
    public String admin;
    public String password;
    public boolean flag = true;
    private String blockNo = "";
    private static final String TAG = "InputCardInfoActivity";
    public static final String FLAG = "flag";
    public String FROM = "from";
    public static int LOGIN_SUCCESS=0X01;
    public static int LOGIN_FAIL=0X02;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int result = msg.what;
            if (result == LOGIN_SUCCESS) {//登录成功
                Intent intent = new Intent(InputCardInfoActivity.this, MainActivity.class);
                intent.putExtra(FLAG, FROM);
                startActivity(intent);
                finish();
            } else if (result == LOGIN_FAIL) {//登录失败
                Toast.makeText(InputCardInfoActivity.this, "登录失败", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        et_admin = (EditText) findViewById(R.id.et_admin);
        et_password = (EditText) findViewById(R.id.et_password);
        et_admin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    flag = true;
                } else {
                    flag = false;
                }
            }
        });
        et_admin.setInputType(InputType.TYPE_NULL);//设置有光标不显示输入法
        et_password.setInputType(InputType.TYPE_NULL);

    }

    void setTextValue(final int id, String value) {
        final String thisValue = value;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTextView(id, thisValue);
            }
        });
    }

    void setTextView(int id, String txt) {
        ((TextView) findViewById(id)).setText(txt);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void callInput(int key, int id) {
        blockNo = blockNo + key;
        setTextValue(id, blockNo);
    }

    private String backKey(String code) {
        if (code != null && code != "") {
            int length = code.length();
            if (length == 1) {
                code = "";
            } else {
                code = code.substring(0, (length - 1));
            }
        }
        return code;
    }

    private void onKeyDown(int keyCode) {
        admin = et_admin.getText().toString();
        password = et_password.getText().toString();
        Log.d(TAG, "onKeyDown: " + keyCode);
        int key = convertKeyCode(keyCode);
        if (key >= 0) {
            if (flag) {//输入账号
                callInput(key, R.id.et_admin);
            } else {//输入密码
                callInput(key, R.id.et_password);
            }
        } else if (keyCode == KeyEvent.KEYCODE_STAR || keyCode == DeviceConfig.DEVICE_KEYCODE_STAR) {
            if (!flag && (password == null || password.equals(""))) {//返回到账号输入框
                et_admin.setFocusable(true);
                et_admin.setFocusableInTouchMode(true);
                et_admin.requestFocus();
            }
            Log.d(TAG, "onKeyDown: flag=" + flag);
            if (flag && !(admin == null || admin.equals(""))) {//删除输入的账号数字
                blockNo = backKey(blockNo);
                setTextValue(R.id.et_admin, blockNo);
            }
            if (!flag && !(password == null || password.equals(""))) {//删除输入的密码数字
                blockNo = backKey(blockNo);
                setTextValue(R.id.et_password, blockNo);
            }
            if (flag && (admin == null || admin.equals(""))) {
                finish();
            }
        } else if (keyCode == KeyEvent.KEYCODE_POUND || keyCode == DeviceConfig.DEVICE_KEYCODE_POUND) {//确认键
            if (flag) {
                if (!blockNo.equals("")) {
                    blockNo = "";
                }
                et_password.setFocusable(true);
                et_password.setFocusableInTouchMode(true);
                et_password.requestFocus();
            } else {
                //进行登录验证
                if (TextUtils.isEmpty(admin) || TextUtils.isEmpty(password)) {
                    Toast.makeText(this, "账号或者密码不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                //请求网络
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        login();
                    }
                }.start();
            }
        } else {
            Log.d(TAG, "按键无作用的时候触发这里 ：" + key);
        }
    }

    /**
     * 登录
     */
    private void login() {
        try {
            String url = DeviceConfig.SERVER_URL + "/app/rfid/adminLogin?username=" + this.admin;
            url = url + "&password=" + this.password;
            Log.d(TAG, "login: url=" + url);
            try {
                URL thisUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) thisUrl.openConnection();
                conn.setRequestMethod("GET");
                Log.d(TAG, "login: token=" + httpServerToken);
                if (httpServerToken != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + httpServerToken);
                }
                conn.setConnectTimeout(5000);
                int code = conn.getResponseCode();
                Log.d(TAG, "login: code=" + code);
                if (code == 200) {
                    InputStream is = conn.getInputStream();
                    String result = HttpUtils.readMyInputStream(is);
                    Log.d(TAG, "login: result=" + result);
                    JSONObject resultObj = Ajax.getJSONObject(result);
                    int resultCode = resultObj.getInt("code");
                    Log.d(TAG, "login: code=" + resultCode);
                    Message message = Message.obtain();
                    if (resultCode == 0) {//登录成功
                        message.what = LOGIN_SUCCESS;
                        handler.sendMessage(message);
                    } else {//登录失败
                        message.what = LOGIN_FAIL;
                        handler.sendMessage(message);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "login: +++++++++");
            }
        } catch (Exception e) {
        }
    }

    private int convertKeyCode(int keyCode) {
        int value = -1;
        if ((keyCode == KeyEvent.KEYCODE_0)) {
            value = 0;
        } else if ((keyCode == KeyEvent.KEYCODE_1)) {
            value = 1;
        } else if ((keyCode == KeyEvent.KEYCODE_2)) {
            value = 2;
        } else if ((keyCode == KeyEvent.KEYCODE_3)) {
            value = 3;
        } else if ((keyCode == KeyEvent.KEYCODE_4)) {
            value = 4;
        } else if ((keyCode == KeyEvent.KEYCODE_5)) {
            value = 5;
        } else if ((keyCode == KeyEvent.KEYCODE_6)) {
            value = 6;
        } else if ((keyCode == KeyEvent.KEYCODE_7)) {
            value = 7;
        } else if ((keyCode == KeyEvent.KEYCODE_8)) {
            value = 8;
        } else if ((keyCode == KeyEvent.KEYCODE_9)) {
            value = 9;
        }
        return value;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG, "dispatchKeyEvent: ??????????");
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            onKeyDown(keyCode);
            Log.e("===keycode1", event.getKeyCode() + "");
        }
        Log.e("===keycode2", event.getKeyCode() + "");
        return false;
    }
}
