package com.parabola.newtone.mvp.view;

import com.parabola.domain.interactor.player.PlayerInteractor.RepeatMode;
import com.parabola.domain.model.Track;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;
import moxy.viewstate.strategy.alias.OneExecution;

@AddToEndSingle
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

    void setTimerButtonVisibility(boolean visible);

    void setViewPagerSlide(boolean lock);

    void refreshTracks(List<Track> tracks);
    void setAlbumImagePosition(int currentTrackPosition, boolean smoothScroll);

    @OneExecution
    void showToast(String toastMessage);
}
