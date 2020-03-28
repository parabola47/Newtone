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
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;
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

import static com.parabola.newtone.util.AndroidTool.convertDpToPixel;

public final class TrackAdapter extends SimpleListAdapter<Track, TrackAdapter.TrackViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {
    private static final String LOG_TAG = TrackAdapter.class.getSimpleName();

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflateByViewType(parent.getContext(), R.layout.item_track, parent);

        return new TrackViewHolder(v);
    }

    private TrackItemView trackItemView;

    public void setViewSettings(TrackItemView trackItemView) {
        this.trackItemView = trackItemView;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Context context = holder.itemView.getContext();

        Track trackItem = get(holder.getAdapterPosition());

        if (trackItemView != null)
            buildItemLayout(holder);

        String trackTitle = Optional.ofNullable(trackItem.getTitle())
                .orElse(trackItem.getFileNameWithoutExtension());
        holder.trackTitle.setText(trackTitle);
        String artistName = Optional.ofNullable(trackItem.getArtistName())
                .orElse(context.getString(R.string.unknown_artist));
        holder.artist.setText(artistName);
        holder.duration.setText(
                TimeFormatterTool.formatMillisecondsToMinutes(trackItem.getDurationMs()));


        if (trackItemView != null && trackItemView.isCoverShows)
            loadCoverAsync(holder, trackItem);


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

    private void buildItemLayout(TrackViewHolder holder) {
        if (trackItemView.isCoverShows) {
            holder.cover.setVisibility(View.VISIBLE);

            float coverCornersRadius = convertDpToPixel(trackItemView.coverCornersRadius, holder.itemView.getContext());
            holder.cover.setShapeAppearanceModel(holder.cover.getShapeAppearanceModel().toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, coverCornersRadius)
                    .build());

            int coverSizePx = (int) convertDpToPixel(trackItemView.coverSize, holder.itemView.getContext());
            ViewGroup.LayoutParams params = holder.cover.getLayoutParams();
            params.width = coverSizePx;
            params.height = coverSizePx;
            holder.cover.setLayoutParams(params);
        } else {
            holder.cover.setVisibility(View.GONE);
        }

        holder.trackTitle.setTextSize(trackItemView.textSize);
        holder.artist.setTextSize(trackItemView.textSize - 2);
        holder.duration.setTextSize(trackItemView.textSize - 4);


        int paddingPx = (int) convertDpToPixel(trackItemView.borderPadding, holder.itemView.getContext());
        holder.itemView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
    }

    private void loadCoverAsync(TrackViewHolder holder, Track trackItem) {
        Single.fromCallable(trackItem::getArtImage)
                .cast(Bitmap.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BiConsumerSingleObserver<>((bitmap, error) ->
                        Glide.with(holder.cover)
                                .load(bitmap)
                                .placeholder(R.drawable.album_default)
                                .into(holder.cover)));
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
        }
    }
}
