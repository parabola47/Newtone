package com.parabola.newtone.mvp.view

import com.parabola.domain.model.Track
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

@AddToEndSingle
interface QueueView : MvpView {
    fun setCurrentTrackPosition(currentTrackPosition: Int)
    fun refreshTracks(tracks: List<Track>)
    fun setTrackCount(tracksCount: Int)

    fun setItemViewSettings(viewSettings: TrackItemView)
    fun setItemDividerShowing(showed: Boolean)

    @OneExecution
    fun goToItem(itemPosition: Int)
}
