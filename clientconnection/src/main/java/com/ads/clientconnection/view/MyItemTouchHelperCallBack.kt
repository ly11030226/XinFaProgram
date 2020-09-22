package com.ads.clientconnection.view

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ads.clientconnection.R


class MyItemTouchHelperCallBack(
    private val context: Context,
    private val listener: ItemTouchHelperCallBackListener
) : ItemTouchHelper.Callback() {
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlag = ItemTouchHelper.END
        return makeMovementFlags(dragFlag, swipeFlag)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        listener.onMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        listener.onItemDelete(viewHolder.adapterPosition)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            //dX大于0时向右滑动，小于0向左滑动

            //dX大于0时向右滑动，小于0向左滑动
            val itemView: View = viewHolder.itemView //获取滑动的view

            val resources: Resources = context.resources
            val bitmap =
                BitmapFactory.decodeResource(resources, R.mipmap.icon_delete) //获取删除指示的背景图片

            val padding = 10 //图片绘制的padding

            val maxDrawWidth = 2 * padding + bitmap.width //最大的绘制宽度

            val paint = Paint()
            paint.color = ContextCompat.getColor(context,R.color.white_pressed)
            val x = Math.round(Math.abs(dX))
            val drawWidth =
                Math.min(x, maxDrawWidth) //实际的绘制宽度，取实时滑动距离x和最大绘制距离maxDrawWidth最小值

            val itemTop: Int = itemView.bottom - itemView.height //绘制的top位置

            //向右滑动
            if (dX > 0) {
                //根据滑动实时绘制一个背景
                c.drawRect(
                    itemView.left.toFloat(),
                    itemTop.toFloat(),
                    drawWidth.toFloat(), itemView.bottom.toFloat(), paint
                )
                //在背景上面绘制图片
                if (x > padding) { //滑动距离大于padding时开始绘制图片
                    //指定图片绘制的位置
                    val rect = Rect() //画图的位置
                    rect.left = itemView.getLeft() + padding
                    rect.top =
                        itemTop + (itemView.getBottom() - itemTop - bitmap.height) / 2 //图片居中
                    val maxRight: Int = rect.left + bitmap.width
                    rect.right = Math.min(x, maxRight)
                    rect.bottom = rect.top + bitmap.height
                    //指定图片的绘制区域
                    var rect1: Rect? = null
                    if (x < maxRight) {
                        rect1 = Rect() //不能再外面初始化，否则dx大于画图区域时，删除图片不显示
                        rect1.left = 0
                        rect1.top = 0
                        rect1.bottom = bitmap.height
                        rect1.right = x - padding
                    }
                    c.drawBitmap(bitmap, rect1, rect, paint)
                }
                //绘制时需调用平移动画，否则滑动看不到反馈
                itemView.translationX = dX
            } else {
                //如果在getMovementFlags指定了向左滑动（ItemTouchHelper。START）时则绘制工作可参考向右的滑动绘制，也可直接使用下面语句交友系统自己处理
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
            val alpha = 1.0f - Math.abs(dX) / itemView.width.toFloat()
            itemView.alpha = alpha
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }


    interface ItemTouchHelperCallBackListener {
        fun onItemDelete(pos: Int)
        fun onMove(fromPos: Int, toPos: Int)
    }

}