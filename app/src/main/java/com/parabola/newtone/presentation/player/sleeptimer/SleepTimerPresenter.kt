package com.parabola.newtone.presentation.player.sleeptimer

import com.parabola.domain.repository.ResourceRepository
import com.parabola.newtone.R
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.ui.router.MainRouter
import com.parabola.newtone.util.TimeFormatterTool
import com.parabola.sleep_timer_feature.SleepTimerInteractor
import com.parabola.sleep_timer_feature.SleepTimerInteractor.TimerAlreadyLaunchedException
import io.reactivex.internal.observers.CallbackCompletableObserver
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class SleepTimerPresenter(appComponent: AppComponent) : MvpPresenter<SleepTimerView>() {

    @Inject
    lateinit var timerInteractor: SleepTimerInteractor

    @Inject
    lateinit var resourceRepo: ResourceRepository

    @Inject
    lateinit var router: MainRouter


    init {
        appComponent.inject(this)
    }


    fun startTimer(timeToSleepMs: Long) {
        timerInteractor.start(timeToSleepMs)
            .subscribe(CallbackCompletableObserver(
                { error ->
                    if (error is TimerAlreadyLaunchedException) {
                        val toastText =
                            resourceRepo.getString(R.string.toast_sleep_timer_already_launched)
                        router.showToast(toastText)
                    } else {
                        throw RuntimeException(error)
                    }
                }
            ) {
                val sleepTimeFormatted =
                    TimeFormatterTool.formatMillisecondsToMinutes(timeToSleepMs)
                val toastText = resourceRepo.getString(
                    R.string.toast_sleep_timer_on_start,
                    sleepTimeFormatted
                )
                router.showToast(toastText)
            })
    }

}
