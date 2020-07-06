package com.parabola.domain.interactor.player;

import com.parabola.domain.model.Track;

import java.util.List;

import io.reactivex.Completable;
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

    void startInShuffleMode(List<Track> trackList);

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
    Observable<MovedTrackItem> onMoveTrack();
    Completable moveTrack(int oldPosition, int newPosition);
    //ключ - id удалённого трека, значение - позиция трека в плейлисте
    Observable<RemovedTrackItem> onRemoveTrack();
    Completable remove(int trackPosition);


    void seekTo(long playbackPositionMs);
    long playbackPosition();
    Flowable<Long> onChangePlaybackPosition();


    enum RepeatMode {OFF, ONE, ALL}

    RepeatMode DEFAULT_REPEAT_MODE = RepeatMode.OFF;

    void toggleRepeatMode();
    void setRepeatMode(RepeatMode repeatMode);
    Observable<RepeatMode> onRepeatModeChange();
    RepeatMode getRepeatMode();


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


    static MovedTrackItem createMoveTrackItem(int oldPosition, int newPosition) {
        return new MovedTrackItem(oldPosition, newPosition);
    }

    static RemovedTrackItem createRemoveTrackItem(int id, int position) {
        return new RemovedTrackItem(id, position);
    }

    class RemovedTrackItem {
        public final int id;
        public final int position;

        private RemovedTrackItem(int id, int position) {
            this.id = id;
            this.position = position;
        }
    }

    class MovedTrackItem {
        public final int oldPosition;
        public final int newPosition;

        private MovedTrackItem(int oldPosition, int newPosition) {
            this.oldPosition = oldPosition;
            this.newPosition = newPosition;
        }
    }

}
