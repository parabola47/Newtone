package com.parabola.newtone.adapter;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.newtone.R;

import java.util.Optional;

import butterknife.BindView;
import butterknife.ButterKnife;

import static androidx.core.content.ContextCompat.getColor;
import static com.parabola.newtone.util.AndroidTool.convertDpToPixel;

public final class QueueAdapter extends SimpleListAdapter<Track, QueueAdapter.ViewHolder> {

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflateByViewType(parent.getContext(), R.layout.item_queue_track, parent);

        return new ViewHolder(v);
    }

    private ViewSettingsInteractor.TrackItemView trackItemView;

    public void setViewSettings(ViewSettingsInteractor.TrackItemView trackItemView) {
        this.trackItemView = trackItemView;
        notifyDataSetChanged();
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Context context = holder.itemView.getContext();

        Track trackItem = get(holder.getAdapterPosition());

        if (trackItemView != null)
            buildItemLayout(holder);

        String trackTitle = Optional.ofNullable(trackItem.getTitle())
                .orElse(trackItem.getFileNameWithoutExtension());
        holder.trackTitle.setText(trackTitle);

        String additionalInfo = trackItemView.isAlbumTitleShows
                ? context.getString(R.string.track_item_artist_with_album, trackItem.getArtistName(), trackItem.getAlbumTitle())
                : trackItem.getArtistName();
        holder.additionalTrackInfo.setText(additionalInfo);

        if (isSelected(holder.getAdapterPosition())) {
            holder.trackTitle.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.additionalTrackInfo.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.itemView.setBackgroundColor(getColor(context, R.color.colorListItemSelectedBackground));
            holder.burgerImg.setColorFilter(getColor(context, android.R.color.white));
            holder.removeImg.setColorFilter(getColor(context, android.R.color.white));
        } else {
            holder.trackTitle.setTextColor(getColor(context, R.color.colorNewtonePrimaryText));
            holder.additionalTrackInfo.setTextColor(getColor(context, R.color.colorNewtoneSecondaryText));
            holder.itemView.setBackgroundColor(getColor(context, R.color.colorListItemDefaultBackground));
            holder.burgerImg.setColorFilter(getColor(context, android.R.color.darker_gray));
            holder.removeImg.setColorFilter(getColor(context, android.R.color.darker_gray));
        }


        holder.removeImg.setOnClickListener(v -> {
            if (onRemoveClickListener != null) {
                onRemoveClickListener.onClickRemoveItem(holder.getAdapterPosition());
            }
        });
        holder.burgerImg.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                    && onMoveItemListener != null) {
                touchHelper.startDrag(holder);
            }
            return false;
        });
    }

    private void buildItemLayout(ViewHolder holder) {
        holder.trackTitle.setTextSize(trackItemView.textSize);
        holder.additionalTrackInfo.setTextSize(trackItemView.textSize - 2);

        int paddingPx = (int) convertDpToPixel(trackItemView.borderPadding, holder.itemView.getContext());
        holder.itemView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
    }

    @Override
    public char getSection(int position) {
        return get(position).getTitle().charAt(0);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.track_title) TextView trackTitle;
        @BindView(R.id.additionalTrackInfo) TextView additionalTrackInfo;
        @BindView(R.id.burger_img) ImageView burgerImg;
        @BindView(R.id.remove_img) ImageView removeImg;


        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
