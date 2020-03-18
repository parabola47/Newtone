package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.TrackInteractor;
import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.utils.EmptyItems;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.FavouritesPlaylistView;
import com.parabola.newtone.ui.router.MainRouter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class FavouritesPlaylistPresenter extends MvpPresenter<FavouritesPlaylistView> {

    @Inject MainRouter router;
    @Inject SchedulerProvider schedulers;

    @Inject TrackInteractor trackInteractor;
    @Inject TrackRepository trackRepo;
    @Inject PlayerInteractor playerInteractor;

    private int currentTrackId = EmptyItems.NO_TRACK.getId();

    private final CompositeDisposable disposables = new CompositeDisposable();

    public FavouritesPlaylistPresenter(AppComponent component) {
        component.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        disposables.addAll(
                observeFavouritesChanged(), observeCurrentTrackChanged(),
                observeTrackDeleting());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observeFavouritesChanged() {
        return trackRepo.observeFavouritesChanged()
                .flatMapSingle(irrelevant -> trackInteractor.getFavourites())
                // ожидаем пока прогрузится анимация входа
                .doOnNext(irrelevant -> { while (!enterSlideAnimationEnded) ; })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(tracks -> {
                    getViewState().refreshTracks(tracks);
                    getViewState().setCurrentTrack(currentTrackId);
                });
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


    public void onClickMenuDeleteTrack(int trackId) {
        trackRepo.deleteTrack(trackId);
    }

    public void onClickMenuAdditionalInfo(int trackId) {
        router.openTrackAdditionInfo(trackId);
    }
}
