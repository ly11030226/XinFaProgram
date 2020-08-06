package com.szty.h5xinfa.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ads.utillibrary.utils.Tools
import com.szty.h5xinfa.R
import com.szty.h5xinfa.RvEntity
import com.szty.h5xinfa.view.ShowRvView
import java.util.zip.Inflater

class RvImageAdapter(var dataList:List<RvEntity>) : RecyclerView.Adapter<RvImageAdapter.ViewHolder>() {
    val TAG : String = "RvImageAdapter"
    var listener: OnItemClickCallBack? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var root:ConstraintLayout = itemView.findViewById(R.id.root)
        var ivImg:ImageView = itemView.findViewById(R.id.iv_img)
        var ivBg:ImageView = itemView.findViewById(R.id.iv_bg)
        fun bind(entity: RvEntity){
            ivImg.setBackgroundResource(entity.resId)
            ivBg.visibility = if(entity.isShow){
                View.GONE
            }else{
                View.VISIBLE
            }
        }
    }

    public fun updateList(mDataList:List<RvEntity>){
        dataList = mDataList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_rv,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.root.setOnClickListener{
            listener?.onItemClick(position)
        }
        var entity = dataList[position]
        holder.bind(entity)
    }

    fun addOnItemClickCallBack(callBack: OnItemClickCallBack){
        listener = callBack
    }
    interface OnItemClickCallBack{
        fun onItemClick(pos:Int)
    }
}