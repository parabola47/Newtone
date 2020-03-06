package com.parabola.newtone.mvp.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactors.player.PlayerInteractor;
import com.parabola.domain.repository.PermissionHandler;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.utils.EmptyItems;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.MainView;
import com.parabola.newtone.ui.router.MainRouter;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

@InjectViewState
public final class MainPresenter extends MvpPresenter<MainView> {

    @Inject PlayerInteractor playerInteractor;
    @Inject PermissionHandler accessRepo;
    @Inject TrackRepository trackRepo;

    @Inject MainRouter router;
    @Inject SchedulerProvider schedulers;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public MainPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        if (!accessRepo.hasPermission(PermissionHandler.Type.FILE_STORAGE)) {
            getViewState().requestStoragePermissionDialog();
        }

        disposables.addAll(
                observeCurrentTrack(), observePlaybackPosition(),
                observeState());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observeCurrentTrack() {
        return playerInteractor.onChangeCurrentTrackId()
                //Прячем/показываем нижнюю панель, если текущего трека нет
                .doOnNext(currentTrackId -> {
                    if (currentTrackId == EmptyItems.NO_TRACK.getId())
                        getViewState().hideBottomSlider();
                    else getViewState().showBottomSlider();
                })
                //Пропускаем, если id текущего трека неверен
                .filter(currentTrackId -> currentTrackId != EmptyItems.NO_TRACK.getId())
                .flatMapSingle(currentTrackId -> trackRepo.getById(currentTrackId))
                .subscribe(track -> {
                    getViewState().setDurationMax((int) track.getDurationMs());
                    getViewState().setDurationProgress((int) playerInteractor.playbackPosition());
                    getViewState().setTrackTitle(track.getTitle());
                    getViewState().setArtistName(track.getArtistName());
                });
    }


    private Disposable observePlaybackPosition() {
        return playerInteractor.onChangePlaybackPosition()
                .subscribe(currentTimeMs -> getViewState().setDurationProgress(currentTimeMs.intValue()));
    }

    private Disposable observeState() {
        return playerInteractor.onChangePlayingState()
                .observeOn(schedulers.ui())
                .subscribe(isPlaying -> {
                    if (isPlaying) getViewState().setPlaybackButtonAsPause();
                    else getViewState().setPlaybackButtonAsPlay();
                });
    }

    public void onClickPlayButton() {
        playerInteractor.toggle();
    }

    public void onClickMenuAddPlaylist() {
        router.openCreatePlaylistDialog();
    }
}
