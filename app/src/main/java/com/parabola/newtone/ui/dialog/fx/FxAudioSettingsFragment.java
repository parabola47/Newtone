package com.parabola.newtone.ui.dialog.fx;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.fx.FxAudioSettingsPresenter;
import com.parabola.newtone.mvp.view.fx.FxAudioSettingsView;
import com.parabola.newtone.ui.view.Croller;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class FxAudioSettingsFragment extends MvpAppCompatFragment
        implements FxAudioSettingsView {
    private static final String LOG_TAG = FxAudioSettingsFragment.class.getSimpleName();

    @InjectPresenter FxAudioSettingsPresenter presenter;

    @BindView(R.id.playbackSpeedCroller) Croller playbackSpeedCroller;
    @BindView(R.id.playbackSpeedSwitch) SwitchCompat playbackSpeedSwitch;

    @BindView(R.id.playbackPitchCroller) Croller playbackPitchCroller;
    @BindView(R.id.playbackPitchSwitch) SwitchCompat playbackPitchSwitch;

    @BindView(R.id.bassBoostCroller) Croller bassBoostCroller;
    @BindView(R.id.bassBoostSwitchButton) SwitchCompat bassBoostSwitch;

    @BindView(R.id.virtualizerCroller) Croller virtualizerCroller;
    @BindView(R.id.virtualizerSwitchButton) SwitchCompat virtualizerSwitch;


    private static final int BASS_BOOST_PROGRESS_STEP = 20;
    private static final int VIRTUALIZER_PROGRESS_STEP = 20;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.tab_fx_audio_settings, container, false);
        ButterKnife.bind(this, layout);

        playbackSpeedCroller.setOnProgressChangedListener(presenter::onPlaybackSpeedProgressChanged);
        playbackPitchCroller.setOnProgressChangedListener(presenter::onPlaybackPitchProgressChanged);
        bassBoostCroller.setOnProgressChangedListener(progress ->
                presenter.onBassBoostProgressChange(progress * BASS_BOOST_PROGRESS_STEP));
        virtualizerCroller.setOnProgressChangedListener(progress ->
                presenter.onVirtualizerProgressChange(progress * VIRTUALIZER_PROGRESS_STEP));

        playbackSpeedSwitch.setOnCheckedChangeListener((view, isChecked) ->
                presenter.onPlaybackSpeedSwitchClick(isChecked));
        playbackPitchSwitch.setOnCheckedChangeListener((view, isChecked) ->
                presenter.onPlaybackPitchSwitchClick(isChecked));
        bassBoostSwitch.setOnCheckedChangeListener((view, isChecked) ->
                presenter.onBassBoostSwitchClick(isChecked));
        virtualizerSwitch.setOnCheckedChangeListener((view, isChecked) ->
                presenter.onVirtualizerSwitchClick(isChecked));

        return layout;
    }


    @ProvidePresenter
    FxAudioSettingsPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new FxAudioSettingsPresenter(appComponent);
    }


    @Override
    public void setPlaybackSpeedSwitch(boolean enabled) {
        playbackSpeedSwitch.setChecked(enabled);
        playbackSpeedCroller.setEnabled(enabled);
    }

    @Override
    public void setPlaybackPitchSwitch(boolean enabled) {
        playbackPitchSwitch.setChecked(enabled);
        playbackPitchCroller.setEnabled(enabled);
    }

    @Override
    public void setPlaybackSpeedSeekbar(int progress) {
        playbackSpeedCroller.setProgress(progress);
    }


    @Override
    public void setPlaybackSpeedText(float speed) {
        playbackSpeedCroller.setLabel(getString(R.string.fx_tempo, speed));
    }

    @Override
    public void setPlaybackPitchSeekbar(int progress) {
        playbackPitchCroller.setProgress(progress);
    }

    @Override
    public void setPlaybackPitchText(float pitch) {
        playbackPitchCroller.setLabel(getString(R.string.fx_pitch, pitch));
    }

    @Override
    public void hideBassBoostPanel() {
        bassBoostSwitch.setVisibility(View.GONE);
        bassBoostCroller.setVisibility(View.GONE);
    }

    @Override
    public void setBassBoostSeekbar(int currentLevel) {
        bassBoostCroller.setProgress(currentLevel / VIRTUALIZER_PROGRESS_STEP);
    }

    @Override
    public void setBassBoostSwitch(boolean bassBoostEnabled) {
        bassBoostSwitch.setChecked(bassBoostEnabled);
        bassBoostCroller.setEnabled(bassBoostEnabled);
    }

    @Override
    public void hideVirtualizerPanel() {
        virtualizerSwitch.setVisibility(View.GONE);
        virtualizerCroller.setVisibility(View.GONE);
    }

    @Override
    public void setVirtualizerSeekbar(int currentLevel) {
        virtualizerCroller.setProgress(currentLevel / VIRTUALIZER_PROGRESS_STEP);
    }


    @Override
    public void setVirtualizerSwitch(boolean virtualizerEnabled) {
        virtualizerSwitch.setChecked(virtualizerEnabled);
        virtualizerCroller.setEnabled(virtualizerEnabled);
    }

}
