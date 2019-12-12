package com.ads.clientconnection.ui.resourceManager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ads.clientconnection.R;
import com.ads.clientconnection.entity.PlayListEntity;
import com.ads.clientconnection.utils.BaseUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ListNameAdapter extends RecyclerView.Adapter<ListNameAdapter.ListNameViewHolder>{

    private List<PlayListEntity> fileEntityList;
    private Context context;
    private ButtonPressListener buttonPressListener;

    public ListNameAdapter(List<PlayListEntity> fileEntityList, Context context) {
        this.fileEntityList = fileEntityList;
        this.context = context;
    }

    @NonNull
    @Override
    public ListNameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_play_list_name,parent,false);
        ListNameViewHolder listNameViewHolder = new ListNameViewHolder(view);
        return listNameViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ListNameViewHolder holder, int position) {
        PlayListEntity playListEntity = fileEntityList.get(position);
        String name = playListEntity.getName();
        if (playListEntity.isChoice()) {
            holder.tvName.setTextSize(BaseUtils.spToPx(context,7));
            holder.tvName.setTextColor(ContextCompat.getColor(context,R.color.blue_main));
            holder.line.setBackgroundResource(R.color.blue_main);
        }else{
            holder.tvName.setTextSize(BaseUtils.spToPx(context,6));
            holder.tvName.setTextColor(ContextCompat.getColor(context,R.color.play_list_name));
            holder.line.setBackgroundResource(R.color.play_list_line);
        }
        //最后一位显示加号
        if (position == fileEntityList.size()-1) {
            holder.rlAdd.setVisibility(View.VISIBLE);
        }else{
            holder.rlAdd.setVisibility(View.GONE);
        }
        holder.tvName.setText(name);
        if (buttonPressListener!=null) {
            holder.rlAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonPressListener.pressAddButton();
                }
            });
            holder.rlName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonPressListener.pressNameButton(holder.rlName,position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return fileEntityList.size();
    }

    class ListNameViewHolder extends RecyclerView.ViewHolder{
        private View line;
        private TextView tvName;
        private RelativeLayout rlAdd,rlName;

        public ListNameViewHolder(@NonNull View itemView) {
            super(itemView);
            line = itemView.findViewById(R.id.view_line);
            tvName = itemView.findViewById(R.id.tv_list_name);
            rlAdd = itemView.findViewById(R.id.rl_add);
            rlName = itemView.findViewById(R.id.rl_list_name);
        }
    }

    public void setButtonPressListener(ButtonPressListener buttonPressListener){
        this.buttonPressListener = buttonPressListener;
    }
    public interface ButtonPressListener{
        public void pressNameButton(View view,int position);
        public void pressAddButton();
    }
}
