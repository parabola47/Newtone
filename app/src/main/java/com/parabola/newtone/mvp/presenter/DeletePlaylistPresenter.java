package com.parabola.newtone.mvp.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.repository.PlaylistRepository;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.DeletePlaylistView;

import javax.inject.Inject;

@InjectViewState
public final class DeletePlaylistPresenter extends MvpPresenter<DeletePlaylistView> {

    private final int playlistId;

    @Inject PlaylistRepository playlistRepo;

    public DeletePlaylistPresenter(AppComponent appComponent, int playlistId) {
        this.playlistId = playlistId;
        appComponent.inject(this);
    }

    public void onClickCancel() {
        getViewState().closeScreen();
    }

    public void onClickDelete() {
        playlistRepo.remove(playlistId)
                .subscribe(getViewState()::closeScreen);
    }
}
