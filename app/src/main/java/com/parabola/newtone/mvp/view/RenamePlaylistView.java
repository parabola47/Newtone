package com.parabola.newtone.mvp.view;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface RenamePlaylistView extends MvpView {

    void setPlaylistTitle(String playlistTitle);
    void setTitleSelected();

    void showToast(String toastText);

    void closeScreen();
    void setPlaylistTitleIsEmptyError();
}
