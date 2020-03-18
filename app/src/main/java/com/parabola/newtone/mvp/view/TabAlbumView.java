package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Album;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumViewType;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface TabAlbumView extends MvpView {
    void refreshAlbums(List<Album> albums);
    void setSectionShowing(boolean enable);

    void setViewType(AlbumViewType viewType);
}
