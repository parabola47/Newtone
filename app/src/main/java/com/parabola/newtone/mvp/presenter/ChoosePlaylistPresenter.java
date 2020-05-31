package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.repository.PlaylistRepository;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.ChoosePlaylistView;
import com.parabola.newtone.ui.router.MainRouter;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.observers.CallbackCompletableObserver;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class ChoosePlaylistPresenter extends MvpPresenter<ChoosePlaylistView> {

    @Inject PlaylistRepository playlistRepo;
    @Inject SchedulerProvider schedulers;

    @Inject MainRouter router;

    private final int[] trackIds;
    private final CompositeDisposable disposables = new CompositeDisposable();

    public ChoosePlaylistPresenter(AppComponent appComponent, int[] trackIds) {
        appComponent.inject(this);
        this.trackIds = trackIds;
    }

    @Override
    protected void onFirstViewAttach() {
        disposables.addAll(observePlaylistsUpdates());
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


    public void onClickCreateNewPlaylist() {
        router.openCreatePlaylistDialog();
    }

    public void onClickPlaylistItem(int playlistId) {
        playlistRepo.addTracksToPlaylist(playlistId, trackIds)
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(new CallbackCompletableObserver(getViewState()::closeScreen));
    }
}
