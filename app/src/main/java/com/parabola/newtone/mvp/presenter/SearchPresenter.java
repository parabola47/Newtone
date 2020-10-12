package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.SearchInteractor;
import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.SearchFragmentView;
import com.parabola.newtone.ui.router.MainRouter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class SearchPresenter extends MvpPresenter<SearchFragmentView> {

    @Inject MainRouter router;
    @Inject PlayerInteractor playerInteractor;
    @Inject SearchInteractor searchInteractor;
    @Inject ViewSettingsInteractor viewSettingsInteractor;
    @Inject SchedulerProvider schedulers;

    private final CompositeDisposable disposables = new CompositeDisposable();
    private Disposable querySearchDisposable;

    private String lastQuery = "";

    public SearchPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        getViewState().clearAllLists();
        getViewState().focusOnSearchView();

        disposables.addAll(
                observeTrackItemViewUpdates(),
                observeIsItemDividerShowed());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observeTrackItemViewUpdates() {
        return viewSettingsInteractor.observeTrackItemViewUpdates()
                .subscribe(getViewState()::setTrackItemViewSettings);
    }

    private Disposable observeIsItemDividerShowed() {
        return viewSettingsInteractor.observeIsItemDividerShowed()
                .subscribe(getViewState()::setItemDividerShowing);
    }

    public void onClickBackButton() {
        router.goBack();
    }


    public void onQueryTextSubmit(String query) {
        query = query.trim();
        if (query.equals(lastQuery))
            return;

        lastQuery = query;

        if (querySearchDisposable != null && !querySearchDisposable.isDisposed())
            querySearchDisposable.dispose();

        querySearchDisposable = searchInteractor.search(query)
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .doOnSubscribe(disposable -> getViewState().setLoadDataProgressBarVisibility(true))
                .doFinally(() -> getViewState().setLoadDataProgressBarVisibility(false))
                .subscribe(this::refreshAll);
        disposables.add(querySearchDisposable);
    }

    private void refreshAll(SearchInteractor.SearchResult searchResult) {
        getViewState().refreshArtists(searchResult.artists);
        getViewState().refreshAlbums(searchResult.albums);
        getViewState().refreshTracks(searchResult.tracks);
        getViewState().refreshPlaylists(searchResult.playlists);
    }

    public void onClearText() {
        getViewState().clearAllLists();
    }

    public void onClickArtistItem(int artistId) {
        router.openArtist(artistId);
    }

    public void onClickAlbumItem(int albumId) {
        router.openAlbum(albumId);
    }

    public void onClickTrackItem(List<Track> tracks, int position) {
        playerInteractor.start(tracks, position);
    }

    public void onClickPlaylistItem(int playlistId) {
        router.openPlaylist(playlistId);
    }
}
