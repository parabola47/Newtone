package com.parabola.newtone.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.domain.model.Playlist;
import com.parabola.newtone.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static androidx.core.content.ContextCompat.getColor;
import static com.parabola.newtone.util.AndroidTool.getStyledColor;

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
        Context context = holder.itemView.getContext();

        Playlist playlist = get(holder.getAdapterPosition());

        holder.titleTxt.setText(playlist.getTitle());
        String tracksCountText = holder.itemView.getResources()
                .getQuantityString(R.plurals.tracks_count, playlist.size(), playlist.size());
        holder.tracksCount.setText(tracksCountText);


        if (isContextSelected(holder.getAdapterPosition())) {
            holder.titleTxt.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.tracksCount.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.itemView.setBackgroundColor(getStyledColor(context, R.attr.colorPrimaryDark));
        } else {
            holder.titleTxt.setTextColor(getColor(context, R.color.colorNewtonePrimaryText));
            holder.tracksCount.setTextColor(getColor(context, R.color.colorNewtoneSecondaryText));
            holder.itemView.setBackgroundColor(getColor(context, R.color.colorListItemDefaultBackground));
        }
    }

    @Override
    public char getSection(int position) {
        return get(position).getTitle().charAt(0);
    }


    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title) TextView titleTxt;
        @BindView(R.id.tracks_count) TextView tracksCount;

        private PlaylistViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
