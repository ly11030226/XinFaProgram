package com.szty.h5xinfa.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.ads.utillibrary.utils.Tools
import com.youth.banner.Banner

class ShowRvView : RelativeLayout {
    val TAG : String = "ShowRvView"
    var iv : ImageView = ImageView(context)
    constructor(context: Context) : this(context,null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs,
            defStyle) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        val size = MeasureSpec.getSize(widthMeasureSpec)
//        val mode = MeasureSpec.getMode(widthMeasureSpec)
//        Log.i(TAG, "建议宽度 ... $size 建议模式 ... $mode")
//        var childView = getChildAt(0)
//        measureChild(childView, widthMeasureSpec, heightMeasureSpec)
//        val childHeight = childView.measuredHeight
//        val childWidth = childView.measuredWidth
//        Log.i(TAG, "childWidth ... $childWidth childHeight ... $childHeight")
//        Log.i(TAG,"paddLeft ... $paddingLeft paddRight ... $paddingRight")
//        setMeasuredDimension(
//                paddingLeft+childWidth+paddingRight,
//                paddingTop+childHeight+paddingBottom)
    }
}

class MyImageView : androidx.appcompat.widget.AppCompatImageView{
    constructor(context: Context) : this(context,null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs,
            defStyle) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = MeasureSpec.getSize(widthMeasureSpec)
        val mode = MeasureSpec.getMode(widthMeasureSpec)
//        Log.i("ShowRvView","MyImageView size ... $size MyImageView mode ... $mode")
        val mWidth = measureWidth(widthMeasureSpec)
        val mHeight = mWidth*9/16
        setMeasuredDimension(mWidth,mHeight)
    }

    private fun measureWidth(widthMeasureSpec: Int):Int{
        val mWidth = MeasureSpec.getSize(widthMeasureSpec)
        return when(MeasureSpec.getMode(widthMeasureSpec)){
            MeasureSpec.EXACTLY -> mWidth
            MeasureSpec.AT_MOST -> mWidth/3
            MeasureSpec.UNSPECIFIED -> mWidth/3
            else -> 298
        }
    }
}

class MyConstraintLayout : ConstraintLayout{
    constructor(context: Context) : this(context,null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs,
            defStyle) {
    }

//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        val childViewLeft = getChildAt(0)
//        val childViewRight = getChildAt(1)
//        val leftHeight = childViewLeft.measuredHeight
//        Log.i("ShowRvView","leftHeight ... $leftHeight")
//        measureChild(childViewRight,widthMeasureSpec,MeasureSpec.makeMeasureSpec(leftHeight,
//                MeasureSpec.EXACTLY))
//    }

}
class MyLinearLayout : LinearLayout{
    constructor(context: Context) : this(context,null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs,
            defStyle) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        Log.i("ShowRvView","widthSize ... $widthSize widthMode ... $widthMode")
        val mHeight = widthSize * 9 /16
        val heightDp = Tools.pxToDp(context, mHeight.toFloat())
        Log.i("ShowRvView","heightDp ... $heightDp")
        setMeasuredDimension(widthSize,mHeight)
    }
}