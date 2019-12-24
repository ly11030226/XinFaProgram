package com.ads.xinfa.base;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 基类Activity
 * @author Ly
 */
public class BaseActivity extends AppCompatActivity  {
    private static final String TAG = "BaseActivity";
    protected  boolean isActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getInstance().addActivity(this);
        isActive = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActive = false;
        AppManager.getInstance().finishActivity(this);

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}