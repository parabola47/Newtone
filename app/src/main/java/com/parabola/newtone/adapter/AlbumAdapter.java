package com.parabola.newtone.adapter;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.parabola.domain.model.Album;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView.AlbumViewType;
import com.parabola.newtone.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.Optional;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.observers.ConsumerSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.parabola.newtone.util.AndroidTool.convertDpToPixel;

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
        View itemView = inflateByViewType(parent.getContext(), viewType, parent);
        switch (viewType) {
            case R.layout.item_album_grid:
                return new GridAlbumViewHolder(itemView);
            case R.layout.item_album_list:
                return new ListAlbumViewHolder(itemView);
            default: throw new IllegalArgumentException("viewType is not correct");
        }
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
            gridHolder.albumTitle.setTextSize(albumItemView.textSize + 2);
            gridHolder.albumArtist.setTextSize(albumItemView.textSize - 4);
            gridHolder.cover.setShapeAppearanceModel(gridHolder.cover.getShapeAppearanceModel().toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, coverCornersRadius)
                    .build());
        } else if (holder instanceof ListAlbumViewHolder) {
            ListAlbumViewHolder listHolder = (ListAlbumViewHolder) holder;
            listHolder.albumTitle.setTextSize(albumItemView.textSize);
            listHolder.albumArtist.setTextSize(albumItemView.textSize - 2);
            listHolder.tracksCount.setTextSize(albumItemView.textSize - 4);

            listHolder.itemView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

            ViewGroup.LayoutParams params = listHolder.cover.getLayoutParams();
            params.width = coverSizePx;
            params.height = coverSizePx;
            listHolder.cover.setLayoutParams(params);

            listHolder.cover.setShapeAppearanceModel(listHolder.cover.getShapeAppearanceModel().toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, coverCornersRadius)
                    .build());
        }
    }

    private void handleAsGrid(GridAlbumViewHolder holder, Album albumItem) {
        String albumTitle = Optional.ofNullable(albumItem.getTitle())
                .orElse(holder.albumTitle.getContext().getString(R.string.unknown_album));
        holder.albumTitle.setText(albumTitle);

        String artistName = Optional.ofNullable(albumItem.getArtistName())
                .orElse(holder.albumArtist.getContext().getString(R.string.unknown_artist));

        holder.albumArtist.setText(artistName);

        Single.fromCallable(albumItem::getArtImage)
                .cast(Bitmap.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ConsumerSingleObserver<>(
                        bitmap -> holder.cover.setImageBitmap(bitmap),
                        error -> holder.cover.setImageResource(R.drawable.album_default)));
    }

    private void handleAsList(ListAlbumViewHolder holder, Album albumItem) {
        String albumTitle = Optional.ofNullable(albumItem.getTitle())
                .orElse(holder.albumTitle.getContext().getString(R.string.unknown_album));
        holder.albumTitle.setText(albumTitle);

        String artistName = Optional.ofNullable(albumItem.getArtistName())
                .orElse(holder.albumArtist.getContext().getString(R.string.unknown_artist));

        holder.albumArtist.setText(artistName);

        String tracksCountString = recyclerView.getResources()
                .getQuantityString(R.plurals.tracks_count, albumItem.getTracksCount(), albumItem.getTracksCount());
        holder.tracksCount.setText(tracksCountString);

        Single.fromCallable(albumItem::getArtImage)
                .cast(Bitmap.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ConsumerSingleObserver<>(
                        bitmap -> holder.cover.setImageBitmap(bitmap),
                        error -> holder.cover.setImageResource(R.drawable.album_default)));
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

    static class GridAlbumViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.album_title) TextView albumTitle;
        @BindView(R.id.author) TextView albumArtist;
        @BindView(R.id.albumCover) ShapeableImageView cover;


        private GridAlbumViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class ListAlbumViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.album_title) TextView albumTitle;
        @BindView(R.id.author) TextView albumArtist;
        @BindView(R.id.tracks_count) TextView tracksCount;
        @BindView(R.id.albumCover) ShapeableImageView cover;


        private ListAlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
