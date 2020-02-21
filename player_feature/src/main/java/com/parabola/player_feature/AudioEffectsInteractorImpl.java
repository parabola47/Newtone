package com.parabola.player_feature;

import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;

import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioListener;
import com.parabola.domain.interactors.player.AudioEffectsInteractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class AudioEffectsInteractorImpl implements AudioEffectsInteractor {
    private static final String LOG_TAG = AudioEffectsInteractorImpl.class.getSimpleName();

    private final BehaviorSubject<Float> playbackSpeed;
    private final BehaviorSubject<Float> playbackPitch;

    private final SimpleExoPlayer exoPlayer;
    private final PlayerSettingSaver settings;


    //FXs
    private Equalizer equalizer;
    private BassBoost bassBoost;
    private Virtualizer virtualizer;

    AudioEffectsInteractorImpl(SimpleExoPlayer exoPlayer, PlayerSettingSaver settings) {
        this.exoPlayer = exoPlayer;
        this.settings = settings;

        PlaybackParameters playbackParameters = new PlaybackParameters(settings.getSavedPlaybackSpeed(), settings.getSavedPlaybackPitch());
        this.exoPlayer.setPlaybackParameters(playbackParameters);

        playbackSpeed = BehaviorSubject.createDefault(exoPlayer.getPlaybackParameters().speed);
        playbackPitch = BehaviorSubject.createDefault(exoPlayer.getPlaybackParameters().pitch);

        this.exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                if (playbackSpeed.getValue() != playbackParameters.speed) {
                    playbackSpeed.onNext(playbackParameters.speed);
                }
                if (playbackPitch.getValue() != playbackParameters.pitch) {
                    playbackPitch.onNext(playbackParameters.pitch);
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
    public float getPlaybackSpeed() {
        return exoPlayer.getPlaybackParameters().speed;
    }

    @Override
    public Observable<Float> observePlaybackSpeed() {
        return playbackSpeed;
    }

    @Override
    public void setPlaybackSpeed(float speed) {
        PlaybackParameters playbackParameters = new PlaybackParameters(
                speed, exoPlayer.getPlaybackParameters().pitch,
                exoPlayer.getPlaybackParameters().skipSilence);

        exoPlayer.setPlaybackParameters(playbackParameters);

        settings.setPlaybackSpeed(speed);
    }


    //    PITCH
    @Override
    public float getPlaybackPitch() {
        return exoPlayer.getPlaybackParameters().pitch;
    }

    @Override
    public Observable<Float> observePlaybackPitch() {
        return playbackPitch;
    }

    @Override
    public void setPlaybackPitch(float pitch) {
        PlaybackParameters playbackParameters = new PlaybackParameters(
                exoPlayer.getPlaybackParameters().speed, pitch,
                exoPlayer.getPlaybackParameters().skipSilence);

        exoPlayer.setPlaybackParameters(playbackParameters);

        settings.setPlaybackPitch(pitch);
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
    public short getBassBoostCurrentLevel() {
        return bassBoost != null ? bassBoost.getRoundedStrength() : 0;
    }

    @Override
    public short getBassBoostMaxLevel() {
        return 1000;
    }

    @Override
    public void setBassBoostLevel(short strength) {
        if (bassBoost != null)
            bassBoost.setStrength(strength);

        settings.setBassBoostStrength(strength);
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
    public short getVirtualizerCurrentLevel() {
        return virtualizer != null ? virtualizer.getRoundedStrength() : 0;
    }

    @Override
    public short getVirtualizerMaxLevel() {
        return 1000;
    }

    @Override
    public void setVirtualizerLevel(short strength) {
        if (virtualizer != null)
            virtualizer.setStrength(strength);

        settings.setVirtualizerStrength(strength);
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
