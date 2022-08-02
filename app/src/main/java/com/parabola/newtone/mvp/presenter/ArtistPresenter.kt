package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.AlbumInteractor;
import com.parabola.domain.interactor.type.Irrelevant;
import com.parabola.domain.model.Artist;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.ArtistRepository;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.ArtistView;
import com.parabola.newtone.ui.router.MainRouter;

import java.util.AbstractMap;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.observers.ConsumerSingleObserver;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class ArtistPresenter extends MvpPresenter<ArtistView> {
    private static final String TAG = ArtistPresenter.class.getSimpleName();

    private final int artistId;

    @Inject MainRouter router;

    @Inject ArtistRepository artistRepo;
    @Inject AlbumInteractor albumInteractor;
    @Inject TrackRepository trackRepo;
    @Inject SortingRepository sortingRepo;
    @Inject ViewSettingsInteractor viewSettingsInteractor;


    @Inject SchedulerProvider schedulers;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public ArtistPresenter(AppComponent appComponent, int artistId) {
        this.artistId = artistId;
        appComponent.inject(this);
    }


    @Override
    protected void onFirstViewAttach() {
        disposables.addAll(
                loadArtist(),
                observeAlbumItemViewUpdates(),
                observeArtistAlbumsSorting(),
                observeTrackDeleting());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observeArtistAlbumsSorting() {
        return sortingRepo.observeArtistAlbumsSorting()
                .flatMapSingle(sorting -> albumInteractor.getByArtist(artistId))
                // ожидаем пока прогрузится анимация входа
                .doOnNext(tracks -> {while (!enterSlideAnimationEnded) ;})
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::refreshAlbums);
    }

    private Disposable observeTrackDeleting() {
        return trackRepo.observeTrackDeleting()
                .flatMapSingle(deletedTrackId -> Single.zip(artistRepo.getById(artistId), albumInteractor.getByArtist(artistId), AbstractMap.SimpleImmutableEntry::new))
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(
                        artistAlbumsEntry -> {
                            Artist artist = artistAlbumsEntry.getKey();
                            getViewState().setTracksCount(artist.getTracksCount());
                            getViewState().setAlbumsCount(artist.getAlbumsCount());
                            getViewState().refreshAlbums(artistAlbumsEntry.getValue());
                        },
                        error -> router.backToRoot());
    }

    private Disposable observeAlbumItemViewUpdates() {
        return Observable.combineLatest(viewSettingsInteractor.observeAlbumItemViewUpdates(), viewSettingsInteractor.observeIsItemDividerShowed(),
                (albumItemView, isItemDividerShowed) -> {
                    getViewState().setAlbumViewSettings(albumItemView);
                    getViewState().setItemDividerShowing(isItemDividerShowed && albumItemView.viewType == ViewSettingsInteractor.AlbumItemView.AlbumViewType.LIST);

                    return Irrelevant.INSTANCE;
                })
                .subscribe();
    }


    public void onClickAllTracks() {
        router.openArtistTracks(artistId);
    }


    public void onClickBack() {
        router.goBack();
    }

    private volatile boolean enterSlideAnimationEnded = false;

    public void onEnterSlideAnimationEnded() {
        enterSlideAnimationEnded = true;
    }

    private Disposable loadArtist() {
        return artistRepo.getById(artistId)
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(artist -> {
                    getViewState().setArtistName(artist.getName());
                    getViewState().setTracksCount(artist.getTracksCount());
                    getViewState().setAlbumsCount(artist.getAlbumsCount());
                });
    }

    public void onAlbumItemClick(int albumId) {
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
