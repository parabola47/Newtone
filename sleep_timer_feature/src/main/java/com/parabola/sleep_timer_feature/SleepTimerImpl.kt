package com.parabola.sleep_timer_feature

import com.parabola.sleep_timer_feature.SleepTimerInteractor.TimerAlreadyLaunchedException
import com.parabola.sleep_timer_feature.SleepTimerInteractor.TimerNotLaunchedException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class SleepTimerImpl : SleepTimerInteractor {
    private val observeIsTimerRunning = BehaviorSubject.createDefault(false)

    private val onTimerFinished = PublishSubject.create<Int>()
    private val remainingTimeObserver = BehaviorSubject.createDefault(0L)

    private var timerTask: Disposable? = null

    override fun start(timeToSleepMs: Long): Completable {
        return Single.fromCallable { launched() }
            .flatMapCompletable { isTimerLaunched: Boolean ->
                if (isTimerLaunched) return@flatMapCompletable Completable.error(
                    TimerAlreadyLaunchedException()
                )
                Completable.fromAction { timerTask = startTimerDisposable(timeToSleepMs) }
            }
    }

    private fun startTimerDisposable(timeToSleepMs: Long): Disposable {
        return Observable.interval(UPDATE_NOTIFYING_TIME_MS, TimeUnit.MILLISECONDS)
            .doOnNext { count: Long -> remainingTimeObserver.onNext(timeToSleepMs - count * UPDATE_NOTIFYING_TIME_MS) }
            .take(timeToSleepMs, TimeUnit.MILLISECONDS)
            .doOnSubscribe { d: Disposable? ->
                observeIsTimerRunning.onNext(true)
                remainingTimeObserver.onNext(timeToSleepMs)
            }
            .doFinally {
                timerTask = null
                remainingTimeObserver.onNext(0L)
                observeIsTimerRunning.onNext(false)
            }
            .doOnComplete { onTimerFinished.onNext(1) }
            .subscribe()
    }

    override fun reset(): Completable {
        return Completable.fromAction { timerTask!!.dispose() }
    }

    override fun launched(): Boolean {
        return observeIsTimerRunning.value!!
    }

    override fun remainingTimeToEnd(): Single<Long> {
        return Single.fromCallable { launched() }
            .flatMap { isTimerLaunched: Boolean? ->
                if (!isTimerLaunched!!) return@flatMap Single.error<Long>(TimerNotLaunchedException())
                Single.just(remainingTimeInternal())
            }
    }

    override fun observeRemainingTimeToEnd(): Observable<Long> {
        return remainingTimeObserver
    }

    private fun remainingTimeInternal(): Long {
        return remainingTimeObserver.value!!
    }

    override fun observeIsTimerRunning(): Observable<Boolean> {
        return observeIsTimerRunning
    }

    override fun onTimerFinished(): Observable<Int> {
        return onTimerFinished
    }

    companion object {
        private const val UPDATE_NOTIFYING_TIME_MS = 200L
    }
}