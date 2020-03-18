package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Artist;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface TabArtistView extends MvpView {
    void refreshArtists(List<Artist> artists);
    void setSectionShowing(boolean enable);
}
