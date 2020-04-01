package com.parabola.newtone.mvp.view;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.OneExecution;

@OneExecution
public interface RenamePlaylistView extends MvpView {

    void focusOnEditText();

    void setPlaylistTitle(String playlistTitle);
    void setTitleSelected();

    void showToast(String toastText);

    void closeScreen();
    void setPlaylistTitleIsEmptyError();
}
