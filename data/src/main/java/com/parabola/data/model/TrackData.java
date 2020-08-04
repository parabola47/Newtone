package com.parabola.data.model;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.parabola.domain.model.Track;

import java.util.function.Function;
import java.util.function.Predicate;

public final class TrackData implements Track {
    public int id;
    public String title;

    public int albumId;
    public String albumTitle;

    public int artistId;
    public String artistName;

    public long durationMs;

    public long dateAdded;
    public int positionInCd;

    public String filePath;
    public int fileSize;    //в байтах

    public int year;

    public Predicate<TrackData> isFavouriteCondition;
    public Function<TrackData, Bitmap> getArtFunction;

    public Function<TrackData, Integer> getGenreIdFunction;
    public Function<TrackData, String> getGenreNameFunction;

    public Function<TrackData, Integer> getBitrateFunction;
    public Function<TrackData, Integer> getSampleRateFunction;


    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getAlbumId() {
        return albumId;
    }

    @Override
    public String getAlbumTitle() {
        return albumTitle;
    }

    @Override
    public Bitmap getArtImage() {
        return getArtFunction.apply(this);
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
    public long getDurationMs() {
        return durationMs;
    }

    @Override
    public long getDateAdded() {
        return dateAdded;
    }

    @Override
    public int getPositionInCd() {
        return positionInCd;
    }

    @Override
    public int getGenreId() {
        return getGenreIdFunction.apply(this);
    }

    @Override
    public String getGenreName() {
        return getGenreNameFunction.apply(this);
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public int getFileSize() {
        return fileSize;
    }

    @Override
    public int getBitrate() {
        return getBitrateFunction.apply(this);
    }

    @Override
    public int getSampleRate() {
        return getSampleRateFunction.apply(this);
    }

    @Override
    public int getYear() {
        return year;
    }

    @Override
    public boolean isFavourite() {
        return isFavouriteCondition.test(this);
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Track)) {
            return false;
        }
        return equals((Track) obj);
    }
}
