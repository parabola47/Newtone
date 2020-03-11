package com.parabola.domain.interactor.observer;

import java.util.concurrent.CountDownLatch;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.util.BlockingHelper;
import io.reactivex.internal.util.ExceptionHelper;
import io.reactivex.plugins.RxJavaPlugins;

public final class ConsumerObserver<T> extends CountDownLatch implements Observer<T>, Disposable {

    T value;
    Throwable error;

    Disposable upstream;

    volatile boolean cancelled;

    final Consumer<? super T> callback;

    public ConsumerObserver(Consumer<T> callback) {
        super(1);
        this.callback = callback;
    }

    public static <T> Observer<T> fromConsumer(Consumer<T> callback) {
        return new ConsumerObserver<>(callback);
    }

    @Override
    public final void onSubscribe(Disposable d) {
        this.upstream = d;
        if (cancelled) {
            d.dispose();
        }
    }

    @Override
    public final void onComplete() {
        countDown();
    }

    @Override
    public void onNext(T value) {
        try {
            callback.accept(value);
        } catch (Exception ex) {
            Exceptions.throwIfFatal(ex);
            RxJavaPlugins.onError(ex);
        }
    }

    @Override
    public void onError(Throwable ex) {
        Exceptions.throwIfFatal(ex);
        RxJavaPlugins.onError(ex);
    }

    @Override
    public final void dispose() {
        cancelled = true;
        Disposable d = this.upstream;
        if (d != null) {
            d.dispose();
        }
    }

    @Override
    public final boolean isDisposed() {
        return cancelled;
    }

    /**
     * Block until the first value arrives and return it, otherwise
     * return null for an empty source and rethrow any exception.
     *
     * @return the first value or null if the source is empty
     */
    public final T blockingGet() {
        if (getCount() != 0) {
            try {
                BlockingHelper.verifyNonBlocking();
                await();
            } catch (InterruptedException ex) {
                dispose();
                throw ExceptionHelper.wrapOrThrow(ex);
            }
        }

        Throwable e = error;
        if (e != null) {
            throw ExceptionHelper.wrapOrThrow(e);
        }
        return value;
    }
}
