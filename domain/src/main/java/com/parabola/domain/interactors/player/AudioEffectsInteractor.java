package com.parabola.domain.interactors.player;

import java.util.List;

import io.reactivex.Observable;

public interface AudioEffectsInteractor {

    Observable<Boolean> observeIsPlaybackSpeedEnabled();
    void setPlaybackSpeedEnabled(boolean enable);
    //результат отличается от реальной скорости проигрывания в случае, если отключено изменение скорости
    float getSavedPlaybackSpeed();
    Observable<Float> observePlaybackSpeed();
    default void setDefaultPlaybackSpeed() {
        setSavedPlaybackSpeed(1.0f);
    }
    void setSavedPlaybackSpeed(float speed);


    Observable<Boolean> observeIsPlaybackPitchEnabled();
    void setPlaybackPitchEnabled(boolean enabled);
    float getSavedPlaybackPitch();
    Observable<Float> observePlaybackPitch();
    default void setDefaultPlaybackPitch() {
        setSavedPlaybackSpeed(1.0f);
    }
    void setSavedPlaybackPitch(float pitch);

    boolean isBassBoostAvailable();
    boolean isBassBoostEnabled();
    void setBassBoostEnable(boolean enable);
    short getBassBoostCurrentLevel();
    short getBassBoostMaxLevel();
    void setBassBoostLevel(short strength); //от 0 до 1000

    boolean isVirtualizerAvailable();
    boolean isVirtualizerEnabled();
    void setVirtualizerEnable(boolean enable);
    short getVirtualizerCurrentLevel();
    short getVirtualizerMaxLevel();
    void setVirtualizerLevel(short strength); //от 0 до 1000


    void setEqEnable(boolean enable);
    short getMinEqBandLevel(); // в дециБеллах
    short getMaxEqBandLevel(); // в дециБеллах
    boolean isEqEnabled();
    void setBandLevel(int bandId, short bandLevel); // в дециБеллах
    List<EqBand> getBands();

    class EqBand {
        public int id;
        public int frequency;       // в Герцах
        public short currentLevel;  // в дециБеллах
    }
}
