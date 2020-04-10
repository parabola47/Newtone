package com.parabola.newtone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import java8.util.Optional;
import java8.util.OptionalInt;
import java8.util.function.Predicate;


public abstract class SimpleListAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>
        implements BaseAdapter<T> {


    protected ItemClickListener itemClickListener;
    protected ItemLongClickListener itemLongClickListener;
    protected RemoveClickListener removeClickListener;
    protected DragListener dragListener;

    private final SelectableList<T> items = new SelectableList<>();

    protected RecyclerView recyclerView;
    protected boolean showSection;


    public SimpleListAdapter() {
    }

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

    @Override
    public void clearSelected() {
        OptionalInt selectedItemIndex = items.getSelectedItemIndex();

        items.clearSelected();

        selectedItemIndex
                .ifPresent(value -> notifyItemChanged(selectedItemIndex.getAsInt()));
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

        if (oldSelectedPosition.isPresent()
                && oldSelectedPosition.getAsInt() != position) {
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
    public Optional<T> getSelectedItem() {
        return items.getSelectedItem();
    }

    @Override
    public OptionalInt getSelectedPosition() {
        return items.getSelectedItemIndex();
    }


    @Override
    public void setItemClickListener(ItemClickListener listener) {
        itemClickListener = listener;
    }

    @Override
    public void setItemLongClickListener(ItemLongClickListener listener) {
        itemLongClickListener = listener;
    }

    @Override
    public void setRemoveClickListener(RemoveClickListener listener) {
        removeClickListener = listener;
    }

    @Override
    public void setDragListener(DragListener listener) {
        dragListener = listener;
    }


    private final float[] longClickLastTouchDownXY = new float[2];

    @Override
    @CallSuper
    public void onBindViewHolder(VH holder, int position) {
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(holder.getAdapterPosition());
            }
        });
        holder.itemView.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                longClickLastTouchDownXY[0] = event.getX();
                longClickLastTouchDownXY[1] = event.getY();
            }
            return false;
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (itemLongClickListener != null) {
                itemLongClickListener.onItemLongClick((ViewGroup) v,
                        (int) longClickLastTouchDownXY[0], (int) longClickLastTouchDownXY[1],
                        holder.getAdapterPosition());
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
    }

}
