package com.parabola.search_feature

import com.parabola.domain.model.Album
import com.parabola.domain.model.Artist
import com.parabola.domain.model.Playlist
import com.parabola.domain.model.Track
import io.reactivex.Single

val EMPTY_SEARCH_RESULT = SearchResult(emptyList(), emptyList(), emptyList(), emptyList())

const val ARTISTS_SEARCH_MAX_LIMIT = 3L
const val ALBUMS_SEARCH_MAX_LIMIT = 5L
const val TRACKS_SEARCH_MAX_LIMIT = 20L
const val PLAYLIST_SEARCH_MAX_LIMIT = 5L

abstract class SearchInteractor {
    fun search(query: String): Single<SearchResult> {
        return Single.zip(
            searchArtists(query),
            searchAlbums(query),
            searchTracks(query),
            searchPlaylists(query)
        ) { artists: List<Artist>, albums: List<Album>, tracks: List<Track>, playlists: List<Playlist> ->
            SearchResult(
                artists,
                albums,
                tracks,
                playlists
            )
        }
            .onErrorReturnItem(EMPTY_SEARCH_RESULT)
    }

    abstract fun searchArtists(query: String): Single<List<Artist>>
    abstract fun searchAlbums(query: String): Single<List<Album>>
    abstract fun searchTracks(query: String): Single<List<Track>>
    abstract fun searchPlaylists(query: String): Single<List<Playlist>>

}
