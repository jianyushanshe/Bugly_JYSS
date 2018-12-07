package com.haylion.haylionbugly;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private TextView tvText;
    private TextView tvVisonName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvText = (TextView) findViewById(R.id.tv_text);
        tvVisonName = (TextView) findViewById(R.id.tv_vison);
        tvVisonName.setText(BuildConfig.VERSION_NAME);
        //tvText.setText("春眠不觉晓,这个有bug");//热修复前
        tvText.setText("春眠不觉晓，处处闻啼鸟");//热修复后
        tvText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //制造异常
//                ArrayList<String> arrayList = new ArrayList();
//                tvText.setText(arrayList.get(5));
               Toast.makeText(getApplicationContext(), "已經修復", Toast.LENGTH_LONG).show();
            }
        });
    }
}
