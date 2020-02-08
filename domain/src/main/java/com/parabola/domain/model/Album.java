package com.parabola.domain.model;

public interface Album {
    int getId();
    String getTitle();
    String getArtLink();

    int getYear();
    int getArtistId();
    String getArtistName();

    int getTracksCount();

    default boolean equals(Album o) {
        return getId() == o.getId();
    }
}
