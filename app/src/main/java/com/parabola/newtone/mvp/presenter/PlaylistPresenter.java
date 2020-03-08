package com.parabola.newtone.mvp.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.TrackInteractor;
import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.PlaylistRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.utils.EmptyItems;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.PlaylistView;
import com.parabola.newtone.ui.router.MainRouter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

@InjectViewState
public final class PlaylistPresenter extends MvpPresenter<PlaylistView> {

    @Inject MainRouter router;

    @Inject PlayerInteractor playerInteractor;
    @Inject PlaylistRepository playlistRepo;
    @Inject TrackInteractor trackInteractor;
    @Inject TrackRepository trackRepo;

    @Inject SchedulerProvider schedulers;

    private final int playlistId;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private int currentTrackId = EmptyItems.NO_TRACK.getId();

    public PlaylistPresenter(AppComponent appComponent, int playlistId) {
        this.playlistId = playlistId;
        appComponent.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        disposables.addAll(
                refreshPlaylistInfo(),
                observePlaylistUpdates(),
                observeCurrentTrackUpdates(),
                observeTrackDeleting());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable refreshPlaylistInfo() {
        return playlistRepo.getById(playlistId)
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(playlist -> getViewState().setPlaylistTitle(playlist.getTitle()));
    }

    private Disposable observePlaylistUpdates() {
        return playlistRepo.observePlaylistsUpdates()
                .flatMapSingle(irrelevant -> trackInteractor.getByPlaylist(playlistId))
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(tracks -> {
                    getViewState().setTracksCount(tracks.size());
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
                .flatMapSingle(irrelevant -> trackInteractor.getByPlaylist(playlistId))
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(tracks -> {
                    if (!tracks.isEmpty()) {
                        getViewState().setTracksCount(tracks.size());
                        getViewState().refreshTracks(tracks);
                        getViewState().setCurrentTrack(currentTrackId);
                    } else router.backToRoot();
                }, error -> router.backToRoot());
    }

    private Disposable observeCurrentTrackUpdates() {
        return playerInteractor.onChangeCurrentTrackId()
                .doOnNext(currentTrackId -> this.currentTrackId = currentTrackId)
                .subscribe(getViewState()::setCurrentTrack);
    }

    public void onClickBack() {
        router.goBack();
    }

    public void onClickTrackItem(List<Track> tracks, int selectedPosition) {
        playerInteractor.start(tracks, selectedPosition);
    }

    public void onClickMenuPlay(List<Track> tracks, int selectedPosition) {
        playerInteractor.start(tracks, selectedPosition);
    }

    public void onClickMenuRemoveFromCurrentPlaylist(int trackId) {
        playlistRepo.removeTrack(playlistId, trackId)
                .subscribe();
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
