package com.ads.clientconnection.ui.resourceManager;

import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.os.Build;
import android.view.View;

import com.marshalchen.ultimaterecyclerview.itemTouchHelper.ItemTouchHelperAdapter;
import com.marshalchen.ultimaterecyclerview.itemTouchHelper.ItemTouchHelperViewHolder;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class MyItemTouchHelperCallback extends ItemTouchHelper.Callback{
    public static final float ALPHA_FULL = 1.0f;

    private final ItemTouchHelperAdapter mAdapter;

    public MyItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // Enable drag and swipe in both directions
        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        final int swipeFlags = -1;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
        if (source.getItemViewType() != target.getItemViewType()) {
            return false;
        }
        // Notify the adapter of the move
        mAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
        // Notify the adapter of the dismissal
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        // Fade out the view as it is swiped out of the parent's bounds
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            View itemView = viewHolder.itemView;
            final float alpha = ALPHA_FULL - Math.abs(dX) / (float) itemView.getWidth();
            itemView.setAlpha(alpha);
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            // Let the view holder know that this item is being moved or dragged
            ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
            itemViewHolder.onItemSelected();
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setAlpha(ALPHA_FULL);
        // Tell the view holder it's time to restore the idle state
        ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
        itemViewHolder.onItemClear();
    }
}
