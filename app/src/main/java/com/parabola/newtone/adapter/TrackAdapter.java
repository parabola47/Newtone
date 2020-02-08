package com.parabola.newtone.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.domain.model.Track;
import com.parabola.newtone.R;
import com.parabola.newtone.util.TimeFormatterTool;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.parabola.domain.utils.StringTool.getOrDefault;

public final class TrackAdapter extends SimpleListAdapter<Track, TrackAdapter.TrackViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {
    private static final String LOG_TAG = TrackAdapter.class.getSimpleName();

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflateByViewType(parent.getContext(), R.layout.item_track, parent);

        return new TrackViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Context context = holder.itemView.getContext();

        Track trackItem = get(holder.getAdapterPosition());

        holder.trackTitle.setText(getOrDefault(trackItem.getTitle(), trackItem.getFileNameWithoutExtension()));
        holder.artist.setText(getOrDefault(trackItem.getArtistName(), context.getString(R.string.unknown_artist)));
        holder.duration.setText(
                TimeFormatterTool.formatMillisecondsToMinutes(trackItem.getDurationMs()));


        if (isSelected(holder.getAdapterPosition())) {
            holder.artist.setTextColor(ContextCompat.getColor(context, R.color.colorSelectedTrack));
            holder.duration.setTextColor(ContextCompat.getColor(context, R.color.colorSelectedTrack));
            holder.itemView.setBackgroundResource(R.color.colorSelectedTrackBackground);
        } else {
            holder.artist.setTextColor(ContextCompat.getColor(context, R.color.colorNotSelectedTrackOther));
            holder.duration.setTextColor(ContextCompat.getColor(context, R.color.colorNotSelectedTrackOther));
            holder.itemView.setBackgroundResource(R.color.colorItemBG);
        }

    }

    @Override
    public char getSection(int position) {
        Track track = get(position);
        String title = getOrDefault(track.getTitle(), track.getFileNameWithoutExtension());

        return Character.toUpperCase(title.charAt(0));
    }

    @Override
    @NonNull
    public String getSectionName(int position) {
        return showSection ? String.valueOf(getSection(position)) : "";
    }

    public class TrackViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.track_title) TextView trackTitle;
        @BindView(R.id.artist) TextView artist;
        @BindView(R.id.song_duration) TextView duration;

        private TrackViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
