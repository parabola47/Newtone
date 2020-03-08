package com.parabola.newtone.mvp.view;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.parabola.domain.model.Album;
import com.parabola.domain.settings.ViewSettingsInteractor;

import java.util.List;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface ArtistView extends MvpView {
    void setArtistName(String artistName);
    void setTracksCount(int tracksCount);
    void setAlbumsCount(int albumsCount);

    void refreshAlbums(List<Album> albums);

    void setViewType(ViewSettingsInteractor.AlbumViewType viewType);
}
