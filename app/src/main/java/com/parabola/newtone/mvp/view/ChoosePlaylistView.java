package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Playlist;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface ChoosePlaylistView extends MvpView {
    void refreshPlaylists(List<Playlist> playlists);

    void closeScreen();
}
