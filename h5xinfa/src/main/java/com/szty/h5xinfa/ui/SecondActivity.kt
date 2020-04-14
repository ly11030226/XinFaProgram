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
import com.szty.h5xinfa.databinding.ActivitySecondBinding
import com.szty.h5xinfa.view.MyListView
import com.youth.banner.listener.OnPageChangeListener
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
    public lateinit var building: ActivitySecondBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        building = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(building.root)
        try {
            initMarqueeView()
            addListener()
            initBanner()
            initRecyclerView()
            initRightItem()
            addTouchListener()
            IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);
        } catch (e: Exception) {
            e.printStackTrace()
            e.printStackTrace()
        }
    }


    private fun addTouchListener() {
        var lastPointX : Int
        var lastPointY : Int
        building.ivTuo.setOnTouchListener{ view, event->
            lastPointX = building.clContent.x.toInt()
            lastPointY = building.clContent.y.toInt()
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
                        var l = building.clContent.left + xDis
                        var t = building.clContent.top + yDis
                        var r = building.clContent.right + xDis
                        var b = building.clContent.bottom + yDis
//                        if(l<0){
//                            l = 0
//                            r = l + building.clContent.width
//                        }
//                        if(){
//
//                        }
                        building.clContent.layout(l,t,r,b)
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
        building.llMainTemp.visibility = View.GONE
        building.rlRate.visibility = View.GONE
        building.myListView.addClickItemCallBack(object:MyListView.ClickItemCallBack{
            override fun click1() {
                building.llMainTemp.visibility = View.GONE
                building.rlRate.visibility = View.GONE
            }

            override fun click2() {
                building.llMainTemp.visibility = View.VISIBLE
                building.mivTemp.setBackgroundResource(R.mipmap.r1)
                building.rlBottomTemp.visibility = View.VISIBLE
                building.rlRate.visibility = View.GONE
            }

            override fun click3() {
                building.llMainTemp.visibility = View.GONE
                building.rlRate.visibility = View.VISIBLE
            }

            override fun click4() {
                building.llMainTemp.visibility = View.VISIBLE
                building.mivTemp.setBackgroundResource(R.mipmap.r3)
                building.rlBottomTemp.visibility = View.VISIBLE
                building.rlRate.visibility = View.GONE
            }

            override fun click5() {
                building.llMainTemp.visibility = View.VISIBLE
                building.mivTemp.setBackgroundResource(R.mipmap.r2)
                building.rlBottomTemp.visibility = View.VISIBLE
                building.rlRate.visibility = View.GONE
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
                building.banner.stop()
                building.banner.currentItem = pos + 1
                building.banner.start()
            }
        }
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.HORIZONTAL
        building.rvSecond.layoutManager = llm
        building.rvSecond.adapter = mRvImageAdapter
    }


    private fun initBanner() {
        building.banner.adapter = MyImageAdapter(imageDataList)
        building.banner.setDelayTime(10000)
        building.banner.addOnPageChangeListener(object:OnPageChangeListener{
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
                    building.rvSecond.smoothScrollToPosition(position)
                }
                needUpdateAdapter = true
            }
        })
        building.banner.start()
    }

    private fun addListener() {
        building.ivBack.setOnClickListener{
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
        building.simpleMarqueeView.setMarqueeFactory(marqueeFactory)
//        building.simpleMarqueeView.startFlipping()
    }


    override fun onStart() {
        super.onStart()
        building.simpleMarqueeView.startFlipping()
    }

    override fun onStop() {
        super.onStop()
        building.simpleMarqueeView.stopFlipping()
    }

    override fun onDestroy() {
        super.onDestroy()
        building.rollview.clear()
    }

}
