package com.parabola.newtone.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.parabola.data.PermissionChangeReceiver;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumViewType;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.ListPopupWindowAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.MainPresenter;
import com.parabola.newtone.mvp.view.MainView;
import com.parabola.newtone.ui.fragment.ArtistFragment;
import com.parabola.newtone.ui.fragment.SettingFragment;
import com.parabola.newtone.ui.fragment.Sortable;
import com.parabola.newtone.ui.fragment.start.TabAlbumFragment;
import com.parabola.newtone.ui.fragment.start.TabPlaylistFragment;
import com.parabola.newtone.ui.router.MainRouter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

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
    @BindView(R.id.duration) RoundCornerProgressBar durationSeekbar;

    @BindView(R.id.track_title) TextView trackTitleTxt;
    @BindView(R.id.song_artist) TextView artistTxt;

    @BindView(R.id.player_toggle) ImageView playerToggle;

    @Inject MainRouter router;
    @Inject ViewSettingsInteractor viewSettingsInteractor;

    @InjectPresenter MainPresenter presenter;

    private Fragment playerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        long time = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        time = System.currentTimeMillis() - time;
        Log.i(TAG, "MainActivityTimeLoading: " + time);
        ButterKnife.bind(this);
        ((MainApplication) getApplication()).getAppComponent().inject(this);

        router.setActivity(this);

        playerFragment = getSupportFragmentManager().findFragmentById(R.id.player_fragment);

        addBottomSliderPanelListener();
    }

    @Override
    protected void onDestroy() {
        router.clearActivity();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (bottomSlider.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
            bottomSlider.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        else super.onBackPressed();
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
    public void onClickMenuButton(ImageView menuButton) {
        Fragment currentFragment = router.currentFragment();

        ListPopupWindow popupWindow = new ListPopupWindow(this);

        ListPopupWindowAdapter adapter = new ListPopupWindowAdapter(this, R.menu.main_menu);
        adapter.setMenuVisibility(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.sorting:
                    return currentFragment instanceof Sortable;
                case R.id.add_playlist:
                    return currentFragment instanceof TabPlaylistFragment;
                case R.id.grid_view:
                    if (currentFragment instanceof TabAlbumFragment && viewSettingsInteractor.getTabAlbumViewType() == AlbumViewType.LIST)
                        return true;
                    if (currentFragment instanceof ArtistFragment && viewSettingsInteractor.getArtistAlbumsViewType() == AlbumViewType.LIST)
                        return true;
                    return false;
                case R.id.list_view:
                    if (currentFragment instanceof TabAlbumFragment && viewSettingsInteractor.getTabAlbumViewType() == AlbumViewType.GRID)
                        return true;
                    if (currentFragment instanceof ArtistFragment && viewSettingsInteractor.getArtistAlbumsViewType() == AlbumViewType.GRID)
                        return true;
                    return false;
                case R.id.settings:
                    return !router.hasInstanceInStack(SettingFragment.class);
            }
            return false;
        });

        popupWindow.setAdapter(adapter);
        popupWindow.setAnchorView(menuButton);
        popupWindow.setVerticalOffset(menuButton.getHeight());
        popupWindow.setModal(true);
        popupWindow.setWidth(adapter.measureContentWidth());
        popupWindow.setOnItemClickListener((parent, view, position, id) -> {
            handleSelectedMenu(adapter.getItem(position), currentFragment);
            popupWindow.dismiss();
        });

        popupWindow.show();
    }

    private void handleSelectedMenu(MenuItem menuItem, Fragment currentFragment) {
        switch (menuItem.getItemId()) {
            case R.id.sorting:
                Sortable sortable = (Sortable) currentFragment;
                router.openSortingDialog(sortable.getListType());
                break;
            case R.id.grid_view:
                if (currentFragment instanceof TabAlbumFragment)
                    viewSettingsInteractor.setTabAlbumViewType(AlbumViewType.GRID);
                else if (currentFragment instanceof ArtistFragment)
                    viewSettingsInteractor.setArtistAlbumsViewType(AlbumViewType.GRID);
                break;
            case R.id.list_view:
                if (currentFragment instanceof TabAlbumFragment)
                    viewSettingsInteractor.setTabAlbumViewType(AlbumViewType.LIST);
                else if (currentFragment instanceof ArtistFragment)
                    viewSettingsInteractor.setArtistAlbumsViewType(AlbumViewType.LIST);
                break;
            case R.id.add_playlist:
                presenter.onClickMenuAddPlaylist();
                break;
            case R.id.settings:
                router.openSettings();
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
    public void setTrackTitle(String trackTitle) {
        trackTitleTxt.setText(trackTitle);
    }

    @Override
    public void setArtistName(String artist) {
        artistTxt.setText(artist);
    }


    @Override
    public void setPlaybackButtonAsPause() {
        playerToggle.setImageResource(R.drawable.ic_pause);
    }

    @Override
    public void setPlaybackButtonAsPlay() {
        playerToggle.setImageResource(R.drawable.ic_play);
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
        playerFragment.requireView().setAlpha(slidePanelOffset);
    }

    private void updateVisibility(SlidingUpPanelLayout.PanelState bottomPanelState) {
        switch (bottomPanelState) {
            case EXPANDED:
                playerBar.setVisibility(View.GONE);
                playerFragment.requireView().setVisibility(View.VISIBLE);
                break;
            case DRAGGING:
                playerBar.setVisibility(View.VISIBLE);
                playerFragment.requireView().setVisibility(View.VISIBLE);
                break;
            case COLLAPSED:
                playerBar.setVisibility(View.VISIBLE);
                playerFragment.requireView().setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }


    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 1;
    private static final String[] STORAGE_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};


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
        durationSeekbar.setMax(max);
    }

    @Override
    public void setDurationProgress(int progress) {
        durationSeekbar.setProgress(progress);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STORAGE_PERMISSIONS_REQUEST_CODE) {
            sendBroadcast(new Intent(PermissionChangeReceiver.ACTION_FILE_STORAGE_PERMISSION_UPDATE));
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
