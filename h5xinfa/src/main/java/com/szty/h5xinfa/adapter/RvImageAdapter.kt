package com.szty.h5xinfa.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ads.utillibrary.utils.Tools
import com.szty.h5xinfa.R
import com.szty.h5xinfa.RvEntity
import com.szty.h5xinfa.databinding.ViewRvBinding
import com.szty.h5xinfa.view.ShowRvView

class RvImageAdapter(var dataList:List<RvEntity>) : RecyclerView.Adapter<RvImageAdapter.ViewHolder>() {
    val TAG : String = "RvImageAdapter"
    var listener: OnItemClickCallBack? = null

    class ViewHolder(var viewBinding: ViewRvBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(entity: RvEntity){
            viewBinding.ivImg.setBackgroundResource(entity.resId)
            viewBinding.ivBg.visibility = if(entity.isShow){
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
        var building : ViewRvBinding = ViewRvBinding.inflate(LayoutInflater.from(parent.context),
                parent,false)
        return ViewHolder(building)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewBinding.root.setOnClickListener{
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