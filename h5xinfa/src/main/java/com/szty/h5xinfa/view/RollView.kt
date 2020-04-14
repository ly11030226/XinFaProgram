package com.szty.h5xinfa.view

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import com.szty.h5xinfa.R
import com.szty.h5xinfa.databinding.ViewRollviewBinding
import com.szty.h5xinfa.ui.IndexActivity
import com.szty.h5xinfa.ui.SecondActivity
import com.szty.h5xinfa.util.BaseUtils
import com.szty.h5xinfa.viewRecycle.MyLinearLayoutManager
import com.szty.h5xinfa.viewRecycle.RecyclerNormalAdapter
import com.szty.h5xinfa.viewRecycle.ScrollHelper
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import kotlin.math.min

class RollView : RelativeLayout {
    val TAG: String = "RollView"
    private var building: ViewRollviewBinding? = null
    private var rollViewWidth: Int = 0
    private var rollViewHeight :Int = 0
    private val mLayoutManager : MyLinearLayoutManager
    private lateinit var mRecyclerViewAdapter: RecyclerNormalAdapter
    private val mScrolHelper: ScrollHelper = ScrollHelper(R.id.video_player,R.id.iv_item)

    companion object{
        const val PLAY_DURATION : Long = 10 * 1000
        const val AUTO_PLAY = 0X22
        const val SECOND_PATH = "szty/spd/second"

        private class MyHandler(activity: SecondActivity) : Handler() {
            private val a : WeakReference<SecondActivity> = WeakReference(activity)
            override fun handleMessage(msg: Message) {
                if(a.get() ==null){
                    return
                }
                val secondActivity = a.get()
                when{
                    (msg.what == RollView.AUTO_PLAY) ->{
                        val pos : Int = msg.obj as Int
                        secondActivity?.building!!.rollview.smoothScrollToPosition(pos)
                    }else ->{}
                }
            }
        }
    }
    var dataList = ArrayList<String>()
    private var handler:MyHandler
    constructor(context: Context) : this(context,null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs,
            defStyle) {
        building = ViewRollviewBinding.inflate(LayoutInflater.from(context), this, true)
        mLayoutManager = MyLinearLayoutManager(context)
        val a = context as SecondActivity
        handler = MyHandler(a)
        createFolderIfNeed()
        initRecyclerView()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initData()
        }
        setDataIfFirstIsImage()
    }
    private fun setDataIfFirstIsImage() {
        if (dataList.size > 0) {
            val firstItem = dataList[0];
            if (!firstItem.contains(".mp4")) {
                val msg = handler.obtainMessage();
                msg.what = IndexActivity.AUTO_PLAY
                msg.obj = 0
                handler.sendMessageDelayed(msg, IndexActivity.PLAY_DURATION)
            }
        }
    }

    private fun smoothScrollToPosition(pos:Int){
        building!!.rv.smoothScrollToPosition(pos+1)
    }

    private fun createFolderIfNeed() {
        var file = context.getExternalFilesDir(SECOND_PATH)
        if(file!!.exists()){
            var number = if(file.list() == null){0} else{file.list()!!.size}
            if (number >= 1){
                dataList.clear()
                val temp = file.list()!!.asList()
                var newList = ArrayList<String>()
                var path:String
                for (i in temp.indices) {
                    val name = temp[i]
                    path = "${file.absolutePath}${File.separator}$name"
                    val childFile = File(path)
                    if(childFile.exists()){
                        val uri = when{
                            (name.contains(".mp4")) ->{
                                childFile.toURI().toString()
                            }else ->{
                                path
                            }
                        }
                        newList.add(uri)
                    }
                }
                dataList.addAll(newList)
            }else{
                copyTestJpg(file)
            }
        }else{
            file.mkdirs()
            copyTestJpg(file)
        }
    }
    private fun copyTestJpg(file:File) {
        Log.i(TAG,"start copy test jpg")
        val input = context.assets.open("test.jpg");
        val path = "${file.absoluteFile}/test.jpg"
        val outputStream = FileOutputStream(path)
        var bytes = ByteArray(1024)
        var len :Int = 0
        while (true) {
            len = input.read(bytes)
            if(len==-1){
                break;
            }else{
                outputStream.write(bytes, 0, len)
            }
        }
        input.close()
        outputStream.close()
        dataList.add(path)
    }

    private fun initRecyclerView() {
        building!!.rv.layoutManager = mLayoutManager
        building!!.rv.setHasFixedSize(true)
        mLayoutManager.orientation = RecyclerView.HORIZONTAL
        val psh = PagerSnapHelper()
        psh.attachToRecyclerView(building!!.rv)
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initData() {
        mRecyclerViewAdapter = RecyclerNormalAdapter(context,dataList,object: IndexActivity
        .PlayCompleteCallBack {
            override fun playComplete(pos: Int) {
                building!!.rv.smoothScrollToPosition(pos+1)
            }
        })
        building!!.rv.adapter = mRecyclerViewAdapter
        //自定播放帮助类
        building!!.rv.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            var firstVisibleItem : Int = 0
            var lastVisibleItem : Int = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition()
                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition()
                mScrolHelper.onScroll(firstVisibleItem,lastVisibleItem,dx,dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                try {
                    if(newState == RecyclerView.SCROLL_STATE_IDLE && firstVisibleItem == lastVisibleItem){
                        handler.removeMessages(RollView.AUTO_PLAY)
                        playVideo(mScrolHelper, firstVisibleItem)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })

    }
    private fun playVideo(scrollHelper: ScrollHelper,pos: Int){
        var mGSYBaseVideoPlayer = mLayoutManager.getChildAt(0)?.findViewById<GSYBaseVideoPlayer>(R
                .id.video_player)
        var visiableState = mGSYBaseVideoPlayer!!.visibility
        if(visiableState == View.VISIBLE){
            scrollHelper.handleHavePagerSnapHelper(mGSYBaseVideoPlayer)
        }else{
            val msg : Message = handler.obtainMessage()
            msg.what = RollView.AUTO_PLAY
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
    public fun clear(){
        handler.removeCallbacksAndMessages(null)
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = BaseUtils.getScreenWidth(context)
        rollViewWidth = width / 2
        rollViewHeight = rollViewWidth*9/16
//        val width = MeasureSpec.getSize(widthMeasureSpec)
        val tempHeight = MeasureSpec.getSize(heightMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        Log.i(TAG,"建议的高度是 ... $tempHeight 建议的模式是 ... $modeHeight")
        Log.i(TAG,"rollViewWidth ... $rollViewWidth rollViewHeight ... $rollViewHeight")
        var childHeight = 0
        var childWidth = 0
        for (i in 0 until childCount) {
            var childView = getChildAt(i)
            measureChild(
                    childView,
                    widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(rollViewHeight,MeasureSpec.EXACTLY)
            )
            childHeight = childView.measuredHeight
            childWidth = childView.measuredWidth
            Log.i(TAG,"childWidth ... $childWidth childHeight ... $childHeight")
        }
        setMeasuredDimension(rollViewWidth,rollViewHeight)

    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        var childView = getChildAt(0)
    }
}

/**
 * 自定义高度的RecyclerView
 * 为了适应16：9
 */
class MyRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {
    val TAG : String = "MyRecyclerView"
    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        val mWidth = MeasureSpec.getSize(widthSpec)
        val mHeight =  mWidth*9/16
        Log.i(TAG,"建议的宽度 ... $mWidth 算出的高度 ... $mHeight")
//        for(i in 0 until childCount){
//            measureChild(getChildAt(i),widthSpec,MeasureSpec.makeMeasureSpec(mHeight,MeasureSpec.EXACTLY))
//        }
        setMeasuredDimension(mWidth,mHeight)
    }
}

