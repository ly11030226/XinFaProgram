package com.ads.xinfa.ui.aboutVideoList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ads.xinfa.R;
import com.ads.xinfa.entity.ImageAndVideoEntity;
import com.ads.xinfa.ui.displayVideoAndImage.DisplayVideoAndImageFragment;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerNormalAdapter extends RecyclerView.Adapter {
    private final static String TAG = "RecyclerBaseAdapter";

    private List<ImageAndVideoEntity.FileEntity> itemDataList = null;
    private Context context = null;
    private DisplayVideoAndImageFragment.PlayCompleteCallBack callBack;

    public RecyclerNormalAdapter(Context context, List<ImageAndVideoEntity.FileEntity> itemDataList,DisplayVideoAndImageFragment.PlayCompleteCallBack callBack) {
        this.itemDataList = itemDataList;
        this.context = context;
        this.callBack = callBack;
    }

    public void notifyData(List<ImageAndVideoEntity.FileEntity> newList){
        if (newList!=null) {
            int previousSize = itemDataList.size();
            itemDataList.clear();
            notifyItemRangeChanged(0,previousSize);
            itemDataList.addAll(newList);
            notifyItemRangeInserted(0,newList.size());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_video_item_normal, parent, false);
        final RecyclerView.ViewHolder holder = new RecyclerItemNormalHolder(context, v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        RecyclerItemNormalHolder recyclerItemViewHolder = (RecyclerItemNormalHolder) holder;
        recyclerItemViewHolder.setRecyclerBaseAdapter(this);
        if (itemDataList.size() > 0) {
            ImageAndVideoEntity.FileEntity videoModel = itemDataList.get(position % itemDataList.size());
            recyclerItemViewHolder.onBind(position, videoModel,callBack);
        }
    }

    @Override
    public int getItemCount() {
        return itemDataList.size() < 2 ? 1 : Integer.MAX_VALUE;
    }


    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    public void setListData(List<ImageAndVideoEntity.FileEntity> data) {
        itemDataList = data;
        notifyDataSetChanged();
    }

    public int getItemDataSize() {
        return itemDataList.size();
    }
}
