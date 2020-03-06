package com.parabola.domain.model;

import java.util.List;

public interface Playlist {
    int getId();
    String getTitle();
    long getDateAddingTimestamp();
    List<TrackItem> getPlaylistTracks();
    int size();

    default boolean equals(Playlist o) {
        return getId() == o.getId();
    }


    interface TrackItem {
        int getTrackId();
        long getAdditionDate();
    }

}
