package com.szty.h5xinfa.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.szty.h5xinfa.R
import com.szty.h5xinfa.baoao.*
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {
    var firstColor by Preference(KEY_FIRST_WINDOW, DEFAULT_FIRST_WINDOW)
    var secondColor by Preference(KEY_SECOND_WINDOW, DEFAULT_SECOND_WINDOW)
    var thirdColor by Preference(KEY_THIRD_WINDOW, DEFAULT_THIRD_WINDOW)
    var forthColor by Preference(KEY_FORTH_WINDOW, DEFAULT_FORTH_WINDOW)
    var textSize by Preference(KEY_TEXT_SIZE, DEFAULT_TEXT_SIZE)
    var textColor by Preference(KEY_TEXT_COLOR, DEFAULT_TEXT_COLOR)
    var winNum by Preference(KEY_WINDOW_NUM, DEFAULT_WINDOW_NUM)
    var serverIp by Preference(KEY_SERVER_IP, DEFAULT_SERVER_IP)
    var serverPort by Preference(KEY_SERVER_PORT, DEFAULT_SERVER_PORT)
    private var isUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        try {
            tiet_ip.setText(serverIp)
            tiet_port.setText(serverPort)
            tiet_window.setText("$winNum")
            tiet_text.setText("$textSize")
            tiet_color.setText(textColor)
            tiet_first.setText(firstColor)
            tiet_second.setText(secondColor)
            tiet_third.setText(thirdColor)
            tiet_forth.setText(forthColor)
            btn_save.setOnClickListener {
                try {
                    val ip = tiet_ip.text.toString()
                    val port = tiet_port.text.toString()
                    val window = tiet_window.text.toString()
                    val size = tiet_text.text.toString()
                    val color = tiet_color.text.toString()
                    val first = tiet_first.text.toString()
                    val second = tiet_second.text.toString()
                    val third = tiet_third.text.toString()
                    val forth = tiet_forth.text.toString()
                    if (!TextUtils.isEmpty(ip) &&
                            !TextUtils.isEmpty(port) &&
                            !TextUtils.isEmpty(window) &&
                            !TextUtils.isEmpty(size) &&
                            !TextUtils.isEmpty(color) &&
                            !TextUtils.isEmpty(first) &&
                            !TextUtils.isEmpty(second) &&
                            !TextUtils.isEmpty(third) &&
                            !TextUtils.isEmpty(forth)) {
                        serverIp = ip
                        serverPort = port
                        winNum = window.toInt()
                        textSize = size.toFloat()
                        textColor = color
                        firstColor = first
                        secondColor = second
                        thirdColor = third
                        forthColor = forth
                        isUpdate = true
                        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    isUpdate = false
                    Toast.makeText(this, "数据格式不正确", Toast.LENGTH_SHORT).show()
                }
            }
            btn_back.setOnClickListener {
                if (isUpdate) {
                    setResult(RESULT_OK)
                }
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}