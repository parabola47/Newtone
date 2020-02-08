package com.parabola.newtone.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.domain.model.Playlist;
import com.parabola.newtone.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class PlaylistAdapter extends SimpleListAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder> {

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflateByViewType(parent.getContext(), R.layout.item_playlist, parent);

        return new PlaylistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        Playlist playlist = get(holder.getAdapterPosition());

        holder.titleTxt.setText(playlist.getTitle());
        holder.tracksCount.setText(
                holder.tracksCount.getContext().getResources()
                        .getQuantityString(R.plurals.tracks_count,
                                playlist.getPlaylistTracks().size(),
                                playlist.getPlaylistTracks().size())
        );
    }

    @Override
    public char getSection(int position) {
        return get(position).getTitle().charAt(0);
    }


    public class PlaylistViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title) TextView titleTxt;
        @BindView(R.id.tracks_count) TextView tracksCount;

        private PlaylistViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
