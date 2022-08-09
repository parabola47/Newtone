package com.parabola.newtone.mvp.presenter

import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.repository.ResourceRepository
import com.parabola.newtone.R
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.mvp.view.TimeToSleepInfoView
import com.parabola.newtone.util.TimeFormatterTool
import com.parabola.sleep_timer_feature.SleepTimerInteractor
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class TimeToSleepInfoPresenter(appComponent: AppComponent) : MvpPresenter<TimeToSleepInfoView>() {

    @Inject
    lateinit var timerInteractor: SleepTimerInteractor

    @Inject
    lateinit var resourceRepo: ResourceRepository

    @Inject
    lateinit var schedulers: SchedulerProvider

    private val disposables = CompositeDisposable()


    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        disposables.addAll(
            observeRemainingTime(),
            closeScreenWhenTimerFinished(),
        )
    }

    override fun onDestroy() {
        disposables.dispose()
    }


    private fun observeRemainingTime(): Disposable {
        return timerInteractor.observeRemainingTimeToEnd()
            .map(TimeFormatterTool::formatMillisecondsToMinutes)
            .map { remainingTimeFormatted ->
                resourceRepo.getString(
                    R.string.time_to_end_sleep_info_dialog,
                    remainingTimeFormatted,
                )
            }
            .observeOn(schedulers.ui())
            .subscribe(viewState::updateTimeToEndText)
    }

    private fun closeScreenWhenTimerFinished(): Disposable {
        return timerInteractor.onTimerFinished()
            .observeOn(schedulers.ui())
            .subscribe { viewState.closeScreen() }
    }

    fun onClickReset() {
        timerInteractor.reset()
            .subscribe()
    }

}
