package com.parabola.domain.interactors;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface SleepTimerInteractor {

    Completable start(long timeToSleepMs);

    Completable reset();

    Single<Boolean> launched();

    Single<Long> remainingTimeToEnd();

    Observable<Long> observeRemainingTimeToEnd();

    Single<State> state();

    Observable<State> observeState();

    enum State {
        NOT_RUNNING, RUNNING, FINISHED
    }

    class TimerNotLaunchedException extends IllegalStateException {
    }

    class TimerAlreadyLaunchedException extends IllegalStateException {
    }
}
