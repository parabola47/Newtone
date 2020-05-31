package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Playlist;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;

@AddToEndSingle
public interface TabPlaylistView extends MvpView {

    void refreshPlaylists(List<Playlist> playlists);

    void setItemDividerShowing(boolean showed);
}
