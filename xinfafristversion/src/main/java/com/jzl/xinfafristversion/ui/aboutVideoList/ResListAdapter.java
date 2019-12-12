package com.jzl.xinfafristversion.ui.aboutVideoList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jzl.xinfafristversion.R;
import com.jzl.xinfafristversion.bean.MyBean;
import com.jzl.xinfafristversion.view.ResLoopView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class ResListAdapter extends RecyclerView.Adapter {
    private final static String TAG = "ResListAdapter";

    private List<MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean> itemDataList = null;
    private Context context = null;
    private ResLoopView.PlayCompleteCallBack callBack;

    public ResListAdapter(Context context,
                          List<MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean> itemDataList,
                          ResLoopView.PlayCompleteCallBack callback) {
        this.itemDataList = itemDataList;
        this.context = context;
        this.callBack = callback;
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
            MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean
                    fileBean = itemDataList.get(position % itemDataList.size());
            recyclerItemViewHolder.onBind(position, fileBean, callBack);
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

    public void setListData(List<MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean> data) {
        itemDataList = data;
        notifyDataSetChanged();
    }

    public int getItemDataSize() {
        return itemDataList.size();
    }
}
