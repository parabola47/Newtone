package com.parabola.newtone.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.newtone.R;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class ExcludedFolderAdapter extends SimpleListAdapter<String, ExcludedFolderAdapter.ViewHolder> {


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflateByViewType(parent.getContext(), R.layout.item_excluded_folder, parent);

        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        String absolutePath = get(holder.getAdapterPosition());
        holder.folderPath.setText(absolutePath);
        if (absolutePath.endsWith(File.separator)) {
            absolutePath = absolutePath.substring(0, absolutePath.length() - 1);
        }
        int index = absolutePath.lastIndexOf(File.separator);

        holder.folderName.setText(absolutePath.substring(index + 1));
        holder.removeButton.setOnClickListener(view -> {
            if (onRemoveClickListener != null) {
                onRemoveClickListener.onClickRemoveItem(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public char getSection(int position) {
        return get(position).charAt(0);
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.folderName) TextView folderName;
        @BindView(R.id.folderPath) TextView folderPath;
        @BindView(R.id.removeButton) ImageView removeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
