package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.ArtistInteractor;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.ArtistRepository;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.TabArtistView;
import com.parabola.newtone.ui.router.MainRouter;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.observers.ConsumerSingleObserver;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class TabArtistPresenter extends MvpPresenter<TabArtistView> {

    private static final String TAG = TabArtistPresenter.class.getSimpleName();

    @Inject MainRouter router;

    @Inject ArtistInteractor artistInteractor;
    @Inject SortingRepository sortingRepo;
    @Inject TrackRepository trackRepo;
    @Inject ViewSettingsInteractor viewSettingsInteractor;

    @Inject SchedulerProvider schedulers;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public TabArtistPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        disposables.addAll(
                observeAllArtistsSorting(),
                observeTrackItemViewUpdates(),
                observeIsItemDividerShowed(),
                observeTrackDeleting());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observeAllArtistsSorting() {
        AtomicBoolean needToShowSection = new AtomicBoolean(false);

        return sortingRepo.observeAllArtistsSorting()
                //включаем/отключаем показ секции в списке, если отсортирован по имени
                .doOnNext(sorting -> needToShowSection.set(sorting == ArtistRepository.Sorting.BY_NAME))
                .flatMapSingle(sorting -> artistInteractor.getAll())
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(artists -> {
                    getViewState().setSectionShowing(needToShowSection.get());
                    getViewState().refreshArtists(artists);
                });
    }

    private Disposable observeTrackItemViewUpdates() {
        return viewSettingsInteractor.observeArtistItemViewUpdates()
                .subscribe(getViewState()::setItemViewSettings);
    }

    private Disposable observeIsItemDividerShowed() {
        return viewSettingsInteractor.observeIsItemDividerShowed()
                .subscribe(getViewState()::setItemDividerShowing);
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


    public void onClickMenuShuffle(int artistId) {
        artistInteractor.shuffleArtist(artistId);
    }

    public void onClickMenuAddToPlaylist(int artistId) {
        trackRepo.getByArtist(artistId)
                .flatMapObservable(Observable::fromIterable)
                .map(Track::getId)
                .toList()
                .map(ids -> ids.stream().mapToInt(Integer::intValue).toArray())
                .subscribe(new ConsumerSingleObserver<>(
                        ids -> router.openAddToPlaylistDialog(ids),
                        Functions.ERROR_CONSUMER
                ));
    }
}
