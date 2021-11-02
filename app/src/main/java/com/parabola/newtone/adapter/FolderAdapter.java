package com.parabola.newtone.adapter;

import static androidx.core.content.ContextCompat.getColor;
import static com.parabola.newtone.util.AndroidTool.getStyledColor;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.domain.model.Folder;
import com.parabola.newtone.R;
import com.parabola.newtone.databinding.ItemFolderBinding;

public final class FolderAdapter extends SimpleListAdapter<Folder, FolderAdapter.FolderViewHolder> {


    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflateByViewType(parent.getContext(), R.layout.item_folder, parent);
        return new FolderViewHolder(ItemFolderBinding.bind(view));
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Folder item = get(holder.getAdapterPosition());
        Context context = holder.itemView.getContext();

        holder.binding.folder.setText(item.getFolderName());
        holder.binding.path.setText(item.getPathToParent());

        String tracksCountText = holder.itemView.getResources()
                .getQuantityString(R.plurals.tracks_count, item.getTracksCount(), item.getTracksCount());
        holder.binding.tracksCount.setText(tracksCountText);

        if (isContextSelected(holder.getAdapterPosition())) {
            holder.binding.folder.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.binding.path.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.binding.tracksCount.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.itemView.setBackgroundColor(getStyledColor(context, R.attr.colorPrimaryDark));
        } else {
            holder.binding.folder.setTextColor(getColor(context, R.color.colorNewtonePrimaryText));
            holder.binding.path.setTextColor(getColor(context, R.color.colorNewtoneSecondaryText));
            holder.binding.tracksCount.setTextColor(getColor(context, R.color.colorNewtoneSecondaryText));
            holder.itemView.setBackgroundColor(getColor(context, R.color.colorListItemDefaultBackground));
        }
    }

    @Override
    public char getSection(int position) {
        return get(position).getFolderName().charAt(0);
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        private final ItemFolderBinding binding;


        private FolderViewHolder(ItemFolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
