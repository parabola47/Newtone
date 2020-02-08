package com.parabola.data.model;

import androidx.annotation.Nullable;

import com.parabola.domain.model.Track;

import java8.util.function.Function;
import java8.util.function.Predicate;

public final class TrackData implements Track {
    public int id;
    public String title;

    public long dateAddingTimestamp;

    public int albumId;
    public String albumTitle;

    public int artistId;
    public String artistName;

    public long durationMs;

    public int positionInCd;

    public String filePath;

    public Predicate<TrackData> isFavouriteCondition;
    public Function<TrackData, Long> favouriteTimeStampFunction;
    public Function<TrackData, String> getArtLinkFunction;


    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public long getDateAddingTimestamp() {
        return dateAddingTimestamp;
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
    public String getArtLink() {
        return getArtLinkFunction.apply(this);
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
    public int getPositionInCd() {
        return positionInCd;
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public boolean isFavourite() {
        return isFavouriteCondition.test(this);
    }

    @Override
    public Long getFavouriteTimestamp() {
        return favouriteTimeStampFunction.apply(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Track)) {
            return false;
        }
        return equals((Track) obj);
    }
}
