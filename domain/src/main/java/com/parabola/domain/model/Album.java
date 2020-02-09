package com.parabola.domain.model;

public interface Album {
    int getId();
    String getTitle();
    <ArtImage> ArtImage getArtImage();

    int getYear();
    int getArtistId();
    String getArtistName();

    int getTracksCount();

    default boolean equals(Album o) {
        return getId() == o.getId();
    }
}
