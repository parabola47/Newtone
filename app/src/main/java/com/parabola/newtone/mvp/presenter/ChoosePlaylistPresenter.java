package com.parabola.newtone.mvp.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.repository.PlaylistRepository;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.ChoosePlaylistView;
import com.parabola.newtone.ui.router.MainRouter;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

@InjectViewState
public final class ChoosePlaylistPresenter extends MvpPresenter<ChoosePlaylistView> {

    @Inject PlaylistRepository playlistRepo;
    @Inject SchedulerProvider schedulers;

    @Inject MainRouter router;

    private final int trackId;
    private final CompositeDisposable disposables = new CompositeDisposable();

    public ChoosePlaylistPresenter(AppComponent appComponent, int trackId) {
        appComponent.inject(this);
        this.trackId = trackId;
    }

    @Override
    protected void onFirstViewAttach() {
        disposables.add(playlistRepo.observePlaylistsUpdates()
                .flatMapSingle(o -> playlistRepo.getAll())
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::refreshPlaylists));
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    public void onClickCreateNewPlaylist() {
        router.openCreatePlaylistDialog();
    }

    public void onClickPlaylistItem(int playlistId) {
        disposables.add(playlistRepo.addTrackToPlaylist(playlistId, trackId)
                .subscribe(getViewState()::closeScreen));
    }

    public void onClickCancel() {
        getViewState().closeScreen();
    }
}
