package com.parabola.newtone.mvp.view;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.OneExecution;

@OneExecution
public interface CreatePlaylistView extends MvpView {

    void focusOnInputField();

    void showPlaylistTitleIsEmptyError();
    void showPlaylistTitleAlreadyExistsError();

    void showPlaylistCreatedToast(String playlistTitle);

    void closeScreen();
}
