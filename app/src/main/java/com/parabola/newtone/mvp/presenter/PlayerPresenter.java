package com.parabola.newtone.mvp.presenter;

import static com.parabola.domain.utils.EmptyItems.NO_TRACK;

import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.SleepTimerInteractor;
import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.ResourceRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.utils.EmptyItems;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.PlayerView;
import com.parabola.newtone.ui.router.MainRouter;
import com.parabola.newtone.util.TimeFormatterTool;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.observers.ConsumerSingleObserver;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class PlayerPresenter extends MvpPresenter<PlayerView> {

    @Inject MainRouter router;

    @Inject PlayerInteractor playerInteractor;
    @Inject SleepTimerInteractor timerInteractor;
    @Inject ResourceRepository resourceRepo;
    @Inject TrackRepository trackRepo;

    @Inject SchedulerProvider schedulers;

    private int currentTrackId = NO_TRACK.getId();

    private boolean isSeekbarPressed = false;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public PlayerPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        disposables.addAll(
                observeTracklistChanging(),
                observeCurrentTrack(),
                observeBottomSlidePanelOffset(),
                observeBottomSlidePanelState(),
                observePlayerState(),
                observePlaybackPosition(),
                observeTimerState(),
                observeFavouritesChanged(),
                observeRepeatModeEnabling(),
                observeShuffleModeEnabling()
        );
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observeRepeatModeEnabling() {
        return playerInteractor.onRepeatModeChange()
                .subscribe(getViewState()::setRepeatMode);
    }

    private boolean enableSlideScrolling;

    private Disposable observeShuffleModeEnabling() {
        return playerInteractor.onShuffleModeChange()
                .doOnNext(isShuffleEnabled -> {
                    enableSlideScrolling = !isShuffleEnabled;
                    getViewState().setViewPagerSlide(!enableSlideScrolling);
                })
                .subscribe(getViewState()::setShuffleEnabling);
    }


    //Слушаем обновления на избранные, в случае, если в избранные добавлен/удалён текущий трек,
    // то меняем иконку
    private Disposable observeFavouritesChanged() {
        return trackRepo.observeFavouritesChanged()
                //Пропускаем, если id текущего трека равен пустому треку
                .filter(irrelevant -> currentTrackId != NO_TRACK.getId())
                .map(irrelevant -> trackRepo.isFavourite(currentTrackId))
                .subscribe(getViewState()::setIsFavourite);
    }

    private Disposable observeCurrentTrack() {
        return playerInteractor.onChangeCurrentTrackId()
                .doOnNext(currentTrackId -> this.currentTrackId = currentTrackId)
                //Пропускаем, если id текущего трека неверен
                .filter(currentTrackId -> currentTrackId != EmptyItems.NO_TRACK.getId())
                .flatMapSingle(trackRepo::getById)
                .observeOn(schedulers.ui())
                .subscribe(track -> {
                    getViewState().setArtist(track.getArtistName());
                    getViewState().setAlbum(track.getAlbumTitle());
                    getViewState().setTitle(track.getTitle());

                    String durationFormatted = TimeFormatterTool.formatMillisecondsToMinutes(track.getDurationMs());
                    getViewState().setDurationText(durationFormatted);
                    getViewState().setDurationMs((int) track.getDurationMs());
                    getViewState().setIsFavourite(track.isFavourite());

                    getViewState().setAlbumImagePosition(playerInteractor.currentTrackPosition(), enableSlideScrolling);
                }, Functions.ERROR_CONSUMER);
    }

    private Disposable observePlayerState() {
        return playerInteractor.onChangePlayingState()
                .observeOn(schedulers.ui())
                .subscribe(isPlaying -> {
                    if (isPlaying) getViewState().setPlaybackButtonAsPause();
                    else getViewState().setPlaybackButtonAsPlay();
                });
    }

    private Disposable observePlaybackPosition() {
        return playerInteractor.onChangePlaybackPosition()
                .filter(currentTimeMs -> !isSeekbarPressed)
                .subscribe(currentTimeMs -> getViewState().setCurrentTimeMs(currentTimeMs.intValue()));
    }

    private Disposable observeTimerState() {
        return timerInteractor.observeIsTimerRunning()
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::setTimerButtonVisibility);
    }


    private Disposable observeTracklistChanging() {
        return playerInteractor.onTracklistChanged()
                .flatMapSingle(trackRepo::getByIds)
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(tracks -> {
                    getViewState().refreshTracks(tracks);
                    getViewState().setAlbumImagePosition(playerInteractor.currentTrackPosition(), false);
                });
    }

    private Disposable observeBottomSlidePanelOffset() {
        return router.observeSlidePanelOffset()
                .subscribe(offset -> {
                    getViewState().setTrackSettingsRotation(360 * offset);//переводим в угол поворота
                    getViewState().setRootViewOpacity(offset);
                });
    }

    private Disposable observeBottomSlidePanelState() {
        return router.observeSlidePanelState()
                .flatMap((Function<SlidingUpPanelLayout.PanelState, ObservableSource<Boolean>>) state -> {
                    switch (state) {
                        case EXPANDED:
                        case DRAGGING:
                            return Observable.just(Boolean.TRUE);
                        case COLLAPSED:
                            return Observable.just(Boolean.FALSE);
                        default:
                            return Observable.empty();
                    }
                })
                .subscribe(getViewState()::setRootViewVisibility);
    }

    public void onClickTimerButton() {
        if (timerInteractor.launched())
            router.openSleepTimerInfoDialog();
    }


    public void onLongClickTimerButton() {
        if (timerInteractor.launched()) {
            timerInteractor.remainingTimeToEnd()
                    .onErrorReturnItem(0L)
                    .map(TimeFormatterTool::formatMillisecondsToMinutes)
                    .map(timeToEndStr -> resourceRepo.getString(R.string.time_to_end_sleep_info_dialog, timeToEndStr))
                    .subscribe(new ConsumerSingleObserver<>(
                            getViewState()::showToast, Functions.ERROR_CONSUMER));
        }
    }

    public void onClickPlayButton() {
        playerInteractor.toggle();
    }

    public void onClickNextTrack() {
        playerInteractor.next();
    }

    public void onClickPrevTrack() {
        playerInteractor.previous();
    }


    public void onClickQueue() {
        router.openQueueFromBackStackIfAvailable();
        router.collapseBottomSlider();
        router.goToTab(3, false);
    }

    public void onClickAudioEffects() {
        router.openAudioEffectsDialog();
    }

    public void onLongClickAudioEffects() {
        router.openEqPresetsSelectorDialog();
    }

    public void onClickFavourite() {
        trackRepo.toggleFavourite(currentTrackId);
    }


    public void onLongClickFavorite() {
        router.openFavouritesFromBackStackIfAvailable();
        router.collapseBottomSlider();
        router.goToTab(3, false);
    }

    public void onClickDropDown() {
        router.collapseBottomSlider();
    }

    public void onStartSeekbarPressed() {
        isSeekbarPressed = true;
    }


    public void onStopSeekbarPressed(int progress) {
        isSeekbarPressed = false;
        playerInteractor.seekTo(progress);
    }

    public void onClickMenuAddTrackToPlaylist() {
        if (currentTrackId != NO_TRACK.getId()) {
            router.openAddToPlaylistDialog(currentTrackId);
        }
    }

    public void onClickMenuTimer() {
        if (timerInteractor.launched())
            router.openSleepTimerInfoDialog();
        else
            router.openStartSleepTimerDialog();
    }

    public void onClickLoop() {
        playerInteractor.toggleRepeatMode();
    }

    public void onClickShuffle() {
        playerInteractor.toggleShuffleMode();
    }

    public void onClickArtist() {
        trackRepo.getById(currentTrackId)
                .subscribe(new ConsumerSingleObserver<>(
                        track -> {
                            router.openArtistFromBackStackIfAvailable(track.getArtistId());
                            router.collapseBottomSlider();
                            router.goToTab(0, false);
                            router.goToArtistInTab(track.getArtistId());
                        }, Functions.ERROR_CONSUMER));
    }

    public void onClickAlbum() {
        trackRepo.getById(currentTrackId)
                .subscribe(new ConsumerSingleObserver<>(
                        track -> {
                            router.openAlbumFromBackStackIfAvailable(track.getAlbumId());
                            router.collapseBottomSlider();
                            router.goToTab(1, false);
                            router.goToAlbumInTab(track.getAlbumId());
                        }, Functions.ERROR_CONSUMER));
    }

    public void onClickTrackTitle() {
        router.backToRoot();
        router.goToTab(2, false);
        router.scrollOnTabTrackToCurrentTrack();
        router.collapseBottomSlider();
    }

    public void onSwipeImage(int lastPosition) {
        int currentTrackPosition = playerInteractor.currentTrackPosition();
        if (lastPosition == currentTrackPosition)
            return;

        if (lastPosition > currentTrackPosition) playerInteractor.next();
        else playerInteractor.previous();
    }


    public void onClickMenuLyrics() {
        trackRepo.getById(currentTrackId)
                .subscribe(new ConsumerSingleObserver<>(
                        router::openLyricsSearch, Functions.ERROR_CONSUMER));
    }

    public void onClickMenuDelete() {
        router.openDeleteTrackDialog(currentTrackId);
    }

    public void onClickMenuShareTrack() {
        trackRepo.getById(currentTrackId)
                .map(Track::getFilePath)
                .subscribe(new ConsumerSingleObserver<>(
                        router::openShareTrack, Functions.ERROR_CONSUMER));
    }

    public void onClickMenuAdditionalInfo() {
        router.openTrackAdditionInfo(currentTrackId);
    }

    public void onClickMenuSettings() {
        router.openSettingsIfAvailable();
        router.collapseBottomSlider();
    }

}
