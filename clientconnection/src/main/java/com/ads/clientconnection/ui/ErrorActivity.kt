package com.ads.clientconnection.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Process
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ads.clientconnection.R
import com.afollestad.materialdialogs.MaterialDialog
import com.tamsiree.rxkit.TLog
import com.tamsiree.rxkit.crash.TCrashTool
import kotlinx.android.synthetic.main.activity_error.*
import kotlin.system.exitProcess

class ErrorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
        try {
            val message = TCrashTool.getAllErrorDetailsFromIntent(this, intent)
            val file = TLog.e(message)
            tv_path.text = "日志路径：\n${file.absolutePath}"
            val dialog = MaterialDialog.Builder(this)
                    .title("错误细节")
                    .content(message)
                    .positiveText("关闭")
                    .neutralText("复制到剪贴板")
                    .onNeutral { _, _ ->
                        copyErrorToClipboard()
                    }
                    .build()
            btn_detail.setOnClickListener {
                dialog.show()
            }
            btn_exit.setOnClickListener {
                finish()
                Process.killProcess(Process.myPid())
                exitProcess(10)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun copyErrorToClipboard() {
        val errorInformation = TCrashTool.getAllErrorDetailsFromIntent(this@ErrorActivity, intent)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        //Are there any devices without clipboard...?
        val clip = ClipData.newPlainText("错误信息", errorInformation)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this@ErrorActivity, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }
}
