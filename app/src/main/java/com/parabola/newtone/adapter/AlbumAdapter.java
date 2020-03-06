package com.parabola.newtone.adapter;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

public final class AlbumAdapter extends SimpleListAdapter<Album, AlbumAdapter.AlbumViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    public AlbumAdapter() {
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflateByViewType(parent.getContext(), R.layout.item_album, parent);

        return new AlbumViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        Album albumItem = get(holder.getAdapterPosition());

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
                                .placeholder(R.drawable.album_holder)
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

    static class AlbumViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.album_title) TextView albumTitle;
        @BindView(R.id.author) TextView albumArtist;
        @BindView(R.id.albumCover) ShapeableImageView albumCover;

        private AlbumViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            albumCover.setShapeAppearanceModel(albumCover.getShapeAppearanceModel().toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, itemView.getResources().getDimension(R.dimen.track_item_album_corner_size))
                    .build());
        }
    }
}
