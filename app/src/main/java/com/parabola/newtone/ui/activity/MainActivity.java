package com.parabola.newtone.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.parabola.data.PermissionChangeReceiver;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.MainPresenter;
import com.parabola.newtone.mvp.view.MainView;
import com.parabola.newtone.ui.fragment.SettingFragment;
import com.parabola.newtone.ui.fragment.Sortable;
import com.parabola.newtone.ui.fragment.start.TabPlaylistFragment;
import com.parabola.newtone.ui.router.MainRouter;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

        PowerMenu.Builder menuBuilder = new PowerMenu.Builder(this)
                .setOnMenuItemClickListener((position, item) -> handleSelectedMenu(item, currentFragment))
                .setMenuRadius(16)
                .setTextColorResource(R.color.colorNewtoneWhite)
                .setTextSize(14)
                .setAnimation(MenuAnimation.SHOWUP_BOTTOM_RIGHT)
                .setMenuColorResource(R.color.colorMenuItemBackground)
                .setBackgroundAlpha(0f)
                .setAutoDismiss(true)
                .setLifecycleOwner(this);

        // Показываем пункт меню "Сортировать по", если его можно сортировать
        if (currentFragment instanceof Sortable)
            menuBuilder.addItem(new PowerMenuItem(getString(R.string.menu_sorting_by), R.drawable.ic_sorting));

        // Показываем пункт меню "Добавить плейлист" в табе плейлистов
        if (currentFragment instanceof TabPlaylistFragment)
            menuBuilder.addItem(new PowerMenuItem(getString(R.string.menu_add_playlist), R.drawable.ic_add_playlist));

        // Скрываем пункт меню "Настройки" если он уже был открыт
        if (!router.hasInstanceInStack(SettingFragment.class))
            menuBuilder.addItem(new PowerMenuItem(getString(R.string.menu_settings), R.drawable.ic_setting));


        PowerMenu powerMenu = menuBuilder.build();
        if (powerMenu.getMenuListView().getCount() == 0) {
            powerMenu.dismiss();
        } else {
            float minWidthPx = getResources().getDimension(R.dimen.context_menu_min_width);
            if (powerMenu.getContentViewWidth() < (int) minWidthPx) {
                powerMenu.setWidth((int) minWidthPx);
            }
            powerMenu.showAsAnchorCenter(menuButton, 0, -(powerMenu.getContentViewHeight() / 4));
        }
    }

    private void handleSelectedMenu(PowerMenuItem menuItem, Fragment currentFragment) {
        if (menuItem.getTitle().equals(getString(R.string.menu_settings))) {
            router.openSettings();
        } else if (menuItem.getTitle().equals(getString(R.string.menu_add_playlist))) {
            presenter.onClickMenuAddPlaylist();
        } else if (menuItem.getTitle().equals(getString(R.string.menu_sorting_by))) {
            String sortingListType = ((Sortable) currentFragment).getListType();
            router.openSortingDialog(sortingListType);
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
