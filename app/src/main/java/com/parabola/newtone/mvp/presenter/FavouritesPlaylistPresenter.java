package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.TrackInteractor;
import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.ResourceRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.domain.utils.EmptyItems;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.FavouritesPlaylistView;
import com.parabola.newtone.ui.router.MainRouter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.observers.ConsumerSingleObserver;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class FavouritesPlaylistPresenter extends MvpPresenter<FavouritesPlaylistView> {

    @Inject MainRouter router;

    @Inject TrackInteractor trackInteractor;
    @Inject TrackRepository trackRepo;
    @Inject PlayerInteractor playerInteractor;
    @Inject ViewSettingsInteractor viewSettingsInteractor;
    @Inject SchedulerProvider schedulers;
    @Inject ResourceRepository resourceRepo;

    private int currentTrackId = EmptyItems.NO_TRACK.getId();

    private final CompositeDisposable disposables = new CompositeDisposable();

    public FavouritesPlaylistPresenter(AppComponent component) {
        component.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        getViewState().setPlaylistChangerActivation(isPlaylistChangerActivated);

        trackInteractor.getFavourites()
                // ожидаем пока прогрузится анимация входа
                .doOnSubscribe(d -> { while (!enterSlideAnimationEnded) ; })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(new ConsumerSingleObserver<>(
                        tracks -> {
                            getViewState().refreshTracks(tracks);
                            getViewState().setCurrentTrack(playerInteractor.currentTrackId());
                        },
                        Functions.ERROR_CONSUMER
                ));
        disposables.addAll(
                observeFavouritesChanged(),
                observeTrackItemViewUpdates(),
                observeIsItemDividerShowed(),
                observeCurrentTrackChanged(),
                observeTrackDeleting());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observeFavouritesChanged() {
        return trackRepo.observeFavouritesChanged()
                .flatMapSingle(irrelevant -> trackInteractor.getFavourites())
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(tracks -> {
                    getViewState().refreshTracks(tracks);
                    getViewState().setCurrentTrack(currentTrackId);
                });
    }

    private Disposable observeTrackItemViewUpdates() {
        return viewSettingsInteractor.observeTrackItemViewUpdates()
                .subscribe(getViewState()::setItemViewSettings);
    }

    private Disposable observeIsItemDividerShowed() {
        return viewSettingsInteractor.observeIsItemDividerShowed()
                .subscribe(getViewState()::setItemDividerShowing);
    }

    private Disposable observeCurrentTrackChanged() {
        return playerInteractor.onChangeCurrentTrackId()
                .doOnNext(currentTrackId -> this.currentTrackId = currentTrackId)
                .subscribe(getViewState()::setCurrentTrack);
    }

    private Disposable observeTrackDeleting() {
        return trackRepo.observeTrackDeleting()
                .doOnNext(deletedTrackId -> {
                    if (deletedTrackId == currentTrackId)
                        currentTrackId = EmptyItems.NO_TRACK.getId();
                })
                .flatMapSingle(removedTrackId -> trackInteractor.getFavourites())
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(tracks -> {
                    getViewState().refreshTracks(tracks);
                    getViewState().setCurrentTrack(currentTrackId);
                });
    }

    public void onClickBack() {
        router.goBack();
    }


    private boolean isPlaylistChangerActivated = false;

    public void onClickDragSwitcher() {
        isPlaylistChangerActivated = !isPlaylistChangerActivated;
        getViewState().setPlaylistChangerActivation(isPlaylistChangerActivated);
    }


    private volatile boolean enterSlideAnimationEnded = false;

    public void onEnterSlideAnimationEnded() {
        enterSlideAnimationEnded = true;
    }

    public void onClickTrackItem(List<Track> tracks, int selectedPosition) {
        playerInteractor.start(tracks, selectedPosition);
    }

    public void onClickMenuPlay(List<Track> tracks, int selectedPosition) {
        playerInteractor.start(tracks, selectedPosition);
    }

    public void onClickMenuAddToPlaylist(int trackId) {
        router.openAddToPlaylistDialog(trackId);
    }


    public void onClickMenuRemoveFromFavourites(int trackId) {
        trackRepo.removeFromFavourites(trackId);
    }

    public void onClickMenuShareTrack(Track selectedTrack) {
        router.openShareTrack(selectedTrack.getFilePath());
    }


    public void onClickMenuDeleteTrack(int trackId, int position) {
        trackRepo.deleteTrack(trackId)
                .observeOn(schedulers.ui())
                .doOnSuccess(isDeleted -> {
                    if (isDeleted) getViewState().removeTrack(trackId, position);
                })
                .map(isDeleted -> isDeleted ? R.string.file_deleted_successfully_toast : R.string.file_not_deleted_toast)
                .map(resourceRepo::getString)
                .subscribe(new ConsumerSingleObserver<>(
                        getViewState()::showToast,
                        Functions.ERROR_CONSUMER));
    }

    public void onClickMenuAdditionalInfo(int trackId) {
        router.openTrackAdditionInfo(trackId);
    }

    public void onRemoveItem(int trackId) {
        trackRepo.removeFromFavourites(trackId);
    }

    public void onMoveItem(int positionFrom, int positionTo) {
        trackRepo.moveFavouriteTrack(positionFrom, positionTo);
    }
}
