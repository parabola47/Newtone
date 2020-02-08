package com.parabola.domain.interactors.player;

import com.parabola.domain.model.Track;

import java.util.List;
import java.util.Map.Entry;

import io.reactivex.Flowable;
import io.reactivex.Observable;

public interface PlayerInteractor {

    default void start(List<Track> trackList, int startTrackPosition) {
        start(trackList, startTrackPosition, true);
    }
    default void start(List<Track> trackList, int startTrackPosition, boolean startImmediately) {
        start(trackList, startTrackPosition, startImmediately, 0);
    }
    void start(List<Track> trackList, int startTrackPosition, boolean startImmediately, long playbackPositionMs);

    void stop();


    default void toggle() {
        if (isPlayWhenReady()) pause();
        else resume();
    }


    void resume();
    void pause();


    void next();
    void previous();


    int tracksCount();
    int currentTrackPosition();
    int currentTrackId();
    //ключ - старая позиция трека, значение - новая позиция трека в плейлисте
    Observable<Entry<Integer, Integer>> onMoveTrack();
    void moveTrack(int oldPosition, int newPosition);
    //ключ - id удалённого трека, значение - позиция трека в плейлисте
    Observable<Entry<Integer, Integer>> onRemoveTrack();
    void remove(int trackPosition);


    void seekTo(long playbackPositionMs);
    long playbackPosition();
    Flowable<Long> onChangePlaybackPosition();


    void toggleRepeatMode();
    void setRepeat(boolean enable);
    Observable<Boolean> onRepeatModeChange();
    boolean isRepeatModeEnabled();


    void toggleShuffleMode();
    void setShuffleMode(boolean enable);
    Observable<Boolean> onShuffleModeChange();
    boolean isShuffleEnabled();


    boolean isPlayWhenReady();


    //оповещает об обновлении текущего плейлиста и возвращает список идентификаторов
    //треков в порядке исполнения
    Observable<List<Integer>> onTracklistChanged();
    Observable<Integer> onChangeCurrentTrackId();   //id трека
    Observable<Boolean> onChangePlayingState();     //true, если плеер играет


    AudioEffectsInteractor getAudioEffectInteractor();
    PlayerSetting getPlayerSetting();
}
