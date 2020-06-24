package com.parabola.newtone.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.Predicate;


public abstract class SimpleListAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>
        implements BaseAdapter<T> {

    private final SelectableList<T> items = new SelectableList<>();

    protected RecyclerView recyclerView;


    @Override
    public int getItemCount() {
        return items.size();
    }

    protected View inflateByViewType(Context context, int viewType, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(viewType, parent, false);
    }

    @Override
    public void add(T newItem) {
        items.add(newItem);
        notifyItemInserted(getItemCount() - 1);
    }

    @Override
    public void add(int position, T newItem) {
        items.add(position, newItem);
        notifyItemInserted(position);
    }

    @Override
    public void addAll(List<T> newItems) {
        items.addAll(newItems);
        notifyItemRangeInserted(getItemCount() - newItems.size(), newItems.size());
    }

    @Override
    public void invalidateItem(int position) {
        notifyItemChanged(position);
    }

    @Override
    public T get(int position) {
        return items.get(position);
    }

    @Override
    public List<T> getAll() {
        return items.getAsList();
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }


    protected boolean showSection;

    @Override
    public void setSectionEnabled(boolean showSection) {
        this.showSection = showSection;
    }

    @Override
    public void moveItem(int from, int to) {
        items.move(from, to);
        notifyItemMoved(from, to);
    }

    @Override
    public void replaceAll(List<T> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public void remove(int position) {
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemChanged(position);
    }

    @Override
    public void removeWithCondition(Predicate<T> condition) {
        for (int i = 0; i < items.size(); i++) {
            if (condition.test(items.get(i))) {
                remove(i);
            }
        }
    }


    private int contextSelectedPosition = -1;

    @Override
    public void clearContextSelected() {
        getContextSelectedPosition().ifPresent(position -> {
            contextSelectedPosition = -1;
            notifyItemChanged(position);
        });
    }

    @Override
    public boolean isContextSelected(int position) {
        return position == contextSelectedPosition;
    }

    @Override
    public void setContextSelected(int position) {
        if (position < 0 || position >= size())
            throw new IndexOutOfBoundsException("Position:  " + position + ". Must be between 0 and " + (size() - 1));

        contextSelectedPosition = position;
        notifyItemChanged(contextSelectedPosition);
    }

    @Override
    public OptionalInt getContextSelectedPosition() {
        if (contextSelectedPosition == -1) {
            return OptionalInt.empty();
        }

        return OptionalInt.of(contextSelectedPosition);
    }

    @Override
    public void clearSelected() {
        OptionalInt selectedItemIndex = items.getSelectedItemIndex();

        items.clearSelected();

        selectedItemIndex.ifPresent(this::notifyItemChanged);
    }

    @Override
    public boolean isSelected(int position) {
        OptionalInt oInt = items.getSelectedItemIndex();

        if (oInt.isPresent()) {
            return oInt.getAsInt() == position;
        } else {
            return false;
        }
    }

    @Override
    public void setSelected(int position) {
        OptionalInt oldSelectedPosition = items.getSelectedItemIndex();
        if (oldSelectedPosition.isPresent() && oldSelectedPosition.getAsInt() == position)
            return;

        items.setSelectedItemIndex(position);
        notifyItemChanged(position);

        if (oldSelectedPosition.isPresent()) {
            notifyItemChanged(oldSelectedPosition.getAsInt());
        }

    }

    @Override
    public void setSelectedCondition(Predicate<T> condition) {
        for (int i = 0; i < items.size(); i++) {
            if (condition.test(items.get(i))) {
                setSelected(i);
                return;
            }
        }
        clearSelected();
    }


    @Override
    public OptionalInt getSelectedPosition() {
        return items.getSelectedItemIndex();
    }


    protected OnItemClickListener onItemClickListener;

    @Override
    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }


    protected OnItemLongClickListener onItemLongClickListener;

    @Override
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        onItemLongClickListener = listener;
    }


    protected OnMoveItemListener onMoveItemListener;

    @Override
    public void setOnMoveItemListener(OnMoveItemListener listener) {
        onMoveItemListener = listener;
    }


    protected OnSwipeItemListener onSwipeItemListener;

    @Override
    public void setOnSwipeItemListener(OnSwipeItemListener listener) {
        onSwipeItemListener = listener;
    }

    @Override
    @CallSuper
    public void onBindViewHolder(VH holder, int position) {
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(holder.getAdapterPosition());
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onItemLongClick(holder.getAdapterPosition());
                return true;
            }
            return false;
        });
    }

    protected RecyclerView.LayoutManager getLayoutManager() {
        if (recyclerView == null)
            return null;
        return recyclerView.getLayoutManager();
    }

    @Override
    @CallSuper
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        recyclerView.setHasFixedSize(true);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    protected final ItemTouchHelper touchHelper = new ItemTouchHelper(
            new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {

                boolean isFirst = true;
                int startPosition;
                int lastPosition;
                int lastActionState = ItemTouchHelper.ACTION_STATE_IDLE;

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    int oldPosition = viewHolder.getAdapterPosition();
                    int newPosition = target.getAdapterPosition();

                    moveItem(oldPosition, newPosition);

                    if (isFirst) {
                        isFirst = false;
                        startPosition = oldPosition;
                    }
                    lastPosition = newPosition;

                    return true;
                }

                @Override
                public boolean isLongPressDragEnabled() {
                    return onMoveItemListener != null;
                }

                @Override
                public boolean isItemViewSwipeEnabled() {
                    return onSwipeItemListener != null;
                }

                @Override
                public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                    super.onSelectedChanged(viewHolder, actionState);

                    if (actionState == ItemTouchHelper.ACTION_STATE_IDLE
                            && lastActionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                        isFirst = true;
                        if (onMoveItemListener != null) {
                            onMoveItemListener.onMoveItem(startPosition, lastPosition);
                        }
                        lastPosition = startPosition;
                    }

                    lastActionState = actionState;
                }

                @Override
                public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                        @NonNull RecyclerView.ViewHolder viewHolder,
                                        float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        float alpha = 1 - (Math.abs(dX) / recyclerView.getWidth());
                        viewHolder.itemView.setAlpha(alpha);
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int oldPosition = viewHolder.getAdapterPosition();
                    if (onSwipeItemListener != null) {
                        onSwipeItemListener.onSwipeItem(oldPosition);
                    }
                }
            });


}
