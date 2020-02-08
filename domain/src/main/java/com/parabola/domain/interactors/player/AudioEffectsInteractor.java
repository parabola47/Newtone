package com.parabola.domain.interactors.player;

import java.util.List;

import io.reactivex.Observable;

public interface AudioEffectsInteractor {

    float getPlaybackSpeed();
    Observable<Float> observePlaybackSpeed();
    default void setDefaultPlaybackSpeed() {
        setPlaybackSpeed(1.0f);
    }
    void setPlaybackSpeed(float speed);


    float getPlaybackPitch();
    Observable<Float> observePlaybackPitch();
    default void setDefaultPlaybackPitch() {
        setPlaybackSpeed(1.0f);
    }
    void setPlaybackPitch(float pitch);

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
