package com.ads.xinfa.ui.scanCode;

import android.os.Bundle;

import com.ads.xinfa.R;
import com.ads.xinfa.base.BaseActivity;

import butterknife.ButterKnife;

/**
 * 扫码界面
 */
public class ScanCodeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_code);
        ButterKnife.bind(this);
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
