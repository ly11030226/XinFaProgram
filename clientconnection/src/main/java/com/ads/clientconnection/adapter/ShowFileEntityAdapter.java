package com.ads.clientconnection.adapter;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ads.clientconnection.R;
import com.ads.clientconnection.base.Constant;
import com.ads.clientconnection.entity.ImageAndVideoEntity;
import com.ads.clientconnection.utils.BaseUtils;
import com.ads.utillibrary.utils.ToastUtils;
import com.marshalchen.ultimaterecyclerview.SwipeableUltimateViewAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.swipe.SwipeLayout;

import java.util.List;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * 显示播放文件列表的Adapter
 */
public class ShowFileEntityAdapter extends SwipeableUltimateViewAdapter<ImageAndVideoEntity.FileEntity> {

    private OnClickListener onClickListener;
    private static final String TAG = "ShowFileEntityAdapter";
    private List<ImageAndVideoEntity.FileEntity> mData;
    private int afterMovePosition = -2;
    private String mWhoUse;

    public ShowFileEntityAdapter(List<ImageAndVideoEntity.FileEntity> mData,String whoUse) {
        super(mData);
        this.mData = mData;
        this.mWhoUse = whoUse;
    }


    public void addOnClickListner(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    protected void withBindHolder(UltimateRecyclerviewViewHolder holder, ImageAndVideoEntity.FileEntity data, int position) {
        super.withBindHolder(holder, data, position);
        try {
            String type = data.getFormat();
            if ("图片".equalsIgnoreCase(type)) {
                ((SVHolder) holder).ivTitle.setImageResource(R.mipmap.logo_img);
                ((SVHolder) holder).tvTimeRemind.setVisibility(View.GONE);
                ((SVHolder) holder).tvTime.setVisibility(View.GONE);
            }else{
                ((SVHolder) holder).ivTitle.setImageResource(R.mipmap.logo_video);
                ((SVHolder) holder).tvTime.setVisibility(View.VISIBLE);
                ((SVHolder) holder).tvTimeRemind.setVisibility(View.VISIBLE);
                ((SVHolder) holder).tvTime.setText(data.getPlayTime());
            }

            ((SVHolder) holder).tvName.setText(data.getName());
            ((SVHolder) holder).tvSize.setText(data.getSize());
            ((SVHolder) holder).tvStay.setText(data.getTime()+"秒");

            ((SVHolder) holder).llMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent intent = null;
                    if (Constant.RES_LIST_USE_ADAPTER.equals(mWhoUse)) {
                        intent = new Intent(Constant.ACTION_SHOW_SETTING_FROM_RESLIST);
                    }else if (Constant.RES_MANAGER_USE_ADAPTER.equals(mWhoUse)) {
                        intent = new Intent(Constant.ACTION_SHOW_SETTING_FROM_RESMANAGER);
                    }
                    intent.putExtra(Constant.KEY_SHOW_SETTING, position);
                    LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(intent);
                    return false;
                }
            });

            //        if (afterMovePosition == position) {
            //            MyLogger.i(TAG, "withBindHolder position ... " + position+"  afterMovePosition ... "+afterMovePosition);
            //            ((SVHolder) holder).swipeLayout.open();
            //            afterMovePosition = -2;
            //        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * the layout id for the normal data
     *
     * @return the ID
     */
    @Override
    protected int getNormalLayoutResId() {
        return SVHolder.layout;
    }

    /**
     * this is the Normal View Holder initiation
     *
     * @param view view
     * @return holder
     */
    @Override
    protected UltimateRecyclerviewViewHolder newViewHolder(final View view) {
        final SVHolder viewHolder = new SVHolder(view, true, this,mWhoUse);
        viewHolder.llMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    int position = viewHolder.getAdapterPosition();
                    onClickListener.onPressItem(position);
                }
            }
        });


        viewHolder.llDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    int position = viewHolder.getAdapterPosition();
                    onClickListener.onPressDelete(position);
                }
            }
        });

        //目前是向下移动
        viewHolder.llDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    int position = viewHolder.getAdapterPosition();
                    if (position == mData.size() - 1) {
                        ToastUtils.showToast(view.getContext(), BaseUtils.getStringByResouceId(R.string.is_end));
                        return;
                    }
                    afterMovePosition = position + 1;
                    swapPositions(position, position + 1);
                    notifyItemMoved(position, position + 1);
                    notifyItemRangeChanged(Math.min(position, position + 1), 2);

                }
            }
        });

        //目前是向上移动
        viewHolder.llEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    int position = viewHolder.getAdapterPosition();
                    if (position == 0) {
                        ToastUtils.showToast(view.getContext(), BaseUtils.getStringByResouceId(R.string.is_start));
                        return;
                    }
                    afterMovePosition = position - 1;
                    swapPositions(position - 1, position);
                    notifyItemMoved(position - 1, position);
                    notifyItemRangeChanged(Math.min(position, position - 1), 2);
                }
            }
        });
        return viewHolder;
    }

    @Override
    public SVHolder newFooterHolder(View view) {
        return new SVHolder(view, false, this,mWhoUse);
    }

    @Override
    public SVHolder newHeaderHolder(View view) {
        return new SVHolder(view, false, this,mWhoUse);
    }

    @Override
    public long generateHeaderId(int position) {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    protected void removeNotifyExternal(int pos) {
        closeItem(pos);
    }


    public static interface OnClickListener {
        void onPressItem(int position);

        void onPressDelete(int postion);

        void onPressDownload(int position);

        void onPressEdit(int position);

        void onLongClick(int position);
    }


    public static class SVHolder extends UltimateRecyclerviewViewHolder {
        public static final int layout = R.layout.item_show_fileentity;
        public LinearLayout llDelete, llDownload, llEdit, llExecute, llMain;
        public SwipeLayout swipeLayout;
        public TextView tvName, tvSize, tvTime,tvStay,tvTimeRemind;
        public ImageView ivTitle;
        ShowFileEntityAdapter adapter;
        String flag;

        public SVHolder(View itemView, boolean bind, ShowFileEntityAdapter adapter,String flag) {
            super(itemView);
            this.adapter = adapter;
            this.flag = flag;
            try {
                if (bind) {
                    llExecute = itemView.findViewById(R.id.ll_execute);
                    llMain = itemView.findViewById(R.id.ll_main);
                    llDelete = itemView.findViewById(R.id.ll_delete);
                    llDownload = itemView.findViewById(R.id.ll_download);
                    llEdit = itemView.findViewById(R.id.ll_edit);
                    swipeLayout = itemView.findViewById(R.id.recyclerview_swipe);
                    swipeLayout.setDrag(SwipeLayout.DragEdge.Right, llExecute);
                    swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);

                    tvName = itemView.findViewById(R.id.tv_name);
                    tvSize = itemView.findViewById(R.id.tv_size);
                    tvTime = itemView.findViewById(R.id.tv_time);
                    tvStay = itemView.findViewById(R.id.tv_stay);
                    ivTitle = itemView.findViewById(R.id.iv_title);
                    tvTimeRemind = itemView.findViewById(R.id.tv_time_remind);

                    if (Constant.RES_LIST_USE_ADAPTER.equals(flag)) {
                        llEdit.setVisibility(View.GONE);
                        llDownload.setVisibility(View.GONE);
                    }else if (Constant.RES_MANAGER_USE_ADAPTER.equals(flag)) {
                        llEdit.setVisibility(View.VISIBLE);
                        llDownload.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
