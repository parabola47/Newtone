package com.parabola.newtone.ui.fragment.settings;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import com.parabola.newtone.BuildConfig;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.SettingPresenter;
import com.parabola.newtone.mvp.view.SettingView;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class SettingFragment extends BaseSwipeToBackFragment
        implements SettingView {
    private static final String LOG_TAG = SettingFragment.class.getSimpleName();

    private static final String VERSION_INFO;

    static {
        VERSION_INFO = String.format(Locale.getDefault(), "v.%s. build-%d", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
    }

    @BindView(R.id.main) TextView settingsTxt;
    @BindView(R.id.additional_info) TextView addInfoTxt;
    @BindView(R.id.otherInfo) TextView otherInfoTxt;

    @InjectPresenter SettingPresenter presenter;

    @BindView(R.id.notification_artwork_show_switch) SwitchCompat notificationArtworkShowSwitch;
    @BindView(R.id.notification_color_switch) SwitchCompat notificationColorSwitch;
    @BindView(R.id.show_item_divider_switch) SwitchCompat showListItemDividerSwitch;
    @BindView(R.id.notification_color_bar) ViewGroup notificationColorBar;
    @BindView(R.id.app_info_version) TextView appInfoVersion;


    public SettingFragment() {
        // Required empty public constructor
    }


    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.fragment_setting, container, false);
        ((ViewGroup) root.findViewById(R.id.container)).addView(contentView);
        ButterKnife.bind(this, root);

        settingsTxt.setText(R.string.setting_title);

        addInfoTxt.setVisibility(View.GONE);
        otherInfoTxt.setVisibility(View.GONE);
        appInfoVersion.setText(VERSION_INFO);

        return root;
    }

    @Override
    protected void onClickBackButton() {
        presenter.onClickBack();
    }

    @OnClick(R.id.color_theme_bar)
    public void onClickColorThemeSetting() {
        presenter.onClickColorThemeSettings();
    }


    @Override
    public void setNotificationArtworkSwitchChecked(boolean isChecked) {
        notificationArtworkShowSwitch.setChecked(isChecked);
        setNotificationColorBarEnabling(isChecked);
    }


    private void setNotificationColorBarEnabling(boolean checked) {
        notificationColorBar.setEnabled(checked);
        for (int i = 0; i < notificationColorBar.getChildCount(); i++) {
            notificationColorBar.getChildAt(i).setEnabled(checked);
        }
    }

    @Override
    public void setNotificationColorSwitchChecked(boolean isChecked) {
        notificationColorSwitch.setChecked(isChecked);
    }

    @OnClick(R.id.notification_color_bar)
    public void onClickNotificationColorSetting() {
        presenter.onClickNotificationColorSetting();
    }


    @OnClick(R.id.notification_artwork_show_bar)
    public void onClickNotificationArtworkShowSetting() {
        presenter.onClickNotificationArtworkShowSetting();
    }


    @Override
    public void setShowListItemDividerSwitchChecked(boolean isChecked) {
        showListItemDividerSwitch.setChecked(isChecked);
    }

    @OnClick(R.id.show_item_divider_bar)
    public void onClickShowItemDivider() {
        presenter.onClickShowItemDivider();
    }


    @OnClick(R.id.excluded_folders_bar)
    public void onClickExcludedFolders() {
        presenter.onClickExcludedFolders();
    }


    @OnClick(R.id.track_item_view_bar)
    public void onClickTrackItemViewSettings() {
        presenter.onClickTrackItemViewSettings();
    }

    @OnClick(R.id.album_item_view_bar)
    public void onClickAlbumItemViewSettings() {
        presenter.onClickAlbumItemViewSettings();
    }

    @OnClick(R.id.artist_item_view_bar)
    public void onClickArtistItemViewSettings() {
        presenter.onClickArtistItemViewSettings();
    }


    @OnClick(R.id.privacy_policy_bar)
    public void onClickPrivacyPolicy() {
        presenter.onClickPrivacyPolicy();
    }


    private int appInfoBarClickCount = 0;

    @OnClick(R.id.app_info_bar)
    public void onClickAppInfoBar() {
        if (++appInfoBarClickCount % 10 == 0) {
            presenter.onClickNewtoneTenTimes();
        }
    }


    @ProvidePresenter
    public SettingPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new SettingPresenter(appComponent);
    }

}
