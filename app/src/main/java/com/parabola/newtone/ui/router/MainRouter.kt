package com.parabola.newtone.ui.router

import androidx.annotation.FloatRange
import androidx.fragment.app.Fragment
import com.parabola.domain.model.Track
import com.parabola.newtone.presentation.mainactivity.MainActivity
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import io.reactivex.Observable

interface MainRouter {

    fun setActivity(activity: MainActivity)
    fun clearActivity()

    fun showToast(toastText: String) = showToast(toastText, longLength = false)
    fun showToast(toastText: String, longLength: Boolean = false) =
        showToast(toastText, longLength, showAtCenter = false)

    // после перехода на котлин всех классов, использующих этот метод можно будет убрать верхние
    // дефолт-методы
    fun showToast(
        toastText: String,
        longLength: Boolean = false,
        showAtCenter: Boolean = false
    )

    fun currentFragment(): Fragment?

    //    ACTIONS
    fun collapseBottomSlider()
    fun goToTab(tabNumber: Int, smoothScroll: Boolean = false)
    fun scrollOnTabTrackToCurrentTrack()
    fun goToArtistInTab(artistId: Int)
    fun goToAlbumInTab(albumId: Int)

    //offset - от 0 до 1. при 0 - панель полностью закрыта, при 1 - панель полностью раскрыта
    fun setBottomSlidePanelOffset(@FloatRange(from = 0.0, to = 1.0) offset: Float)
    fun observeSlidePanelOffset(): Observable<Float>
    fun setBottomSlidePanelState(state: PanelState)
    fun observeSlidePanelState(): Observable<PanelState>

    fun <F : Fragment> hasInstanceInStack(fragment: Class<F>): Boolean
    fun backToRoot()
    val isRoot: Boolean
    fun goBack()

    //    FROM START
    fun openArtist(artistId: Int)
    fun openArtistFromBackStackIfAvailable(artistId: Int)
    fun openAlbum(albumId: Int)
    fun openAlbumFromBackStackIfAvailable(albumId: Int)
    fun openPlaylist(playlistId: Int)
    fun openRequestStoragePermissionScreen()
    fun openSearchScreen()

    //    SYSTEM PLAYLISTS
    fun openRecentlyAdded()
    fun openFavourites()
    fun openFavouritesFromBackStackIfAvailable()
    fun openQueue()
    fun openQueueFromBackStackIfAvailable()
    fun openFoldersList()

    //    SETTINGS
    fun openSettings()
    fun openSettingsIfAvailable()
    fun openColorThemeSelectorSettings()
    fun openExcludedFolders()
    fun openTrackItemDisplaySettings()
    fun openAlbumItemDisplaySettings()
    fun openArtistItemDisplaySettings()

    //    FROM FOLDERS LIST
    fun openFolder(folderPath: String)

    //    FROM ARTIST
    fun openArtistTracks(artistId: Int)

    //    DIALOGUES
    fun openCreatePlaylistDialog()
    fun openRenamePlaylistDialog(playlistId: Int)
    fun openStartSleepTimerDialog()
    fun openSleepTimerInfoDialog()
    fun openAddToPlaylistDialog(vararg trackIds: Int)
    fun openSortingDialog(sortingListType: String)
    fun openTrackAdditionInfo(trackId: Int)
    fun openAudioEffectsDialog()
    fun openEqPresetsSelectorDialog()
    fun openNewtoneDialog()
    fun openDeleteTrackDialog(trackId: Int)

    //    COMMUNICATION WITH OTHER APPS
    fun openLyricsSearch(track: Track)
    fun openContactDevelopersViaEmail()
    fun openPrivacyPolicyWebPage()
    fun openShareTrack(filePath: String)
}
