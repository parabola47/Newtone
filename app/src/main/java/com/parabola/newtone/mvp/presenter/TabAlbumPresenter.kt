package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.AlbumInteractor;
import com.parabola.domain.interactor.type.Irrelevant;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.AlbumRepository;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView.AlbumViewType;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.TabAlbumView;
import com.parabola.newtone.ui.router.MainRouter;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.observers.ConsumerSingleObserver;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class TabAlbumPresenter extends MvpPresenter<TabAlbumView> {
    private static final String TAG = TabAlbumPresenter.class.getSimpleName();

    @Inject MainRouter router;

    @Inject AlbumInteractor albumInteractor;
    @Inject SortingRepository sortingRepo;
    @Inject TrackRepository trackRepo;
    @Inject ViewSettingsInteractor viewSettingsInteractor;

    @Inject SchedulerProvider schedulers;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public TabAlbumPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        disposables.addAll(
                observerAllAlbums(),
                observeAllAlbumsSorting(),
                observeAlbumItemViewUpdates(),
                observeTrackDeleting());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }


    private Disposable observerAllAlbums() {
        return albumInteractor.observeAllAlbumsUpdates()
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::refreshAlbums);
    }

    private Disposable observeAllAlbumsSorting() {
        return sortingRepo.observeAllAlbumsSorting()
                //включаем/отключаем показ секции в списке, если отсортирован по названию
                .map(sorting -> sorting == AlbumRepository.Sorting.BY_TITLE)
                .subscribe(getViewState()::setSectionShowing);
    }

    private Disposable observeTrackDeleting() {
        return trackRepo.observeTrackDeleting()
                .flatMapSingle(deletedTrackId -> albumInteractor.getAll())
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::refreshAlbums);
    }

    private Disposable observeAlbumItemViewUpdates() {
        return Observable.combineLatest(viewSettingsInteractor.observeAlbumItemViewUpdates(), viewSettingsInteractor.observeIsItemDividerShowed(),
                (albumItemView, isItemDividerShowed) -> {
                    getViewState().setAlbumViewSettings(albumItemView);
                    getViewState().setItemDividerShowing(isItemDividerShowed && albumItemView.viewType == AlbumViewType.LIST);

                    return Irrelevant.INSTANCE;
                })
                .subscribe();
    }

    public void onItemClick(int albumId) {
        router.openAlbum(albumId);
    }

    public void onClickMenuShuffle(int albumId) {
        albumInteractor.shuffleAlbum(albumId);
    }

    public void onClickMenuAddToPlaylist(int albumId) {
        trackRepo.getByAlbum(albumId)
                .flatMapObservable(Observable::fromIterable)
                .map(Track::getId)
                .toList()
                .map(ids -> ids.stream().mapToInt(Integer::intValue).toArray())
                .subscribe(new ConsumerSingleObserver<>(
                        router::openAddToPlaylistDialog,
                        Functions.ERROR_CONSUMER
                ));
    }
}
