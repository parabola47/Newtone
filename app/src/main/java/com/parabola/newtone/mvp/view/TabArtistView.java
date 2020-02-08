package com.parabola.newtone.mvp.view;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.parabola.domain.model.Artist;

import java.util.List;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface TabArtistView extends MvpView {
    void refreshArtists(List<Artist> artists);
    void setSectionShowing(boolean enable);
}
