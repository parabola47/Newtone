package com.parabola.newtone.ui.router

import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.Gravity
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.parabola.domain.model.Track
import com.parabola.newtone.BuildConfig
import com.parabola.newtone.R
import com.parabola.newtone.presentation.DeleteTrackDialog
import com.parabola.newtone.presentation.album.AlbumFragment
import com.parabola.newtone.presentation.artist.ArtistFragment
import com.parabola.newtone.presentation.artisttracks.ArtistTracksFragment
import com.parabola.newtone.presentation.playlist.chooseplaylist.ChoosePlaylistDialog
import com.parabola.newtone.presentation.playlist.createplaylist.CreatePlaylistDialog
import com.parabola.newtone.presentation.folder.FolderFragment
import com.parabola.newtone.presentation.playlist.favourites.FavouritesPlaylistFragment
import com.parabola.newtone.presentation.playlist.folderslist.FoldersListFragment
import com.parabola.newtone.presentation.playlist.playlist.PlaylistFragment
import com.parabola.newtone.presentation.playlist.queue.QueueFragment
import com.parabola.newtone.presentation.playlist.recentlyadded.RecentlyAddedPlaylistFragment
import com.parabola.newtone.presentation.mainactivity.MainActivity
import com.parabola.newtone.ui.dialog.*
import com.parabola.newtone.ui.dialog.fx.AudioEffectsDialog
import com.parabola.newtone.ui.dialog.fx.EqPresetsSelectorDialog
import com.parabola.newtone.presentation.settings.dialog.IsaacNewtoneDialog
import com.parabola.newtone.presentation.main.start.StartFragment
import com.parabola.newtone.presentation.player.PlayerFragment
import com.parabola.newtone.presentation.player.timetosleepinfo.TimeToSleepInfoDialog
import com.parabola.newtone.presentation.playlist.renameplaylist.RenamePlaylistDialog
import com.parabola.newtone.presentation.search.SearchFragment
import com.parabola.newtone.presentation.settings.AlbumItemDisplaySettingFragment
import com.parabola.newtone.presentation.settings.ArtistItemDisplaySettingFragment
import com.parabola.newtone.presentation.settings.ColorThemeSelectorFragment
import com.parabola.newtone.presentation.settings.ExcludedFoldersFragment
import com.parabola.newtone.presentation.settings.SettingFragment
import com.parabola.newtone.presentation.settings.TrackItemDisplaySettingFragment
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.io.File
import java.util.*

class MainRouterImpl : MainRouter {
    private var activity: MainActivity? = null
    private var firstFragment: StartFragment? = null

    override fun setActivity(activity: MainActivity) {
        this.activity = activity

        firstFragment = getInstance(StartFragment::class.java)
        if (firstFragment == null) {
            firstFragment = StartFragment()
            activity.supportFragmentManager.beginTransaction()
                .add(R.id.nav_host_fragment, firstFragment!!)
                .commit()
        }
    }

    override fun clearActivity() {
        activity = null
        firstFragment = null
    }

    override fun showToast(toastText: String, longLength: Boolean, showAtCenter: Boolean) {
        val toast = Toast.makeText(
            activity,
            toastText,
            if (longLength) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        )
        if (showAtCenter) {
            toast.setGravity(Gravity.CENTER, 0, 0)
        }

        toast.show()
    }

    override fun currentFragment(): Fragment? {
        return if (isRoot) firstFragment?.currentSelectedFragment
        else activity?.supportFragmentManager?.primaryNavigationFragment
    }

    override fun collapseBottomSlider() {
        activity?.setBottomSliderPanelState(PanelState.COLLAPSED)
    }

    override fun goToTab(tabNumber: Int, smoothScroll: Boolean) {
        firstFragment?.goToTab(tabNumber, smoothScroll)
    }

    override fun scrollOnTabTrackToCurrentTrack() {
        firstFragment?.scrollOnTabTrackToCurrentTrack()
    }

    override fun goToArtistInTab(artistId: Int) {
        firstFragment?.scrollToArtistInTab(artistId)
    }

    override fun goToAlbumInTab(albumId: Int) {
        firstFragment?.scrollToAlbumInTab(albumId)
    }

    override fun <T : Fragment> hasInstanceInStack(fragment: Class<T>): Boolean {
        for (f in activity?.supportFragmentManager?.fragments ?: Collections.emptyList()) {
            if (fragment.isInstance(f)) {
                return true
            }
        }
        return false
    }

    private fun <T : Fragment> getInstance(fragment: Class<T>): T? {
        for (f in activity?.supportFragmentManager?.fragments ?: Collections.emptyList()) {
            if (fragment.isInstance(f)) {
                return fragment.cast(f)
            }
        }
        return null
    }

    override fun backToRoot() {
        while (!isRoot) {
            activity?.supportFragmentManager?.popBackStackImmediate()
        }
    }

    override val isRoot: Boolean
        get() = activity?.supportFragmentManager?.backStackEntryCount == 0

    override fun goBack() {
        activity?.supportFragmentManager?.popBackStackImmediate()
    }

    override fun openArtist(artistId: Int) {
        openFragment(ArtistFragment.newInstance(artistId))
    }

    override fun openArtistFromBackStackIfAvailable(artistId: Int) {
        while (!isRoot) {
            val currentFragment = currentFragment()
            if (currentFragment is ArtistFragment) {
                if (currentFragment.artistId == artistId) return
            }
            goBack()
        }
        openArtist(artistId)
    }

    override fun openAlbum(albumId: Int) {
        openFragment(AlbumFragment.newInstance(albumId))
    }

    override fun openAlbumFromBackStackIfAvailable(albumId: Int) {
        while (!isRoot) {
            val currentFragment = currentFragment()
            if (currentFragment is AlbumFragment) {
                if (currentFragment.albumId == albumId) return
            }
            goBack()
        }
        openAlbum(albumId)
    }

    override fun openPlaylist(playlistId: Int) {
        openFragment(PlaylistFragment.newInstance(playlistId))
    }

    override fun openRequestStoragePermissionScreen() {
        activity?.requestStoragePermission()
    }

    override fun openSearchScreen() {
        openFragment(SearchFragment.newInstance(), false)
    }

    override fun openSettings() {
        openFragment(SettingFragment())
    }

    override fun openSettingsIfAvailable() {
        while (!isRoot) {
            val currentFragment = currentFragment()
            if (currentFragment is SettingFragment) {
                return
            }
            goBack()
        }
        openSettings()
    }

    override fun openColorThemeSelectorSettings() {
        openFragment(ColorThemeSelectorFragment())
    }

    override fun openExcludedFolders() {
        val fragmentTransaction = activity!!.supportFragmentManager.beginTransaction()
        //закрываются все окна, за исключением настроек, плеера и начального экрана
        //чтобы после исключения папок не было ненужных ошибок
        for (fragment in activity?.supportFragmentManager?.fragments ?: Collections.emptyList()) {
            if (fragment !is SettingFragment
                && fragment !is StartFragment
                && fragment !is PlayerFragment
            ) {
                fragmentTransaction.remove(fragment)
            }
        }
        fragmentTransaction.commit()
        openFragment(ExcludedFoldersFragment.newInstance())
    }

    override fun openTrackItemDisplaySettings() {
        openFragment(TrackItemDisplaySettingFragment.newInstance())
    }

    override fun openAlbumItemDisplaySettings() {
        openFragment(AlbumItemDisplaySettingFragment.newInstance())
    }

    override fun openArtistItemDisplaySettings() {
        openFragment(ArtistItemDisplaySettingFragment.newInstance())
    }

    override fun openRecentlyAdded() {
        openFragment(RecentlyAddedPlaylistFragment())
    }

    override fun openFavourites() {
        openFragment(FavouritesPlaylistFragment())
    }

    override fun openFavouritesFromBackStackIfAvailable() {
        while (!isRoot) {
            val currentFragment = currentFragment()
            if (currentFragment is FavouritesPlaylistFragment) {
                return
            }
            goBack()
        }
        openFavourites()
    }

    override fun openQueue() {
        openFragment(QueueFragment())
    }

    override fun openQueueFromBackStackIfAvailable() {
        while (!isRoot) {
            val currentFragment = currentFragment()
            if (currentFragment is QueueFragment) {
                return
            }
            goBack()
        }
        openQueue()
    }

    override fun openFoldersList() {
        openFragment(FoldersListFragment())
    }

    override fun openFolder(folderPath: String) {
        openFragment(FolderFragment.newInstance(folderPath))
    }

    override fun openArtistTracks(artistId: Int) {
        openFragment(ArtistTracksFragment.newInstance(artistId))
    }

    override fun openCreatePlaylistDialog() {
        showDialogFragment(CreatePlaylistDialog.newInstance())
    }

    override fun openRenamePlaylistDialog(playlistId: Int) {
        showDialogFragment(RenamePlaylistDialog.newInstance(playlistId))
    }

    override fun openStartSleepTimerDialog() {
        showDialogFragment(SleepTimerDialog.newInstance())
    }

    override fun openSleepTimerInfoDialog() {
        showDialogFragment(TimeToSleepInfoDialog.newInstance())
    }

    override fun openAddToPlaylistDialog(vararg trackIds: Int) {
        showDialogFragment(ChoosePlaylistDialog.newInstance(*trackIds))
    }

    override fun openSortingDialog(sortingListType: String) {
        showDialogFragment(SortingDialog.newInstance(sortingListType))
    }

    override fun openTrackAdditionInfo(trackId: Int) {
        showDialogFragment(TrackAdditionalInfoDialog.newInstance(trackId))
    }

    override fun openAudioEffectsDialog() {
        showDialogFragment(AudioEffectsDialog())
    }

    override fun openEqPresetsSelectorDialog() {
        showDialogFragment(EqPresetsSelectorDialog())
    }

    override fun openNewtoneDialog() {
        showDialogFragment(IsaacNewtoneDialog())
    }

    override fun openDeleteTrackDialog(trackId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity!!.showRequestForDeletingTrack(trackId)
        } else {
            showDialogFragment(DeleteTrackDialog.newInstance(trackId))
        }
    }

    override fun openLyricsSearch(track: Track) {
        val searchQuery = String.format("%s %s lyrics", track.artistName, track.title)
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
            .putExtra(SearchManager.QUERY, searchQuery)
        startActivity(intent)
    }

    override fun openContactDevelopersViaEmail() {
        val sendMailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf("zizik.zizik@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Newtone. Android")
        }
        Intent.createChooser(
            sendMailIntent,
            activity!!.getString(R.string.setting_contact_developers_intent_title)
        )

        activity?.apply {
            val intentTitle = getString(R.string.setting_contact_developers_intent_title)
            startActivity(Intent.createChooser(sendMailIntent, intentTitle))
        }
    }

    override fun openPrivacyPolicyWebPage() {
        val url = "https://github.com/parabola47/privacy_policy/blob/master/newtone_pp.md"
        val intent = Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse(url))
        startActivity(intent)
    }

    override fun openShareTrack(filePath: String) {
        val uri = FileProvider.getUriForFile(
            activity!!.application,
            BuildConfig.APPLICATION_ID,
            File(filePath)
        )
        val share = Intent(Intent.ACTION_SEND)
            .putExtra(Intent.EXTRA_STREAM, uri)
            .setType("audio/*")
        val intent = Intent.createChooser(
            share,
            activity!!.getString(R.string.track_menu_share_track_intent_title)
        )
        startActivity(intent)
    }

    private fun startActivity(intent: Intent) {
        activity?.startActivity(intent)
    }

    private fun openFragment(fragment: Fragment, withAnimation: Boolean = true) {
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            if (withAnimation) {
                setCustomAnimations(
                    R.anim.anim_in,
                    R.anim.anim_out,
                    R.anim.anim_in,
                    R.anim.anim_out,
                )
            }
            add(R.id.nav_host_fragment, fragment)
            addToBackStack(null)
            setPrimaryNavigationFragment(fragment)
            commit()
        }
    }

    private fun showDialogFragment(dialog: DialogFragment) {
        activity?.supportFragmentManager?.let { dialog.show(it, null) }
    }

    private val slidePanelOffsetUpdates = BehaviorSubject.createDefault(0f)
    private val slidePanelStateUpdates = BehaviorSubject.createDefault(PanelState.COLLAPSED)
    override fun setBottomSlidePanelOffset(offset: Float) {
        if (offset < 0f || offset > 1f) {
            return
        }
        slidePanelOffsetUpdates.onNext(offset)
    }

    override fun observeSlidePanelOffset(): Observable<Float> = slidePanelOffsetUpdates

    override fun setBottomSlidePanelState(state: PanelState) {
        slidePanelStateUpdates.onNext(state)
    }

    override fun observeSlidePanelState(): Observable<PanelState> = slidePanelStateUpdates
}
