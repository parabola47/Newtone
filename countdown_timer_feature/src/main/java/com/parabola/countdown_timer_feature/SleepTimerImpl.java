package com.parabola.countdown_timer_feature;

import com.parabola.domain.interactors.SleepTimerInteractor;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

public class SleepTimerImpl implements SleepTimerInteractor {

    private Timer timer;
    private TimerTask sleepTimerTask;

    private final BehaviorSubject<State> state = BehaviorSubject.createDefault(State.NOT_RUNNING);

    @Override
    public Completable start(long timeToSleepMs) {
        return Single.fromCallable(this::launchedInternal)
                .flatMapCompletable(isTimerLaunched -> {
                    if (isTimerLaunched)
                        return Completable.error(new TimerAlreadyLaunchedException());
                    return Completable.fromAction(() -> {
                        timer = new Timer();

                        sleepTimerTask = createTimerTask();
                        timer.schedule(sleepTimerTask, timeToSleepMs);
                        state.onNext(State.RUNNING);
                    });
                });
    }

    private TimerTask createTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                state.onNext(State.FINISHED);
            }
        };
    }

    @Override
    public Completable reset() {
        return Completable.fromAction(() -> {
            timer.cancel();
            state.onNext(State.NOT_RUNNING);
        });
    }

    @Override
    public Single<Boolean> launched() {
        return Single.fromCallable(this::launchedInternal);
    }


    @Override
    public Single<Long> remainingTimeToEnd() {
        return Single.fromCallable(this::launchedInternal)
                .flatMap(isTimerLaunched -> {
                    if (!isTimerLaunched)
                        return Single.error(new TimerNotLaunchedException());

                    return Single.just(remainingTimeInternal());
                });
    }


    @Override
    public Observable<Long> observeRemainingTimeToEnd() {
        return Observable.interval(0L, 200L, TimeUnit.MILLISECONDS)
                .filter(aLong -> launchedInternal())
                .map(aLong -> remainingTimeInternal());
    }

    private long remainingTimeInternal() {
        return sleepTimerTask.scheduledExecutionTime() - System.currentTimeMillis();
    }

    private boolean launchedInternal() {
        return state.getValue() == State.RUNNING;
    }


    @Override
    public Single<State> state() {
        return state.singleOrError();
    }

    @Override
    public Observable<State> observeState() {
        return state;
    }
}
