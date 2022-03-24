package com.parabola.sleep_timer_feature

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface SleepTimerInteractor {
    fun start(timeToSleepMs: Long): Completable

    fun reset(): Completable

    fun launched(): Boolean

    fun remainingTimeToEnd(): Single<Long>

    fun observeRemainingTimeToEnd(): Observable<Long>
    fun observeIsTimerRunning(): Observable<Boolean>

    fun onTimerFinished(): Observable<Int>

    class TimerNotLaunchedException : IllegalStateException()
    class TimerAlreadyLaunchedException : IllegalStateException()
}
