package com.example.zeng.zengtestdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        PieChart pie= (PieChart) findViewById(R.id.pie);
        pie.notifyDraw();
        pie.initSrc(new float[]{40f,60f,80f}, new String[]{"#00aaff",
                "#6be5a9", "#fecf5b"},new PieChart.OnItemClickListener() {

            @Override
            public void click(int position) {
                // TODO Auto-generated method stub

            }
        });
        System.out.println("测试代码提交");
    }
}
