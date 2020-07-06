package com.parabola.newtone.mvp.view;

import com.parabola.domain.interactor.player.PlayerInteractor.RepeatMode;
import com.parabola.domain.model.Track;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface PlayerView extends MvpView {
    void setArtist(String artistName);
    void setAlbum(String albumTitle);
    void setTitle(String trackTitle);
    void setDurationText(String durationFormatted);
    void setDurationMs(int durationMs);
    void setIsFavourite(boolean isFavourite);

    void setPlaybackButtonAsPause();
    void setPlaybackButtonAsPlay();
    void setRepeatMode(RepeatMode repeatMode);
    void setShuffleEnabling(boolean enable);

    void setCurrentTimeMs(int currentTimeMs);

    void setTimerColored();
    void setTimerNotColored();

    void setViewPagerSlide(boolean lock);

    void refreshTracks(List<Track> tracks);
    void setAlbumImagePosition(int currentTrackPosition, boolean smoothScroll);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void moveTrack(int oldPosition, int newPosition);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void removeTrack(int position);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showToast(String toastMessage);
}
