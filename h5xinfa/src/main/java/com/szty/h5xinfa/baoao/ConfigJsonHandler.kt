package com.szty.h5xinfa.baoao

import android.content.Context
import android.os.Handler
import com.google.gson.Gson
import com.szty.h5xinfa.Constant
import com.szty.h5xinfa.model.ConfigBean
import java.io.File

//Kotlin实现
class ConfigJsonHandler private constructor() {
    companion object {
        const val TAG = "ConfigJsonHandler"
        private var instance: ConfigJsonHandler? = null
            get() {
                if (field == null) {
                    field = ConfigJsonHandler()
                }
                return field
            }

        @Synchronized
        fun get(): ConfigJsonHandler {
            return instance!!
        }

        var mConfigBean: ConfigBean? = null
    }

    fun readConfig(handler: Handler, context: Context) {
        val parent = context.getExternalFilesDir("szty/config")
        parent?.let {
            if (it.exists()) {
                val child = File(parent, FILE_CONFIG_JSON)
                if (child.exists()) {
                    val jsonData = child.readText()
                    val configBean = Gson().fromJson(jsonData, ConfigBean::class.java)
                    mConfigBean = configBean
                    handler.sendEmptyMessage(Constant.LOAD_XML_FINISH)
                } else {
                    handler.sendEmptyMessage(Constant.FILE_NOT_EXIST)
                }
            } else {
                it.mkdirs()
                handler.sendEmptyMessage(Constant.FILE_NOT_EXIST)
            }
        }
    }

}