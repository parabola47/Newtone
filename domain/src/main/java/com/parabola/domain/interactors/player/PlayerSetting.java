package com.parabola.domain.interactors.player;

import io.reactivex.Observable;

public interface PlayerSetting {
    void setNotificationBackgroundColorized(boolean colorized);
    boolean isNotificationBackgroundColorized();
    Observable<Boolean> observeIsNotificationBackgroundColorized();


    void setNotificationArtworkShow(boolean show);
    boolean isNotificationArtworkShow();
    Observable<Boolean> observeNotificationArtworkShow();
}
