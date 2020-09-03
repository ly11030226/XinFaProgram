package com.szty.h5xinfa.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gongwen.marqueen.SimpleMF
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import com.szty.h5xinfa.R
import com.szty.h5xinfa.RvEntity

import com.szty.h5xinfa.adapter.MyImageAdapter
import com.szty.h5xinfa.adapter.RvImageAdapter
import com.szty.h5xinfa.view.MyListView
import com.youth.banner.listener.OnPageChangeListener
import kotlinx.android.synthetic.main.activity_second.*
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class SecondActivity : AppCompatActivity() {
    val TAG : String = "SecondActivity"
    lateinit var mRvImageAdapter : RvImageAdapter
    var dataList = listOf(
            RvEntity(true,R.mipmap.c1),RvEntity(false,R.mipmap.c2),
            RvEntity(false,R.mipmap.c3),RvEntity(false,R.mipmap.c4),
            RvEntity(false,R.mipmap.c5),RvEntity(false,R.mipmap.c6),
            RvEntity(false,R.mipmap.c7),RvEntity(false,R.mipmap.c8))
    private val imageDataList = listOf(
            R.mipmap.c1, R.mipmap.c2,R.mipmap.c3, R.mipmap.c4,
            R.mipmap.c5, R.mipmap.c6, R.mipmap.c7, R.mipmap.c8)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        try {
//            initMarqueeView()
            addListener()
            initBanner()
            initRecyclerView()
            initRightItem()
//            addTouchListener()
        } catch (e: Exception) {
            e.printStackTrace()
            e.printStackTrace()
        }
    }


    private fun addTouchListener() {
        var lastPointX : Int
        var lastPointY : Int
        iv_tuo.setOnTouchListener{ view, event->
            lastPointX = cl_content.x.toInt()
            lastPointY = cl_content.y.toInt()
            when(event.action){
                MotionEvent.ACTION_DOWN ->{
                    lastPointX = event.rawX.toInt()
                    lastPointY = event.rawY.toInt()
                    false
                }
                MotionEvent.ACTION_MOVE->{
                    val xDis = event.rawX.toInt() - lastPointX
                    val yDis = event.rawY.toInt() - lastPointY
                    if(xDis!=0 && yDis!=0){
                        var l = cl_content.left + xDis
                        var t = cl_content.top + yDis
                        var r = cl_content.right + xDis
                        var b = cl_content.bottom + yDis
//                        if(l<0){
//                            l = 0
//                            r = l + building.clContent.width
//                        }
//                        if(){
//
//                        }
                        cl_content.layout(l,t,r,b)
                    }
                    lastPointX = event.rawX.toInt()
                    lastPointY = event.rawY.toInt()
                    false
                }
                MotionEvent.ACTION_UP->{
                    true
                }else-> true
            }
        }
    }

    private fun initRightItem() {
        //默认隐藏
        ll_main_temp.visibility = View.GONE
        rl_rate.visibility = View.GONE
        myListView.addClickItemCallBack(object:MyListView.ClickItemCallBack{
            override fun click1() {
                ll_main_temp.visibility = View.GONE
                rl_rate.visibility = View.GONE
            }

            override fun click2() {
                ll_main_temp.visibility = View.VISIBLE
                miv_temp.setBackgroundResource(R.mipmap.r1)
                rl_bottom_temp.visibility = View.VISIBLE
                rl_rate.visibility = View.GONE
            }

            override fun click3() {
                ll_main_temp.visibility = View.GONE
                rl_rate.visibility = View.VISIBLE
            }

            override fun click4() {
                ll_main_temp.visibility = View.VISIBLE
                miv_temp.setBackgroundResource(R.mipmap.r3)
                rl_bottom_temp.visibility = View.VISIBLE
                rl_rate.visibility = View.GONE
            }

            override fun click5() {
                ll_main_temp.visibility = View.VISIBLE
                miv_temp.setBackgroundResource(R.mipmap.r2)
                rl_bottom_temp.visibility = View.VISIBLE
                rl_rate.visibility = View.GONE
            }
        })
    }
    var needUpdateAdapter : Boolean = true
    private fun initRecyclerView() {
        mRvImageAdapter = RvImageAdapter(dataList)
        mRvImageAdapter.listener = object : RvImageAdapter.OnItemClickCallBack{
            override fun onItemClick(pos: Int) {
                Log.i(TAG,"onItemClick pos ... $pos")
                for (i in dataList.indices) {
                    dataList[i].isShow = i==pos
                }
                mRvImageAdapter.notifyDataSetChanged()
                needUpdateAdapter = false
                banner.stop()
                banner.currentItem = pos + 1
                banner.start()
            }
        }
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.HORIZONTAL
        rv_second.layoutManager = llm
        rv_second.adapter = mRvImageAdapter
    }


    private fun initBanner() {
        banner.adapter = MyImageAdapter(imageDataList)
        banner.setDelayTime(10000)
        banner.addOnPageChangeListener(object:OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
//                Log.i(TAG,"banner onPageSelected pos ... $position")
                if(needUpdateAdapter){
                    for (i in dataList.indices) {
                        dataList[i].isShow = i==position
                    }
                    mRvImageAdapter.notifyDataSetChanged()
                    rv_second.smoothScrollToPosition(position)
                }
                needUpdateAdapter = true
            }
        })
        banner.start()
    }

    private fun addListener() {
        iv_back.setOnClickListener{
            startActivity(Intent(this,IndexActivity::class.java))
            finish()
        }
    }

    /**
     * 初始化跑马灯view
     */
    private fun initMarqueeView() {
        val marqueeList: List<String> =
                listOf(
                        "秉承 笃守诚信,创造卓越 的核心价值观",
                        "打造一流数字生态银行",
                        "让金融为美好生活创造价值",
                        "购买国债安全理财 绿色金融共创美好生活")
        val marqueeFactory = SimpleMF<String>(this)
        marqueeFactory.data = marqueeList
//        simpleMarqueeView.setMarqueeFactory(marqueeFactory as Nothing)
//        building.simpleMarqueeView.startFlipping()
    }


    override fun onStart() {
        super.onStart()
        simpleMarqueeView.startFlipping()
    }

    override fun onStop() {
        super.onStop()
        simpleMarqueeView.stopFlipping()
    }

    override fun onDestroy() {
        super.onDestroy()
        rollview.clear()
    }
    override fun onPause() {
        super.onPause()
        GSYVideoManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        GSYVideoManager.onResume()
    }

}
