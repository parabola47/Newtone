package com.parabola.newtone.mvp.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.exceptions.AlreadyExistsException;
import com.parabola.domain.repository.PlaylistRepository;
import com.parabola.domain.repository.ResourceRepository;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.CreatePlaylistView;

import javax.inject.Inject;

@InjectViewState
public final class CreatePlaylistPresenter extends MvpPresenter<CreatePlaylistView> {

    @Inject PlaylistRepository playlistRepo;

    @Inject ResourceRepository resourceRepo;

    public CreatePlaylistPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    public void onClickCreatePlaylist(String newPlaylistTitle) {
        newPlaylistTitle = newPlaylistTitle.trim();

        if (newPlaylistTitle.isEmpty()) {
            getViewState().setPlaylistTitleIsEmptyError();
            return;
        }

        playlistRepo.addNew(newPlaylistTitle)
                .subscribe((playlist, error) -> {
                    if (playlist != null) {
                        String toastText = resourceRepo.getString(R.string.toast_playlist_created, playlist.getTitle());
                        getViewState().showToast(toastText);
                        getViewState().closeScreen();
                    } else if (error instanceof AlreadyExistsException) {
                        String toastText = resourceRepo.getString(R.string.toast_playlist_already_exist);
                        getViewState().showToast(toastText);
                    } else if (error != null) {
//                        TODO обозначить неизвестную ошибку
                    }
                });
    }

    public void onClickCancel() {
        getViewState().closeScreen();
    }
}
