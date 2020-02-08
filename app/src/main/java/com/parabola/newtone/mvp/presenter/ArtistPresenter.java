package com.parabola.newtone.mvp.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactors.AlbumInteractor;
import com.parabola.domain.interactors.ArtistInteractor;
import com.parabola.domain.model.Artist;
import com.parabola.domain.repository.ArtistRepository;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.ArtistView;
import com.parabola.newtone.ui.router.MainRouter;

import java.util.AbstractMap;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

@InjectViewState
public final class ArtistPresenter extends MvpPresenter<ArtistView> {
    private static final String TAG = ArtistPresenter.class.getSimpleName();

    private final int artistId;

    @Inject MainRouter router;

    @Inject ArtistRepository artistRepo;
    @Inject AlbumInteractor albumInteractor;
    @Inject TrackRepository trackRepo;
    @Inject SortingRepository sortingRepo;

    @Inject SchedulerProvider schedulers;

    private CompositeDisposable disposables = new CompositeDisposable();

    public ArtistPresenter(AppComponent appComponent, int artistId) {
        this.artistId = artistId;
        appComponent.inject(this);
    }


    @Override
    protected void onFirstViewAttach() {
        loadArtist();
        disposables.addAll(
                observeArtistAlbumsSorting(),
                 observeTrackDeleting()
        );
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

    private void loadArtist() {
        artistRepo.getById(artistId)
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
}
