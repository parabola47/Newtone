package com.parabola.newtone.presentation.base

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import moxy.MvpPresenter
import moxy.MvpView

abstract class BasePresenter<View : MvpView> : MvpPresenter<View>() {

    private val disposables = CompositeDisposable()

    override fun onDestroy() {
        disposables.dispose()
    }

    fun <T : Any> Observable<T>.schedule(
        onNext: ((T) -> Unit)? = null,
        onError: (Throwable) -> Unit = { internalError -> throw internalError },
    ) {
        subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { item -> onNext?.invoke(item) },
                { error -> onError.invoke(error) },
            )
            .also { disposables.add(it) }
    }

    fun <T : Any> Single<T>.schedule(
        onSuccess: ((T) -> Unit)? = null,
        onError: (Throwable) -> Unit = { internalError -> throw internalError },
    ) {
        subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { item -> onSuccess?.invoke(item) },
                { error -> onError.invoke(error) },
            )
            .also { disposables.add(it) }
    }

    fun Completable.schedule(
        onComplete: (() -> Unit)? = null,
        onError: (Throwable) -> Unit = { internalError -> throw internalError },
    ) {
        subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onComplete?.invoke() },
                { error -> onError.invoke(error) },
            )
            .also { disposables.add(it) }
    }

}
