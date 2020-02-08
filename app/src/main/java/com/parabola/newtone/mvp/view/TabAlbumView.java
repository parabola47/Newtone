package com.parabola.newtone.mvp.view;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.parabola.domain.model.Album;

import java.util.List;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface TabAlbumView extends MvpView {
    void refreshAlbums(List<Album> albums);
    void setSectionShowing(boolean enable);
}
