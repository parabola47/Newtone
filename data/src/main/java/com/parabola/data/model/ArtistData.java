package com.parabola.data.model;

import androidx.annotation.Nullable;

import com.parabola.domain.model.Artist;

public final class ArtistData implements Artist {
    public int id;
    public String name;

    public int albumsCount;
    public int tracksCount;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAlbumsCount() {
        return albumsCount;
    }

    @Override
    public int getTracksCount() {
        return tracksCount;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Artist)) {
            return false;
        }
        return equals((Artist) obj);
    }
}
