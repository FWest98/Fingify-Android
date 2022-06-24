package com.fwest98.fingify.Helpers;


import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import lombok.Getter;
import lombok.Setter;

public class SortableRecyclerViewCallback extends ItemTouchHelper.Callback {
    private final ItemTouchHelperAdapter adapter;
    @Getter @Setter private boolean allowsSwipe = false;
    @Getter @Setter private boolean allowsDrag = false;

    public SortableRecyclerViewCallback(ItemTouchHelperAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {}

    @Override
    public boolean isLongPressDragEnabled() {
        return allowsDrag;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return allowsSwipe;
    }

    public interface ItemTouchHelperAdapter {
        void onItemMove(int fromPosition, int toPosition);
    }
}
