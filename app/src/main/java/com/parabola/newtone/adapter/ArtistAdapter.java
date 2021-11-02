package com.parabola.newtone.adapter;

import static androidx.core.content.ContextCompat.getColor;
import static com.parabola.newtone.util.AndroidTool.convertDpToPixel;
import static com.parabola.newtone.util.AndroidTool.getStyledColor;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.domain.model.Artist;
import com.parabola.domain.settings.ViewSettingsInteractor.ArtistItemView;
import com.parabola.newtone.R;
import com.parabola.newtone.databinding.ItemArtistBinding;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.Optional;


public final class ArtistAdapter extends SimpleListAdapter<Artist, ArtistAdapter.ArtistViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {


    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflateByViewType(parent.getContext(), R.layout.item_artist, parent);
        return new ArtistViewHolder(ItemArtistBinding.bind(view));
    }

    private ArtistItemView artistItemView;

    public void setViewSettings(ArtistItemView artistItemView) {
        this.artistItemView = artistItemView;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Context context = holder.itemView.getContext();

        Artist artistItem = get(holder.getAdapterPosition());

        if (artistItemView != null)
            buildItemLayout(holder);

        String artistName = Optional.ofNullable(artistItem.getName())
                .orElse(holder.binding.artist.getContext().getString(R.string.unknown_artist));
        holder.binding.artist.setText(artistName);

        holder.binding.artistInfo.setText(getTracksAndAlbumsCount(artistItem));

        if (isContextSelected(holder.getAdapterPosition())) {
            holder.binding.artist.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.binding.artistInfo.setTextColor(getColor(context, R.color.colorListItemSelectedText));
            holder.itemView.setBackgroundColor(getStyledColor(context, R.attr.colorPrimaryDark));
        } else {
            holder.binding.artist.setTextColor(getColor(context, R.color.colorNewtonePrimaryText));
            holder.binding.artistInfo.setTextColor(getColor(context, R.color.colorNewtoneSecondaryText));
            holder.itemView.setBackgroundColor(getColor(context, R.color.colorListItemDefaultBackground));
        }
    }

    private void buildItemLayout(ArtistViewHolder holder) {
        holder.binding.artist.setTextSize(artistItemView.textSize);
        holder.binding.artistInfo.setTextSize(artistItemView.textSize - 2);


        int paddingPx = (int) convertDpToPixel(artistItemView.borderPadding, holder.itemView.getContext());
        holder.itemView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
    }

    private String getTracksAndAlbumsCount(Artist artist) {
        int tracksCount = artist.getTracksCount();
        int albumsCount = artist.getAlbumsCount();

        String tracksCountStr = recyclerView.getResources()
                .getQuantityString(R.plurals.tracks_count, tracksCount, tracksCount);
        String albumsCountStr = recyclerView.getResources()
                .getQuantityString(R.plurals.albums_count, albumsCount, albumsCount);

        return recyclerView.getResources()
                .getString(R.string.artist_item_addition_info, albumsCountStr, tracksCountStr);
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

    static class ArtistViewHolder extends RecyclerView.ViewHolder {
        private final ItemArtistBinding binding;


        private ArtistViewHolder(ItemArtistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
