package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Artist;
import com.parabola.domain.settings.ViewSettingsInteractor.ArtistItemView;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;

@AddToEndSingle
public interface TabArtistView extends MvpView {

    void refreshArtists(List<Artist> artists);
    void setItemViewSettings(ArtistItemView viewSettings);
    void setItemDividerShowing(boolean showed);

    void setSectionShowing(boolean enable);
}
