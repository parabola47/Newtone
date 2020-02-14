package com.parabola.newtone.mvp.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.interactors.SleepTimerInteractor;
import com.parabola.domain.repository.ResourceRepository;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.SleepTimerView;
import com.parabola.newtone.util.TimeFormatterTool;

import javax.inject.Inject;

@InjectViewState
public final class SleepTimerPresenter extends MvpPresenter<SleepTimerView> {

    @Inject SleepTimerInteractor timerInteractor;
    @Inject ResourceRepository resourceRepo;

    public SleepTimerPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    public void startTimer(long timeToSleepMs) {
        timerInteractor.start(timeToSleepMs)
                .subscribe(
                        () -> {
                            String sleepTimeFormatted = TimeFormatterTool.formatMillisecondsToMinutes(timeToSleepMs);
                            String toastText = resourceRepo.getString(R.string.toast_sleep_timer_on_start, sleepTimeFormatted);
                            getViewState().showToast(toastText);
                            getViewState().closeScreen();
                        },
                        error -> {
                            if (error instanceof SleepTimerInteractor.TimerAlreadyLaunchedException) {
                                String toastText = resourceRepo.getString(R.string.toast_sleep_timer_already_launched);
                                getViewState().showToast(toastText);
                                getViewState().closeScreen();
                            } else {
                                throw new RuntimeException(error);
                            }
                        });
    }

    public void onClickCancel() {
        getViewState().closeScreen();
    }
}
