package com.parabola.newtone.mvp.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactors.player.PlayerInteractor;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.FolderRepository;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.utils.EmptyItems;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.FolderView;
import com.parabola.newtone.ui.router.MainRouter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

@InjectViewState
public final class FolderPresenter extends MvpPresenter<FolderView> {

    private volatile int currentTrackId = EmptyItems.NO_TRACK.getId();

    @Inject MainRouter router;

    @Inject PlayerInteractor playerInteractor;
    @Inject FolderRepository folderRepo;
    @Inject SortingRepository sortingRepo;
    @Inject TrackRepository trackRepo;

    @Inject SchedulerProvider schedulers;

    private final String folderPath;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public FolderPresenter(AppComponent component, String folderPath) {
        this.folderPath = folderPath;
        component.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        getViewState().setFolderPath(folderPath);

        disposables.addAll(
                observeCurrentTrack(), observeFolderTracksSorting(), observeTrackDeleting());
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

    private Disposable observeFolderTracksSorting() {
        return sortingRepo.observeFolderTracksSorting()
                //включаем/отключаем показ секции в списке, если отсортирован по названию
                .doOnNext(sorting -> getViewState().setSectionShowing(sorting == TrackRepository.Sorting.BY_TITLE))
                .flatMapSingle(sorting -> folderRepo.getTracksByFolder(folderPath))
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
                .flatMapSingle(removedTrackId -> folderRepo.getTracksByFolder(folderPath))
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(tracks -> {
                    if (!tracks.isEmpty()) {
                        getViewState().refreshTracks(tracks);
                        getViewState().setCurrentTrack(currentTrackId);
                    } else router.backToRoot();
                });
    }

    public void onTrackClick(List<Track> tracks, int selectedPosition) {
        playerInteractor.start(tracks, selectedPosition);
    }

    public void onClickBack() {
        router.goBack();
    }

    private volatile boolean enterSlideAnimationEnded = false;

    public void onEnterSlideAnimationEnded() {
        enterSlideAnimationEnded = true;
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
