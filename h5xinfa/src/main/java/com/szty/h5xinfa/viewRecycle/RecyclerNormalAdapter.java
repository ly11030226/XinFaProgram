package com.szty.h5xinfa.viewRecycle;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.szty.h5xinfa.R;
import com.szty.h5xinfa.ui.IndexActivity;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerNormalAdapter extends RecyclerView.Adapter {
    private final static String TAG = "RecyclerBaseAdapter";

    private List<String> itemDataList = null;
    private Context context = null;
    private IndexActivity.PlayCompleteCallBack callBack;

    public RecyclerNormalAdapter(Context context, List<String> itemDataList, IndexActivity.PlayCompleteCallBack callBack) {
        this.itemDataList = itemDataList;
        this.context = context;
        this.callBack = callBack;
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
            boolean isOne = itemDataList.size() == 1;
            String url = itemDataList.get(position % itemDataList.size());
            Log.i(TAG,"onBindViewHolder itemDataList size ... "+itemDataList.size());
            Log.i(TAG,"onBindVieHolder url ... "+url);
            recyclerItemViewHolder.onBind(isOne,position, url,callBack);
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

    public void setListData(List<String> data) {
        itemDataList = data;
        notifyDataSetChanged();
    }

    public int getItemDataSize() {
        return itemDataList.size();
    }
}
