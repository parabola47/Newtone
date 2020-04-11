package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.SleepTimerInteractor;
import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.repository.ResourceRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.utils.EmptyItems;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.PlayerView;
import com.parabola.newtone.ui.router.MainRouter;
import com.parabola.newtone.util.TimeFormatterTool;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

import static com.parabola.domain.utils.EmptyItems.NO_TRACK;

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
                observePlayerState(),
                observePlaybackPosition(),
                observeTimerState(),
                observeFavouritesChanged(),
                observeRepeatModeEnabling(),
                observeShuffleModeEnabling(),
                observeTrackMoving(),
                observeTrackRemoving());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observeRepeatModeEnabling() {
        return playerInteractor.onRepeatModeChange()
                .subscribe(getViewState()::setLoopEnabling);
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
                .filter(irrelevant -> currentTrackId != EmptyItems.NO_TRACK.getId())
                .flatMapSingle(irrelevant -> trackRepo.getById(currentTrackId))
                .subscribe(track -> getViewState().setIsFavourite(track.isFavourite()));
    }

    private Disposable observeCurrentTrack() {
        return playerInteractor.onChangeCurrentTrackId()
                .doOnNext(currentTrackId -> this.currentTrackId = currentTrackId)
                //Пропускаем, если id текущего трека неверен
                .filter(currentTrackId -> currentTrackId != EmptyItems.NO_TRACK.getId())
                .flatMapSingle(trackRepo::getById)
                .subscribe(track -> {
                    getViewState().setArtist(track.getArtistName());
                    getViewState().setAlbum(track.getAlbumTitle());
                    getViewState().setTitle(track.getTitle());

                    String durationFormatted = TimeFormatterTool.formatMillisecondsToMinutes(track.getDurationMs());
                    getViewState().setDurationText(durationFormatted);
                    getViewState().setDurationMs((int) track.getDurationMs());
                    getViewState().setIsFavourite(track.isFavourite());

                    getViewState().setAlbumImagePosition(playerInteractor.currentTrackPosition(), enableSlideScrolling);
                });
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
                .subscribe(isTimerRunning -> {
                    if (isTimerRunning) getViewState().setTimerColored();
                    else getViewState().setTimerNotColored();
                });
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

    private Disposable observeTrackMoving() {
        return playerInteractor.onMoveTrack()
                .observeOn(schedulers.ui())
                .subscribe(movedTrackItem -> {
                    getViewState().moveTrack(movedTrackItem.oldPosition, movedTrackItem.newPosition);
                    getViewState().setAlbumImagePosition(playerInteractor.currentTrackPosition(), enableSlideScrolling);
                });
    }

    private Disposable observeTrackRemoving() {
        return playerInteractor.onRemoveTrack()
                .map(removedTrackItem -> removedTrackItem.position)
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::removeTrack);
    }

    public void onClickTimerButton() {
        if (timerInteractor.launched())
            router.openSleepTimerInfoDialog();
        else
            router.openStartSleepTimerDialog();
    }


    public void onLongClickTimerButton() {
        if (timerInteractor.launched()) {
            disposables.add(timerInteractor.remainingTimeToEnd()
                    .onErrorReturnItem(0L)
                    .subscribe(remainingTimeToEndMs -> {
                        String timeToEndStr = TimeFormatterTool.formatMillisecondsToMinutes(remainingTimeToEndMs);
                        String toastMessage = resourceRepo.getString(R.string.toast_time_to_end_player_screen, timeToEndStr);
                        getViewState().showToast(toastMessage);
                    }));
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

    public void onClickFavourite() {
        trackRepo.toggleFavourite(currentTrackId);
    }


    public void onLongClickFavorite() {
        router.openFavouritesFromBackStackIfAvailable();
        router.collapseBottomSlider();
        router.goToTab(3, false);
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

    public void onClickLoop() {
        playerInteractor.toggleRepeatMode();
    }

    public void onClickShuffle() {
        playerInteractor.toggleShuffleMode();
    }

    public void onClickArtist() {
        disposables.add(trackRepo.getById(currentTrackId)
                .subscribe(track -> {
                    router.openArtistFromBackStackIfAvailable(track.getArtistId());
                    router.collapseBottomSlider();
                    router.goToTab(0, false);
                    router.goToArtistInTab(track.getArtistId());
                }));
    }

    public void onClickAlbum() {
        disposables.add(trackRepo.getById(currentTrackId)
                .subscribe(track -> {
                    router.openAlbumFromBackStackIfAvailable(track.getAlbumId());
                    router.collapseBottomSlider();
                    router.goToTab(1, false);
                    router.goToAlbumInTab(track.getAlbumId());
                }));
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
        disposables.add(trackRepo.getById(currentTrackId)
                .subscribe(track -> router.openLyricsSearch(track)));
    }

    public void onClickMenuDelete() {
        trackRepo.deleteTrack(currentTrackId);
    }

    public void onClickMenuShareTrack() {
        disposables.add(trackRepo.getById(currentTrackId)
                .subscribe(track -> router.openShareTrack(track.getFilePath())));
    }

    public void onClickMenuAdditionalInfo() {
        router.openTrackAdditionInfo(currentTrackId);
    }
}
