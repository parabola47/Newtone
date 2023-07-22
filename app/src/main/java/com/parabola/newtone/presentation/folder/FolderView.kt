package com.parabola.newtone.presentation.folder

import com.parabola.domain.model.Track
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface FolderView : MvpView {
    fun setFolderPath(folderPath: String)

    fun refreshTracks(tracks: List<Track>)

    fun setItemViewSettings(viewSettings: TrackItemView)
    fun setItemDividerShowing(showed: Boolean)
    fun setSectionShowing(enable: Boolean)

    fun setCurrentTrack(trackId: Int)
}
