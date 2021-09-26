package com.szty.h5xinfa.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.szty.h5xinfa.Constant
import com.szty.h5xinfa.R
import com.szty.h5xinfa.baoao.*
import com.szty.h5xinfa.baoao.ConfigJsonHandler.Companion.get
import com.szty.h5xinfa.util.BaseUtils
import kotlinx.android.synthetic.main.activity_splash.*
import permissions.dispatcher.*
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.io.File
import java.lang.ref.WeakReference
import java.nio.charset.Charset
import kotlin.concurrent.thread
import kotlin.system.exitProcess

@RuntimePermissions
class SplashActivity : AppCompatActivity() {
    private val handler = MyHandler(this)
    private lateinit var startActivityLaunch: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        try {
            showVersion()
            registerActivityResult()
            requestPermissionWithPermissionCheck()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showVersion() {
        val version = BaseUtils.getVersionCode(this@SplashActivity)
        if (version.isNullOrEmpty()) {
            tv_version.visibility = View.GONE
        } else {
            tv_version.visibility = View.VISIBLE
            tv_version.text = "V $version"
        }
    }

    private fun checkCode() {
        getPsdFromFile()
    }

    private fun registerActivityResult() {
        startActivityLaunch =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    val resultCode = it.resultCode
                    if (resultCode == RESULT_OK) {
                        showCodeDialog()
                    } else if (resultCode == RESULT_CODE_EXIT) {
                        finish()
                        exitProcess(0)
                    }
                }
    }

    private fun initOther() {
        //开启EXO模式
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
        //ijk关闭log
        IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT)
        //切换渲染模式
        GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL)
        readBgImg()
        readConfig()
    }


    private fun readBgImg() {
        //生成盛放背景图片的文件夹
        val bg: File? = getExternalFilesDir("szty/bg")
        bg?.let {
            if (!it.exists()) {
                it.mkdirs()
            } else {
                val bgJpg = File(it, "bg.jpg")
                val bgPng = File(it, "bg.png")
                when {
                    bgJpg.exists() -> {
                        bgBitmapDrawable = BitmapDrawable(resources, BitmapFactory.decodeFile(bgJpg.absolutePath))
                    }
                    bgPng.exists() -> {
                        bgBitmapDrawable = BitmapDrawable(resources, BitmapFactory.decodeFile(bgPng.absolutePath))
                    }
                    else -> {
                        L.e("Please make sure the bg.jpg or bg.png exists")
                    }
                }
            }
        }
    }

    private fun readConfig() {
        //获取 android/data/packagename/files/szty 目录
        val file: File? = this.getExternalFilesDir(Constant.PATH_SZTY)
        val configF = File(file, Constant.PATH_CONFIG)
        //如果android/data/packagename/files/szty/config 目录不存在 则要创建各级文件夹
        if (!configF.exists()) {
            configF.mkdirs()
            //发送消息
            handler.sendEmptyMessage(Constant.FILE_NOT_EXIST)
        } else {
            //单独开辟一个线程用来加载config目录下面的xml数据
            thread {
                try {
                    get().readConfig(handler, this)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    handler.sendEmptyMessage(Constant.LOAD_XML_ERROR)
                }
            }
        }
    }

    private fun getPsdFromFile() {
        val file = getExternalFilesDir(null)
        val f = File(file, FILE_CODE)
        f.let {
            if (it.exists()) {
                val key = it.readText(Charset.forName("utf-8"))
                L.i("read key ... $key")
                identifyStr = getIMEIDeviceId(this)
                if (identifyStr.isNullOrEmpty()) {
                    L.i(R.string.deviceid_is_error)
                    Toast.makeText(this, R.string.deviceid_is_error, Toast.LENGTH_SHORT).show()
                    return
                }
                //存在 code.txt 但内容为空
                if (TextUtils.isEmpty(key)) {
                    showCodeDialog()
                } else {
                    compareInputCode(key)
                }
            } else {
                showCodeDialog()
            }
        }
    }

    /**
     * 跳转到系统设置界面
     *
     * @param activity
     */
    private fun jumpSystemSet(activity: Activity) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri: Uri =
                Uri.fromParts("package", activity.packageName, null)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.data = uri
        activity.startActivity(intent)
    }

    private fun showCodeDialog() {
        MaterialDialog(this).show {
            cancelable(false)
            cancelOnTouchOutside(false)
            title(R.string.dialog_title)
            message(R.string.dialog_code_path)
            positiveButton(R.string.dialog_input_again) {
                getPsdFromFile()
            }
            negativeButton(R.string.dialog_device_id) {
                jumpToOtherActivity()
            }
        }
    }

    private fun jumpToOtherActivity() {
        startActivityLaunch.launch(Intent(this, CodeActivity::class.java))
    }

    /**
     * 比对验证码的正确性
     */
    private fun compareInputCode(input: String) {
        val content = StringBuffer()
                .append(resources.getString(R.string.pre_str))
                .append(com.szty.h5xinfa.BuildConfig.LAST_STR)
                .append(SZTY)
                .toString()
//    L.i("input ... $input  |  content ... $content")
        val decryptStr = AESCipher.DeCode(input, content)
//    L.i("decryptStr ... $decryptStr  | identifyStr ... $identifyStr")
        if (decryptStr == identifyStr) {
            Toast.makeText(this, R.string.dialog_jump, Toast.LENGTH_SHORT).show()
            initOther()
        } else {
            showCompareResultDialog(resources.getString(R.string.code_file_is_error))
        }
    }

    private fun showCompareResultDialog(input: String) {
        MaterialDialog(this).show {
            cancelable(false)
            cancelOnTouchOutside(false)
            title(R.string.dialog_title)
            message(text = input)
            positiveButton(R.string.dialog_input_again) {
                getPsdFromFile()
            }
            negativeButton(R.string.dialog_device_id) {
                jumpToOtherActivity()
            }
        }
    }

    class MyHandler(a: SplashActivity) : Handler() {
        private val wr = WeakReference(a)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            wr.get()?.let {
                it.handler.postDelayed({
                    val intent = Intent(it, MainActivity::class.java)
                    intent.putExtra(Constant.KEY_HANDLE_CODE, msg.what)
                    it.startActivity(intent)
                    it.finish()
                }, 2000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    ///////////////////////////////权限相关 start//////////////////////////////////

    @NeedsPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    )
    fun requestPermission() {
        try {
            checkCode()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    @OnShowRationale(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    )
    fun showRationaleForPermission(request: PermissionRequest) {
        MaterialDialog(this)
                .cancelable(false)
                .cancelOnTouchOutside(false)
                .message(R.string.res_no_permission)
                .positiveButton(R.string.dialog_confirm) {
                    request.proceed()
                }
                .negativeButton(R.string.dialog_cancel) {
                    request.cancel()
                }.show()
    }

    @OnPermissionDenied(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    )
    fun onPermissionDenied() {
    }

    @OnNeverAskAgain(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    )
    fun onPermissionNeverAskAgain() {
        MaterialDialog(this)
                .cancelable(false)
                .cancelOnTouchOutside(false)
                .message { R.string.res_no_permission }
                .positiveButton(R.string.dialog_setting) {
                    jumpSystemSet(this)
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