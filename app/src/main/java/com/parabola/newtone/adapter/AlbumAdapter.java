package com.parabola.newtone.adapter;

import android.graphics.Bitmap;
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
        ;
        Glide.with(holder.albumImage)
                .load((Bitmap) albumItem.getArtImage())
                .placeholder(R.drawable.album_holder)
                .into(holder.albumImage);
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
