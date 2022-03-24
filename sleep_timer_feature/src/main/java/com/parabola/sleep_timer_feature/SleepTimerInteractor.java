package com.parabola.sleep_timer_feature;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface SleepTimerInteractor {

    Completable start(long timeToSleepMs);

    Completable reset();

    boolean launched();

    Single<Long> remainingTimeToEnd();

    Observable<Long> observeRemainingTimeToEnd();

    Observable<Boolean> observeIsTimerRunning();

    Observable<Integer> onTimerFinished();


    class TimerNotLaunchedException extends IllegalStateException {
    }

    class TimerAlreadyLaunchedException extends IllegalStateException {
    }
}
