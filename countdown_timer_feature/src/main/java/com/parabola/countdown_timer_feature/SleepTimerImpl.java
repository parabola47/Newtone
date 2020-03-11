package com.parabola.countdown_timer_feature;

import com.parabola.domain.interactor.SleepTimerInteractor;
import com.parabola.domain.interactor.type.Irrelevant;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class SleepTimerImpl implements SleepTimerInteractor {


    private final BehaviorSubject<Boolean> observeIsTimerRunning = BehaviorSubject.createDefault(Boolean.FALSE);
    private final PublishSubject<Irrelevant> onTimerFinished = PublishSubject.create();

    private final BehaviorSubject<Long> remainingTimeObserver = BehaviorSubject.createDefault(0L);

    private Disposable timerTask;

    private static final Long UPDATE_NOTIFYING_TIME_MS = 200L;

    @Override
    public Completable start(long timeToSleepMs) {
        return Single.fromCallable(this::launched)
                .flatMapCompletable(isTimerLaunched -> {
                    if (isTimerLaunched)
                        return Completable.error(new TimerAlreadyLaunchedException());
                    return Completable.fromAction(() ->
                            timerTask = startTimerDisposable(timeToSleepMs));
                });
    }

    private Disposable startTimerDisposable(long timeToSleepMs) {
        return Observable.interval(UPDATE_NOTIFYING_TIME_MS, TimeUnit.MILLISECONDS)
                .doOnNext(count -> remainingTimeObserver.onNext(timeToSleepMs - (count * UPDATE_NOTIFYING_TIME_MS)))
                .take(timeToSleepMs, TimeUnit.MILLISECONDS)
                .doOnSubscribe(d -> {
                    observeIsTimerRunning.onNext(Boolean.TRUE);
                    remainingTimeObserver.onNext(timeToSleepMs);
                })
                .doFinally(() -> {
                    timerTask = null;
                    remainingTimeObserver.onNext(0L);
                    observeIsTimerRunning.onNext(Boolean.FALSE);
                })
                .doOnComplete(() -> onTimerFinished.onNext(Irrelevant.INSTANCE))
                .subscribe();
    }


    @Override
    public Completable reset() {
        return Completable.fromAction(timerTask::dispose);
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
        return remainingTimeObserver;
    }

    private long remainingTimeInternal() {
        return remainingTimeObserver.getValue();
    }


    public Observable<Boolean> observeIsTimerRunning() {
        return observeIsTimerRunning;
    }

    @Override
    public Observable<Irrelevant> onTimerFinished() {
        return onTimerFinished;
    }
}
