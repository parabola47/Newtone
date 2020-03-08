package com.parabola.newtone.mvp.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.AlbumInteractor;
import com.parabola.domain.repository.AlbumRepository;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.TabAlbumView;
import com.parabola.newtone.ui.router.MainRouter;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

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
                observeAllAlbumsSorting(), observeTrackDeleting(),
                observeTabAlbumViewType());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observeAllAlbumsSorting() {
        return sortingRepo.observeAllAlbumsSorting()
                .doOnNext(sorting -> getViewState().setSectionShowing(sorting == AlbumRepository.Sorting.BY_TITLE))
                .flatMapSingle(sorting -> albumInteractor.getAll())
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::refreshAlbums);
    }

    private Disposable observeTrackDeleting() {
        return trackRepo.observeTrackDeleting()
                .flatMapSingle(deletedTrackId -> albumInteractor.getAll())
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::refreshAlbums);
    }

    private Disposable observeTabAlbumViewType() {
        return viewSettingsInteractor.observeTabAlbumViewType()
                .subscribe(getViewState()::setViewType);
    }

    public void onItemClick(int albumId) {
        router.openAlbum(albumId);
    }
}
