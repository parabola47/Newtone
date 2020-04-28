package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.repository.PlaylistRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.TabPlaylistView;
import com.parabola.newtone.ui.router.MainRouter;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class TabPlaylistPresenter extends MvpPresenter<TabPlaylistView> {

    @Inject MainRouter router;

    @Inject PlaylistRepository playlistRepo;
    @Inject TrackRepository trackRepo;
    @Inject SchedulerProvider schedulers;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public TabPlaylistPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        disposables.addAll(
                observePlaylistsUpdates(), observeTrackDeleting());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observePlaylistsUpdates() {
        return playlistRepo.observePlaylistsUpdates()
                .flatMapSingle(o -> playlistRepo.getAll())
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::refreshPlaylists);
    }

    private Disposable observeTrackDeleting() {
        return trackRepo.observeTrackDeleting()
                .flatMapSingle(deletedTrackId -> playlistRepo.getAll())
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::refreshPlaylists);
    }

    public void onClickPlaylistItem(int selectedPlaylistId) {
        router.openPlaylist(selectedPlaylistId);
    }

    public void onClickMenuRenamePlaylist(int playlistId) {
        router.openRenamePlaylistDialog(playlistId);
    }

    public void onClickMenuDeletePlaylist(int deletedPlaylistId) {
        playlistRepo.remove(deletedPlaylistId)
                .subscribe();
    }

    public void onClickRecentlyAdded() {
        router.openRecentlyAdded();
    }

    public void onClickFavourites() {
        router.openFavourites();
    }

    public void onClickQueue() {
        router.openQueue();
    }

    public void onClickFolders() {
        router.openFoldersList();
    }
}
