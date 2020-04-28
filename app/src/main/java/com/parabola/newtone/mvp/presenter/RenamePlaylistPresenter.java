package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.exception.AlreadyExistsException;
import com.parabola.domain.repository.PlaylistRepository;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.RenamePlaylistView;

import javax.inject.Inject;

import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.observers.CallbackCompletableObserver;
import io.reactivex.internal.observers.ConsumerSingleObserver;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class RenamePlaylistPresenter extends MvpPresenter<RenamePlaylistView> {

    private final int playlistId;
    @Inject PlaylistRepository playlistRepo;


    public RenamePlaylistPresenter(AppComponent appComponent, int playlistId) {
        appComponent.inject(this);
        this.playlistId = playlistId;
    }

    @Override
    protected void onFirstViewAttach() {
        getViewState().focusOnInputField();

        playlistRepo.getById(playlistId)
                .subscribe(new ConsumerSingleObserver<>(
                        playlist -> {
                            getViewState().setPlaylistTitle(playlist.getTitle());
                            getViewState().setTitleSelected();
                        }, Functions.ERROR_CONSUMER));
    }

    public void onClickRenamePlaylist(String newPlaylistTitle) {
        newPlaylistTitle = newPlaylistTitle.trim();
        if (newPlaylistTitle.isEmpty()) {
            getViewState().showPlaylistTitleIsEmptyError();
            return;
        }

        playlistRepo.rename(playlistId, newPlaylistTitle)
                .subscribe(new CallbackCompletableObserver(
                        error -> {
                            if (error instanceof AlreadyExistsException)
                                getViewState().showPlaylistTitleAlreadyExistsError();
                        },
                        getViewState()::closeScreen));
    }
}
