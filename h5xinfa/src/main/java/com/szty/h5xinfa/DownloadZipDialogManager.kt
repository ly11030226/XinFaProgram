package com.szty.h5xinfa

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView

/**
 * @author Ly
 */
class DownloadZipDialogManager(context: Context) {
    private val tvshowprogress: TextView
    private val progressBar: ProgressBar
    private val btncommit: AppCompatButton
    private val materialDialog: MaterialDialog?
    fun showDialog() {
        materialDialog?.show()
    }

    fun setProgress(num: Int) {
        progressBar.progress = num
    }

    fun setContent(content: String?) {
        tvshowprogress.text = content
    }

    val isShowing: Boolean
        get() = materialDialog?.isShowing ?: false

    fun dismiss() {
        materialDialog?.dismiss()
    }

    init {
        materialDialog = MaterialDialog(context)
        materialDialog.customView(R.layout.dialog_download)
        materialDialog.cancelable(false)
        materialDialog.cancelOnTouchOutside(false)
        val view = materialDialog.getCustomView()
        btncommit = view.findViewById<View>(R.id.btn_commit) as AppCompatButton
        progressBar = view.findViewById<View>(R.id.progressBar) as ProgressBar
        tvshowprogress = view.findViewById<View>(R.id.tv_show_progress) as TextView
        progressBar.max = 100
        progressBar.isIndeterminate = false
        //设置不能点击
        btncommit.isEnabled = false
        btncommit.setOnClickListener { }
    }
}