package com.szty.h5xinfakey

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.activity_main.*
import permissions.dispatcher.*
import java.io.File
import kotlin.system.exitProcess

@RuntimePermissions
class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            mb.setOnClickListener {
                val content: String = tiet.text.toString()
                Log.i(TAG, "device identify ... $content")
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(this, "请输入设备指纹", Toast.LENGTH_SHORT).show()
                } else {
                    val finalStr: String? = AESCipher.EnCode(content)
                    Log.i(TAG, "finalStr ... $finalStr")
                    mtv.text = finalStr
                }
            }
            mb_save.setOnClickListener {
                requestPermissionWithPermissionCheck()
            }
            mb_exit.setOnClickListener {
                exitProcess(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun saveFile() {
        try {
            val file = getExternalFilesDir(null)
            val f = File(file, "code.txt")
            if (f.exists()) {
                f.delete()
            }
            f.createNewFile()
            val content = mtv.text.toString()
            if (TextUtils.isEmpty(content)) {
                Toast.makeText(this, "验证码不能为空", Toast.LENGTH_SHORT).show()
            } else {
                f.writeText(content)
                tv_show.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "保存文件错误", Toast.LENGTH_SHORT).show()
        }
    }

    ///////////////////////////////权限相关 start//////////////////////////////////

    @NeedsPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    )
    fun requestPermission() {
        saveFile()
    }

    @OnShowRationale(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    )
    fun showRationaleForPermission(request: PermissionRequest) {
        MaterialDialog(this)
                .cancelable(false)
                .cancelOnTouchOutside(false)
                .message { R.string.dialog_request_permission }
                .positiveButton(R.string.dialog_confirm) {
                    request.proceed()
                }
                .negativeButton(R.string.dialog_cancel) {
                    request.cancel()
                }
    }

    @OnPermissionDenied(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    )
    fun onPermissionDenied() {
    }

    @OnNeverAskAgain(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    )
    fun onPermissionNeverAskAgain() {
        MaterialDialog(this)
            .cancelable(false)
            .cancelOnTouchOutside(false)
            .message { R.string.dialog_request_permission }
            .positiveButton(R.string.dialog_do) {
                Tools.jumpSystemSet(this)
            }
            .negativeButton(R.string.dialog_cancel)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated function
        onRequestPermissionsResult(requestCode, grantResults)
    }

    ///////////////////////////////权限相关 end//////////////////////////////////
}
