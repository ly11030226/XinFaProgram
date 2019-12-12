package com.ads.xinfa.floatView;

import android.content.Intent;
import android.os.Bundle;

import com.ads.xinfa.R;
import com.ads.xinfa.base.BaseActivity;

public class FloatActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_float);
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        Intent intent = new Intent(FloatActivity.this, FloatViewService.class);
        //启动FloatViewService
        startService(intent);
        super.onStart();
    }

    @Override
    protected void onStop() {
        // 销毁悬浮窗
        Intent intent = new Intent(FloatActivity.this, FloatViewService.class);
        //终止FloatViewService
        stopService(intent);
        super.onStop();
    }
}
