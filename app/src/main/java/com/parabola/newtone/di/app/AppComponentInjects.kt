package com.parabola.newtone.di.app

import com.parabola.newtone.presentation.audioeffects.settings.FxAudioSettingsPresenter
import com.parabola.newtone.presentation.audioeffects.equalizer.FxEqualizerPresenter
import com.parabola.newtone.presentation.album.AlbumPresenter
import com.parabola.newtone.presentation.artist.ArtistPresenter
import com.parabola.newtone.presentation.artisttracks.ArtistTracksPresenter
import com.parabola.newtone.presentation.playlist.chooseplaylist.ChoosePlaylistPresenter
import com.parabola.newtone.presentation.playlist.createplaylist.CreatePlaylistPresenter
import com.parabola.newtone.presentation.folder.FolderPresenter
import com.parabola.newtone.presentation.main.start.StartPresenter
import com.parabola.newtone.presentation.main.albums.TabAlbumPresenter
import com.parabola.newtone.presentation.main.artists.TabArtistPresenter
import com.parabola.newtone.presentation.main.playlists.TabPlaylistPresenter
import com.parabola.newtone.presentation.main.tracks.TabTrackPresenter
import com.parabola.newtone.presentation.player.PlayerPresenter
import com.parabola.newtone.presentation.playlist.favourites.FavouritesPlaylistPresenter
import com.parabola.newtone.presentation.playlist.folderslist.FoldersListPresenter
import com.parabola.newtone.presentation.playlist.playlist.PlaylistPresenter
import com.parabola.newtone.presentation.playlist.queue.QueuePresenter
import com.parabola.newtone.presentation.playlist.recentlyadded.RecentlyAddedPlaylistPresenter
import com.parabola.newtone.presentation.search.SearchPresenter
import com.parabola.newtone.presentation.mainactivity.MainActivity
import com.parabola.newtone.presentation.mainactivity.MainPresenter
import com.parabola.newtone.presentation.DeleteTrackDialog
import com.parabola.newtone.presentation.player.sleeptimer.SleepTimerPresenter
import com.parabola.newtone.presentation.player.timetosleepinfo.TimeToSleepInfoPresenter
import com.parabola.newtone.presentation.playlist.renameplaylist.RenamePlaylistPresenter
import com.parabola.newtone.presentation.SortingDialog
import com.parabola.newtone.presentation.audioeffects.EqPresetsSelectorDialog
import com.parabola.newtone.presentation.settings.AlbumItemDisplaySettingFragment
import com.parabola.newtone.presentation.settings.ArtistItemDisplaySettingFragment
import com.parabola.newtone.presentation.settings.ExcludedFoldersFragment
import com.parabola.newtone.presentation.settings.SettingPresenter
import com.parabola.newtone.presentation.settings.TrackItemDisplaySettingFragment
import com.parabola.newtone.presentation.settings.colorthemeselector.ColorThemeSelectorPresenter
import com.parabola.newtone.presentation.trackadditionalinfo.TrackAdditionalInfoPresenter

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
    fun inject(presenter: FxEqualizerPresenter)
    fun inject(presenter: FxAudioSettingsPresenter)
}
