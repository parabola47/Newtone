package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.exception.AlreadyExistsException;
import com.parabola.domain.repository.PlaylistRepository;
import com.parabola.domain.repository.ResourceRepository;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.CreatePlaylistView;
import com.parabola.newtone.ui.router.MainRouter;

import javax.inject.Inject;

import io.reactivex.internal.observers.BiConsumerSingleObserver;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class CreatePlaylistPresenter extends MvpPresenter<CreatePlaylistView> {

    @Inject MainRouter router;

    @Inject PlaylistRepository playlistRepo;
    @Inject ResourceRepository resourceRepo;


    public CreatePlaylistPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        getViewState().focusOnInputField();
    }

    public void onClickCreatePlaylist(String newPlaylistTitle) {
        newPlaylistTitle = newPlaylistTitle.trim();

        if (newPlaylistTitle.isEmpty()) {
            getViewState().showPlaylistTitleIsEmptyError();
            return;
        }

        playlistRepo.addNew(newPlaylistTitle)
                .subscribe(new BiConsumerSingleObserver<>((playlist, error) -> {
                    if (playlist != null) {
                        String toastText = resourceRepo.getString(R.string.toast_playlist_created, playlist.getTitle());
                        router.showToast(toastText);
                        getViewState().closeScreen();
                    } else if (error instanceof AlreadyExistsException) {
                        getViewState().showPlaylistTitleAlreadyExistsError();
                    } else if (error != null) {
                        throw new RuntimeException(error);
                    }
                }));
    }
}
