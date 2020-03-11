package com.parabola.newtone.mvp.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.TrackInteractor;
import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.utils.EmptyItems;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.RecentlyAddedPlaylistView;
import com.parabola.newtone.ui.router.MainRouter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

@InjectViewState
public final class RecentlyAddedPlaylistPresenter extends MvpPresenter<RecentlyAddedPlaylistView> {

    @Inject MainRouter router;
    @Inject TrackInteractor trackInteractor;
    @Inject TrackRepository trackRepo;
    @Inject SchedulerProvider schedulers;
    @Inject PlayerInteractor playerInteractor;

    private final CompositeDisposable disposables = new CompositeDisposable();

    private volatile int currentTrackId = EmptyItems.NO_TRACK.getId();

    public RecentlyAddedPlaylistPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    public void onFirstViewAttach() {
        disposables.addAll(
                refreshPlaylists(),
                observeCurrentTrack(),
                observeTrackDeleting());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observeCurrentTrack() {
        return playerInteractor.onChangeCurrentTrackId()
                .subscribe(trackId -> {
                    currentTrackId = trackId;
                    getViewState().setCurrentTrack(currentTrackId);
                });
    }

    private Disposable observeTrackDeleting() {
        return trackRepo.observeTrackDeleting()
                .doOnNext(deletedTrackId -> {
                    if (deletedTrackId == currentTrackId)
                        currentTrackId = EmptyItems.NO_TRACK.getId();
                })
                .observeOn(schedulers.ui())
                .subscribe(removedTrackId -> {
                    getViewState().removeTrack(removedTrackId);
                    getViewState().setCurrentTrack(currentTrackId);
                });
    }


    public void onClickTrackItem(List<Track> tracks, int selectedPosition) {
        playerInteractor.start(tracks, selectedPosition);
    }


    public void onClickMenuPlay(List<Track> tracks, int selectedPosition) {
        playerInteractor.start(tracks, selectedPosition);
    }


    private Disposable refreshPlaylists() {
        return trackInteractor.getRecentlyAddedTracks()
                // ожидаем пока прогрузится анимация входа
                .doOnSuccess(tracks -> {while (!enterSlideAnimationEnded) ;})
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

    public void onClickMenuAddToPlaylist(int trackId) {
        router.openAddToPlaylistDialog(trackId);
    }

    public void onClickMenuAddToFavourites(int trackId) {
        trackRepo.addToFavourites(trackId);
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
