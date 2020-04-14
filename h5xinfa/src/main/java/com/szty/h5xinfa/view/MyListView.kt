package com.szty.h5xinfa.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.szty.h5xinfa.databinding.ViewMylistviewBinding
import com.szty.h5xinfa.databinding.ViewRollviewBinding
import com.szty.h5xinfa.ui.SecondActivity
import com.szty.h5xinfa.viewRecycle.MyLinearLayoutManager

class MyListView :RelativeLayout{
    private var clickItemCallBack : ClickItemCallBack?=null
    private var building : ViewMylistviewBinding? = null
    constructor(context: Context) : this(context,null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs,
            defStyle) {
        building = ViewMylistviewBinding.inflate(LayoutInflater.from(context), this, true)
        building?.ll1?.setOnClickListener {
            clickItemCallBack?.click1()
        }
        building?.ll2?.setOnClickListener {
            clickItemCallBack?.click2()
        }
        building?.ll3?.setOnClickListener {
            clickItemCallBack?.click3()
        }
        building?.ll4?.setOnClickListener {
            clickItemCallBack?.click4()
        }
        building?.ll5?.setOnClickListener {
            clickItemCallBack?.click5()
        }
    }
    fun addClickItemCallBack(c : ClickItemCallBack){
        clickItemCallBack = c
    }

    interface ClickItemCallBack{
        /**
         * 点击财富管理
         */
        fun click1()
        /**
         * 点击小浦金店
         */
        fun click2()
        /**
         * 点击人民币存款利率
         */
        fun click3()
        /**
         * 点击浦发ETC
         */
        fun click4()
        /**
         * 点击普及金融
         */
        fun click5()
    }

}