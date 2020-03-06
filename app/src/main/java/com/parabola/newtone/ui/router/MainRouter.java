package com.parabola.newtone.ui.router;

import androidx.fragment.app.Fragment;

import com.parabola.domain.model.Track;
import com.parabola.newtone.ui.activity.MainActivity;

public interface MainRouter {

    void setActivity(MainActivity activity);
    void clearActivity();

    Fragment currentFragment();

    //    ACTIONS
    void collapseBottomSlider();
    void goToTab(int tabNumber, boolean smoothScroll);
    void scrollOnTabTrackToCurrentTrack();
    void goToArtistInTab(int artistId);
    void goToAlbumInTab(int albumId);


    <F extends Fragment> boolean hasInstanceInStack(Class<F> fragment);
    void backToRoot();
    boolean isRoot();
    void goBack();


    //    FROM START
    void openArtist(int artistId);
    void openArtistFromBackStackIfAvailable(int artistId);

    void openAlbum(int albumId);
    void openAlbumFromBackStackIfAvailable(int albumId);
    void openPlaylist(int playlistId);
    void openRequestStoragePermissionScreen();
    //    SYSTEM PLAYLISTS
    void openRecentlyAdded();
    void openFavourites();
    void openFavouritesFromBackStackIfAvailable();
    void openQueue();
    void openQueueFromBackStackIfAvailable();
    void openFoldersList();

    void openSettings();

    //    FROM FOLDERS LIST
    void openFolder(String folderPath);

    //    FROM ARTIST
    void openArtistTracks(int artistId);

    //    DIALOGUES
    void openCreatePlaylistDialog();
    void openRenamePlaylistDialog(int playlistId);
    void openDeletePlaylistDialog(int playlistId);
    void openStartSleepTimerDialog();
    void openSleepTimerInfoDialog();
    void openAddToPlaylistDialog(int trackId);
    void openSortingDialog(String sortingListType);
    void openTrackAdditionInfo(int trackId);
    void openAudioEffectsDialog();
    void openNewtoneDialog();

    //    COMMUNICATION WITH OTHER APPS
    void openLyricsSearch(Track track);
    void openPrivacyPolicyWebPage();
    void openShareTrack(String filePath);
}
