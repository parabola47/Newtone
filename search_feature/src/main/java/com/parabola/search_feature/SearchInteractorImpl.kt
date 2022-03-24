package com.parabola.search_feature

import com.parabola.domain.model.Album
import com.parabola.domain.model.Artist
import com.parabola.domain.model.Playlist
import com.parabola.domain.model.Track
import com.parabola.domain.repository.AlbumRepository
import com.parabola.domain.repository.ArtistRepository
import com.parabola.domain.repository.PlaylistRepository
import com.parabola.domain.repository.TrackRepository
import io.reactivex.Single
import org.simmetrics.StringMetric
import org.simmetrics.metrics.StringMetrics

class SearchInteractorImpl(
    private val artistRepo: ArtistRepository,
    private val albumRepo: AlbumRepository,
    private val trackRepo: TrackRepository,
    private val playlistRepo: PlaylistRepository,
) : SearchInteractor() {


    companion object {
        private const val artistMinimumThreshold = 0.8f
        private const val albumMinimumThreshold = 0.75f
        private const val trackMinimumThreshold = 0.7f
        private const val playlistMinimumThreshold = 0.8f
    }


    private val hasMatchCheckMetric: StringMetric = StringMetrics.smithWatermanGotoh()
    private val sortingMetric: StringMetric = StringMetrics.levenshtein()


    override fun searchArtists(query: String): Single<List<Artist>> {
        val validatedQuery = query.lowercase()
        val artistComparator = compareBy<Artist> {
            sortingMetric.compare(validatedQuery, it.searchView)
        }
            .reversed()

        return artistRepo.allAsObservable
            .filter {
                passMinimumThreshold(query, it.searchView, artistMinimumThreshold)
            }
            .sorted(artistComparator)
            .take(ARTISTS_SEARCH_MAX_LIMIT)
            .toList()
    }


    override fun searchAlbums(query: String): Single<List<Album>> {
        val validatedQuery = query.lowercase()
        val albumComparator = compareBy<Album> {
            sortingMetric.compare(validatedQuery, it.searchView)
        }
            .reversed()

        return albumRepo.allAsObservable
            .filter {
                passMinimumThreshold(validatedQuery, it.searchView, albumMinimumThreshold)
            }
            .sorted(albumComparator)
            .take(ALBUMS_SEARCH_MAX_LIMIT)
            .toList()
    }


    override fun searchTracks(query: String): Single<List<Track>> {
        val validatedQuery = query.lowercase()
        val trackComparator = compareBy<Track> {
            sortingMetric.compare(validatedQuery, it.searchView)
        }
            .reversed()

        return trackRepo.allAsObservable
            .filter {
                passMinimumThreshold(validatedQuery, it.searchView, trackMinimumThreshold)
            }
            .sorted(trackComparator)
            .take(TRACKS_SEARCH_MAX_LIMIT)
            .toList()
    }


    override fun searchPlaylists(query: String): Single<List<Playlist>> {
        val validatedQuery = query.lowercase()
        val playlistComparator = compareBy<Playlist> {
            sortingMetric.compare(validatedQuery, it.searchView)
        }
            .reversed()

        return playlistRepo.allAsObservable
            .filter {
                passMinimumThreshold(validatedQuery, it.searchView, playlistMinimumThreshold)
            }
            .sorted(playlistComparator)
            .take(PLAYLIST_SEARCH_MAX_LIMIT)
            .toList()
    }


    private fun passMinimumThreshold(
        firstString: String,
        secondString: String,
        minimumMatchThreshold: Float
    ): Boolean = hasMatchCheckMetric.compare(firstString, secondString) >= minimumMatchThreshold

}
