package com.parabola.newtone.adapter;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.parabola.domain.model.Album;
import com.parabola.newtone.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.observers.BiConsumerSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java8.util.Optional;

public final class AlbumAdapter extends SimpleListAdapter<Album, RecyclerView.ViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private int viewType = GRID_VIEW_TYPE;

    private static final int GRID_VIEW_TYPE = 0;
    private static final int LIST_VIEW_TYPE = 1;

    public AlbumAdapter() {
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }


    public void showAsGrid(int spanCount) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) recyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(recyclerView.getContext(), spanCount);
        viewType = GRID_VIEW_TYPE;
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.scrollToPosition(scrollPosition);
    }

    public void showAsList() {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) recyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
        viewType = LIST_VIEW_TYPE;
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.scrollToPosition(scrollPosition);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case GRID_VIEW_TYPE:
                return new GridAlbumViewHolder(inflateByViewType(parent.getContext(), R.layout.item_album_grid, parent));
            case LIST_VIEW_TYPE:
                return new ListAlbumViewHolder(inflateByViewType(parent.getContext(), R.layout.item_album_list, parent));
            default: throw new IllegalStateException();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        Album albumItem = get(holder.getAdapterPosition());
        switch (viewType) {
            case GRID_VIEW_TYPE:
                handleAsGrid((GridAlbumViewHolder) holder, albumItem);
                break;
            case LIST_VIEW_TYPE:
                handleAsList((ListAlbumViewHolder) holder, albumItem);
                break;
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
                .subscribe(new BiConsumerSingleObserver<>(
                        (bitmap, error) -> Glide.with(holder.albumCover)
                                .load(bitmap)
                                .placeholder(R.drawable.album_default)
                                .into(holder.albumCover)));
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
                .subscribe(new BiConsumerSingleObserver<>(
                        (bitmap, error) -> Glide.with(holder.albumCover)
                                .load(bitmap)
                                .placeholder(R.drawable.album_default)
                                .into(holder.albumCover)));
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
        @BindView(R.id.albumCover) ShapeableImageView albumCover;

        private GridAlbumViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            albumCover.setShapeAppearanceModel(albumCover.getShapeAppearanceModel().toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, itemView.getResources().getDimension(R.dimen.track_item_album_corner_size))
                    .build());
        }
    }

    static class ListAlbumViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.album_title) TextView albumTitle;
        @BindView(R.id.author) TextView albumArtist;
        @BindView(R.id.tracks_count) TextView tracksCount;
        @BindView(R.id.albumCover) ShapeableImageView albumCover;


        private ListAlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            albumCover.setShapeAppearanceModel(albumCover.getShapeAppearanceModel().toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, itemView.getResources().getDimension(R.dimen.track_item_album_corner_size))
                    .build());
        }
    }
}
