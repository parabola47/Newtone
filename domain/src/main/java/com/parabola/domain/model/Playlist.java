package com.parabola.domain.model;

import java.util.List;

public interface Playlist {
    int getId();
    String getTitle();
    List<TrackItem> getPlaylistTracks();
    int size();

    default String getSearchView() {
        return getTitle().toLowerCase();
    }

    default boolean equals(Playlist o) {
        return getId() == o.getId();
    }


    interface TrackItem {
        int getTrackId();
    }

}
