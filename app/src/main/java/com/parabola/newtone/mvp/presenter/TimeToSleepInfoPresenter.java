package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.SleepTimerInteractor;
import com.parabola.domain.repository.ResourceRepository;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.TimeToSleepInfoView;
import com.parabola.newtone.util.TimeFormatterTool;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class TimeToSleepInfoPresenter extends MvpPresenter<TimeToSleepInfoView> {

    @Inject SleepTimerInteractor timerInteractor;
    @Inject ResourceRepository resourceRepo;
    @Inject SchedulerProvider schedulers;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public TimeToSleepInfoPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }


    @Override
    protected void onFirstViewAttach() {
        disposables.addAll(
                observeRemainingTime(),
                closeScreenWhenTimerFinished());
    }


    @Override
    public void onDestroy() {
        disposables.dispose();
    }


    private Disposable observeRemainingTime() {
        return timerInteractor.observeRemainingTimeToEnd()
                .map(TimeFormatterTool::formatMillisecondsToMinutes)
                .map(remainingTimeFormatted -> resourceRepo.getString(R.string.time_to_end_sleep_info_dialog, remainingTimeFormatted))
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::updateTimeToEndText);
    }


    private Disposable closeScreenWhenTimerFinished() {
        return timerInteractor.onTimerFinished()
                .observeOn(schedulers.ui())
                .subscribe(i -> getViewState().closeScreen());
    }


    public void onClickReset() {
        timerInteractor.reset()
                .subscribe();
    }

}
