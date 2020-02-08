package com.parabola.newtone.ui.dialog.fx;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

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

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class FxAudioSettingsFragment extends MvpAppCompatFragment
        implements FxAudioSettingsView {
    private static final String TAG = FxAudioSettingsFragment.class.getSimpleName();

    @InjectPresenter FxAudioSettingsPresenter presenter;

    @BindView(R.id.playbackSpeedSeekBar) SeekBar playbackSpeedSeekBar;
    @BindView(R.id.playbackSpeedTextView) TextView playbackSpeedTextView;

    @BindView(R.id.playbackPitchSeekBar) SeekBar playbackPitchSeekBar;
    @BindView(R.id.playbackPitchTextView) TextView playbackPitchTextView;

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

        playbackSpeedSeekBar.setOnSeekBarChangeListener(new SeekBarChangeAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                presenter.onPlaybackSpeedProgressChanged(progress);
            }
        });
        playbackPitchSeekBar.setOnSeekBarChangeListener(new SeekBarChangeAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                presenter.onPlaybackPitchProgressChanged(progress);
            }
        });
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

    private static final String FORMAT_PLAYBACK_SPEED = "x%.2f";   //x0.50 x1.00 x2.00
    private static final String FORMAT_PLAYBACK_PITCH = "x%.2f";   //x0.50 x1.00 x2.00
    private static final String FORMAT_VOLUME = "%d";

    @Override
    public void setPlaybackSpeedSeekbar(int progress) {
        playbackSpeedSeekBar.setProgress(progress);
    }


    @Override
    public void setPlaybackSpeedText(float speed) {
        String tempoString = String.format(Locale.getDefault(), FORMAT_PLAYBACK_SPEED, speed);
        playbackSpeedTextView.setText(tempoString);
    }

    @Override
    public void setPlaybackPitchSeekbar(int progress) {
        playbackPitchSeekBar.setProgress(progress);
    }

    @Override
    public void setPlaybackPitchText(float pitch) {
        String pitchString = String.format(Locale.getDefault(), FORMAT_PLAYBACK_PITCH, pitch);
        playbackPitchTextView.setText(pitchString);
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
