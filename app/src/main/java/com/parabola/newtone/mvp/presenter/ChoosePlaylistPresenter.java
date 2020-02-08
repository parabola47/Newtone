package com.parabola.newtone.mvp.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.repository.PlaylistRepository;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.ChoosePlaylistView;
import com.parabola.newtone.ui.router.MainRouter;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

@InjectViewState
public final class ChoosePlaylistPresenter extends MvpPresenter<ChoosePlaylistView> {

    @Inject PlaylistRepository playlistRepo;
    @Inject SchedulerProvider schedulers;

    @Inject MainRouter router;

    private final int trackId;
    private Disposable playlistObserver;

    public ChoosePlaylistPresenter(AppComponent appComponent, int trackId) {
        appComponent.inject(this);
        this.trackId = trackId;
    }

    @Override
    protected void onFirstViewAttach() {
        playlistRepo.getAll()
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::refreshPlaylists);

        playlistObserver = playlistRepo.observePlaylistsUpdates()
                .flatMapSingle(o -> playlistRepo.getAll())
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::refreshPlaylists);
    }

    @Override
    public void onDestroy() {
        playlistObserver.dispose();
    }

    public void onClickCreateNewPlaylist() {
        router.openCreatePlaylistDialog();
    }

    public void onClickPlaylistItem(int playlistId) {
        playlistRepo.addTrackToPlaylist(playlistId, trackId)
                .subscribe(getViewState()::closeScreen);
    }

    public void onClickCancel() {
        getViewState().closeScreen();
    }
}
