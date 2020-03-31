package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Album;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumViewType;
import com.parabola.newtone.mvp.CustomAddToEndSingleStrategy;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(CustomAddToEndSingleStrategy.class)
public interface TabAlbumView extends MvpView {
    void refreshAlbums(List<Album> albums);
    void setSectionShowing(boolean enable);

    void setViewType(AlbumViewType viewType);
}
