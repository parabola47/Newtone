package com.parabola.domain.interactor;

import com.parabola.domain.model.Album;
import com.parabola.domain.model.Artist;
import com.parabola.domain.model.Playlist;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.AlbumRepository;
import com.parabola.domain.repository.ArtistRepository;
import com.parabola.domain.repository.PlaylistRepository;
import com.parabola.domain.repository.TrackRepository;

import java.util.List;

import io.reactivex.Single;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

public final class SearchInteractor {

    private static final int ARTISTS_SEARCH_MAX_LIMIT = 3;
    private static final int ALBUMS_SEARCH_MAX_LIMIT = 5;
    private static final int TRACKS_SEARCH_MAX_LIMIT = 20;
    private static final int PLAYLIST_SEARCH_MAX_LIMIT = 5;

    private final ArtistRepository artistRepo;
    private final AlbumRepository albumRepo;
    private final TrackRepository trackRepo;
    private final PlaylistRepository playlistRepo;


    public SearchInteractor(ArtistRepository artistRepo,
                            AlbumRepository albumRepo,
                            TrackRepository trackRepo,
                            PlaylistRepository playlistRepo) {
        this.artistRepo = artistRepo;
        this.albumRepo = albumRepo;
        this.trackRepo = trackRepo;
        this.playlistRepo = playlistRepo;
    }


    public Single<SearchResult> search(String query) {
        return Single.zip(searchArtists(query), searchAlbums(query), searchTracks(query), searchPlaylists(query), SearchResult::new)
                .onErrorReturnItem(SearchResult.EMPTY_SEARCH_RESULT);
    }


    private Single<List<Artist>> searchArtists(String query) {
        return artistRepo.getByQuery(query, ARTISTS_SEARCH_MAX_LIMIT);
    }

    private Single<List<Album>> searchAlbums(String query) {
        return albumRepo.getByQuery(query, ALBUMS_SEARCH_MAX_LIMIT);
    }

    private Single<List<Track>> searchTracks(String query) {
        return trackRepo.getByQuery(query, TRACKS_SEARCH_MAX_LIMIT);
    }

    private Single<List<Playlist>> searchPlaylists(String query) {
        return playlistRepo.getByQuery(query, PLAYLIST_SEARCH_MAX_LIMIT);
    }


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
