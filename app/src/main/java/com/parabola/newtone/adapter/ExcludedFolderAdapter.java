package com.parabola.newtone.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.newtone.R;
import com.parabola.newtone.databinding.ItemExcludedFolderBinding;

import java.io.File;

public final class ExcludedFolderAdapter extends SimpleListAdapter<String, ExcludedFolderAdapter.ViewHolder> {


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflateByViewType(parent.getContext(), R.layout.item_excluded_folder, parent);
        return new ViewHolder(ItemExcludedFolderBinding.bind(view));
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        String absolutePath = get(holder.getAdapterPosition());
        holder.binding.folderPath.setText(absolutePath);
        if (absolutePath.endsWith(File.separator)) {
            absolutePath = absolutePath.substring(0, absolutePath.length() - 1);
        }
        int index = absolutePath.lastIndexOf(File.separator);

        holder.binding.folderName.setText(absolutePath.substring(index + 1));
        holder.binding.removeButton.setOnClickListener(view -> {
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
        private final ItemExcludedFolderBinding binding;


        private ViewHolder(ItemExcludedFolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
