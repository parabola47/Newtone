package com.parabola.newtone.mvp.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.exceptions.AlreadyExistsException;
import com.parabola.domain.repository.PlaylistRepository;
import com.parabola.domain.repository.ResourceRepository;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.RenamePlaylistView;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

@InjectViewState
public final class RenamePlaylistPresenter extends MvpPresenter<RenamePlaylistView> {

    private final int playlistId;
    @Inject PlaylistRepository playlistRepo;

    @Inject ResourceRepository resourceRepo;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public RenamePlaylistPresenter(AppComponent appComponent, int playlistId) {
        appComponent.inject(this);
        this.playlistId = playlistId;
    }

    @Override
    protected void onFirstViewAttach() {
        disposables.add(playlistRepo.getById(playlistId)
                .subscribe(playlist -> {
                    getViewState().setPlaylistTitle(playlist.getTitle());
                    getViewState().setTitleSelected();
                }));
    }

    public void onClickCancel() {
        getViewState().closeScreen();
    }

    public void onClickRenamePlaylist(String newPlaylistTitle) {
        newPlaylistTitle = newPlaylistTitle.trim();
        if (newPlaylistTitle.isEmpty()) {
            getViewState().setPlaylistTitleIsEmptyError();
            return;
        }

        disposables.add(playlistRepo.rename(playlistId, newPlaylistTitle)
                .subscribe(getViewState()::closeScreen,
                        error -> {
                            if (error instanceof AlreadyExistsException) {
                                String toastText = resourceRepo.getString(R.string.rename_toast_playlist_already_exist);
                                getViewState().showToast(toastText);
                            }
                        }));
    }
}
