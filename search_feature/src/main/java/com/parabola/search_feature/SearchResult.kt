package com.parabola.search_feature

import com.parabola.domain.model.Album
import com.parabola.domain.model.Artist
import com.parabola.domain.model.Playlist
import com.parabola.domain.model.Track
import java.util.*

class SearchResult(
    artists: List<Artist>,
    albums: List<Album>,
    tracks: List<Track>,
    playlists: List<Playlist>
) {
    @JvmField
    val artists: List<Artist> = Collections.unmodifiableList(artists)

    @JvmField
    val albums: List<Album> = Collections.unmodifiableList(albums)

    @JvmField
    val tracks: List<Track> = Collections.unmodifiableList(tracks)

    @JvmField
    val playlists: List<Playlist> = Collections.unmodifiableList(playlists)
}
