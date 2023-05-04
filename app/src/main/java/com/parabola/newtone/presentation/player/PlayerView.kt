package com.parabola.newtone.presentation.player

import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.model.Track
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface PlayerView : MvpView {
    fun setArtist(artistName: String)
    fun setAlbum(albumTitle: String)
    fun setTitle(trackTitle: String)
    fun setDurationText(durationFormatted: String)
    fun setDurationMs(durationMs: Int)
    fun setIsFavourite(isFavourite: Boolean)

    fun setPlaybackButtonAsPause()
    fun setPlaybackButtonAsPlay()
    fun setRepeatMode(repeatMode: PlayerInteractor.RepeatMode)
    fun setShuffleEnabling(enable: Boolean)

    fun setCurrentTimeMs(currentTimeMs: Int)

    fun setTimerButtonVisibility(visible: Boolean)

    fun setViewPagerSlide(lock: Boolean)

    fun refreshTracks(tracks: List<Track>)
    fun setAlbumImagePosition(currentTrackPosition: Int, smoothScroll: Boolean)

    //в градусах
    fun setTrackSettingsRotation(rotation: Float)

    fun setRootViewOpacity(alpha: Float)
    fun setRootViewVisibility(visible: Boolean)
}
