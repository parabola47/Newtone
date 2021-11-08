package com.parabola.domain.model;

public interface Artist {
    int getId();
    String getName();

    int getAlbumsCount();
    int getTracksCount();

    default String getSearchView() {
        return getName().toLowerCase();
    }

    default boolean equals(Artist o) {
        return getId() == o.getId();
    }
}
