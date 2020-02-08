package com.parabola.newtone.mvp.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactors.SleepTimerInteractor;
import com.parabola.domain.repository.ResourceRepository;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.TimeToSleepInfoView;
import com.parabola.newtone.util.TimeFormatterTool;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

@InjectViewState
public final class TimeToSleepInfoPresenter extends MvpPresenter<TimeToSleepInfoView> {

    @Inject SleepTimerInteractor timerInteractor;

    @Inject ResourceRepository resourceRepo;

    @Inject SchedulerProvider schedulerProvider;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public TimeToSleepInfoPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        disposables.addAll(
                observeRemainingTime(),
                observeTimerState());
    }


    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observeRemainingTime() {
        return timerInteractor.observeRemainingTimeToEnd()
                .observeOn(schedulerProvider.ui())
                .subscribe(remainingTimeMs -> {
                    String newTime = TimeFormatterTool.formatMillisecondsToMinutes(remainingTimeMs);
                    String timeToEndText = resourceRepo.getString(R.string.time_to_end_sleep_info_dialog, newTime);
                    getViewState().updateTimeToEndText(timeToEndText);
                });
    }

    private Disposable observeTimerState() {
        return timerInteractor.observeState()
                .subscribe(state -> {
                    if (state != SleepTimerInteractor.State.RUNNING)
                        getViewState().closeScreen();
                });
    }



    public void onClickReset() {
        timerInteractor.reset()
                .subscribe(getViewState()::closeScreen);
    }

    public void onClickCancel() {
        getViewState().closeScreen();
    }
}
