package com.parabola.newtone.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.view.menu.MenuBuilder;

import com.parabola.newtone.R;

import java8.util.function.Function;

@SuppressLint("RestrictedApi")
public class ListPopupWindowAdapter extends BaseAdapter {

    private final LayoutInflater layoutInflater;
    private final MenuBuilder menu;
    private final Context context;

    public ListPopupWindowAdapter(Context context, int menuResId) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.menu = new MenuBuilder(context);

        MenuInflater menuInflater = new SupportMenuInflater(context);
        menuInflater.inflate(menuResId, menu);
    }

    @Override
    public int getCount() {
        return menu.getVisibleItems().size();
    }

    @Override
    public MenuItem getItem(int position) {
        return menu.getVisibleItems().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_menu, parent, false);
        }

        MenuItem menuItem = getItem(position);

        ((TextView) convertView.findViewById(R.id.label)).setText(menuItem.getTitle());
        ((ImageView) convertView.findViewById(R.id.icon)).setImageDrawable(menuItem.getIcon());

        return convertView;
    }

    public void setMenuVisibility(Function<MenuItem, Boolean> itemsVisibility) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            item.setVisible(itemsVisibility.apply(item));
        }
    }

    public int measureContentWidth() {
        ViewGroup mMeasureParent = null;
        int maxWidth = 0;
        View itemView = null;
        int itemType = 0;

        final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int count = getCount();
        for (int i = 0; i < count; i++) {
            final int positionType = getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }

            if (mMeasureParent == null) {
                mMeasureParent = new FrameLayout(context);
            }

            itemView = getView(i, itemView, mMeasureParent);
            itemView.measure(widthMeasureSpec, heightMeasureSpec);

            final int itemWidth = itemView.getMeasuredWidth();

            if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }
        }

        return maxWidth;
    }
}
