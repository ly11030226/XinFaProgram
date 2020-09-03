package com.szty.h5xinfa.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gongwen.marqueen.MarqueeFactory
import com.gongwen.marqueen.SimpleMF
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import com.szty.h5xinfa.R
import com.szty.h5xinfa.viewRecycle.MyLinearLayoutManager
import com.szty.h5xinfa.viewRecycle.RecyclerNormalAdapter
import com.szty.h5xinfa.viewRecycle.ScrollHelper
import kotlinx.android.synthetic.main.activity_index.*
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference


class IndexActivity : AppCompatActivity() {

    companion object {
        val TAG: String = "IndexActivity"
        const val AUTO_PLAY: Int = 0x11
        const val PLAY_DURATION: Long = 10 * 1000

        private class MyHandler(activity: IndexActivity) : Handler() {

            private val a: WeakReference<IndexActivity> = WeakReference(activity)
            override fun handleMessage(msg: Message) {
                if (a.get() == null) {
                    return
                }
                val indexActivity = a.get()
                when {
                    (msg.what == AUTO_PLAY) -> {
                        val pos: Int = msg.obj as Int
                        a.get()?.recyclerview?.smoothScrollToPosition(pos + 1)
                    }
                    else -> {
                    }
                }
            }
        }
    }


    private val spdPath = "szty/spd/first"
    private var dataList = ArrayList<String>()
    private val mLayoutManager: MyLinearLayoutManager = MyLinearLayoutManager(this)
    private lateinit var mRecyclerViewAdapter: RecyclerNormalAdapter
    private val mScrolHelper: ScrollHelper = ScrollHelper(R.id.video_player, R.id.iv_item)
    private lateinit var handler: MyHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        building = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_index)
        try {
            handler = MyHandler(this)
            createSpdFolderIfNeed()
            initView()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                initData()
            }
            addListener()
            //第一张是图片 启动定时翻页
            setDataIfFirstIsImage()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setDataIfFirstIsImage() {
        if (dataList.size > 0) {
            val firstItem = dataList[0];
            if (!firstItem.contains(".mp4")) {
                val msg = handler.obtainMessage();
                msg.what = AUTO_PLAY
                msg.obj = 0
                handler.sendMessageDelayed(msg, PLAY_DURATION)
            }
        }
    }

    private fun addListener() {
        iv_jump.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
            finish()
        }
    }

    private fun initView() {
        initMarqueeView()
        loadGif()
        initRecyclerView()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initData() {
        Log.i(TAG, "initData list size ... ${dataList.size}")
        mRecyclerViewAdapter = RecyclerNormalAdapter(this, dataList, object : PlayCompleteCallBack {
            override fun playComplete(pos: Int) {
                Log.i(TAG, "playComplete pos ... $pos")
                recyclerview.smoothScrollToPosition(pos + 1)
            }
        })
        recyclerview.adapter = mRecyclerViewAdapter
        //自定播放帮助类
        recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var firstVisibleItem: Int = 0
            var lastVisibleItem: Int = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.i(TAG, "onScrolled")
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition()
                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition()
                mScrolHelper.onScroll(firstVisibleItem, lastVisibleItem, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                try {
                    Log.i(TAG, "onScrollStateChanged")
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && firstVisibleItem == lastVisibleItem) {
                        handler.removeMessages(AUTO_PLAY)
                        playVideo(mScrolHelper, firstVisibleItem)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun initRecyclerView() {
        recyclerview.layoutManager = mLayoutManager
        recyclerview.setHasFixedSize(true)
        mLayoutManager.orientation = RecyclerView.HORIZONTAL
        val psh = PagerSnapHelper()
        psh.attachToRecyclerView(recyclerview)
    }

    private fun loadGif() {
        Glide.with(this).load(R.mipmap.ck).into(iv_jump)
    }

    /**
     * 初始化跑马灯view
     */
    private fun initMarqueeView() {
        val marqueeList: List<String> =
                listOf(
                        "秉承笃守诚信，创造卓越的核心价值观",
                        "打造一流数字生态银行",
                        "让金融为美好生活创造价值",
                        "购买国债安全理财 绿色金融共创美好生活")
        val marqueeFactory = SimpleMF<String>(this)
        marqueeFactory.data = marqueeList
        val mf = marqueeFactory as MarqueeFactory<TextView,String>
//        simpleMarqueeView.setMarqueeFactory(mf)
//        building.simpleMarqueeView.startFlipping()
        simpleMarqueeView.visibility = View.GONE
    }

    private fun createSpdFolderIfNeed() {
        val file = getExternalFilesDir(spdPath)
        if (file!!.exists()) {
            /**
             * 读Android/data/packname/szty/spd之前清空dataList
             */
            var number = if (file.list() == null) {
                0
            } else {
                file.list()!!.size
            }
            if (number >= 1) {
                dataList.clear()
                val temp = file.list()!!.asList()
//                var newList = ArrayList<String>()
                var path: String
                for (i in temp.indices) {
                    val name = temp[i]
                    path = "${file.absolutePath}${File.separator}$name"
                    val childFile = File(path)
                    if (childFile.exists()) {
                        val uri = when {
                            (name.contains(".mp4")) -> {
                                childFile.toURI().toString()
                            }
                            else -> {
                                path
                            }
                        }
                        dataList.add(uri)
                        Log.i(TAG, "添加文件 ... $uri")
//                        newList.add(uri)
                    }
                }
//                dataList.addAll(newList)
            } else {
                copyTestJpg(file)
            }
        } else {
            file.mkdirs()
            copyTestJpg(file)
        }
    }

    private fun copyTestJpg(file: File) {
        val input = this.assets.open("test.jpg");
        val path = "${file.absoluteFile}/test.jpg"
        Log.i(TAG, "copyTestJpg ... $path")
        val outputStream = FileOutputStream(path)
        var bytes = ByteArray(1024)
        var len: Int = 0
        while (true) {
            len = input.read(bytes)
            if (len == -1) {
                break;
            } else {
                outputStream.write(bytes, 0, len)
            }
        }
        input.close()
        outputStream.close()
        dataList.add(path)
    }

    private fun playVideo(scrollHelper: ScrollHelper, pos: Int) {
        var mGSYBaseVideoPlayer = mLayoutManager.getChildAt(0)?.findViewById<GSYBaseVideoPlayer>(R
                .id.video_player)
        var visiableState = mGSYBaseVideoPlayer!!.visibility
        if (visiableState == View.VISIBLE) {
            scrollHelper.handleHavePagerSnapHelper(mGSYBaseVideoPlayer)
        } else {
            val msg: Message = Message.obtain()
            msg.what = AUTO_PLAY
            msg.obj = pos
            handler.sendMessageDelayed(msg, PLAY_DURATION)
            releaseVideoAndNotify()
            scrollHelper.releaseVideo()
        }
    }


    private fun releaseVideoAndNotify() {
        GSYVideoManager.releaseAllVideos()
        mRecyclerViewAdapter.notifyDataSetChanged()
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
        handler.removeCallbacksAndMessages(null)
    }

    override fun onPause() {
        super.onPause()
        GSYVideoManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        GSYVideoManager.onResume()
    }

    /**
     * 视频播放完毕回调接口，用来跳转到下一个视频
     */
    interface PlayCompleteCallBack {
        fun playComplete(pos: Int)
    }

}
