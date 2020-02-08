package com.parabola.newtone.mvp.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.interactors.SleepTimerInteractor;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.SleepTimerView;

import javax.inject.Inject;

@InjectViewState
public final class SleepTimerPresenter extends MvpPresenter<SleepTimerView> {

    @Inject SleepTimerInteractor timerInteractor;

    public SleepTimerPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    public void startTimer(long timeToSleepMs) {
        timerInteractor.start(timeToSleepMs)
                .subscribe(getViewState()::closeScreen);
    }

    public void onClickCancel() {
        getViewState().closeScreen();
    }
}
