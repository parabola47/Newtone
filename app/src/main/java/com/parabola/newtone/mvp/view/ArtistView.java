package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Album;
import com.parabola.domain.settings.ViewSettingsInteractor;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface ArtistView extends MvpView {
    void setArtistName(String artistName);
    void setTracksCount(int tracksCount);
    void setAlbumsCount(int albumsCount);

    void refreshAlbums(List<Album> albums);

    void setViewType(ViewSettingsInteractor.AlbumViewType viewType);
}
