package com.parabola.newtone.ui.activity;

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
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.ProgressIndicator;
import com.parabola.data.AudioDeletedReceiver;
import com.parabola.data.PermissionChangeReceiver;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.domain.settings.ViewSettingsInteractor.PrimaryColor;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.ListPopupWindowAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.MainPresenter;
import com.parabola.newtone.mvp.view.MainView;
import com.parabola.newtone.ui.fragment.SearchFragment;
import com.parabola.newtone.ui.fragment.Sortable;
import com.parabola.newtone.ui.fragment.settings.SettingFragment;
import com.parabola.newtone.ui.fragment.start.TabPlaylistFragment;
import com.parabola.newtone.ui.router.MainRouter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class MainActivity extends MvpAppCompatActivity implements MainView {
    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.bottom_slider) SlidingUpPanelLayout bottomSlider;
    @BindView(R.id.player_bar) ViewGroup playerBar;
    @BindView(R.id.track_settings) ImageButton playerSetting;
    @BindView(R.id.trackPositionProgressBar) ProgressIndicator trackPositionProgressBar;

    @BindView(R.id.track_title) TextView trackTitleTxt;
    @BindView(R.id.song_artist) TextView artistTxt;

    @BindView(R.id.player_toggle) ImageView playerToggle;

    @Inject MainRouter router;
    @Inject ViewSettingsInteractor viewSettingsInteractor;

    @InjectPresenter MainPresenter presenter;

    private Fragment playerFragment;

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
            default: throw new IllegalStateException();
        }
        setTheme(themeId);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        router.setActivity(this);

        playerFragment = getSupportFragmentManager().findFragmentById(R.id.player_fragment);

        addBottomSliderPanelListener();
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
        if (bottomSlider.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
            bottomSlider.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        else {
            try {
                super.onBackPressed();
            } catch (IllegalStateException ignored) {
            }
        }
    }

    @OnClick(R.id.player_toggle)
    public void onClickPlayButton() {
        presenter.onClickPlayButton();
    }

    @OnClick(R.id.drop_down)
    public void onClickDropDownButton() {
        bottomSlider.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    @OnClick(R.id.menu_button)
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
        popupWindow.setAnchorView(playerBar.findViewById(R.id.menu_tmp));
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
        bottomSlider.setPanelState(panelState);
    }


    @Override
    public void refreshPrimaryColor(PrimaryColor primaryColor) {
        String currentThemeName = getCurrentThemeName();

        if ((primaryColor == PrimaryColor.NEWTONE && !currentThemeName.equals(getString(R.string.theme_newtone))) ||
                (primaryColor == PrimaryColor.ARIUM && !currentThemeName.equals(getString(R.string.theme_arium))) ||
                (primaryColor == PrimaryColor.BLUES && !currentThemeName.equals(getString(R.string.theme_blues))) ||
                (primaryColor == PrimaryColor.FLOYD && !currentThemeName.equals(getString(R.string.theme_floyd))) ||
                (primaryColor == PrimaryColor.PURPLE && !currentThemeName.equals(getString(R.string.theme_purple))) ||
                (primaryColor == PrimaryColor.PASSION && !currentThemeName.equals(getString(R.string.theme_passion)))) {
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
        trackTitleTxt.setText(trackTitle);
    }

    @Override
    public void setArtistName(String artist) {
        artistTxt.setText(artist);
    }


    @Override
    public void setPlaybackButtonAsPause() {
        playerToggle.setImageResource(R.drawable.ic_pause_accent);
    }

    @Override
    public void setPlaybackButtonAsPlay() {
        playerToggle.setImageResource(R.drawable.ic_play_accent);
    }

    @Override
    public void showBottomSlider() {
        if (bottomSlider.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN)
            bottomSlider.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    @Override
    public void hideBottomSlider() {
        bottomSlider.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    private void addBottomSliderPanelListener() {
        bottomSlider.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                updateVisibility(slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel,
                                            SlidingUpPanelLayout.PanelState previousState,
                                            SlidingUpPanelLayout.PanelState newState) {
                updateVisibility(newState);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateVisibility(bottomSlider.getPanelState());
        if (bottomSlider.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            updateVisibility(1.0f);
        } else if (bottomSlider.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            updateVisibility(0.0f);
        }
    }

    private void updateVisibility(float slidePanelOffset) {
        playerBar.setAlpha(1f - slidePanelOffset);
        playerSetting.setRotation(360 * slidePanelOffset);
        Optional.ofNullable(playerFragment.getView())
                .ifPresent(view -> view.setAlpha(slidePanelOffset));
    }

    private void updateVisibility(SlidingUpPanelLayout.PanelState bottomPanelState) {
        switch (bottomPanelState) {
            case EXPANDED:
                playerBar.setVisibility(View.GONE);
                Optional.ofNullable(playerFragment.getView())
                        .ifPresent(view -> view.setVisibility(View.VISIBLE));
                break;
            case DRAGGING:
                playerBar.setVisibility(View.VISIBLE);
                Optional.ofNullable(playerFragment.getView())
                        .ifPresent(view -> view.setVisibility(View.VISIBLE));
                break;
            case COLLAPSED:
                playerBar.setVisibility(View.VISIBLE);
                Optional.ofNullable(playerFragment.getView())
                        .ifPresent(view -> view.setVisibility(View.GONE));
                break;
            default:
                break;
        }
    }


    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 1;
    private static final String[] STORAGE_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final int DELETE_TRACK_REQUEST_CODE = 2;


    public void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, STORAGE_PERMISSIONS, STORAGE_PERMISSIONS_REQUEST_CODE);
        } else {
            Toast toast = Toast.makeText(this, R.string.request_permission_toast, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, STORAGE_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void requestStoragePermissionDialog() {
        ActivityCompat.requestPermissions(this, STORAGE_PERMISSIONS, STORAGE_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void setDurationMax(int max) {
        trackPositionProgressBar.setMax(max);
    }

    @Override
    public void setDurationProgress(int progress) {
        trackPositionProgressBar.setProgress(progress);
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
            sendBroadcast(new Intent(PermissionChangeReceiver.ACTION_FILE_STORAGE_PERMISSION_UPDATE));
        } else if (requestCode == DELETE_TRACK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(AudioDeletedReceiver.ACTION_AUDIO_REMOVED_FROM_STORAGE)
                    .putExtra(AudioDeletedReceiver.TRACK_ID_ARG, deleteTrackId);
            sendBroadcast(intent);
            deleteTrackId = -1;
            Toast.makeText(this, R.string.file_deleted_successfully_toast, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSIONS_REQUEST_CODE) {
            sendBroadcast(new Intent(PermissionChangeReceiver.ACTION_FILE_STORAGE_PERMISSION_UPDATE));
        }
    }
}
