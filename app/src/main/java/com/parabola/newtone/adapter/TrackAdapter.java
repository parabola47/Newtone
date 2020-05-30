package com.parabola.newtone.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;
import com.parabola.newtone.R;
import com.parabola.newtone.util.TimeFormatterTool;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.Optional;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.observers.ConsumerSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static androidx.core.content.ContextCompat.getColor;
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
        String additionalInfo = (trackItemView != null && trackItemView.isAlbumTitleShows)
                ? context.getString(R.string.track_item_artist_with_album, trackItem.getArtistName(), trackItem.getAlbumTitle())
                : trackItem.getArtistName();

        holder.additionalTrackInfo.setText(additionalInfo);
        holder.duration.setText(
                TimeFormatterTool.formatMillisecondsToMinutes(trackItem.getDurationMs()));


        if (trackItemView != null && trackItemView.isCoverShows)
            loadCoverAsync(holder, trackItem);


        if (isContextSelected(holder.getAdapterPosition())) {
            holder.trackTitle.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.additionalTrackInfo.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.duration.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.itemView.setBackgroundColor(getColor(context, R.color.colorListContextMenuBackground));
        } else if (isSelected(holder.getAdapterPosition())) {
            holder.trackTitle.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.additionalTrackInfo.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.duration.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.itemView.setBackgroundColor(getColor(context, R.color.colorListItemSelectedBackground));
        } else {
            holder.trackTitle.setTextColor(getColor(context, R.color.colorNewtonePrimaryText));
            holder.additionalTrackInfo.setTextColor(getColor(context, R.color.colorNewtoneSecondaryText));
            holder.duration.setTextColor(getColor(context, R.color.colorNewtoneSecondaryText));
            holder.itemView.setBackgroundColor(getColor(context, R.color.colorListItemDefaultBackground));
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
        holder.additionalTrackInfo.setTextSize(trackItemView.textSize - 2);
        holder.duration.setTextSize(trackItemView.textSize - 4);


        int paddingPx = (int) convertDpToPixel(trackItemView.borderPadding, holder.itemView.getContext());
        holder.itemView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
    }

    private void loadCoverAsync(TrackViewHolder holder, Track trackItem) {
        Single.fromCallable(trackItem::getArtImage)
                .cast(Bitmap.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ConsumerSingleObserver<>(
                        bitmap -> holder.cover.setImageBitmap(bitmap),
                        error -> holder.cover.setImageResource(R.drawable.album_default)));
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
        @BindView(R.id.additionalTrackInfo) TextView additionalTrackInfo;
        @BindView(R.id.song_duration) TextView duration;
        @BindView(R.id.cover) ShapeableImageView cover;

        private TrackViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
