package com.parabola.data.model;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.parabola.domain.model.Album;

import java8.util.function.Function;

public final class AlbumData implements Album {
    public int id;
    public String title;
    public Function<AlbumData, Bitmap> getArtFunction;

    public int year;

    public int artistId;
    public String artistName;

    public int tracksCount;


    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Bitmap getArtImage() {
        return getArtFunction.apply(this);
    }

    @Override
    public int getYear() {
        return year;
    }

    @Override
    public int getArtistId() {
        return artistId;
    }

    @Override
    public String getArtistName() {
        return artistName;
    }

    @Override
    public int getTracksCount() {
        return tracksCount;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Album)) {
            return false;
        }
        return equals((Album) obj);
    }
}
