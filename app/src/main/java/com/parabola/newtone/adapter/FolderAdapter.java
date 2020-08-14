package com.parabola.newtone.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.domain.model.Folder;
import com.parabola.newtone.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static androidx.core.content.ContextCompat.getColor;
import static com.parabola.newtone.util.AndroidTool.getStyledColor;

public final class FolderAdapter extends SimpleListAdapter<Folder, FolderAdapter.FolderViewHolder> {


    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflateByViewType(parent.getContext(), R.layout.item_folder, parent);

        return new FolderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Folder item = get(holder.getAdapterPosition());
        Context context = holder.itemView.getContext();

        holder.folderNameTxt.setText(item.getFolderName());
        holder.pathToFolder.setText(item.getPathToParent());

        String tracksCountText = holder.itemView.getResources()
                .getQuantityString(R.plurals.tracks_count, item.getTracksCount(), item.getTracksCount());
        holder.tracksCount.setText(tracksCountText);

        if (isContextSelected(holder.getAdapterPosition())) {
            holder.folderNameTxt.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.pathToFolder.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.tracksCount.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.itemView.setBackgroundColor(getStyledColor(context, R.attr.colorPrimaryDark));
        } else {
            holder.folderNameTxt.setTextColor(getColor(context, R.color.colorNewtonePrimaryText));
            holder.pathToFolder.setTextColor(getColor(context, R.color.colorNewtoneSecondaryText));
            holder.tracksCount.setTextColor(getColor(context, R.color.colorNewtoneSecondaryText));
            holder.itemView.setBackgroundColor(getColor(context, R.color.colorListItemDefaultBackground));
        }
    }

    @Override
    public char getSection(int position) {
        return get(position).getFolderName().charAt(0);
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.folder) TextView folderNameTxt;
        @BindView(R.id.path) TextView pathToFolder;
        @BindView(R.id.tracks_count) TextView tracksCount;

        private FolderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
}
