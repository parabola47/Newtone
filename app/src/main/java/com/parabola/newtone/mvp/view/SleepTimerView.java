package com.parabola.newtone.mvp.view;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.OneExecution;

@OneExecution
public interface SleepTimerView extends MvpView {

    void showToast(String toastText);
}
