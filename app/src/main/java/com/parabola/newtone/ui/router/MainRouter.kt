package com.parabola.newtone.ui.router;

import androidx.annotation.FloatRange;
import androidx.fragment.app.Fragment;

import com.parabola.domain.model.Track;
import com.parabola.newtone.ui.activity.MainActivity;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import io.reactivex.Observable;

public interface MainRouter {

    void setActivity(MainActivity activity);
    void clearActivity();

    default void showToast(String toastText) {
        showToast(toastText, false);
    }

    default void showToast(String toastText, boolean longLength) {
        showToast(toastText, longLength, false);
    }

    void showToast(String toastText, boolean longLength, boolean showAtCenter);

    Fragment currentFragment();

    //    ACTIONS
    void collapseBottomSlider();
    void goToTab(int tabNumber, boolean smoothScroll);
    void scrollOnTabTrackToCurrentTrack();
    void goToArtistInTab(int artistId);
    void goToAlbumInTab(int albumId);

    //offset - от 0 до 1. при 0 - панель полностью закрыта, при 1 - панель полностью раскрыта
    void setBottomSlidePanelOffset(@FloatRange(from = 0.0f, to = 1.0f) float offset);
    Observable<Float> observeSlidePanelOffset();
    void setBottomSlidePanelState(PanelState state);
    Observable<PanelState> observeSlidePanelState();


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
    void openSearchScreen();
    //    SYSTEM PLAYLISTS
    void openRecentlyAdded();
    void openFavourites();
    void openFavouritesFromBackStackIfAvailable();
    void openQueue();
    void openQueueFromBackStackIfAvailable();
    void openFoldersList();

    //    SETTINGS
    void openSettings();
    void openSettingsIfAvailable();
    void openColorThemeSelectorSettings();
    void openExcludedFolders();
    void openTrackItemDisplaySettings();
    void openAlbumItemDisplaySettings();
    void openArtistItemDisplaySettings();


    //    FROM FOLDERS LIST
    void openFolder(String folderPath);

    //    FROM ARTIST
    void openArtistTracks(int artistId);

    //    DIALOGUES
    void openCreatePlaylistDialog();
    void openRenamePlaylistDialog(int playlistId);
    void openStartSleepTimerDialog();
    void openSleepTimerInfoDialog();
    void openAddToPlaylistDialog(int... trackIds);
    void openSortingDialog(String sortingListType);
    void openTrackAdditionInfo(int trackId);
    void openAudioEffectsDialog();
    void openEqPresetsSelectorDialog();
    void openNewtoneDialog();
    void openDeleteTrackDialog(int trackId);

    //    COMMUNICATION WITH OTHER APPS
    void openLyricsSearch(Track track);
    void openContactDevelopersViaEmail();
    void openPrivacyPolicyWebPage();
    void openShareTrack(String filePath);

}
