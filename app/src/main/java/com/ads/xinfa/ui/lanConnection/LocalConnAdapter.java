package com.ads.xinfa.ui.lanConnection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ads.xinfa.R;
import com.ads.xinfa.base.MyLogger;
import com.gongw.remote.Device;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LocalConnAdapter extends RecyclerView.Adapter<LocalConnAdapter.LocalViewHolder> {
    private static final String TAG = "LocalConnAdapter";
    private Context context;
    private List<Device> deviceList;
    private ClickListener clickListener;

    public LocalConnAdapter(Context context, List<Device> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public LocalConnAdapter.LocalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_device_list,parent,false);
        LocalViewHolder localViewHolder = new LocalViewHolder(view);
        return localViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LocalConnAdapter.LocalViewHolder holder, int position) {
        Device device = deviceList.get(position);
        String result = "ip ... "+device.getIp()+":"+device.getPort()+"-"+device.getUuid();
        holder.tvContent.setText(result);
        if (this.clickListener!=null) {
            holder.tvContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onClick(v,position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        MyLogger.i(TAG,"item count ... "+deviceList.size());
        return deviceList.size();
    }

    public void updateData(List<Device> newList){
        this.deviceList = newList;
        notifyDataSetChanged();
    }


    static class LocalViewHolder extends RecyclerView.ViewHolder{

        TextView tvContent;

        public LocalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);
        }
    }
    public void addClickListener(ClickListener clickListener){
        this.clickListener = clickListener;
    }


    interface ClickListener{
        public void onClick(View v , int position);
    }
}
