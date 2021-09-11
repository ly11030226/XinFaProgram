package com.szty.h5xinfa.baoao
import android.view.View

abstract class DoubleClickListener : View.OnClickListener{
    private val TIME_DURATION = 1000
    private var preTime: Long = 0


    override fun onClick(v: View?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - preTime < TIME_DURATION) {
            doubleClick(v)
        }
        preTime = currentTime
    }

    abstract fun doubleClick(v: View?)
}