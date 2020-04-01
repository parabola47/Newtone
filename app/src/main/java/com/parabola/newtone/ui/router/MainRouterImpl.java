package com.parabola.newtone.ui.router;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.parabola.domain.model.Track;
import com.parabola.newtone.BuildConfig;
import com.parabola.newtone.R;
import com.parabola.newtone.ui.activity.MainActivity;
import com.parabola.newtone.ui.dialog.ChoosePlaylistDialog;
import com.parabola.newtone.ui.dialog.CreatePlaylistDialog;
import com.parabola.newtone.ui.dialog.DeletePlaylistDialog;
import com.parabola.newtone.ui.dialog.IsaacNewtoneDialog;
import com.parabola.newtone.ui.dialog.RenamePlaylistDialog;
import com.parabola.newtone.ui.dialog.SleepTimerDialog;
import com.parabola.newtone.ui.dialog.SortingDialog;
import com.parabola.newtone.ui.dialog.TimeToSleepInfoDialog;
import com.parabola.newtone.ui.dialog.TrackAdditionalInfoDialog;
import com.parabola.newtone.ui.dialog.fx.AudioEffectsDialog;
import com.parabola.newtone.ui.fragment.AlbumFragment;
import com.parabola.newtone.ui.fragment.ArtistFragment;
import com.parabola.newtone.ui.fragment.ArtistTracksFragment;
import com.parabola.newtone.ui.fragment.FolderFragment;
import com.parabola.newtone.ui.fragment.SearchFragment;
import com.parabola.newtone.ui.fragment.playlist.FavoritesPlaylistFragment;
import com.parabola.newtone.ui.fragment.playlist.FoldersListFragment;
import com.parabola.newtone.ui.fragment.playlist.PlaylistFragment;
import com.parabola.newtone.ui.fragment.playlist.QueueFragment;
import com.parabola.newtone.ui.fragment.playlist.RecentlyAddedPlaylistFragment;
import com.parabola.newtone.ui.fragment.settings.SettingFragment;
import com.parabola.newtone.ui.fragment.settings.TrackItemDisplaySettingFragment;
import com.parabola.newtone.ui.fragment.start.StartFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;

public final class MainRouterImpl implements MainRouter {
    private MainActivity activity;
    private StartFragment firstFragment;

    @Override
    public void setActivity(MainActivity activity) {
        this.activity = activity;

        firstFragment = getInstance(StartFragment.class);
        if (firstFragment == null) {
            firstFragment = new StartFragment();

            activity.getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_host_fragment, firstFragment)
                    .commit();
        }
    }

    @Override
    public void clearActivity() {
        this.activity = null;
        this.firstFragment = null;
    }

    @Override
    public Fragment currentFragment() {
        Fragment currentFragment;
        if (isRoot()) {
            currentFragment = firstFragment.getCurrentSelectedFragment();
        } else {
            currentFragment = activity.getSupportFragmentManager()
                    .getPrimaryNavigationFragment();
        }
        return currentFragment;
    }

    @Override
    public void collapseBottomSlider() {
        activity.setBottomSliderPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    @Override
    public void goToTab(int tabNumber, boolean smoothScroll) {
        firstFragment.goToTab(tabNumber, smoothScroll);
    }

    @Override
    public void scrollOnTabTrackToCurrentTrack() {
        firstFragment.scrollOnTabTrackToCurrentTrack();
    }


    @Override
    public void goToArtistInTab(int artistId) {
        firstFragment.scrollToArtistInTab(artistId);
    }


    @Override
    public void goToAlbumInTab(int albumId) {
        firstFragment.scrollToAlbumInTab(albumId);
    }

    @Override
    public <T extends Fragment> boolean hasInstanceInStack(Class<T> fragment) {
        for (Fragment f : activity.getSupportFragmentManager().getFragments()) {
            if (fragment.isInstance(f)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    private <T extends Fragment> T getInstance(Class<T> fragment) {
        for (Fragment f : activity.getSupportFragmentManager().getFragments()) {
            if (fragment.isInstance(f)) {
                return fragment.cast(f);
            }
        }
        return null;
    }

    @Override
    public void backToRoot() {
        while (!isRoot()) {
            activity.getSupportFragmentManager().popBackStackImmediate();
        }
    }

    @Override
    public boolean isRoot() {
        return activity.getSupportFragmentManager().getBackStackEntryCount() == 0;
    }

    @Override
    public void goBack() {
        activity.getSupportFragmentManager()
                .popBackStackImmediate();
    }


    @Override
    public void openArtist(int artistId) {
        ArtistFragment artistFragment = ArtistFragment.newInstance(artistId);

        activity.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
                .add(R.id.nav_host_fragment, artistFragment)
                .addToBackStack(null)
                .setPrimaryNavigationFragment(artistFragment)
                .commit();
    }

    @Override
    public void openArtistFromBackStackIfAvailable(int artistId) {
        while (!isRoot()) {
            Fragment currentFragment = currentFragment();
            if (currentFragment instanceof ArtistFragment) {
                ArtistFragment artistFragment = (ArtistFragment) currentFragment;
                if (artistFragment.getArtistId() == artistId)
                    return;
            }

            goBack();
        }

        openArtist(artistId);
    }


    @Override
    public void openAlbum(int albumId) {
        AlbumFragment fragment = AlbumFragment.newInstance(albumId);

        activity.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
                .add(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .setPrimaryNavigationFragment(fragment)
                .commit();
    }

    @Override
    public void openAlbumFromBackStackIfAvailable(int albumId) {
        while (!isRoot()) {
            Fragment currentFragment = currentFragment();
            if (currentFragment instanceof AlbumFragment) {
                AlbumFragment albumFragment = (AlbumFragment) currentFragment;
                if (albumFragment.getAlbumId() == albumId)
                    return;
            }

            goBack();
        }

        openAlbum(albumId);
    }


    @Override
    public void openPlaylist(int playlistId) {
        PlaylistFragment playlistFragment = PlaylistFragment.newInstance(playlistId);

        activity.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
                .add(R.id.nav_host_fragment, playlistFragment)
                .addToBackStack(null)
                .setPrimaryNavigationFragment(playlistFragment)
                .commit();
    }

    @Override
    public void openRequestStoragePermissionScreen() {
        activity.requestStoragePermission();
    }

    @Override
    public void openSearchScreen() {
        SearchFragment searchFragment = SearchFragment.newInstance();
        activity.getSupportFragmentManager().beginTransaction()
                .add(R.id.nav_host_fragment, searchFragment)
                .addToBackStack(null)
                .setPrimaryNavigationFragment(searchFragment)
                .commit();
    }

    @Override
    public void openSettings() {
        SettingFragment fragment = new SettingFragment();

        activity.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
                .add(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .setPrimaryNavigationFragment(fragment)
                .commit();
    }

    @Override
    public void openTrackItemDisplaySettings() {
        TrackItemDisplaySettingFragment fragment = TrackItemDisplaySettingFragment.newInstance();

        activity.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
                .add(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .setPrimaryNavigationFragment(fragment)
                .commit();
    }

    @Override
    public void openRecentlyAdded() {
        RecentlyAddedPlaylistFragment fragment = new RecentlyAddedPlaylistFragment();

        activity.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
                .add(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .setPrimaryNavigationFragment(fragment)
                .commit();
    }

    @Override
    public void openFavourites() {
        FavoritesPlaylistFragment fragment = new FavoritesPlaylistFragment();

        activity.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
                .add(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .setPrimaryNavigationFragment(fragment)
                .commit();
    }

    @Override
    public void openFavouritesFromBackStackIfAvailable() {
        while (!isRoot()) {
            Fragment currentFragment = currentFragment();
            if (currentFragment instanceof FavoritesPlaylistFragment) {
                return;
            }

            goBack();
        }

        openFavourites();
    }

    @Override
    public void openQueue() {
        QueueFragment fragment = new QueueFragment();

        activity.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
                .add(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .setPrimaryNavigationFragment(fragment)
                .commit();
    }

    @Override
    public void openQueueFromBackStackIfAvailable() {
        while (!isRoot()) {
            Fragment currentFragment = currentFragment();
            if (currentFragment instanceof QueueFragment) {
                return;
            }

            goBack();
        }

        openQueue();
    }

    @Override
    public void openFoldersList() {
        FoldersListFragment fragment = new FoldersListFragment();

        activity.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
                .add(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .setPrimaryNavigationFragment(fragment)
                .commit();
    }

    @Override
    public void openFolder(String folderPath) {
        Bundle args = new Bundle();
        args.putString("folderPath", folderPath);

        FolderFragment fragment = new FolderFragment();
        fragment.setArguments(args);

        activity.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
                .add(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .setPrimaryNavigationFragment(fragment)
                .commit();
    }

    @Override
    public void openArtistTracks(int artistId) {
        Bundle args = new Bundle();
        args.putInt("artistId", artistId);

        ArtistTracksFragment fragment = new ArtistTracksFragment();
        fragment.setArguments(args);

        activity.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_in, R.anim.anim_out, R.anim.anim_in, R.anim.anim_out)
                .add(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .setPrimaryNavigationFragment(fragment)
                .commit();
    }

    @Override
    public void openCreatePlaylistDialog() {
        CreatePlaylistDialog dialogFragment = CreatePlaylistDialog.newInstance();
        dialogFragment.show(activity.getSupportFragmentManager(), null);
    }

    @Override
    public void openRenamePlaylistDialog(int playlistId) {
        RenamePlaylistDialog dialogFragment = RenamePlaylistDialog.newInstance(playlistId);
        dialogFragment.show(activity.getSupportFragmentManager(), null);
    }

    @Override
    public void openDeletePlaylistDialog(int playlistId) {
        DeletePlaylistDialog dialogFragment = DeletePlaylistDialog.newInstance(playlistId);
        dialogFragment.show(activity.getSupportFragmentManager(), null);
    }

    @Override
    public void openStartSleepTimerDialog() {
        SleepTimerDialog dialogFragment = SleepTimerDialog.newInstance();
        dialogFragment.show(activity.getSupportFragmentManager(), null);
    }

    @Override
    public void openSleepTimerInfoDialog() {
        TimeToSleepInfoDialog dialogFragment = TimeToSleepInfoDialog.newInstance();
        dialogFragment.show(activity.getSupportFragmentManager(), null);
    }


    @Override
    public void openAddToPlaylistDialog(int trackId) {
        ChoosePlaylistDialog dialogFragment = ChoosePlaylistDialog.newInstance(trackId);
        dialogFragment.show(activity.getSupportFragmentManager(), null);
    }

    @Override
    public void openSortingDialog(String sortingListType) {
        SortingDialog dialogFragment = SortingDialog.newInstance(sortingListType);
        dialogFragment.show(activity.getSupportFragmentManager(), null);
    }

    @Override
    public void openTrackAdditionInfo(int trackId) {
        TrackAdditionalInfoDialog dialogFragment = TrackAdditionalInfoDialog.newInstance(trackId);
        dialogFragment.show(activity.getSupportFragmentManager(), null);
    }

    @Override
    public void openAudioEffectsDialog() {
        AudioEffectsDialog dialog = new AudioEffectsDialog();
        dialog.show(activity.getSupportFragmentManager(), null);
    }

    @Override
    public void openNewtoneDialog() {
        IsaacNewtoneDialog dialog = new IsaacNewtoneDialog();
        dialog.show(activity.getSupportFragmentManager(), null);
    }

    @Override
    public void openLyricsSearch(Track track) {
        String searchQuery = String.format("%s %s lyrics", track.getArtistName(), track.getTitle());
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH)
                .putExtra(SearchManager.QUERY, searchQuery);
        activity.startActivity(intent);
    }

    @Override
    public void openPrivacyPolicyWebPage() {
        String url = "https://github.com/parabola47/privacy_policy/blob/master/newtone_pp.md";
        Intent i = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse(url));
        activity.startActivity(i);
    }

    @Override
    public void openShareTrack(String trackFilePath) {
        Uri uri = FileProvider.getUriForFile(activity.getApplication(), BuildConfig.APPLICATION_ID, new File(trackFilePath));
        Intent share = new Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_STREAM, uri)
                .setType("audio/*");
        activity.startActivity(Intent.createChooser(share, activity.getString(R.string.track_menu_share_track_intent_title)));
    }
}
