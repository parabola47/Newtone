package com.parabola.countdown_timer_feature;

import com.parabola.domain.interactors.SleepTimerInteractor;
import com.parabola.domain.interactors.type.Irrelevant;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class SleepTimerImpl implements SleepTimerInteractor {

    private Timer timer;
    private TimerTask sleepTimerTask;

    private final BehaviorSubject<Boolean> observeIsTimerRunning = BehaviorSubject.createDefault(Boolean.FALSE);
    private final PublishSubject<Irrelevant> onTimerFinished = PublishSubject.create();

    @Override
    public Completable start(long timeToSleepMs) {
        return Single.fromCallable(this::launched)
                .flatMapCompletable(isTimerLaunched -> {
                    if (isTimerLaunched)
                        return Completable.error(new TimerAlreadyLaunchedException());
                    return Completable.fromAction(() -> {
                        timer = new Timer();

                        sleepTimerTask = createTimerTask();
                        timer.schedule(sleepTimerTask, timeToSleepMs);
                        observeIsTimerRunning.onNext(Boolean.TRUE);
                    });
                });
    }

    private TimerTask createTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                onTimerFinished.onNext(Irrelevant.INSTANCE);
                observeIsTimerRunning.onNext(Boolean.FALSE);
            }
        };
    }

    @Override
    public Completable reset() {
        return Completable.fromAction(() -> {
            timer.cancel();
            observeIsTimerRunning.onNext(Boolean.FALSE);
        });
    }

    @Override
    public boolean launched() {
        return observeIsTimerRunning.getValue();
    }


    @Override
    public Single<Long> remainingTimeToEnd() {
        return Single.fromCallable(this::launched)
                .flatMap(isTimerLaunched -> {
                    if (!isTimerLaunched)
                        return Single.error(new TimerNotLaunchedException());

                    return Single.just(remainingTimeInternal());
                });
    }


    @Override
    public Observable<Long> observeRemainingTimeToEnd() {
        return Observable.interval(0L, 200L, TimeUnit.MILLISECONDS)
                .filter(l -> launched())
                .map(l -> remainingTimeInternal());
    }

    private long remainingTimeInternal() {
        return sleepTimerTask.scheduledExecutionTime() - System.currentTimeMillis();
    }


    public Observable<Boolean> observeIsTimerRunning() {
        return observeIsTimerRunning;
    }

    @Override
    public Observable<Irrelevant> onTimerFinished() {
        return onTimerFinished;
    }
}
