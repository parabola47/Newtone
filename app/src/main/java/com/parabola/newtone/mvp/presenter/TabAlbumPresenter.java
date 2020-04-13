package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.AlbumInteractor;
import com.parabola.domain.repository.AlbumRepository;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.TabAlbumView;
import com.parabola.newtone.ui.router.MainRouter;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
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
                observeAllAlbumsSorting(),
                observeAlbumItemViewUpdates(),
                observeTrackDeleting());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observeAllAlbumsSorting() {
        AtomicBoolean needToShowSection = new AtomicBoolean(false);

        return sortingRepo.observeAllAlbumsSorting()
                //включаем/отключаем показ секции в списке, если отсортирован по названию
                .doOnNext(sorting -> needToShowSection.set(sorting == AlbumRepository.Sorting.BY_TITLE))
                .flatMapSingle(sorting -> albumInteractor.getAll())
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(albums -> {
                    getViewState().setSectionShowing(needToShowSection.get());
                    getViewState().refreshAlbums(albums);
                });
    }

    private Disposable observeTrackDeleting() {
        return trackRepo.observeTrackDeleting()
                .flatMapSingle(deletedTrackId -> albumInteractor.getAll())
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::refreshAlbums);
    }

    private Disposable observeAlbumItemViewUpdates() {
        return viewSettingsInteractor.observeAlbumItemViewUpdates()
                .subscribe(getViewState()::setAlbumViewSettings);
    }

    public void onItemClick(int albumId) {
        router.openAlbum(albumId);
    }
}
