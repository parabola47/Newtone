package com.parabola.newtone.adapter;

import static androidx.core.content.ContextCompat.getColor;
import static com.parabola.newtone.util.AndroidTool.getStyledColor;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.domain.model.Playlist;
import com.parabola.newtone.R;
import com.parabola.newtone.databinding.ItemPlaylistBinding;

public final class PlaylistAdapter extends SimpleListAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder> {

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflateByViewType(parent.getContext(), R.layout.item_playlist, parent);
        return new PlaylistViewHolder(ItemPlaylistBinding.bind(view));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Context context = holder.itemView.getContext();

        Playlist playlist = get(holder.getAdapterPosition());

        holder.binding.title.setText(playlist.getTitle());
        String tracksCountText = holder.itemView.getResources()
                .getQuantityString(R.plurals.tracks_count, playlist.size(), playlist.size());
        holder.binding.tracksCount.setText(tracksCountText);


        if (isContextSelected(holder.getAdapterPosition())) {
            holder.binding.title.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.binding.tracksCount.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.itemView.setBackgroundColor(getStyledColor(context, R.attr.colorPrimaryDark));
        } else {
            holder.binding.title.setTextColor(getColor(context, R.color.colorNewtonePrimaryText));
            holder.binding.tracksCount.setTextColor(getColor(context, R.color.colorNewtoneSecondaryText));
            holder.itemView.setBackgroundColor(getColor(context, R.color.colorListItemDefaultBackground));
        }
    }

    @Override
    public char getSection(int position) {
        return get(position).getTitle().charAt(0);
    }


    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private final ItemPlaylistBinding binding;

        private PlaylistViewHolder(ItemPlaylistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
