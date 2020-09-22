package com.ads.clientconnection.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ads.clientconnection.R;
import com.ads.clientconnection.base.Constant;
import com.ads.clientconnection.entity.ImageAndVideoEntity;
import com.ads.clientconnection.ui.resourceManager.ResourceManagerActivity;
import com.ads.clientconnection.view.MyItemTouchHelperCallBack;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 显示播放文件列表的Adapter
 *
 * @author Ly
 */
public class ShowFileEntityAdapter extends RecyclerView.Adapter<ShowFileEntityAdapter.MyViewHolder> implements MyItemTouchHelperCallBack.ItemTouchHelperCallBackListener {

    private static final String TAG = "ShowFileEntityAdapter";
    private List<ImageAndVideoEntity.FileEntity> mData;
    private String mWhoUse;
    private Context context;

    public ShowFileEntityAdapter(Context context, List<ImageAndVideoEntity.FileEntity> mData, String whoUse) {
        this.context = context;
        this.mData = mData;
        this.mWhoUse = whoUse;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_show_fileentity, parent, false);
        MyViewHolder mvh = new MyViewHolder(v);
        return mvh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ImageAndVideoEntity.FileEntity data = mData.get(position);
        String type = data.getFormat();
        if ("图片".equalsIgnoreCase(type)) {
            holder.ivTitle.setImageResource(R.mipmap.logo_img);
            holder.tvTimeRemind.setVisibility(View.GONE);
            holder.tvTime.setVisibility(View.GONE);
        } else {
            holder.ivTitle.setImageResource(R.mipmap.logo_video);
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvTimeRemind.setVisibility(View.VISIBLE);
            holder.tvTime.setText(data.getPlayTime());
        }

        holder.tvName.setText(data.getName());
        holder.tvSize.setText(data.getSize());
        holder.tvStay.setText(data.getTime() + "秒");

        holder.llMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                if (Constant.RES_LIST_USE_ADAPTER.equals(mWhoUse)) {
                    intent = new Intent(Constant.ACTION_SHOW_SETTING_FROM_RESLIST);
                } else if (Constant.RES_MANAGER_USE_ADAPTER.equals(mWhoUse)) {
                    intent = new Intent(Constant.ACTION_SHOW_SETTING_FROM_RESMANAGER);
                }
                intent.putExtra(Constant.KEY_SHOW_SETTING, position);
                LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(intent);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (mData == null) {
            return 0;
        } else {
            return mData.size();
        }
    }


    @Override
    public void onItemDelete(int pos) {
        mData.remove(mData.get(pos));
        notifyItemRemoved(pos);
        //发送广播更新数据
        Intent i = new Intent(ResourceManagerActivity.ACTION_FILE_CHANGE);
        i.putExtra(Constant.KEY_UPDATE_LIST, (Serializable) mData);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    @Override
    public void onMove(int fromPos, int toPos) {
        Collections.swap(mData, fromPos, toPos);
        notifyItemMoved(fromPos, toPos);
        //发送广播更新数据
        Intent i = new Intent(ResourceManagerActivity.ACTION_FILE_CHANGE);
        i.putExtra(Constant.KEY_UPDATE_LIST, (Serializable) mData);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivTitle;
        private TextView tvName;
        private TextView tvSize;
        private TextView tvTime;
        private TextView tvStay;
        private TextView tvTimeRemind;
        private LinearLayout llMain;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTitle = itemView.findViewById(R.id.iv_title);
            tvName = itemView.findViewById(R.id.tv_name);
            tvSize = itemView.findViewById(R.id.tv_size);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvStay = itemView.findViewById(R.id.tv_stay);
            tvTimeRemind = itemView.findViewById(R.id.tv_time_remind);
            llMain = itemView.findViewById(R.id.ll_main);
        }
    }
}
