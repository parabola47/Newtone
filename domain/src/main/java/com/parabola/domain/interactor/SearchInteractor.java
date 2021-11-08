package com.parabola.domain.interactor;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import com.parabola.domain.model.Album;
import com.parabola.domain.model.Artist;
import com.parabola.domain.model.Playlist;
import com.parabola.domain.model.Track;

import java.util.List;

import io.reactivex.Single;

public abstract class SearchInteractor {

    protected static final int ARTISTS_SEARCH_MAX_LIMIT = 3;
    protected static final int ALBUMS_SEARCH_MAX_LIMIT = 5;
    protected static final int TRACKS_SEARCH_MAX_LIMIT = 20;
    protected static final int PLAYLIST_SEARCH_MAX_LIMIT = 5;


    public final Single<SearchResult> search(String query) {
        return Single.zip(searchArtists(query), searchAlbums(query), searchTracks(query), searchPlaylists(query), SearchResult::new)
                .onErrorReturnItem(SearchResult.EMPTY_SEARCH_RESULT);
    }


    public abstract Single<List<Artist>> searchArtists(String query);

    public abstract Single<List<Album>> searchAlbums(String query);

    public abstract Single<List<Track>> searchTracks(String query);

    public abstract Single<List<Playlist>> searchPlaylists(String query);


    public static final class SearchResult {
        private static final SearchResult EMPTY_SEARCH_RESULT
                = new SearchResult(emptyList(), emptyList(), emptyList(), emptyList());

        public final List<Artist> artists;
        public final List<Album> albums;
        public final List<Track> tracks;
        public final List<Playlist> playlists;

        private SearchResult(List<Artist> artists, List<Album> albums, List<Track> tracks, List<Playlist> playlists) {
            this.artists = unmodifiableList(artists);
            this.albums = unmodifiableList(albums);
            this.tracks = unmodifiableList(tracks);
            this.playlists = unmodifiableList(playlists);
        }
    }

}
