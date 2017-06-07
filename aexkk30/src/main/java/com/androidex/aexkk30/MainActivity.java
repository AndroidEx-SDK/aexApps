package com.androidex.aexkk30;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    private void initView() {
        Button btn_oneKeyText = (Button) findViewById(R.id.btn_oneKeyText);
        btn_oneKeyText.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_oneKeyText:
                Intent intent = new Intent(this, OneKeyTextActivity.class);
                startActivity(intent);
                break;
        }
    }
}
