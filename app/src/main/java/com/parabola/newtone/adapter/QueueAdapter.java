package com.parabola.newtone.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.newtone.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import java8.util.Optional;

import static com.parabola.newtone.util.AndroidTool.convertDpToPixel;

public final class QueueAdapter extends SimpleListAdapter<Track, QueueAdapter.ViewHolder> {


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        touchHelper.attachToRecyclerView(recyclerView);
    }

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
            holder.additionalTrackInfo.setTextColor(ContextCompat.getColor(context, R.color.colorSelectedTrackTextColor));
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            holder.burgerImg.setColorFilter(ContextCompat.getColor(context, android.R.color.white));
            holder.removeImg.setColorFilter(ContextCompat.getColor(context, android.R.color.white));
        } else {
            holder.additionalTrackInfo.setTextColor(ContextCompat.getColor(context, R.color.colorDefaultTrackOtherInfo));
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorListItemDefaultBackground));
            holder.burgerImg.setColorFilter(ContextCompat.getColor(context, android.R.color.darker_gray));
            holder.removeImg.setColorFilter(ContextCompat.getColor(context, android.R.color.darker_gray));
        }


        holder.removeImg.setOnClickListener(v -> {
            if (removeClickListener != null) {
                removeClickListener.onClickRemoveItem(holder.getAdapterPosition());
            }
        });
        holder.burgerImg.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                    && dragListener != null) {
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

    private final ItemTouchHelper touchHelper = new ItemTouchHelper(
            new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {

                boolean isFirst = true;
                int startPosition;
                int lastPosition;
                int lastActionState = ItemTouchHelper.ACTION_STATE_IDLE;

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    int oldPosition = viewHolder.getAdapterPosition();
                    int newPosition = target.getAdapterPosition();

                    moveItem(oldPosition, newPosition);

                    if (isFirst) {
                        isFirst = false;
                        startPosition = oldPosition;
                    }
                    lastPosition = newPosition;

                    return true;
                }


                @Override
                public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                    super.onSelectedChanged(viewHolder, actionState);

                    if (actionState == ItemTouchHelper.ACTION_STATE_IDLE
                            && lastActionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                        isFirst = true;
                        if (dragListener != null) {
                            dragListener.onMoveItem(startPosition, lastPosition);
                        }
                        lastPosition = startPosition;
                    }

                    lastActionState = actionState;
                }

                @Override
                public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                        @NonNull RecyclerView.ViewHolder viewHolder,
                                        float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        float alpha = 1 - (Math.abs(dX) / recyclerView.getWidth());
                        viewHolder.itemView.setAlpha(alpha);
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int oldPosition = viewHolder.getAdapterPosition();
                    if (dragListener != null) {
                        dragListener.onSwipeItem(oldPosition);
                    }
                }
            });

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
