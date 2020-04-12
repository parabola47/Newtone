package com.parabola.newtone.di.app;

import com.parabola.newtone.mvp.presenter.AlbumPresenter;
import com.parabola.newtone.mvp.presenter.ArtistPresenter;
import com.parabola.newtone.mvp.presenter.ArtistTracksPresenter;
import com.parabola.newtone.mvp.presenter.ChoosePlaylistPresenter;
import com.parabola.newtone.mvp.presenter.CreatePlaylistPresenter;
import com.parabola.newtone.mvp.presenter.DeletePlaylistPresenter;
import com.parabola.newtone.mvp.presenter.FavouritesPlaylistPresenter;
import com.parabola.newtone.mvp.presenter.FolderPresenter;
import com.parabola.newtone.mvp.presenter.FoldersListPresenter;
import com.parabola.newtone.mvp.presenter.MainPresenter;
import com.parabola.newtone.mvp.presenter.PlayerPresenter;
import com.parabola.newtone.mvp.presenter.PlaylistPresenter;
import com.parabola.newtone.mvp.presenter.QueuePresenter;
import com.parabola.newtone.mvp.presenter.RecentlyAddedPlaylistPresenter;
import com.parabola.newtone.mvp.presenter.RenamePlaylistPresenter;
import com.parabola.newtone.mvp.presenter.SearchPresenter;
import com.parabola.newtone.mvp.presenter.SettingPresenter;
import com.parabola.newtone.mvp.presenter.SleepTimerPresenter;
import com.parabola.newtone.mvp.presenter.StartPresenter;
import com.parabola.newtone.mvp.presenter.TabAlbumPresenter;
import com.parabola.newtone.mvp.presenter.TabArtistPresenter;
import com.parabola.newtone.mvp.presenter.TabPlaylistPresenter;
import com.parabola.newtone.mvp.presenter.TabTrackPresenter;
import com.parabola.newtone.mvp.presenter.TimeToSleepInfoPresenter;
import com.parabola.newtone.mvp.presenter.TrackAdditionalInfoPresenter;
import com.parabola.newtone.mvp.presenter.fx.FxAudioSettingsPresenter;
import com.parabola.newtone.mvp.presenter.fx.TabEqualizerPresenter;
import com.parabola.newtone.ui.activity.MainActivity;
import com.parabola.newtone.ui.dialog.SortingDialog;
import com.parabola.newtone.ui.fragment.settings.AlbumItemDisplaySettingFragment;
import com.parabola.newtone.ui.fragment.settings.ArtistItemDisplaySettingFragment;
import com.parabola.newtone.ui.fragment.settings.TrackItemDisplaySettingFragment;

public interface AppComponentInjects {

    void inject(MainActivity activity);

    void inject(MainPresenter presenter);

    //    FIRST SCREENS
    void inject(StartPresenter presenter);
    void inject(TabArtistPresenter presenter);
    void inject(TabAlbumPresenter presenter);
    void inject(TabTrackPresenter presenter);
    void inject(TabPlaylistPresenter presenter);


    void inject(ArtistPresenter presenter);
    void inject(AlbumPresenter presenter);
    void inject(ArtistTracksPresenter presenter);
    void inject(PlayerPresenter presenter);
    void inject(FolderPresenter presenter);
    void inject(SearchPresenter presenter);


    //  SETTINGS
    void inject(SettingPresenter presenter);
    void inject(TrackItemDisplaySettingFragment fragment);
    void inject(AlbumItemDisplaySettingFragment fragment);
    void inject(ArtistItemDisplaySettingFragment fragment);


    //  PLAYLISTS
    void inject(PlaylistPresenter presenter);
    void inject(FavouritesPlaylistPresenter presenter);
    void inject(RecentlyAddedPlaylistPresenter presenter);
    void inject(QueuePresenter presenter);
    void inject(FoldersListPresenter presenter);


    //    DIALOGS
    void inject(CreatePlaylistPresenter presenter);
    void inject(RenamePlaylistPresenter presenter);
    void inject(DeletePlaylistPresenter presenter);
    void inject(SleepTimerPresenter presenter);
    void inject(TimeToSleepInfoPresenter presenter);
    void inject(ChoosePlaylistPresenter presenter);
    void inject(SortingDialog dialog);
    void inject(TrackAdditionalInfoPresenter presenter);


    //    FXs
    void inject(TabEqualizerPresenter presenter);
    void inject(FxAudioSettingsPresenter presenter);
}
