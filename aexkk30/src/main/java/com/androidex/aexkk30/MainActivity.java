package com.androidex.aexkk30;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    private void initView() {
        Button btn_oneKeyText = (Button) findViewById(R.id.btn_oneKeyText);
        Button btn_uninstall = (Button) findViewById(R.id.btn_uninstall);
        btn_oneKeyText.setOnClickListener(this);
        btn_uninstall.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_oneKeyText:
                Intent intent = new Intent(this, OneKeyTextActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_uninstall:
                //uninstall2();
                uninstallSlient();
                break;
        }
    }


    //静默卸载
    private void uninstallSlient() {
        String PACKAGE_NAME = "com.androidex.aexkk30";
        String cmd = "pm uninstall " + PACKAGE_NAME;
        Process process = null;
        DataOutputStream os = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        try {
            //卸载也需要root权限
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.write(cmd.getBytes());
            os.writeBytes("\n");
            os.writeBytes("exit\n");
            os.flush();
            //执行命令
            process.waitFor();
            //获取返回结果
            successMsg = new StringBuilder();
            errorMsg = new StringBuilder();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (successMsg != null) {

            Toast.makeText(this, "成功消息：" + successMsg.toString(), Toast.LENGTH_LONG).show();
            Log.e("====uninstall", "成功消息：" + successMsg.toString());
        } else if (errorMsg!=null){
            Toast.makeText(this, "错误消息: " + errorMsg.toString(), Toast.LENGTH_LONG).show();

            Log.e("====uninstall", "错误消息: " + errorMsg.toString());
        }else {
            Log.e("====uninstall", "错误消息:  null ");
        }
    }

    private void uninstall2() {
        String[] args = {"pm", "uninstall", "com.androidex.aexkk30"};
        String result = null;
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Log.e("====uninstall2()", "卸载程序执行");

        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
    }
}
