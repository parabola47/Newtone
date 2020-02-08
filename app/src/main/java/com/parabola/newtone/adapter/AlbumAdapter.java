package com.parabola.newtone.adapter;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parabola.domain.model.Album;
import com.parabola.newtone.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.parabola.domain.utils.StringTool.getOrDefault;

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

        holder.albumTitle.setText(
                getOrDefault(albumItem.getTitle(), holder.albumTitle.getContext().getString(R.string.unknown_album)));

        holder.albumArtist.setText(
                getOrDefault(albumItem.getArtistName(), holder.albumArtist.getContext().getString(R.string.unknown_artist)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumItem.getId());
                Size size = new Size(500, 500);

                Bitmap albumArt = holder.itemView.getContext().getContentResolver()
                        .loadThumbnail(uri, size, null);

                holder.albumImage.setImageBitmap(albumArt);
            } catch (IOException ignored) {
            }
        } else {
            Glide.with(holder.albumImage)
                    .load(albumItem.getArtLink())
                    .placeholder(R.drawable.album_holder)
                    .into(holder.albumImage);
        }

    }

    @Override
    public char getSection(int position) {
        Album album = get(position);
        String albumName = getOrDefault(album.getTitle(), recyclerView.getContext().getString(R.string.unknown_album));

        return Character.toUpperCase(albumName.charAt(0));
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return showSection ? String.valueOf(getSection(position)) : "";
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.album_title) TextView albumTitle;
        @BindView(R.id.author) TextView albumArtist;
        @BindView(R.id.album_image) ImageView albumImage;

        private AlbumViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
