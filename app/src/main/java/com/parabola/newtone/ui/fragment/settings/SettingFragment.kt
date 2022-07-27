package com.parabola.newtone.ui.fragment.settings;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.parabola.newtone.BuildConfig;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.databinding.FragmentSettingBinding;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.SettingPresenter;
import com.parabola.newtone.mvp.view.SettingView;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;

import java.util.Locale;

import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class SettingFragment extends BaseSwipeToBackFragment
        implements SettingView {
    private static final String LOG_TAG = SettingFragment.class.getSimpleName();

    private static final String VERSION_INFO;

    static {
        VERSION_INFO = String.format(Locale.getDefault(), "v.%s. build-%d", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
    }

    private FragmentSettingBinding binding;


    @InjectPresenter SettingPresenter presenter;


    public SettingFragment() {
        // Required empty public constructor
    }


    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        getRootBinding().container.addView(binding.getRoot());


        getRootBinding().main.setText(R.string.setting_title);

        getRootBinding().additionalInfo.setVisibility(View.GONE);
        getRootBinding().otherInfo.setVisibility(View.GONE);
        binding.appInfoVersion.setText(VERSION_INFO);

        binding.colorThemeBar.setOnClickListener(v -> presenter.onClickColorThemeSettings());
        binding.notificationColorBar.setOnClickListener(v -> presenter.onClickNotificationColorSetting());
        binding.notificationArtworkShowBar.setOnClickListener(v -> presenter.onClickNotificationArtworkShowSetting());
        binding.showItemDividerBar.setOnClickListener(v -> presenter.onClickShowItemDivider());
        binding.excludedFoldersBar.setOnClickListener(v -> presenter.onClickExcludedFolders());
        binding.trackItemViewBar.setOnClickListener(v -> presenter.onClickTrackItemViewSettings());
        binding.albumItemViewBar.setOnClickListener(v -> presenter.onClickAlbumItemViewSettings());
        binding.artistItemViewBar.setOnClickListener(v -> presenter.onClickArtistItemViewSettings());
        binding.privacyPolicyBar.setOnClickListener(v -> presenter.onClickPrivacyPolicy());
        binding.contactDevelopersBar.setOnClickListener(v -> presenter.onClickContactDevelopers());
        binding.appInfoBar.setOnClickListener(v -> presenter.onClickAppInfoBar());

        return root;
    }

    @Override
    protected void onClickBackButton() {
        presenter.onClickBack();
    }


    @Override
    public void setNotificationArtworkSwitchChecked(boolean isChecked) {
        binding.notificationArtworkShowSwitch.setChecked(isChecked);
        setNotificationColorBarEnabling(isChecked);
    }

    private void setNotificationColorBarEnabling(boolean checked) {
        binding.notificationColorBar.setEnabled(checked);
    }

    @Override
    public void setNotificationColorSwitchChecked(boolean isChecked) {
        binding.notificationColorSwitch.setChecked(isChecked);
    }

    @Override
    public void setShowListItemDividerSwitchChecked(boolean isChecked) {
        binding.showItemDividerSwitch.setChecked(isChecked);
    }

    @ProvidePresenter
    public SettingPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new SettingPresenter(appComponent);
    }

}
