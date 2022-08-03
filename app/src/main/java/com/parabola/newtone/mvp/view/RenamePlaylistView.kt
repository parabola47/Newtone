package com.parabola.newtone.mvp.view;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.OneExecution;

@OneExecution
public interface RenamePlaylistView extends MvpView {

    void focusOnInputField();

    void setPlaylistTitle(String playlistTitle);
    void setTitleSelected();

    void closeScreen();

    void showPlaylistTitleIsEmptyError();
    void showPlaylistTitleAlreadyExistsError();
}
