package com.parabola.newtone.mvp.view;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.parabola.domain.model.Track;

import java.util.List;

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
    void setLoopEnabling(Boolean enable);
    void setShuffleEnabling(Boolean enable);

    void setCurrentTimeText(String currentTimeFormatted);
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
