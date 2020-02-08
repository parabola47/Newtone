package com.parabola.newtone.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.domain.model.Folder;
import com.parabola.newtone.R;

import butterknife.BindView;
import butterknife.ButterKnife;

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

        holder.folderNameTxt.setText(item.getFolderName());
        holder.pathToFolder.setText(item.getPathToParent());
        holder.tracksCount.setText(
                holder.tracksCount.getContext().getResources().getQuantityString(R.plurals.tracks_count,
                        item.getTracksCount(), item.getTracksCount()));
    }

    @Override
    public char getSection(int position) {
        return get(position).getFolderName().charAt(0);
    }

    public class FolderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.folder) TextView folderNameTxt;
        @BindView(R.id.path) TextView pathToFolder;
        @BindView(R.id.tracks_count) TextView tracksCount;

        public FolderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
}
