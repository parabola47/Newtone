package com.parabola.newtone.ui.dialog.fx;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.databinding.TabFxAudioSettingsBinding;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.fx.FxAudioSettingsPresenter;
import com.parabola.newtone.mvp.view.fx.FxAudioSettingsView;

import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class FxAudioSettingsFragment extends MvpAppCompatFragment
        implements FxAudioSettingsView {
    private static final String LOG_TAG = FxAudioSettingsFragment.class.getSimpleName();

    @InjectPresenter FxAudioSettingsPresenter presenter;

    private TabFxAudioSettingsBinding binding;


    private static final int BASS_BOOST_PROGRESS_STEP = 20;
    private static final int VIRTUALIZER_PROGRESS_STEP = 20;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = TabFxAudioSettingsBinding.inflate(inflater, container, false);

        binding.playbackSpeedCroller.setOnProgressChangedListener(presenter::onPlaybackSpeedProgressChanged);
        binding.playbackPitchCroller.setOnProgressChangedListener(presenter::onPlaybackPitchProgressChanged);
        binding.bassBoostCroller.setOnProgressChangedListener(progress ->
                presenter.onBassBoostProgressChange(progress * BASS_BOOST_PROGRESS_STEP));
        binding.virtualizerCroller.setOnProgressChangedListener(progress ->
                presenter.onVirtualizerProgressChange(progress * VIRTUALIZER_PROGRESS_STEP));

        binding.playbackSpeedCroller.setOnDoubleTapListener(() -> binding.playbackSpeedCroller.setProgress(50));
        binding.playbackPitchCroller.setOnDoubleTapListener(() -> binding.playbackPitchCroller.setProgress(50));
        binding.bassBoostCroller.setOnDoubleTapListener(() -> binding.bassBoostCroller.setProgress(0));
        binding.virtualizerCroller.setOnDoubleTapListener(() -> binding.virtualizerCroller.setProgress(0));

        binding.playbackSpeedSwitch.setOnCheckedChangeListener((view, isChecked) ->
                presenter.onPlaybackSpeedSwitchClick(isChecked));
        binding.playbackPitchSwitch.setOnCheckedChangeListener((view, isChecked) ->
                presenter.onPlaybackPitchSwitchClick(isChecked));
        binding.bassBoostSwitchButton.setOnCheckedChangeListener((view, isChecked) ->
                presenter.onBassBoostSwitchClick(isChecked));
        binding.virtualizerSwitchButton.setOnCheckedChangeListener((view, isChecked) ->
                presenter.onVirtualizerSwitchClick(isChecked));

        return binding.getRoot();
    }


    @ProvidePresenter
    FxAudioSettingsPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new FxAudioSettingsPresenter(appComponent);
    }


    @Override
    public void setPlaybackSpeedSwitch(boolean enabled) {
        binding.playbackSpeedSwitch.setChecked(enabled);
        binding.playbackSpeedCroller.setEnabled(enabled);
    }

    @Override
    public void setPlaybackPitchSwitch(boolean enabled) {
        binding.playbackPitchSwitch.setChecked(enabled);
        binding.playbackPitchCroller.setEnabled(enabled);
    }

    @Override
    public void setPlaybackSpeedSeekbar(int progress) {
        binding.playbackSpeedCroller.setProgress(progress);
    }


    @Override
    public void setPlaybackSpeedText(float speed) {
        binding.playbackSpeedCroller.setLabel(getString(R.string.fx_tempo, speed));
    }

    @Override
    public void setPlaybackPitchSeekbar(int progress) {
        binding.playbackPitchCroller.setProgress(progress);
    }

    @Override
    public void setPlaybackPitchText(float pitch) {
        binding.playbackPitchCroller.setLabel(getString(R.string.fx_pitch, pitch));
    }

    @Override
    public void hideBassBoostPanel() {
        binding.bassBoostSwitchButton.setVisibility(View.GONE);
        binding.bassBoostCroller.setVisibility(View.GONE);
    }

    @Override
    public void setBassBoostSeekbar(int currentLevel) {
        binding.bassBoostCroller.setProgress(currentLevel / VIRTUALIZER_PROGRESS_STEP);
    }

    @Override
    public void setBassBoostSwitch(boolean bassBoostEnabled) {
        binding.bassBoostSwitchButton.setChecked(bassBoostEnabled);
        binding.bassBoostCroller.setEnabled(bassBoostEnabled);
    }

    @Override
    public void hideVirtualizerPanel() {
        binding.virtualizerSwitchButton.setVisibility(View.GONE);
        binding.virtualizerCroller.setVisibility(View.GONE);
    }

    @Override
    public void setVirtualizerSeekbar(int currentLevel) {
        binding.virtualizerCroller.setProgress(currentLevel / VIRTUALIZER_PROGRESS_STEP);
    }


    @Override
    public void setVirtualizerSwitch(boolean virtualizerEnabled) {
        binding.virtualizerSwitchButton.setChecked(virtualizerEnabled);
        binding.virtualizerCroller.setEnabled(virtualizerEnabled);
    }

}
