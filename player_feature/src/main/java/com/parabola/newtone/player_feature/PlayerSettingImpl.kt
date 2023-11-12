package com.parabola.newtone.player_feature

import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.parabola.domain.interactor.player.PlayerSetting
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class PlayerSettingImpl(
    private val settingSaver: PlayerSettingSaver,
    private val notificationManager: PlayerNotificationManager,
) : PlayerSetting {

    private val observeIsNotificationColorized: BehaviorSubject<Boolean> =
        BehaviorSubject.createDefault(settingSaver.isNotificationBackgroundColorized)
    private val observeIsNotificationArtworkShow: BehaviorSubject<Boolean> =
        BehaviorSubject.createDefault(settingSaver.isNotificationArtworkShow)


    init {
        //  Восстанавливаем режим отображения уведомлений
        notificationManager.setColorized(observeIsNotificationColorized.value!!)
    }

    override fun setNotificationBackgroundColorized(colorized: Boolean) {
        settingSaver.isNotificationBackgroundColorized = colorized
        observeIsNotificationColorized.onNext(colorized)
        notificationManager.setColorized(colorized)
    }

    override fun isNotificationBackgroundColorized(): Boolean =
        observeIsNotificationColorized.value!!

    override fun observeIsNotificationBackgroundColorized(): Observable<Boolean> =
        observeIsNotificationColorized


    override fun setNotificationArtworkShow(show: Boolean) {
        settingSaver.isNotificationArtworkShow = show
        observeIsNotificationArtworkShow.onNext(show)
        notificationManager.invalidate()
    }

    override fun isNotificationArtworkShow(): Boolean =
        observeIsNotificationArtworkShow.value!!

    override fun observeNotificationArtworkShow(): Observable<Boolean> =
        observeIsNotificationArtworkShow

}
