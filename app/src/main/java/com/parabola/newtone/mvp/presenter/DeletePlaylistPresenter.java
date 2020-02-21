package com.parabola.newtone.mvp.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.repository.PlaylistRepository;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.DeletePlaylistView;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

@InjectViewState
public final class DeletePlaylistPresenter extends MvpPresenter<DeletePlaylistView> {

    private final int playlistId;

    @Inject PlaylistRepository playlistRepo;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public DeletePlaylistPresenter(AppComponent appComponent, int playlistId) {
        this.playlistId = playlistId;
        appComponent.inject(this);
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    public void onClickCancel() {
        getViewState().closeScreen();
    }

    public void onClickDelete() {
        disposables.add(playlistRepo.remove(playlistId)
                .subscribe(getViewState()::closeScreen));
    }
}
