package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.repository.TrackRepository;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.TrackAdditionalInfoView;

import javax.inject.Inject;

import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.observers.ConsumerSingleObserver;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class TrackAdditionalInfoPresenter extends MvpPresenter<TrackAdditionalInfoView> {

    private final int trackId;

    @Inject TrackRepository trackRepo;

    public TrackAdditionalInfoPresenter(AppComponent appComponent, int trackId) {
        this.trackId = trackId;
        appComponent.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        trackRepo.getById(trackId).subscribe(new ConsumerSingleObserver<>(
                getViewState()::setTrack, Functions.ERROR_CONSUMER));
    }

}
