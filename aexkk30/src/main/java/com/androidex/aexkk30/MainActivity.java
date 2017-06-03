package com.androidex.aexkk30;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.androidex.common.AndroidExActivityBase;

public class MainActivity extends AndroidExActivityBase implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    private void initView() {
        Button btn_text485 = (Button) findViewById(R.id.btn_text485);
        btn_text485.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_text485:

                Intent intent = new Intent(this, Text485Activity.class);
                startActivity(intent);

                break;
        }
    }
}
