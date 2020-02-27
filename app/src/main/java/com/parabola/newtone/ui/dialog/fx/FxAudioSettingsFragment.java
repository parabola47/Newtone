package com.parabola.newtone.ui.dialog.fx;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.mvp.presenter.fx.FxAudioSettingsPresenter;
import com.parabola.newtone.mvp.view.fx.FxAudioSettingsView;
import com.parabola.newtone.util.SeekBarChangeAdapter;
import com.parabola.newtone.view.Croller;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class FxAudioSettingsFragment extends MvpAppCompatFragment
        implements FxAudioSettingsView {
    private static final String TAG = FxAudioSettingsFragment.class.getSimpleName();

    @InjectPresenter FxAudioSettingsPresenter presenter;

    @BindView(R.id.playbackSpeedCroller) Croller playbackSpeedCroller;
    @BindView(R.id.playbackPitchCroller) Croller playbackPitchCroller;

    @BindView(R.id.bassBoostPanel) ViewGroup bassBoostPanel;
    @BindView(R.id.bassBoostSeekBar) SeekBar bassBoostSeekBar;
    @BindView(R.id.bassBoostSwitchButton) SwitchCompat bassBoostSwitch;

    @BindView(R.id.virtualizerPanel) ViewGroup virtualizerPanel;
    @BindView(R.id.virtualizerSeekBar) SeekBar virtualizerSeekBar;
    @BindView(R.id.virtualizerSwitchButton) SwitchCompat virtualizerSwitchButton;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fx_audio_settings, container, false);
        ButterKnife.bind(this, layout);

        playbackSpeedCroller.setOnProgressChangedListener(presenter::onPlaybackSpeedProgressChanged);
        playbackPitchCroller.setOnProgressChangedListener(presenter::onPlaybackPitchProgressChanged);
        bassBoostSeekBar.setOnSeekBarChangeListener(new SeekBarChangeAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                presenter.onBassBoostProgressChange(progress);
            }
        });
        virtualizerSeekBar.setOnSeekBarChangeListener(new SeekBarChangeAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                presenter.onVirtualizerProgressChange(progress);
            }
        });

        return layout;
    }


    @OnClick(R.id.bassBoostSwitchButton)
    public void onClickBassBoostSwitch() {
        presenter.onBassBoostSwitchCheck(bassBoostSwitch.isChecked());
    }

    @OnClick(R.id.virtualizerSwitchButton)
    public void onClickVirtualizerSwitch() {
        presenter.onVirtualizerSwitchCheck(virtualizerSwitchButton.isChecked());
    }

    @ProvidePresenter
    FxAudioSettingsPresenter providePresenter() {
        return new FxAudioSettingsPresenter(MainApplication.getComponent());
    }


    @Override
    public void setPlaybackSpeedSeekbar(int progress) {
        playbackSpeedCroller.setProgress(progress);
    }


    @Override
    public void setPlaybackSpeedText(float speed) {
        String tempoString = getString(R.string.fx_tempo, speed);
        playbackSpeedCroller.setLabel(tempoString);
    }

    @Override
    public void setPlaybackPitchSeekbar(int progress) {
        playbackPitchCroller.setProgress(progress);
    }

    @Override
    public void setPlaybackPitchText(float pitch) {
        String pitchString = getString(R.string.fx_pitch, pitch);
        playbackPitchCroller.setLabel(pitchString);
    }

    @Override
    public void hideBassBoostPanel() {
        bassBoostPanel.setVisibility(View.GONE);
    }

    @Override
    public void setBassBoostSeekbar(int currentLevel) {
        bassBoostSeekBar.setProgress(currentLevel);
    }

    @Override
    public void setMaxBassBoostSeekbar(int maxStrength) {
        bassBoostSeekBar.setMax(maxStrength);
    }

    @Override
    public void setBassBoostSwitch(boolean bassBoostEnabled) {
        bassBoostSwitch.setChecked(bassBoostEnabled);
    }

    @Override
    public void hideVirtualizerPanel() {
        virtualizerPanel.setVisibility(View.GONE);
    }

    @Override
    public void setVirtualizerSeekbar(int currentLevel) {
        virtualizerSeekBar.setProgress(currentLevel);
    }

    @Override
    public void setMaxVirtualizerSeekbar(int maxStrength) {
        virtualizerSeekBar.setMax(maxStrength);
    }

    @Override
    public void setVirtualizerSwitch(boolean virtualizerEnabled) {
        virtualizerSwitchButton.setChecked(virtualizerEnabled);
    }

}
