package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.exception.AlreadyExistsException;
import com.parabola.domain.repository.PlaylistRepository;
import com.parabola.domain.repository.ResourceRepository;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.CreatePlaylistView;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class CreatePlaylistPresenter extends MvpPresenter<CreatePlaylistView> {

    @Inject PlaylistRepository playlistRepo;

    @Inject ResourceRepository resourceRepo;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public CreatePlaylistPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        getViewState().focusOnEditText();
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    public void onClickCreatePlaylist(String newPlaylistTitle) {
        newPlaylistTitle = newPlaylistTitle.trim();

        if (newPlaylistTitle.isEmpty()) {
            getViewState().setPlaylistTitleIsEmptyError();
            return;
        }

        disposables.add(playlistRepo.addNew(newPlaylistTitle)
                .subscribe((playlist, error) -> {
                    if (playlist != null) {
                        String toastText = resourceRepo.getString(R.string.toast_playlist_created, playlist.getTitle());
                        getViewState().showToast(toastText);
                        getViewState().closeScreen();
                    } else if (error instanceof AlreadyExistsException) {
                        String toastText = resourceRepo.getString(R.string.toast_playlist_already_exist);
                        getViewState().showToast(toastText);
                    } else if (error != null) {
                        throw new RuntimeException(error);
                    }
                })
        );
    }

    public void onClickCancel() {
        getViewState().closeScreen();
    }
}
