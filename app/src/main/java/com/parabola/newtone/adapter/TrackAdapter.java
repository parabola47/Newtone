package com.parabola.newtone.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.parabola.domain.model.Track;
import com.parabola.newtone.R;
import com.parabola.newtone.util.TimeFormatterTool;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.observers.BiConsumerSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java8.util.Optional;

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

        String trackTitle = Optional.ofNullable(trackItem.getTitle())
                .orElse(trackItem.getFileNameWithoutExtension());
        holder.trackTitle.setText(trackTitle);
        String artistName = Optional.ofNullable(trackItem.getArtistName())
                .orElse(context.getString(R.string.unknown_artist));
        holder.artist.setText(artistName);
        holder.duration.setText(
                TimeFormatterTool.formatMillisecondsToMinutes(trackItem.getDurationMs()));


        Single.fromCallable(trackItem::getArtImage)
                .cast(Bitmap.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BiConsumerSingleObserver<>((bitmap, error) ->
                        Glide.with(holder.cover)
                                .load(bitmap)
                                .placeholder(R.drawable.album_holder)
                                .into(holder.cover)));

        if (isSelected(holder.getAdapterPosition())) {
            holder.artist.setTextColor(ContextCompat.getColor(context, R.color.colorSelectedTrackTextColor));
            holder.duration.setTextColor(ContextCompat.getColor(context, R.color.colorSelectedTrackTextColor));
            holder.itemView.setBackgroundResource(R.color.colorAccent);
        } else {
            holder.artist.setTextColor(ContextCompat.getColor(context, R.color.colorDefaultTrackOtherInfo));
            holder.duration.setTextColor(ContextCompat.getColor(context, R.color.colorDefaultTrackOtherInfo));
            holder.itemView.setBackgroundResource(R.color.colorListItemDefaultBackground);
        }

    }

    @Override
    public char getSection(int position) {
        Track track = get(position);
        String title = Optional.ofNullable(track.getTitle())
                .orElse(track.getFileNameWithoutExtension());

        return Character.toUpperCase(title.charAt(0));
    }

    @Override
    @NonNull
    public String getSectionName(int position) {
        return showSection ? String.valueOf(getSection(position)) : "";
    }

    static class TrackViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.track_title) TextView trackTitle;
        @BindView(R.id.artist) TextView artist;
        @BindView(R.id.song_duration) TextView duration;
        @BindView(R.id.cover) ShapeableImageView cover;

        private TrackViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            cover.setShapeAppearanceModel(cover.getShapeAppearanceModel().toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, itemView.getResources().getDimension(R.dimen.track_item_album_corner_size))
                    .build());
        }
    }
}
