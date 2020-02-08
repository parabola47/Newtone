package com.parabola.newtone.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.domain.model.Artist;
import com.parabola.newtone.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.parabola.domain.utils.StringTool.getOrDefault;

public final class ArtistAdapter extends SimpleListAdapter<Artist, ArtistAdapter.ArtistViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {


    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflateByViewType(parent.getContext(), R.layout.item_artist, parent);

        return new ArtistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        Artist artistItem = get(holder.getAdapterPosition());

        holder.artistTxt.setText(
                getOrDefault(artistItem.getName(), holder.artistTxt.getContext().getString(R.string.unknown_artist)));

        holder.artistInfo.setText(getTracksAndAlbumsCount(artistItem));
    }

    private String getTracksAndAlbumsCount(Artist artist) {
        int tracksCount = artist.getTracksCount();
        int albumsCount = artist.getAlbumsCount();

        String tracksCountStr =
                recyclerView.getContext().getResources().getQuantityString(R.plurals.tracks_count, tracksCount, tracksCount);
        String albumsCountStr =
                recyclerView.getContext().getResources().getQuantityString(R.plurals.albums_count, albumsCount, albumsCount);

        return String.format("%s, %s", albumsCountStr, tracksCountStr);
    }

    @Override
    public char getSection(int position) {
        Artist artist = get(position);

        return Character.toUpperCase(artist.getName() != null
                ? artist.getName().charAt(0)
                : recyclerView.getContext().getString(R.string.unknown_artist).charAt(0));
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return showSection ? String.valueOf(getSection(position)) : "";
    }

    public class ArtistViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.artist) TextView artistTxt;
        @BindView(R.id.artist_info) TextView artistInfo;

        public ArtistViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
