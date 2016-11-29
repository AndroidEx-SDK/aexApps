package com.androidex.apps.aexSmartBiz;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidex.common.LogFragment;
import com.androidex.devices.aexddB58Printer;
import com.androidex.devices.appDeviceDriver;
import com.androidex.devices.appDevicesManager;
import com.androidex.logger.Log;
import com.androidex.logger.LogWrapper;
import com.androidex.logger.MessageOnlyLogFilter;

import java.io.UnsupportedEncodingException;


/**
 *
 */
public class SmartBizMainActivity extends AppCompatActivity {

      public static final String TAG = "SmartBiz";
      private Button btn_test_printer;
      private Button btn_bank_reader;
      private Button btn_cas_reader;
      private Button btn_test_password;
      private appDevicesManager mDevices;
      private SmartBizMainActivity mActivity;
      private Button btn_exit;
      private TextView tv_sdk_version;
      private TextView tv_uuid;

      @Override
      protected void onDestroy() {
            super.onDestroy();
            mDevices.mPrinter.Close();
            mDevices.mBankCardReader.Close();
            mDevices.mCasCardReader.Close();
      }

      @Override
      protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_smart_biz_main);

            mActivity = this;
            mDevices = new appDevicesManager(this);

            initializeLogging();

            btn_test_printer = (Button) findViewById(R.id.btn_test_printer);
            btn_bank_reader = (Button) findViewById(R.id.btn_reader_card);
            btn_cas_reader = (Button) findViewById(R.id.btn_cas_reader);
            btn_test_password = (Button) findViewById(R.id.btn_test_password);
            btn_exit = (Button) findViewById(R.id.btn_exit);

            tv_sdk_version = (TextView) findViewById(R.id.tv_sdk_version);
            tv_uuid = (TextView) findViewById(R.id.tv_uuid);

            tv_sdk_version.setText(mDevices.mHwservice.getSdkVersion());
            tv_uuid.setText(mDevices.mHwservice.get_uuid());
            Log.i(TAG, mDevices.mHwservice.getSdkVersion());
            Log.i(TAG, mDevices.mHwservice.get_uuid());
            Log.i(TAG, String.format("flag0=0x%02X,flag1=0x%02X", mDevices.mHwservice.get_flag0(), mDevices.mHwservice.get_flag1()));
            //打印机
            btn_test_printer.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                        Log.i(TAG, "打印测试程序...");
                        if (mDevices.mPrinter.Open()) {
                              try {
                                    mDevices.mPrinter.selfTest();
                                    String str = "安卓工控";
                                    try {
                                          mDevices.mPrinter.WriteData(str.getBytes("GBK"), str.getBytes().length);
                                          aexddB58Printer printer = (aexddB58Printer) (mDevices.mPrinter);
                                          printer.ln();
                                    } catch (UnsupportedEncodingException e) {
                                          e.printStackTrace();
                                    }
                                    mDevices.mPrinter.WriteDataHex("1D564200");
                                    mDevices.mPrinter.Close();
                              } catch (Exception e) {
                                    Log.i(TAG, e.getLocalizedMessage());
                                    e.printStackTrace();
                              }
                              Log.i(TAG, "打印测试结束，关闭打印机设备。");
                        } else {
                              String s = String.format("Open printer fial:%s", mDevices.mPrinter.mParams.optString(appDeviceDriver.PORT_ADDRESS));
                              Log.i(TAG, s);
                              Toast.makeText(mActivity, s, Toast.LENGTH_LONG).show();
                        }
                  }
            });
            //银行卡
            btn_bank_reader.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                        if (mDevices.mBankCardReader.Open()) {
                              //mDevices.mBankCardReader.reset();
                              mDevices.mBankCardReader.ReciveDataLoop();
                              //mDevices.mBankCardReader.queryCard();
                              //mDevices.mBankCardReader.popCard();
                              //mDevices.mBankCardReader.Close();
                        } else {
                              Toast.makeText(mActivity, String.format("Open bank reader fial:%s", mDevices.mBankCardReader.mParams.optString(appDeviceDriver.PORT_ADDRESS)), Toast.LENGTH_LONG).show();
                        }
                  }
            });
            //燃气卡
            btn_cas_reader.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                        if (mDevices.mCasCardReader.Open()) {
                              //mDevices.mCasCardReader.reset();
                              mDevices.mCasCardReader.ReciveDataLoop();
                              //mDevices.mCasCardReader.queryCard();
                              //mDevices.mCasCardReader.popCard();
                              //mDevices.mCasCardReader.Close();
                        } else {
                              Toast.makeText(mActivity, String.format("Open cas reader fial:%s", mDevices.mCasCardReader.mParams.optString(appDeviceDriver.PORT_ADDRESS)), Toast.LENGTH_LONG).show();
                        }
                  }
            });
            //密码键盘
            btn_test_password.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                        if (mDevices.mPasswordKeypad.Open()) {
                              mDevices.mPasswordKeypad.pkReset();
                              String pkVersion = mDevices.mPasswordKeypad.pkGetVersion();
                              Log.d(TAG, pkVersion);
                              Toast.makeText(mActivity, pkVersion, Toast.LENGTH_LONG).show();
                              mDevices.mPasswordKeypad.Close();
                        } else {
                              Toast.makeText(mActivity, String.format("Open password keypad fial:%s", mDevices.mPasswordKeypad.mParams.optString(appDeviceDriver.PORT_ADDRESS)), Toast.LENGTH_LONG).show();

                        }
                  }
            });
            btn_exit.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                        System.exit(0);
                  }
            });
      }

      /**
       * Create a chain of targets that will receive log data
       */
      public void initializeLogging() {
            // Wraps Android's native log framework.
            LogWrapper logWrapper = new LogWrapper();
            // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
            Log.setLogNode(logWrapper);

            // Filter strips out everything except the message text.
            MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
            logWrapper.setNext(msgFilter);

            // On screen logging via a fragment with a TextView.
            LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                      .findFragmentById(R.id.log_fragment);
            msgFilter.setNext(logFragment.getLogView());

            Log.i(TAG, "就绪");
      }
}
