package com.parabola.newtone.adapter;

import android.view.ViewGroup;

import java.util.List;

import java8.util.OptionalInt;
import java8.util.function.Predicate;

public interface BaseAdapter<T> {
    void add(T newItem);
    void add(int position, T newItem);
    void addAll(List<T> newItems);

    void invalidateItem(int position);
    T get(int position);
    List<T> getAll();
    int size();

    void remove(int position);
    void removeWithCondition(Predicate<T> predicate);

    void clear();

    void replaceAll(List<T> newItems);

    void moveItem(int from, int to);

    void setSectionEnabled(boolean sectionEnabled);
    char getSection(int position);


    //CONTEXT SELECTED
    void clearContextSelected();
    boolean isContextSelected(int position);
    void setContextSelected(int position);
    OptionalInt getContextSelectedPosition();


    //JUST SELECTED
    void clearSelected();
    boolean isSelected(int position);
    void setSelected(int position);
    void setSelectedCondition(Predicate<T> predicate);
    OptionalInt getSelectedPosition();


    //LISTENERS
    void setItemClickListener(ItemClickListener listener);
    void setItemLongClickListener(ItemLongClickListener listener);
    void setRemoveClickListener(RemoveClickListener listener);
    void setDragListener(DragListener listener);


    interface ItemClickListener {
        void onItemClick(int position);
    }

    interface ItemLongClickListener {
        void onItemLongClick(ViewGroup rootView, int clickPosX, int clickPosY, int itemPosition);
    }

    interface RemoveClickListener {
        void onClickRemoveItem(int position);
    }

    interface DragListener {
        void onSwipeItem(int position);
        void onMoveItem(int oldPosition, int newPosition);
    }

}
