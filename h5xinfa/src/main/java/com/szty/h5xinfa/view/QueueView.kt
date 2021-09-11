package com.szty.h5xinfa.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.szty.h5xinfa.baoao.*

class QueueView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr:
Int = 0) :
        LinearLayout(context, attrs, defStyleAttr) {
    private val queue = mutableListOf<TextView>()
    private val colors = mutableListOf<Int>()
    private val contents = mutableListOf<String>()
    private var firstColor by Preference(KEY_FIRST_WINDOW, DEFAULT_FIRST_WINDOW)
    private var secondColor by Preference(KEY_SECOND_WINDOW, DEFAULT_SECOND_WINDOW)
    private var thirdColor by Preference(KEY_THIRD_WINDOW, DEFAULT_THIRD_WINDOW)
    private var forthColor by Preference(KEY_FORTH_WINDOW, DEFAULT_FORTH_WINDOW)
    private var textSize by Preference(KEY_TEXT_SIZE, DEFAULT_TEXT_SIZE)
    private var textColor by Preference(KEY_TEXT_COLOR, DEFAULT_TEXT_COLOR)


    init {
        colors.add(transformColor(firstColor))
        colors.add(transformColor(secondColor))
        colors.add(transformColor(thirdColor))
        colors.add(transformColor(forthColor))

        orientation = VERTICAL
        setBackgroundColor(Color.rgb(255, 255, 255))
        val iv = ImageView(context)
        bgBitmapDrawable?.let {
            iv.setImageDrawable(it)
        }
        val lp = LayoutParams(
                LayoutParams.MATCH_PARENT,
                150
        )
        iv.scaleType = ImageView.ScaleType.FIT_XY
        iv.layoutParams = lp
        addView(iv)
        val ll = LinearLayout(context)
        ll.orientation = VERTICAL
        ll.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 450)
        ll.weightSum = 4.0f
        addView(ll)
        for (i in 0..3) {
            val tv = TextView(context)
            tv.gravity = Gravity.CENTER
            tv.textSize = textSize
            tv.setTextColor(transformColor(textColor))
            val layoutParam = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f)
            tv.layoutParams = layoutParam
            tv.setBackgroundColor(colors[i])
            ll.addView(tv)
            queue.add(tv)
        }
    }

    fun setContent(str: String) {
        if (contents.size == queue.size) {
            contents.removeAt(contents.size - 1)
        }
        contents.add(0, str)
        for (i in 0 until contents.size) {
            queue[i].text = contents[i]
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val heightMeasure = MeasureSpec.makeMeasureSpec(600, MeasureSpec.EXACTLY)
        setMeasuredDimension(widthMeasureSpec, heightMeasure)
    }


}