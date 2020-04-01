package com.parabola.newtone.mvp.view;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.OneExecution;

@OneExecution
public interface CreatePlaylistView extends MvpView {

    void focusOnEditText();

    void setPlaylistTitleIsEmptyError();

    void showToast(String toastText);

    void closeScreen();
}
