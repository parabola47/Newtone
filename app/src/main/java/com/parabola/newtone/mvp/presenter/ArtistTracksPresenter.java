package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.TrackInteractor;
import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.ArtistRepository;
import com.parabola.domain.repository.ResourceRepository;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.domain.utils.EmptyItems;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.ArtistTracksView;
import com.parabola.newtone.ui.router.MainRouter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class ArtistTracksPresenter extends MvpPresenter<ArtistTracksView> {
    private static final String TAG = ArtistTracksPresenter.class.getSimpleName();

    private final int artistId;

    private volatile int currentTrackId = EmptyItems.NO_TRACK.getId();

    @Inject MainRouter router;

    @Inject TrackInteractor trackInteractor;
    @Inject ViewSettingsInteractor viewSettingsInteractor;
    @Inject TrackRepository trackRepo;
    @Inject ArtistRepository artistRepository;
    @Inject PlayerInteractor playerInteractor;
    @Inject SortingRepository sortingRepo;
    @Inject ResourceRepository resourceRepository;

    @Inject SchedulerProvider schedulers;


    private final CompositeDisposable disposables = new CompositeDisposable();

    public ArtistTracksPresenter(AppComponent appComponent, int artistId) {
        this.artistId = artistId;
        appComponent.inject(this);
    }


    @Override
    protected void onFirstViewAttach() {
        disposables.addAll(
                loadArtist(),
                observeCurrentTrack(),
                observeSortingUpdates(),
                observeTrackItemViewUpdates(),
                observeTrackDeleting());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable loadArtist() {
        return artistRepository.getById(artistId)
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(artist -> {
                    getViewState().setArtistName(artist.getName());
                    updateTracksCount(artist.getTracksCount());
                });
    }

    private Disposable observeCurrentTrack() {
        return playerInteractor.onChangeCurrentTrackId()
                .doOnNext(currentTrackId -> this.currentTrackId = currentTrackId)
                .subscribe(getViewState()::setCurrentTrack);
    }

    private Disposable observeSortingUpdates() {
        return sortingRepo.observeArtistTracksSorting()
                //включаем/отключаем показ секции в списке, если отсортирован по названию
                .doOnNext(sorting -> getViewState().setSectionShowing(sorting == TrackRepository.Sorting.BY_TITLE))
                .flatMapSingle(sorting -> trackInteractor.getByArtist(artistId))
                // ожидаем пока прогрузится анимация входа
                .doOnNext(tracks -> {while (!enterSlideAnimationEnded) ;})
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

    private Disposable observeTrackDeleting() {
        return trackRepo.observeTrackDeleting()
                .flatMapSingle(sorting -> trackInteractor.getByArtist(artistId))
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(tracks -> {
                    if (!tracks.isEmpty()) {
                        getViewState().refreshTracks(tracks);
                        getViewState().setCurrentTrack(currentTrackId);
                        updateTracksCount(tracks.size());
                    } else router.backToRoot();
                });
    }


    public void onClickBack() {
        router.goBack();
    }

    private volatile boolean enterSlideAnimationEnded = false;

    public void onEnterSlideAnimationEnded() {
        enterSlideAnimationEnded = true;
    }

    private void updateTracksCount(int tracksCount) {
        String tracksCountStr = resourceRepository.getQuantityString(R.plurals.tracks_count, tracksCount);
        getViewState().setTracksCountTxt(tracksCountStr);
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

    public void onClickMenuAdditionalInfo(int trackId) {
        router.openTrackAdditionInfo(trackId);
    }
}
