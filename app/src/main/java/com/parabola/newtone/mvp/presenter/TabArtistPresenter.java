package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.ArtistInteractor;
import com.parabola.domain.repository.ArtistRepository;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.TabArtistView;
import com.parabola.newtone.ui.router.MainRouter;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class TabArtistPresenter extends MvpPresenter<TabArtistView> {

    private static final String TAG = TabArtistPresenter.class.getSimpleName();

    @Inject MainRouter router;

    @Inject ArtistInteractor artistInteractor;
    @Inject SortingRepository sortingRepo;
    @Inject TrackRepository trackRepo;

    @Inject SchedulerProvider schedulers;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public TabArtistPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        disposables.addAll(
                observeAllArtistsSorting(), observeTrackDeleting());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observeAllArtistsSorting() {
        return sortingRepo.observeAllArtistsSorting()
                //включаем/отключаем показ секции в списке, если отсортирован по имени
                .doOnNext(sorting -> getViewState().setSectionShowing(sorting == ArtistRepository.Sorting.BY_NAME))
                .flatMapSingle(sorting -> artistInteractor.getAll())
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::refreshArtists);
    }

    private Disposable observeTrackDeleting() {
        return trackRepo.observeTrackDeleting()
                .flatMapSingle(deletedTrackId -> artistInteractor.getAll())
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::refreshArtists);
    }

    public void onItemClick(int artistId) {
        router.openArtist(artistId);
    }
}
