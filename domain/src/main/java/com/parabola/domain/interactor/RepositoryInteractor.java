package com.parabola.domain.interactor;

import io.reactivex.Observable;

public interface RepositoryInteractor {


    Observable<LoadingState> observeLoadingState();


    enum LoadingState {
        IN_LOADING, LOADED
    }
}
