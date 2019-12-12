package com.ads.clientconnection.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ads.clientconnection.R;
import com.ads.clientconnection.entity.ImageAndVideoEntity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ShowResAdapter extends RecyclerView.Adapter<ShowResAdapter.ShowResViewHolder> {

    private List<ImageAndVideoEntity.FileEntity> fileEntities;
    private Context context;

    public ShowResAdapter(Context context,List<ImageAndVideoEntity.FileEntity> fileEntities) {
        this.fileEntities = fileEntities;
        this.context = context;
    }

    @NonNull
    @Override
    public ShowResViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_show_res,parent,false);
        ShowResViewHolder showResViewHolder = new ShowResViewHolder(view);
        return showResViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ShowResViewHolder holder, int position) {
        ImageAndVideoEntity.FileEntity fileEntity = fileEntities.get(position);
        String type = fileEntity.getFormat();
        if ("图片".equalsIgnoreCase(type)) {
            holder.ivTitle.setImageResource(R.mipmap.logo_img);
            holder.tvTimeRemind.setVisibility(View.INVISIBLE);
            holder.tvTime.setVisibility(View.INVISIBLE);
        }else{
            holder.ivTitle.setImageResource(R.mipmap.logo_video);
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvTimeRemind.setVisibility(View.VISIBLE);
            holder.tvTime.setText(fileEntity.getPlayTime());
        }
        holder.tvName.setText(fileEntity.getName());
        holder.tvSize.setText(fileEntity.getSize());
        //停留时长
        holder.tvStay.setText(fileEntity.getTime()+"秒");
    }

    @Override
    public int getItemCount() {
        return fileEntities.size();
    }

    class ShowResViewHolder extends RecyclerView.ViewHolder{

        private ImageView ivTitle;
        private TextView tvName,tvSize,tvTime,tvTimeRemind,tvStay;

        public ShowResViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTitle = itemView.findViewById(R.id.iv_title);
            tvName = itemView.findViewById(R.id.tv_name);
            tvSize = itemView.findViewById(R.id.tv_size);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvTimeRemind = itemView.findViewById(R.id.tv_time_remind);
            tvStay = itemView.findViewById(R.id.tv_stay);
        }
    }
}
