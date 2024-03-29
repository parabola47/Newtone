package com.parabola.domain.interactor.player;

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
    Observable<Short> observeBassBoostLevel();
    void setBassBoostLevel(short strength); //от 0 до 1000

    boolean isVirtualizerAvailable();
    boolean isVirtualizerEnabled();
    void setVirtualizerEnable(boolean enable);
    Observable<Short> observeVirtualizerLevel();
    void setVirtualizerLevel(short strength); //от 0 до 1000


    boolean isEqAvailable();
    void setEqEnable(boolean enable);
    Observable<Boolean> observeEqEnabling();

    int getMinEqBandLevel(); // в дециБеллах
    int getMaxEqBandLevel(); // в дециБеллах
    void setBandLevel(int bandId, int bandLevel); // в дециБеллах
    List<EqBand> getBands();

    void usePreset(int presetIndex);
    List<String> getPresets();

    class EqBand {
        public int id;
        public int frequency;       // в Герцах
        public int currentLevel;  // в дециБеллах
    }
}
