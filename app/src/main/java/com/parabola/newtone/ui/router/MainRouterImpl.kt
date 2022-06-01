package com.parabola.newtone.ui.router

import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.parabola.domain.model.Track
import com.parabola.newtone.BuildConfig
import com.parabola.newtone.R
import com.parabola.newtone.ui.activity.MainActivity
import com.parabola.newtone.ui.dialog.*
import com.parabola.newtone.ui.dialog.fx.AudioEffectsDialog
import com.parabola.newtone.ui.dialog.fx.EqPresetsSelectorDialog
import com.parabola.newtone.ui.fragment.*
import com.parabola.newtone.ui.fragment.playlist.*
import com.parabola.newtone.ui.fragment.settings.*
import com.parabola.newtone.ui.fragment.settings.dialog.IsaacNewtoneDialog
import com.parabola.newtone.ui.fragment.start.StartFragment
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
        val artistFragment = ArtistFragment.newInstance(artistId)
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
            .add(R.id.nav_host_fragment, artistFragment)
            .addToBackStack(null)
            .setPrimaryNavigationFragment(artistFragment)
            .commit()
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
        val fragment = AlbumFragment.newInstance(albumId)
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
            .add(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .setPrimaryNavigationFragment(fragment)
            .commit()
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
        val playlistFragment = PlaylistFragment.newInstance(playlistId)
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
            .add(R.id.nav_host_fragment, playlistFragment)
            .addToBackStack(null)
            .setPrimaryNavigationFragment(playlistFragment)
            .commit()
    }

    override fun openRequestStoragePermissionScreen() {
        activity?.requestStoragePermission()
    }

    override fun openSearchScreen() {
        val searchFragment = SearchFragment.newInstance()
        activity!!.supportFragmentManager.beginTransaction()
            .add(R.id.nav_host_fragment, searchFragment)
            .addToBackStack(null)
            .setPrimaryNavigationFragment(searchFragment)
            .commit()
    }

    override fun openSettings() {
        val fragment = SettingFragment()
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
            .add(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .setPrimaryNavigationFragment(fragment)
            .commit()
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
        val fragment = ColorThemeSelectorFragment()
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
            .add(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .setPrimaryNavigationFragment(fragment)
            .commit()
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
        val fragment = ExcludedFoldersFragment.newInstance()
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
            .add(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .setPrimaryNavigationFragment(fragment)
            .commit()
    }

    override fun openTrackItemDisplaySettings() {
        val fragment = TrackItemDisplaySettingFragment.newInstance()
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
            .add(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .setPrimaryNavigationFragment(fragment)
            .commit()
    }

    override fun openAlbumItemDisplaySettings() {
        val fragment = AlbumItemDisplaySettingFragment.newInstance()
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
            .add(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .setPrimaryNavigationFragment(fragment)
            .commit()
    }

    override fun openArtistItemDisplaySettings() {
        val fragment = ArtistItemDisplaySettingFragment.newInstance()
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
            .add(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .setPrimaryNavigationFragment(fragment)
            .commit()
    }

    override fun openRecentlyAdded() {
        val fragment = RecentlyAddedPlaylistFragment()
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
            .add(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .setPrimaryNavigationFragment(fragment)
            .commit()
    }

    override fun openFavourites() {
        val fragment = FavoritesPlaylistFragment()
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
            .add(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .setPrimaryNavigationFragment(fragment)
            .commit()
    }

    override fun openFavouritesFromBackStackIfAvailable() {
        while (!isRoot) {
            val currentFragment = currentFragment()
            if (currentFragment is FavoritesPlaylistFragment) {
                return
            }
            goBack()
        }
        openFavourites()
    }

    override fun openQueue() {
        val fragment = QueueFragment()
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
            .add(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .setPrimaryNavigationFragment(fragment)
            .commit()
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
        val fragment = FoldersListFragment()
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
            .add(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .setPrimaryNavigationFragment(fragment)
            .commit()
    }

    override fun openFolder(folderPath: String) {
        val args = Bundle()
        args.putString("folderPath", folderPath)
        val fragment = FolderFragment()
        fragment.arguments = args
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
            .add(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .setPrimaryNavigationFragment(fragment)
            .commit()
    }

    override fun openArtistTracks(artistId: Int) {
        val args = Bundle()
        args.putInt("artistId", artistId)
        val fragment = ArtistTracksFragment()
        fragment.arguments = args
        activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
            .add(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .setPrimaryNavigationFragment(fragment)
            .commit()
    }

    override fun openCreatePlaylistDialog() {
        val dialogFragment = CreatePlaylistDialog.newInstance()
        dialogFragment.show(activity!!.supportFragmentManager, null)
    }

    override fun openRenamePlaylistDialog(playlistId: Int) {
        val dialogFragment = RenamePlaylistDialog.newInstance(playlistId)
        dialogFragment.show(activity!!.supportFragmentManager, null)
    }

    override fun openStartSleepTimerDialog() {
        val dialogFragment = SleepTimerDialog.newInstance()
        dialogFragment.show(activity!!.supportFragmentManager, null)
    }

    override fun openSleepTimerInfoDialog() {
        val dialogFragment = TimeToSleepInfoDialog.newInstance()
        dialogFragment.show(activity!!.supportFragmentManager, null)
    }

    override fun openAddToPlaylistDialog(vararg trackIds: Int) {
        val dialogFragment = ChoosePlaylistDialog.newInstance(*trackIds)
        dialogFragment.show(activity!!.supportFragmentManager, null)
    }

    override fun openSortingDialog(sortingListType: String) {
        val dialogFragment = SortingDialog.newInstance(sortingListType)
        dialogFragment.show(activity!!.supportFragmentManager, null)
    }

    override fun openTrackAdditionInfo(trackId: Int) {
        val dialogFragment = TrackAdditionalInfoDialog.newInstance(trackId)
        dialogFragment.show(activity!!.supportFragmentManager, null)
    }

    override fun openAudioEffectsDialog() {
        val dialog = AudioEffectsDialog()
        dialog.show(activity!!.supportFragmentManager, null)
    }

    override fun openEqPresetsSelectorDialog() {
        val dialog = EqPresetsSelectorDialog()
        dialog.show(activity!!.supportFragmentManager, null)
    }

    override fun openNewtoneDialog() {
        val dialog = IsaacNewtoneDialog()
        dialog.show(activity!!.supportFragmentManager, null)
    }

    override fun openDeleteTrackDialog(trackId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity!!.showRequestForDeletingTrack(trackId)
        } else {
            val dialog = DeleteTrackDialog.newInstance(trackId)
            dialog.show(activity!!.supportFragmentManager, null)
        }
    }

    override fun openLyricsSearch(track: Track) {
        val searchQuery = String.format("%s %s lyrics", track.artistName, track.title)
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
            .putExtra(SearchManager.QUERY, searchQuery)
        activity!!.startActivity(intent)
    }

    override fun openContactDevelopersViaEmail() {
        val sendMailIntent = Intent(Intent.ACTION_SENDTO)
        sendMailIntent.data = Uri.parse("mailto:") // only email apps should handle this
        sendMailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("zizik.zizik@gmail.com"))
        sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, "Newtone. Android")
        val intentTitle = activity!!.getString(R.string.setting_contact_developers_intent_title)
        activity!!.startActivity(Intent.createChooser(sendMailIntent, intentTitle))
    }

    override fun openPrivacyPolicyWebPage() {
        val url = "https://github.com/parabola47/privacy_policy/blob/master/newtone_pp.md"
        val i = Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse(url))
        activity?.startActivity(i)
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
        activity!!.startActivity(
            Intent.createChooser(
                share,
                activity!!.getString(R.string.track_menu_share_track_intent_title)
            )
        )
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