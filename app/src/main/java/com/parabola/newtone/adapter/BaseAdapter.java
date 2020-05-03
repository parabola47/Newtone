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
    void setOnItemClickListener(OnItemClickListener listener);
    void setOnItemLongClickListener(OnItemLongClickListener listener);
    void setOnRemoveClickListener(OnRemoveClickListener listener);

    void setOnMoveItemListener(OnMoveItemListener listener);
    void setOnSwipeItemListener(OnSwipeItemListener listener);


    interface OnItemClickListener {
        void onItemClick(int position);
    }

    interface OnItemLongClickListener {
        void onItemLongClick(ViewGroup rootView, int clickPosX, int clickPosY, int itemPosition);
    }

    interface OnRemoveClickListener {
        void onClickRemoveItem(int position);
    }

    interface OnSwipeItemListener {
        void onSwipeItem(int position);
    }

    interface OnMoveItemListener {
        void onMoveItem(int oldPosition, int newPosition);
    }

}
