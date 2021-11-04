package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.interactor.SleepTimerInteractor;
import com.parabola.domain.repository.ResourceRepository;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.SleepTimerView;
import com.parabola.newtone.ui.router.MainRouter;
import com.parabola.newtone.util.TimeFormatterTool;

import javax.inject.Inject;

import io.reactivex.internal.observers.CallbackCompletableObserver;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class SleepTimerPresenter extends MvpPresenter<SleepTimerView> {

    @Inject SleepTimerInteractor timerInteractor;
    @Inject ResourceRepository resourceRepo;
    @Inject MainRouter router;

    public SleepTimerPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }


    public void startTimer(long timeToSleepMs) {
        timerInteractor.start(timeToSleepMs)
                .subscribe(new CallbackCompletableObserver(
                        error -> {
                            if (error instanceof SleepTimerInteractor.TimerAlreadyLaunchedException) {
                                String toastText = resourceRepo.getString(R.string.toast_sleep_timer_already_launched);
                                router.showToast(toastText);
                            } else {
                                throw new RuntimeException(error);
                            }
                        },
                        () -> {
                            String sleepTimeFormatted = TimeFormatterTool.formatMillisecondsToMinutes(timeToSleepMs);
                            String toastText = resourceRepo.getString(R.string.toast_sleep_timer_on_start, sleepTimeFormatted);
                            router.showToast(toastText);
                        }));
    }

}
