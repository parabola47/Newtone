package com.parabola.newtone.mvp.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.TrackAdditionalInfoView;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

@InjectViewState
public final class TrackAdditionalInfoPresenter extends MvpPresenter<TrackAdditionalInfoView> {

    private final int trackId;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject TrackRepository trackRepo;

    public TrackAdditionalInfoPresenter(AppComponent appComponent, int trackId) {
        this.trackId = trackId;
        appComponent.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        disposables.add(trackRepo.getById(trackId)
                .subscribe(getViewState()::setTrack));
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    public void onClickCancel() {
        getViewState().closeScreen();
    }
}
