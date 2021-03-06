package com.ads.clientconnection.ui.qrCode;

import android.os.Bundle;
import android.view.KeyEvent;

import com.ads.clientconnection.R;
import com.ads.clientconnection.base.BaseActivity;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ViewfinderView;

import androidx.annotation.NonNull;


public class QRCodeActivity extends BaseActivity {
    private CaptureManager capture;
    private DecoratedBarcodeView bv_barcode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        try {
            bv_barcode = (DecoratedBarcodeView) findViewById(R.id.bv_barcode);
            capture = new CaptureManager(this, bv_barcode);
            capture.initializeFromIntent(getIntent(), savedInstanceState);
            capture.decode();
            ViewfinderView viewfinderView = bv_barcode.getViewFinder();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return bv_barcode.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

}
