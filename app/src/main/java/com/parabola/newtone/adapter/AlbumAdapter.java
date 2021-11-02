package com.parabola.newtone.adapter;

import static androidx.core.content.ContextCompat.getColor;
import static com.parabola.newtone.util.AndroidTool.convertDpToPixel;
import static com.parabola.newtone.util.AndroidTool.getStyledColor;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.shape.CornerFamily;
import com.parabola.domain.model.Album;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView.AlbumViewType;
import com.parabola.newtone.R;
import com.parabola.newtone.databinding.ItemAlbumGridBinding;
import com.parabola.newtone.databinding.ItemAlbumListBinding;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.Optional;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.observers.ConsumerSingleObserver;
import io.reactivex.schedulers.Schedulers;

public final class AlbumAdapter extends SimpleListAdapter<Album, RecyclerView.ViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {


    private int spanCount = 1;
    private AlbumItemView albumItemView;


    @Override
    public int getItemViewType(int position) {
        if (albumItemView == null || albumItemView.viewType == AlbumViewType.GRID)
            return R.layout.item_album_grid;
        else return R.layout.item_album_list;
    }


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        ((GridLayoutManager) getLayoutManager()).setSpanCount(spanCount);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflateByViewType(parent.getContext(), viewType, parent);
        if (viewType == R.layout.item_album_grid) {
            return new GridAlbumViewHolder(ItemAlbumGridBinding.bind(view));
        } else if (viewType == R.layout.item_album_list) {
            return new ListAlbumViewHolder(ItemAlbumListBinding.bind(view));
        }
        throw new IllegalArgumentException("viewType is not correct");
    }


    public void setViewSettings(AlbumItemView albumItemView, int spanCount) {
        this.albumItemView = albumItemView;
        this.spanCount = spanCount;

        if (getLayoutManager() != null) {
            ((GridLayoutManager) getLayoutManager()).setSpanCount(spanCount);
        }

        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        Album albumItem = get(holder.getAdapterPosition());

        if (albumItemView != null)
            buildItemLayout(holder);

        if (holder instanceof GridAlbumViewHolder)
            handleAsGrid((GridAlbumViewHolder) holder, albumItem);
        else handleAsList((ListAlbumViewHolder) holder, albumItem);
    }

    private void buildItemLayout(RecyclerView.ViewHolder holder) {
        float coverCornersRadius = convertDpToPixel(albumItemView.coverCornersRadius, holder.itemView.getContext());
        int coverSizePx = (int) convertDpToPixel(albumItemView.coverSize, holder.itemView.getContext());
        int paddingPx = (int) convertDpToPixel(albumItemView.borderPadding, holder.itemView.getContext());

        if (holder instanceof GridAlbumViewHolder) {
            GridAlbumViewHolder gridHolder = (GridAlbumViewHolder) holder;
            gridHolder.binding.albumTitle.setTextSize(albumItemView.textSize + 2);
            gridHolder.binding.author.setTextSize(albumItemView.textSize - 4);
            gridHolder.binding.albumCover.setShapeAppearanceModel(gridHolder.binding.albumCover.getShapeAppearanceModel().toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, coverCornersRadius)
                    .build());
        } else if (holder instanceof ListAlbumViewHolder) {
            ListAlbumViewHolder listHolder = (ListAlbumViewHolder) holder;
            listHolder.binding.albumTitle.setTextSize(albumItemView.textSize);
            listHolder.binding.author.setTextSize(albumItemView.textSize - 2);
            listHolder.binding.tracksCount.setTextSize(albumItemView.textSize - 4);

            listHolder.itemView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

            ViewGroup.LayoutParams params = listHolder.binding.albumCover.getLayoutParams();
            params.width = coverSizePx;
            params.height = coverSizePx;
            listHolder.binding.albumCover.setLayoutParams(params);

            listHolder.binding.albumCover.setShapeAppearanceModel(listHolder.binding.albumCover.getShapeAppearanceModel().toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, coverCornersRadius)
                    .build());
        }
    }

    private void handleAsGrid(GridAlbumViewHolder holder, Album albumItem) {
        Context context = holder.binding.albumTitle.getContext();
        String albumTitle = Optional.ofNullable(albumItem.getTitle())
                .orElse(context.getString(R.string.unknown_album));
        holder.binding.albumTitle.setText(albumTitle);

        String artistName = Optional.ofNullable(albumItem.getArtistName())
                .orElse(context.getString(R.string.unknown_artist));

        holder.binding.author.setText(artistName);

        Single.fromCallable(albumItem::getArtImage)
                .cast(Bitmap.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ConsumerSingleObserver<>(
                        holder.binding.albumCover::setImageBitmap,
                        error -> holder.binding.albumCover.setImageResource(R.drawable.album_default)));

        if (isContextSelected(holder.getAdapterPosition()))
            holder.itemView.setBackgroundColor(getStyledColor(context, R.attr.colorPrimaryDark));
        else
            holder.itemView.setBackground(null);
    }

    private void handleAsList(ListAlbumViewHolder holder, Album albumItem) {
        Context context = holder.binding.author.getContext();
        String albumTitle = Optional.ofNullable(albumItem.getTitle())
                .orElse(context.getString(R.string.unknown_album));
        holder.binding.albumTitle.setText(albumTitle);

        String artistName = Optional.ofNullable(albumItem.getArtistName())
                .orElse(context.getString(R.string.unknown_artist));

        holder.binding.author.setText(artistName);

        String tracksCountString = context.getResources()
                .getQuantityString(R.plurals.tracks_count, albumItem.getTracksCount(), albumItem.getTracksCount());
        holder.binding.tracksCount.setText(tracksCountString);

        Single.fromCallable(albumItem::getArtImage)
                .cast(Bitmap.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ConsumerSingleObserver<>(
                        holder.binding.albumCover::setImageBitmap,
                        error -> holder.binding.albumCover.setImageResource(R.drawable.album_default)));

        if (isContextSelected(holder.getAdapterPosition())) {
            holder.binding.albumTitle.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.binding.author.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.binding.tracksCount.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.itemView.setBackgroundColor(getStyledColor(context, R.attr.colorPrimaryDark));
        } else {
            holder.binding.albumTitle.setTextColor(getColor(context, R.color.colorNewtonePrimaryText));
            holder.binding.author.setTextColor(getColor(context, R.color.colorNewtoneSecondaryText));
            holder.binding.tracksCount.setTextColor(getColor(context, R.color.colorNewtoneSecondaryText));
            holder.itemView.setBackgroundColor(getColor(context, R.color.colorListItemDefaultBackground));
        }
    }


    @Override
    public char getSection(int position) {
        Album album = get(position);
        String albumTitle = Optional.ofNullable(album.getTitle())
                .orElse(recyclerView.getContext().getString(R.string.unknown_album));

        return Character.toUpperCase(albumTitle.charAt(0));
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return showSection ? String.valueOf(getSection(position)) : "";
    }

    private static class GridAlbumViewHolder extends RecyclerView.ViewHolder {
        private final ItemAlbumGridBinding binding;


        private GridAlbumViewHolder(ItemAlbumGridBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static class ListAlbumViewHolder extends RecyclerView.ViewHolder {
        private final ItemAlbumListBinding binding;


        private ListAlbumViewHolder(ItemAlbumListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
