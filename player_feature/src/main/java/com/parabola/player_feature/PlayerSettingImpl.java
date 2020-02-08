package com.parabola.player_feature;

import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.parabola.domain.interactors.player.PlayerSetting;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public final class PlayerSettingImpl implements PlayerSetting {

    private final PlayerSettingSaver settingSaver;
    private final PlayerNotificationManager notificationManager;

    private final BehaviorSubject<Boolean> observeIsNotificationColorized;
    private final BehaviorSubject<Boolean> observeIsNotificationArtworkShow;

    public PlayerSettingImpl(PlayerSettingSaver settingSaver, PlayerNotificationManager notificationManager) {
        this.settingSaver = settingSaver;
        this.notificationManager = notificationManager;

        //  Восстанавливаем режим отображения уведомлений
        this.observeIsNotificationColorized = BehaviorSubject.createDefault(settingSaver.isNotificationBackgroundColorized());
        this.observeIsNotificationArtworkShow = BehaviorSubject.createDefault(settingSaver.isNotificationArtworkShow());

        this.notificationManager.setColorized(observeIsNotificationColorized.getValue());
    }

    @Override
    public void setNotificationBackgroundColorized(boolean colorized) {
        settingSaver.setNotificationBackgroundColorized(colorized);
        observeIsNotificationColorized.onNext(colorized);
        notificationManager.setColorized(colorized);
    }

    @Override
    public boolean isNotificationBackgroundColorized() {
        return observeIsNotificationColorized.getValue();
    }

    @Override
    public Observable<Boolean> observeIsNotificationBackgroundColorized() {
        return observeIsNotificationColorized;
    }


    @Override
    public void setNotificationArtworkShow(boolean show) {
        settingSaver.setNotificationArtworkShow(show);
        observeIsNotificationArtworkShow.onNext(show);
        notificationManager.invalidate();
    }

    @Override
    public boolean isNotificationArtworkShow() {
        return observeIsNotificationArtworkShow.getValue();
    }

    @Override
    public Observable<Boolean> observeNotificationArtworkShow() {
        return observeIsNotificationArtworkShow;
    }
}
