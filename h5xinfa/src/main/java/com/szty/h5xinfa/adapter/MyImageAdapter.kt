package com.szty.h5xinfa.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.youth.banner.adapter.BannerAdapter

class MyImageAdapter(imageDataList: List<Int>?) : BannerAdapter<Int, MyImageAdapter.BannerViewHolder>
(imageDataList) {

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): BannerViewHolder {
        var iv = ImageView(parent?.context)
        //注意，必须设置为match_parent，这个是viewpager2强制要求的
        val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup
                .LayoutParams.MATCH_PARENT)
        iv.layoutParams = lp
        iv.scaleType = ImageView.ScaleType.CENTER_CROP
        return BannerViewHolder(iv)
    }

    override fun onBindView(holder: BannerViewHolder?, data: Int?, position: Int, size: Int) {
        holder?.itemView?.setBackgroundResource(data!!)
    }
    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}