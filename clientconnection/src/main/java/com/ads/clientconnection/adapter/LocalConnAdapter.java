package com.ads.clientconnection.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ads.clientconnection.R;
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
        String name = device.getName().trim();
        if (name.contains("-PHONE")) {
            name = name.replace("-PHONE","").trim();
            holder.ivType.setImageResource(R.mipmap.device_android_logo);
        }else{
            holder.ivType.setImageResource(R.mipmap.device_pc_logo);
        }
        String ip = device.getIp();
        String os = device.getOs();
        int port = device.getPort();
        holder.tvName.setText(name);
        holder.tvIp.setText(ip);
        holder.tvPort.setText(port+"");
        holder.tvOs.setText(os);
        if (this.clickListener!=null) {
            holder.llMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onClick(v,position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
//        Log.i(TAG,"item count ... "+deviceList.size());
        return deviceList.size();
    }

    public void updateData(List<Device> newList){
        this.deviceList = newList;
        notifyDataSetChanged();
    }


    static class LocalViewHolder extends RecyclerView.ViewHolder{

        TextView tvName,tvIp,tvPort,tvOs;
        LinearLayout llMain;
        ImageView ivType;

        public LocalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvIp = itemView.findViewById(R.id.tv_ip);
            tvPort = itemView.findViewById(R.id.tv_port);
            tvOs = itemView.findViewById(R.id.tv_os);
            llMain = itemView.findViewById(R.id.ll_main);
            ivType = itemView.findViewById(R.id.iv_device_type);
        }
    }
    public void addClickListener(ClickListener clickListener){
        this.clickListener = clickListener;
    }


    public interface ClickListener{
        public void onClick(View v, int position);
    }
}
