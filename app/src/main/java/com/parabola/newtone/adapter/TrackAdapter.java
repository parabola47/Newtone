package com.parabola.newtone.adapter;

import static androidx.core.content.ContextCompat.getColor;
import static com.parabola.newtone.util.AndroidTool.convertDpToPixel;
import static com.parabola.newtone.util.AndroidTool.getStyledColor;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.shape.CornerFamily;
import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;
import com.parabola.newtone.R;
import com.parabola.newtone.databinding.ItemTrackBinding;
import com.parabola.newtone.util.TimeFormatterTool;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.Optional;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.observers.ConsumerSingleObserver;
import io.reactivex.schedulers.Schedulers;

public final class TrackAdapter extends SimpleListAdapter<Track, TrackAdapter.TrackViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {
    private static final String LOG_TAG = TrackAdapter.class.getSimpleName();

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflateByViewType(parent.getContext(), R.layout.item_track, parent);
        return new TrackViewHolder(ItemTrackBinding.bind(view));
    }

    private TrackItemView trackItemView;

    public void setViewSettings(TrackItemView trackItemView) {
        this.trackItemView = trackItemView;
        notifyDataSetChanged();
    }


    private boolean isMoveItemIconVisible = false;

    public void setMoveItemIconVisibility(boolean visible) {
        isMoveItemIconVisible = visible;
        LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
        if (layoutManager == null)
            return;

        int updateFrom = layoutManager.findFirstVisibleItemPosition() - layoutManager.getInitialPrefetchItemCount() - 1;
        if (updateFrom < 0) updateFrom = 0;
        int updateTo = layoutManager.findLastVisibleItemPosition() + layoutManager.getInitialPrefetchItemCount() + 1;
        if (updateTo > getItemCount() - 1) updateTo = getItemCount() - 1;

        for (int i = updateFrom; i <= updateTo; i++) {
            TrackViewHolder holder = (TrackViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (holder != null)
                holder.binding.burgerImg.setVisibility(isMoveItemIconVisible ? View.VISIBLE : View.GONE);
            else invalidateItem(i);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Context context = holder.itemView.getContext();

        Track trackItem = get(holder.getAdapterPosition());

        if (trackItemView != null)
            buildItemLayout(holder);
        holder.binding.burgerImg.setVisibility(isMoveItemIconVisible ? View.VISIBLE : View.GONE);

        String trackTitle = Optional.ofNullable(trackItem.getTitle())
                .orElse(trackItem.getFileNameWithoutExtension());
        holder.binding.trackTitle.setText(trackTitle);
        String additionalInfo = (trackItemView != null && trackItemView.isAlbumTitleShows)
                ? context.getString(R.string.track_item_artist_with_album, trackItem.getArtistName(), trackItem.getAlbumTitle())
                : trackItem.getArtistName();

        holder.binding.additionalTrackInfo.setText(additionalInfo);
        holder.binding.durationTxt.setText(
                TimeFormatterTool.formatMillisecondsToMinutes(trackItem.getDurationMs()));


        if (trackItemView != null && trackItemView.isCoverShows)
            loadCoverAsync(holder, trackItem);


        if (isContextSelected(holder.getAdapterPosition())) {
            holder.binding.trackTitle.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.binding.additionalTrackInfo.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.binding.durationTxt.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.itemView.setBackgroundColor(getStyledColor(context, R.attr.colorPrimaryDark));
            holder.binding.burgerImg.setColorFilter(getColor(context, android.R.color.white));
        } else if (isSelected(holder.getAdapterPosition())) {
            holder.binding.trackTitle.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.binding.additionalTrackInfo.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.binding.durationTxt.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.itemView.setBackgroundColor(getStyledColor(context, R.attr.colorPrimary));
            holder.binding.burgerImg.setColorFilter(getColor(context, android.R.color.white));
        } else {
            holder.binding.trackTitle.setTextColor(getColor(context, R.color.colorNewtonePrimaryText));
            holder.binding.additionalTrackInfo.setTextColor(getColor(context, R.color.colorNewtoneSecondaryText));
            holder.binding.durationTxt.setTextColor(getColor(context, R.color.colorNewtoneSecondaryText));
            holder.itemView.setBackgroundColor(getColor(context, R.color.colorListItemDefaultBackground));
            holder.binding.burgerImg.setColorFilter(getColor(context, android.R.color.darker_gray));
        }

        holder.binding.burgerImg.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                    && onMoveItemListener != null) {
                touchHelper.startDrag(holder);
            }
            return false;
        });
    }

    private void buildItemLayout(TrackViewHolder holder) {
        if (trackItemView.isCoverShows) {
            holder.binding.cover.setVisibility(View.VISIBLE);

            float coverCornersRadius = convertDpToPixel(trackItemView.coverCornersRadius, holder.itemView.getContext());
            holder.binding.cover.setShapeAppearanceModel(holder.binding.cover.getShapeAppearanceModel().toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, coverCornersRadius)
                    .build());

            int coverSizePx = (int) convertDpToPixel(trackItemView.coverSize, holder.itemView.getContext());
            ViewGroup.LayoutParams params = holder.binding.cover.getLayoutParams();
            params.width = coverSizePx;
            params.height = coverSizePx;
            holder.binding.cover.setLayoutParams(params);
        } else {
            holder.binding.cover.setVisibility(View.GONE);
        }

        holder.binding.trackTitle.setTextSize(trackItemView.textSize);
        holder.binding.additionalTrackInfo.setTextSize(trackItemView.textSize - 2);
        holder.binding.durationTxt.setTextSize(trackItemView.textSize - 4);


        int paddingPx = (int) convertDpToPixel(trackItemView.borderPadding, holder.itemView.getContext());
        holder.binding.trackContent.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
    }

    private void loadCoverAsync(TrackViewHolder holder, Track trackItem) {
        Single.fromCallable(trackItem::getArtImage)
                .cast(Bitmap.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ConsumerSingleObserver<>(
                        holder.binding.cover::setImageBitmap,
                        error -> holder.binding.cover.setImageResource(R.drawable.album_default)));
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
        private final ItemTrackBinding binding;


        private TrackViewHolder(ItemTrackBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
