package com.parabola.newtone.mvp.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactors.TrackInteractor;
import com.parabola.domain.interactors.player.PlayerInteractor;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.AlbumRepository;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.utils.EmptyItems;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.AlbumView;
import com.parabola.newtone.ui.router.MainRouter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

@InjectViewState
public final class AlbumPresenter extends MvpPresenter<AlbumView> {
    private static final String TAG = AlbumPresenter.class.getSimpleName();

    private final int albumId;

    private volatile int currentTrackId = EmptyItems.NO_TRACK.getId();

    private volatile boolean enterSlideAnimationEnded = false;


    @Inject MainRouter router;
    @Inject SchedulerProvider schedulers;

    @Inject AlbumRepository albumRepo;
    @Inject TrackInteractor trackInteractor;
    @Inject TrackRepository trackRepo;
    @Inject PlayerInteractor playerInteractor;
    @Inject SortingRepository sortingRepo;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public AlbumPresenter(AppComponent appComponent, int albumId) {
        this.albumId = albumId;
        appComponent.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        loadAlbum();
        disposables.addAll(
                observeCurrentTrack(), observeSortingUpdates(),
                observeTrackDeleting());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observeCurrentTrack() {
        return playerInteractor.onChangeCurrentTrackId()
                .doOnNext(currentTrackId -> this.currentTrackId = currentTrackId)
                .subscribe(getViewState()::setCurrentTrack);
    }

    private Disposable observeSortingUpdates() {
        return sortingRepo.observeAlbumTracksSorting()
                .flatMapSingle(sorting -> trackInteractor.getByAlbum(albumId))
                // ожидаем пока прогрузится анимация входа
                .doOnNext(tracks -> {while (!enterSlideAnimationEnded) ;})
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(tracks -> {
                    getViewState().refreshTracks(tracks);
                    getViewState().setCurrentTrack(currentTrackId);
                });
    }

    private Disposable observeTrackDeleting() {
        return trackRepo.observeTrackDeleting()
                .doOnNext(removedTrackId -> {
                    if (removedTrackId == currentTrackId)
                        currentTrackId = EmptyItems.NO_TRACK.getId();
                })
                .flatMapSingle(removedTrackId -> trackInteractor.getByAlbum(albumId))
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(tracks -> {
                    if (!tracks.isEmpty()) {
                        getViewState().refreshTracks(tracks);
                        getViewState().setCurrentTrack(currentTrackId);
                    } else router.backToRoot();
                });
    }

    private void loadAlbum() {
        albumRepo.getById(albumId)
                .subscribe(album -> {
                    getViewState().setAlbumTitle(album.getTitle());
                    getViewState().setAlbumArtist(album.getArtistName());
                    getViewState().setAlbumArt(album.getArtImage());
                });
    }

    public void onClickBack() {
        router.goBack();
    }


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

    public void onClickMenuAddToFavourites(int trackId) {
        trackRepo.addToFavourites(trackId);
    }

    public void onClickMenuRemoveFromFavourites(int trackId) {
        trackRepo.removeFromFavourites(trackId);
    }

    public void onClickMenuShareTrack(Track track) {
        router.openShareTrack(track.getFilePath());
    }

    public void onClickMenuDeleteTrack(int trackId) {
        trackRepo.deleteTrack(trackId);
    }
}
