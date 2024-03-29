package com.parabola.newtone.presentation.mainactivity;

import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.repository.PermissionHandler;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.domain.utils.EmptyItems;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.presentation.router.MainRouter;
import com.parabola.player_feature.PlayerInteractorImpl;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class MainPresenter extends MvpPresenter<MainView> {

    @Inject PlayerInteractor playerInteractor;
    @Inject PermissionHandler accessRepo;
    @Inject TrackRepository trackRepo;

    @Inject MainRouter router;
    @Inject ViewSettingsInteractor viewSettingsInteractor;
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
                observeCurrentTrack(),
                bottomSliderShowingOnChangeCurrentTrack(),
                observePlaybackPosition(),
                observeBottomSlidePanelOffset(),
                observeBottomSlidePanelState(),
                observePlayingState(),
                observerPrimaryColor());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    public void onFinishing() {
        ((PlayerInteractorImpl) playerInteractor).closeNotificationIfPaused();
    }

    private Disposable observeCurrentTrack() {
        return playerInteractor.onChangeCurrentTrackId()
                //Пропускаем, если id текущего трека неверен
                .filter(currentTrackId -> currentTrackId != EmptyItems.NO_TRACK.getId())
                .flatMapSingle(trackRepo::getById)
                .observeOn(schedulers.ui())
                .subscribe(track -> {
                    getViewState().setDurationMax((int) track.getDurationMs());
                    getViewState().setDurationProgress((int) playerInteractor.playbackPosition());
                    getViewState().setTrackTitle(track.getTitle());
                    getViewState().setArtistName(track.getArtistName());
                }, Functions.ERROR_CONSUMER);
    }

    //Прячем нижнюю панель, если текущего трека нет, показываем если есть
    private Disposable bottomSliderShowingOnChangeCurrentTrack() {
        return playerInteractor.onChangeCurrentTrackId()
                .map(currentTrackId -> currentTrackId != EmptyItems.NO_TRACK.getId())
                .observeOn(schedulers.ui())
                .subscribe(hasTrack -> {
                    if (hasTrack) getViewState().showBottomSlider();
                    else getViewState().hideBottomSlider();
                });
    }


    private Disposable observePlaybackPosition() {
        return playerInteractor.onChangePlaybackPosition()
                .subscribe(currentTimeMs -> getViewState().setDurationProgress(currentTimeMs.intValue()));
    }

    private Disposable observePlayingState() {
        return playerInteractor.onChangePlayingState()
                .observeOn(schedulers.ui())
                .subscribe(isPlaying -> {
                    if (isPlaying) getViewState().setPlaybackButtonAsPause();
                    else getViewState().setPlaybackButtonAsPlay();
                });
    }

    private Disposable observeBottomSlidePanelOffset() {
        return router.observeSlidePanelOffset()
                .map(offset -> 1f - offset) //чем ниже панель опущена, тем виднее PlayerBar
                .subscribe(getViewState()::setPlayerBarOpacity);
    }

    private Disposable observeBottomSlidePanelState() {
        return router.observeSlidePanelState()
                .flatMap((Function<PanelState, ObservableSource<Boolean>>) state -> {
                    switch (state) {
                        case EXPANDED:
                            return Observable.just(Boolean.FALSE);
                        case DRAGGING:
                        case COLLAPSED:
                            return Observable.just(Boolean.TRUE);
                        default:
                            return Observable.empty();
                    }
                })
                .subscribe(getViewState()::setPlayerBarVisibility);
    }

    private Disposable observerPrimaryColor() {
        return viewSettingsInteractor.observePrimaryColor()
                .subscribe(getViewState()::refreshPrimaryColor);
    }

    public void onClickPlayButton() {
        playerInteractor.toggle();
    }


    //MENU
    public void onClickMenuSearch() {
        router.openSearchScreen();
    }

    public void onClickMenuSorting(String listType) {
        router.openSortingDialog(listType);
    }

    public void onClickMenuAddPlaylist() {
        router.openCreatePlaylistDialog();
    }

    public void onClickMenuSettings() {
        router.openSettings();
    }

}
