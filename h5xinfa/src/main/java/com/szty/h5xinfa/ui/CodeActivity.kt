package com.szty.h5xinfa.ui

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import com.szty.h5xinfa.R
import com.szty.h5xinfa.baoao.L
import com.szty.h5xinfa.baoao.RESULT_CODE_EXIT
import com.szty.h5xinfa.baoao.getIMEIDeviceId
import kotlinx.android.synthetic.main.activity_code.*

class CodeActivity : AppCompatActivity() {

    companion object {
        const val TAG = "DeviceCodeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code)
        try {
            iv_back.setOnClickListener {
                onBackPressed()
            }
            mb_code.setOnClickListener {
                try {
                    val result = getIMEIDeviceId(this)
                    L.i("getIMEIDeviceId ... $result")
                    if (result.isNullOrEmpty()) {
                        Toast.makeText(this, "获取设备指纹有误", Toast.LENGTH_SHORT).show()
                    } else {
                        tv_code.text = result
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "获取设备指纹有误", Toast.LENGTH_SHORT).show()
                }
            }
            mb_exit.setOnClickListener {
                setResult(RESULT_CODE_EXIT)
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed()
        }
        return super.onKeyDown(keyCode, event)
    }

}