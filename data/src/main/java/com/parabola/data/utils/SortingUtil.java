package com.parabola.data.utils;

import com.parabola.domain.model.Album;
import com.parabola.domain.model.Artist;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.AlbumRepository;
import com.parabola.domain.repository.ArtistRepository;
import com.parabola.domain.repository.TrackRepository;

import java.util.Comparator;

import static java.util.Comparator.comparing;

public final class SortingUtil {

    private SortingUtil() {
        throw new IllegalAccessError();
    }


    public static Comparator<Track> getTrackComparatorBySorting(TrackRepository.Sorting sorting) {
        if (sorting == null) return comparing(Track::getTitle);

        switch (sorting) {
            case BY_TITLE: return comparing(Track::getTitle);
            case BY_TITLE_DESC: return comparing(Track::getTitle).reversed();
            case BY_ARTIST: return comparing(Track::getArtistName);
            case BY_ARTIST_DESC: return comparing(Track::getArtistName).reversed();
            case BY_DURATION: return comparing(Track::getDurationMs);
            case BY_DURATION_DESC: return comparing(Track::getDurationMs).reversed();
            case BY_DATE_ADDING: return comparing(Track::getDateAdded);
            case BY_DATE_ADDING_DESC: return comparing(Track::getDateAdded).reversed();
            case BY_ALBUM_POSITION: return comparing(Track::getPositionInCd);
            case BY_ALBUM_POSITION_DESC: return comparing(Track::getPositionInCd).reversed();
            default: throw new IllegalArgumentException("variable 'sorting' has incorrect value");
        }
    }


    public static Comparator<Album> getAlbumComparatorBySorting(AlbumRepository.Sorting sorting) {
        if (sorting == null) return comparing(Album::getTitle);

        switch (sorting) {
            case BY_TITLE: return comparing(Album::getTitle);
            case BY_TITLE_DESC: return comparing(Album::getTitle).reversed();
            case BY_YEAR: return comparing(Album::getYear);
            case BY_YEAR_DESC: return comparing(Album::getYear).reversed();
            case BY_ARTIST: return comparing(Album::getArtistName).thenComparing(Album::getTitle);
            case BY_ARTIST_DESC: return comparing(Album::getArtistName).reversed().thenComparing(Album::getTitle);
            default: throw new IllegalArgumentException("variable 'sorting' has incorrect value");
        }
    }


    public static Comparator<Artist> getArtistComparatorBySorting(ArtistRepository.Sorting sorting) {
        if (sorting == null) return comparing(Artist::getName);

        switch (sorting) {
            case BY_NAME: return comparing(Artist::getName);
            case BY_NAME_DESC: return comparing(Artist::getName).reversed();
            case BY_TRACKS_COUNT: return comparing(Artist::getTracksCount);
            case BY_TRACKS_COUNT_DESC: return comparing(Artist::getTracksCount).reversed();
            default: throw new IllegalArgumentException("variable 'sorting' has incorrect value");
        }
    }

}
