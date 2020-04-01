package com.parabola.player_feature;

import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;

import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioListener;
import com.parabola.domain.interactor.player.AudioEffectsInteractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class AudioEffectsInteractorImpl implements AudioEffectsInteractor {
    private static final String LOG_TAG = AudioEffectsInteractorImpl.class.getSimpleName();

    private final BehaviorSubject<Boolean> playbackSpeedEnabledUpdates;
    private final BehaviorSubject<Float> savedPlaybackSpeed;
    private final BehaviorSubject<Boolean> playbackPitchEnabledUpdates;
    private final BehaviorSubject<Float> savedPlaybackPitch;

    private final BehaviorSubject<Short> savedVirtualizerLevelUpdates;
    private final BehaviorSubject<Short> savedBassBoostLevelUpdates;

    private final SimpleExoPlayer exoPlayer;
    private final PlayerSettingSaver settings;


    //FXs
    private Equalizer equalizer;
    private BassBoost bassBoost;
    private Virtualizer virtualizer;

    private static final float DEFAULT_PLAYBACK_SPEED = 1.0f;
    private static final float DEFAULT_PLAYBACK_PITCH = 1.0f;

    AudioEffectsInteractorImpl(SimpleExoPlayer exoPlayer, PlayerSettingSaver settings) {
        this.exoPlayer = exoPlayer;
        this.settings = settings;

        float speed = settings.getSavedPlaybackSpeedEnabled() ? settings.getSavedPlaybackSpeed() : DEFAULT_PLAYBACK_SPEED;
        float pitch = settings.getSavedPlaybackPitchEnabled() ? settings.getSavedPlaybackPitch() : DEFAULT_PLAYBACK_PITCH;

        PlaybackParameters playbackParameters = new PlaybackParameters(speed, pitch);
        this.exoPlayer.setPlaybackParameters(playbackParameters);

        savedPlaybackSpeed = BehaviorSubject.createDefault(settings.getSavedPlaybackSpeed());
        savedPlaybackPitch = BehaviorSubject.createDefault(settings.getSavedPlaybackPitch());

        playbackSpeedEnabledUpdates = BehaviorSubject.createDefault(settings.getSavedPlaybackSpeedEnabled());
        playbackPitchEnabledUpdates = BehaviorSubject.createDefault(settings.getSavedPlaybackPitchEnabled());

        savedVirtualizerLevelUpdates = BehaviorSubject.createDefault(settings.getSavedVirtualizerStrength());
        savedBassBoostLevelUpdates = BehaviorSubject.createDefault(settings.getSavedBassBoostStrength());

        this.exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                if (savedPlaybackSpeed.getValue() != playbackParameters.speed && settings.getSavedPlaybackSpeedEnabled()) {
                    savedPlaybackSpeed.onNext(playbackParameters.speed);
                }
                if (savedPlaybackPitch.getValue() != playbackParameters.pitch && settings.getSavedPlaybackPitchEnabled()) {
                    savedPlaybackPitch.onNext(playbackParameters.pitch);
                }
            }
        });

        this.exoPlayer.addAudioListener(new AudioListener() {
            @Override
            public void onAudioSessionId(int audioSessionId) {
                try {
                    bassBoost = new BassBoost(0, audioSessionId);
                } catch (RuntimeException ignored) {
                }
                if (bassBoost != null) {
                    bassBoost.setEnabled(settings.getSavedBassBoostEnabled());
                    bassBoost.setStrength(settings.getSavedBassBoostStrength());
                }

                try {
                    virtualizer = new Virtualizer(0, audioSessionId);
                } catch (RuntimeException ignored) {
                }
                if (virtualizer != null) {
                    virtualizer.setEnabled(settings.getSavedVirtualizerEnabled());
                    virtualizer.setStrength(settings.getSavedVirtualizerStrength());
                }

                try {
                    equalizer = new Equalizer(0, audioSessionId);
                } catch (RuntimeException ignored) {
                }
                if (equalizer != null) {
                    equalizer.setEnabled(settings.getSavedIsEqEnabled());
                    for (short i = 0; i < equalizer.getNumberOfBands(); i++) {
                        equalizer.setBandLevel(i, (short) (settings.getSavedBandLevel(i) * 100));
                    }
                }
            }
        });
    }


    //    SPEED
    @Override
    public Observable<Boolean> observeIsPlaybackSpeedEnabled() {
        return playbackSpeedEnabledUpdates;
    }

    @Override
    public void setPlaybackSpeedEnabled(boolean enable) {
        settings.setSavedPlaybackSpeedEnabled(enable);
        playbackSpeedEnabledUpdates.onNext(enable);

        float speed = enable ? settings.getSavedPlaybackSpeed() : DEFAULT_PLAYBACK_SPEED;

        PlaybackParameters playbackParameters = new PlaybackParameters(
                speed, exoPlayer.getPlaybackParameters().pitch, exoPlayer.getPlaybackParameters().skipSilence);
        this.exoPlayer.setPlaybackParameters(playbackParameters);
    }

    @Override
    public float getSavedPlaybackSpeed() {
        return savedPlaybackSpeed.getValue();
    }

    @Override
    public Observable<Float> observePlaybackSpeed() {
        return savedPlaybackSpeed;
    }

    @Override
    public void setSavedPlaybackSpeed(float speed) {
        settings.setPlaybackSpeed(speed);

        if (settings.getSavedPlaybackSpeedEnabled()) {
            PlaybackParameters playbackParameters = new PlaybackParameters(
                    speed, exoPlayer.getPlaybackParameters().pitch, exoPlayer.getPlaybackParameters().skipSilence);

            exoPlayer.setPlaybackParameters(playbackParameters);
        }
    }


    //    PITCH
    @Override
    public Observable<Boolean> observeIsPlaybackPitchEnabled() {
        return playbackPitchEnabledUpdates;
    }

    @Override
    public void setPlaybackPitchEnabled(boolean enabled) {
        settings.setSavedPlaybackPitchEnabled(enabled);
        playbackPitchEnabledUpdates.onNext(enabled);

        float pitch = enabled ? settings.getSavedPlaybackPitch() : DEFAULT_PLAYBACK_PITCH;

        PlaybackParameters playbackParameters = new PlaybackParameters(
                exoPlayer.getPlaybackParameters().speed, pitch, exoPlayer.getPlaybackParameters().skipSilence);
        this.exoPlayer.setPlaybackParameters(playbackParameters);
    }

    @Override
    public float getSavedPlaybackPitch() {
        return savedPlaybackPitch.getValue();
    }

    @Override
    public Observable<Float> observePlaybackPitch() {
        return savedPlaybackPitch;
    }

    @Override
    public void setSavedPlaybackPitch(float pitch) {
        settings.setPlaybackPitch(pitch);

        if (settings.getSavedPlaybackPitchEnabled()) {
            PlaybackParameters playbackParameters = new PlaybackParameters(
                    exoPlayer.getPlaybackParameters().speed, pitch, exoPlayer.getPlaybackParameters().skipSilence);

            exoPlayer.setPlaybackParameters(playbackParameters);
        }
    }


    //    BASS BOOST
    @Override
    public boolean isBassBoostAvailable() {
        return bassBoost != null && bassBoost.getStrengthSupported();
    }

    @Override
    public boolean isBassBoostEnabled() {
        return bassBoost != null && bassBoost.getEnabled();
    }

    @Override
    public void setBassBoostEnable(boolean enable) {
        if (bassBoost != null)
            bassBoost.setEnabled(enable);

        settings.setBassBoostEnabled(enable);
    }

    @Override
    public Observable<Short> observeBassBoostLevel() {
        return savedBassBoostLevelUpdates;
    }

    @Override
    public void setBassBoostLevel(short strength) {
        if (bassBoost != null)
            bassBoost.setStrength(strength);

        settings.setBassBoostStrength(strength);
        savedBassBoostLevelUpdates.onNext(strength);
    }


    //    VIRTUALIZER
    @Override
    public boolean isVirtualizerAvailable() {
        return virtualizer != null && virtualizer.getStrengthSupported();
    }

    @Override
    public boolean isVirtualizerEnabled() {
        return virtualizer != null && virtualizer.getEnabled();
    }

    @Override
    public void setVirtualizerEnable(boolean enable) {
        if (virtualizer != null)
            virtualizer.setEnabled(enable);

        settings.setVirtualizerEnabled(enable);
    }

    @Override
    public Observable<Short> observeVirtualizerLevel() {
        return savedVirtualizerLevelUpdates;
    }

    @Override
    public void setVirtualizerLevel(short strength) {
        if (virtualizer != null)
            virtualizer.setStrength(strength);

        settings.setVirtualizerStrength(strength);
        savedVirtualizerLevelUpdates.onNext(strength);
    }

    //    EQ
    @Override
    public void setEqEnable(boolean enable) {
        if (equalizer != null)
            equalizer.setEnabled(enable);

        settings.setEqEnabled(enable);
    }

    @Override
    public boolean isEqEnabled() {
        return equalizer != null && equalizer.getEnabled();
    }

    @Override
    public short getMaxEqBandLevel() {
        return equalizer != null ? (short) ((equalizer.getBandLevelRange()[1]) / 100) : 0;
    }

    @Override
    public short getMinEqBandLevel() {
        return equalizer != null ? (short) (equalizer.getBandLevelRange()[0] / 100) : 0;
    }

    @Override
    public void setBandLevel(int bandId, short bandLevel) {
        if (equalizer != null)
            equalizer.setBandLevel((short) bandId, (short) (bandLevel * 100));

        settings.setBandLevel((short) bandId, bandLevel);
    }

    @Override
    public List<EqBand> getBands() {
        if (equalizer == null)
            return Collections.emptyList();

        List<EqBand> bands = new ArrayList<>(equalizer.getNumberOfBands());

        for (short i = 0; i < equalizer.getNumberOfBands(); i++) {
            EqBand band = new EqBand();
            band.id = i;
            band.frequency = equalizer.getCenterFreq(i) / 1000;
            band.currentLevel = (short) (equalizer.getBandLevel(i) / 100);
            bands.add(band);
        }

        return bands;
    }
}
