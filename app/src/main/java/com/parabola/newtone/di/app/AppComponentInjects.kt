package com.parabola.newtone.di.app

import com.parabola.newtone.mvp.presenter.*
import com.parabola.newtone.mvp.presenter.fx.FxAudioSettingsPresenter
import com.parabola.newtone.mvp.presenter.fx.TabEqualizerPresenter
import com.parabola.newtone.presentation.playlist.favourites.FavouritesPlaylistPresenter
import com.parabola.newtone.ui.activity.MainActivity
import com.parabola.newtone.ui.dialog.DeleteTrackDialog
import com.parabola.newtone.ui.dialog.SortingDialog
import com.parabola.newtone.ui.dialog.fx.EqPresetsSelectorDialog
import com.parabola.newtone.ui.fragment.settings.AlbumItemDisplaySettingFragment
import com.parabola.newtone.ui.fragment.settings.ArtistItemDisplaySettingFragment
import com.parabola.newtone.ui.fragment.settings.ExcludedFoldersFragment
import com.parabola.newtone.ui.fragment.settings.TrackItemDisplaySettingFragment

interface AppComponentInjects {
    fun inject(activity: MainActivity)
    fun inject(presenter: MainPresenter)

    //    FIRST SCREENS
    fun inject(presenter: StartPresenter)
    fun inject(presenter: TabArtistPresenter)
    fun inject(presenter: TabAlbumPresenter)
    fun inject(presenter: TabTrackPresenter)
    fun inject(presenter: TabPlaylistPresenter)
    fun inject(presenter: ArtistPresenter)
    fun inject(presenter: AlbumPresenter)
    fun inject(presenter: ArtistTracksPresenter)
    fun inject(presenter: PlayerPresenter)
    fun inject(presenter: FolderPresenter)
    fun inject(presenter: SearchPresenter)

    //  SETTINGS
    fun inject(presenter: SettingPresenter)
    fun inject(presenter: ColorThemeSelectorPresenter)
    fun inject(fragment: ExcludedFoldersFragment)
    fun inject(fragment: TrackItemDisplaySettingFragment)
    fun inject(fragment: AlbumItemDisplaySettingFragment)
    fun inject(fragment: ArtistItemDisplaySettingFragment)

    //  PLAYLISTS
    fun inject(presenter: PlaylistPresenter)
    fun inject(presenter: FavouritesPlaylistPresenter)
    fun inject(presenter: RecentlyAddedPlaylistPresenter)
    fun inject(presenter: QueuePresenter)
    fun inject(presenter: FoldersListPresenter)

    //    DIALOGS
    fun inject(presenter: CreatePlaylistPresenter)
    fun inject(presenter: RenamePlaylistPresenter)
    fun inject(presenter: SleepTimerPresenter)
    fun inject(presenter: TimeToSleepInfoPresenter)
    fun inject(presenter: ChoosePlaylistPresenter)
    fun inject(dialog: SortingDialog)
    fun inject(dialog: EqPresetsSelectorDialog)
    fun inject(presenter: TrackAdditionalInfoPresenter)
    fun inject(dialog: DeleteTrackDialog)

    //    FXs
    fun inject(presenter: TabEqualizerPresenter)
    fun inject(presenter: FxAudioSettingsPresenter)
}
