package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Album;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;

@AddToEndSingle
public interface ArtistView extends MvpView {
    void setArtistName(String artistName);
    void setTracksCount(int tracksCount);
    void setAlbumsCount(int albumsCount);

    void refreshAlbums(List<Album> albums);

    void setAlbumViewSettings(AlbumItemView albumViewSettings);
    void setItemDividerShowing(boolean showed);

}
