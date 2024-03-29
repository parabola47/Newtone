package com.parabola.newtone.presentation.mainactivity;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.parabola.domain.repository.PermissionHandler;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.domain.settings.ViewSettingsInteractor.PrimaryColor;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.ListPopupWindowAdapter;
import com.parabola.newtone.databinding.ActivityMainBinding;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.presentation.search.SearchFragment;
import com.parabola.newtone.presentation.base.Sortable;
import com.parabola.newtone.presentation.settings.SettingFragment;
import com.parabola.newtone.presentation.main.playlists.TabPlaylistFragment;
import com.parabola.newtone.presentation.router.MainRouter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class MainActivity extends MvpAppCompatActivity implements MainView {

    @InjectPresenter MainPresenter presenter;

    @Inject MainRouter router;
    @Inject ViewSettingsInteractor viewSettingsInteractor;
    @Inject PermissionHandler permissionHandler;
    @Inject TrackRepository trackRepository;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((MainApplication) getApplication()).getAppComponent().inject(this);

        int themeId;
        switch (viewSettingsInteractor.getPrimaryColor()) {
            case NEWTONE: themeId = R.style.Newtone; break;
            case ARIUM: themeId = R.style.Arium; break;
            case BLUES: themeId = R.style.Blues; break;
            case FLOYD: themeId = R.style.Floyd; break;
            case PURPLE: themeId = R.style.Purple; break;
            case PASSION: themeId = R.style.Passion; break;
            case SKY: themeId = R.style.Sky; break;
            default: throw new IllegalStateException();
        }
        setTheme(themeId);

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        router.setActivity(this);

        addBottomSliderPanelListener();

        binding.playerBar.playerToggle.setOnClickListener(v -> presenter.onClickPlayButton());
        binding.playerBar.menuButton.setOnClickListener(v -> onClickMenuButton());
    }

    @Override
    protected void onDestroy() {
        if (isFinishing())
            presenter.onFinishing();
        router.clearActivity();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (binding.bottomSlider.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
            binding.bottomSlider.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        else {
            try {
                super.onBackPressed();
            } catch (IllegalStateException ignored) {
            }
        }
    }

    public void onClickMenuButton() {
        Fragment currentFragment = router.currentFragment();

        ListPopupWindow popupWindow = new ListPopupWindow(this);

        ListPopupWindowAdapter adapter = new ListPopupWindowAdapter(this, R.menu.main_menu);
        adapter.setMenuVisibility(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.search:
                    return !router.hasInstanceInStack(SearchFragment.class);
                case R.id.sorting:
                    return currentFragment instanceof Sortable;
                case R.id.add_playlist:
                    return currentFragment instanceof TabPlaylistFragment;
                case R.id.settings:
                    return !router.hasInstanceInStack(SettingFragment.class);
            }
            return false;
        });

        popupWindow.setAdapter(adapter);
        popupWindow.setAnchorView(binding.playerBar.menuTmp);
        popupWindow.setModal(true);
        popupWindow.setWidth(adapter.measureContentWidth());
        popupWindow.setOnItemClickListener((parent, view, position, id) -> {
            handleSelectedMenu(adapter.getItem(position), currentFragment);
            popupWindow.dismiss();
        });

        if (adapter.getCount() > 0)
            popupWindow.show();
    }

    private void handleSelectedMenu(MenuItem menuItem, Fragment currentFragment) {
        switch (menuItem.getItemId()) {
            case R.id.search:
                presenter.onClickMenuSearch();
                break;
            case R.id.sorting:
                Sortable sortable = (Sortable) currentFragment;
                presenter.onClickMenuSorting(sortable.getListType());
                break;
            case R.id.add_playlist:
                presenter.onClickMenuAddPlaylist();
                break;
            case R.id.settings:
                presenter.onClickMenuSettings();
                break;
        }
    }


    @ProvidePresenter
    public MainPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) getApplication()).getAppComponent();
        return new MainPresenter(appComponent);
    }

    public void setBottomSliderPanelState(SlidingUpPanelLayout.PanelState panelState) {
        binding.bottomSlider.setPanelState(panelState);
    }


    @Override
    public void refreshPrimaryColor(PrimaryColor primaryColor) {
        String currentThemeName = getCurrentThemeName();

        if ((primaryColor == PrimaryColor.NEWTONE && !currentThemeName.equals(getString(R.string.theme_newtone))) ||
                (primaryColor == PrimaryColor.ARIUM && !currentThemeName.equals(getString(R.string.theme_arium))) ||
                (primaryColor == PrimaryColor.BLUES && !currentThemeName.equals(getString(R.string.theme_blues))) ||
                (primaryColor == PrimaryColor.FLOYD && !currentThemeName.equals(getString(R.string.theme_floyd))) ||
                (primaryColor == PrimaryColor.PURPLE && !currentThemeName.equals(getString(R.string.theme_purple))) ||
                (primaryColor == PrimaryColor.PASSION && !currentThemeName.equals(getString(R.string.theme_passion))) ||
                (primaryColor == PrimaryColor.SKY && !currentThemeName.equals(getString(R.string.theme_sky)))) {
            recreate();
        }
    }


    public String getCurrentThemeName() {
        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.themeName, outValue, true);
        return outValue.string.toString();
    }

    @Override
    public void setTrackTitle(String trackTitle) {
        binding.playerBar.trackTitle.setText(trackTitle);
    }

    @Override
    public void setArtistName(String artist) {
        binding.playerBar.songArtist.setText(artist);
    }


    @Override
    public void setPlaybackButtonAsPause() {
        binding.playerBar.playerToggle.setImageResource(R.drawable.ic_pause_accent);
    }

    @Override
    public void setPlaybackButtonAsPlay() {
        binding.playerBar.playerToggle.setImageResource(R.drawable.ic_play_accent);
    }

    @Override
    public void showBottomSlider() {
        if (binding.bottomSlider.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN)
            binding.bottomSlider.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    @Override
    public void hideBottomSlider() {
        binding.bottomSlider.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    private void addBottomSliderPanelListener() {
        binding.bottomSlider.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                router.setBottomSlidePanelOffset(slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel,
                                            SlidingUpPanelLayout.PanelState previousState,
                                            SlidingUpPanelLayout.PanelState newState) {
                router.setBottomSlidePanelState(newState);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        router.setBottomSlidePanelState(binding.bottomSlider.getPanelState());
        if (binding.bottomSlider.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            router.setBottomSlidePanelOffset(1.0f);
        } else if (binding.bottomSlider.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            router.setBottomSlidePanelOffset(0.0f);
        }
    }


    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 1;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private static final String[] STORAGE_PERMISSIONS_TIRAMISU_AND_ABOVE = {Manifest.permission.READ_MEDIA_AUDIO};
    private static final String[] STORAGE_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final int DELETE_TRACK_REQUEST_CODE = 2;


    public void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, STORAGE_PERMISSIONS, STORAGE_PERMISSIONS_REQUEST_CODE);
        } else {
            router.showToast(getString(R.string.request_permission_toast), true, true);

            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, STORAGE_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void requestStoragePermissionDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, STORAGE_PERMISSIONS_TIRAMISU_AND_ABOVE, STORAGE_PERMISSIONS_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this, STORAGE_PERMISSIONS, STORAGE_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void setDurationMax(int max) {
        binding.playerBar.trackPositionProgressBar.setMax(max);
    }

    @Override
    public void setDurationProgress(int progress) {
        binding.playerBar.trackPositionProgressBar.setProgress(progress);
    }

    @Override
    public void setPlayerBarOpacity(float alpha) {
        binding.playerBar.getRoot().setAlpha(alpha);
    }

    @Override
    public void setPlayerBarVisibility(boolean visible) {
        binding.playerBar.getRoot().setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private int deleteTrackId = -1;

    //начиная с Android R необходимо показывать системное диалоговое окно с подтверждением удаления медиа-файлов
    @RequiresApi(api = Build.VERSION_CODES.R)
    public void showRequestForDeletingTrack(int trackId) {
        List<Uri> uris = new ArrayList<>();
        uris.add(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId));
        deleteTrackId = trackId;
        PendingIntent deleteTrackIntent = MediaStore.createDeleteRequest(getContentResolver(), uris);
        try {
            startIntentSenderForResult(deleteTrackIntent.getIntentSender(), MainActivity.DELETE_TRACK_REQUEST_CODE, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STORAGE_PERMISSIONS_REQUEST_CODE) {
            permissionHandler.invalidatePermission(PermissionHandler.Type.FILE_STORAGE);
        } else if (requestCode == DELETE_TRACK_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {
            trackRepository.trackDeletedInternally(deleteTrackId);
            deleteTrackId = -1;
            router.showToast(getString(R.string.file_deleted_successfully_toast), true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSIONS_REQUEST_CODE) {
            permissionHandler.invalidatePermission(PermissionHandler.Type.FILE_STORAGE);
        }
    }

}
