package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.TrackInteractor;
import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.utils.EmptyItems;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.TabTrackView;
import com.parabola.newtone.ui.router.MainRouter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class TabTrackPresenter extends MvpPresenter<TabTrackView> {
    private static final String TAG = TabTrackPresenter.class.getSimpleName();

    private int currentTrackId = EmptyItems.NO_TRACK.getId();

    @Inject MainRouter router;

    @Inject TrackInteractor trackInteractor;
    @Inject TrackRepository trackRepo;
    @Inject PlayerInteractor playerInteractor;
    @Inject SortingRepository sortingRepo;

    @Inject SchedulerProvider schedulers;

    private final CompositeDisposable disposables = new CompositeDisposable();


    public TabTrackPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        disposables.addAll(
                observeCurrentTrack(),
                observeAllTracksSorting(),
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

    private Disposable observeAllTracksSorting() {
        return sortingRepo.observeAllTracksSorting()
                //включаем/отключаем показ секции в списке, если отсортирован по названию
                .doOnNext(sorting -> getViewState().setSectionShowing(sorting == TrackRepository.Sorting.BY_TITLE))
                .flatMapSingle(sorting -> trackInteractor.getAll())
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(tracks -> {
                    getViewState().refreshTracks(tracks);
                    getViewState().setCurrentTrack(currentTrackId);
                });
    }

    private Disposable observeTrackDeleting() {
        return trackRepo.observeTrackDeleting()
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::removeTrack);
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

    public void onClickMenuAdditionalInfo(int trackId) {
        router.openTrackAdditionInfo(trackId);
    }

    public void onClickMenuDeleteTrack(int trackId) {
        trackRepo.deleteTrack(trackId);
    }
}
