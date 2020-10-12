package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.QueueView;
import com.parabola.newtone.ui.router.MainRouter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class QueuePresenter extends MvpPresenter<QueueView> {

    @Inject MainRouter router;

    private boolean isFirstTracklistUpdate = true;

    @Inject PlayerInteractor playerInteractor;
    @Inject ViewSettingsInteractor viewSettingsInteractor;
    @Inject TrackRepository trackRepo;
    @Inject SchedulerProvider schedulers;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public QueuePresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        disposables.addAll(
                observeTracklistUpdates(),
                observeTrackItemViewUpdates(),
                observeIsItemDividerShowed(),
                observeCurrentTrackUpdates()
        );
        getViewState().goToItem(playerInteractor.currentTrackPosition());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observeTracklistUpdates() {
        return playerInteractor.onTracklistChanged()
                .flatMapSingle(trackRepo::getByIds)
                // ожидаем пока прогрузится анимация входа
                .doOnNext(irrelevant -> { while (!enterSlideAnimationEnded) ; })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(tracks -> {
                    getViewState().refreshTracks(tracks);
                    getViewState().setTrackCount(tracks.size());
                    getViewState().setCurrentTrackPosition(playerInteractor.currentTrackPosition());
                    if (isFirstTracklistUpdate) {
                        isFirstTracklistUpdate = false;
                        getViewState().goToItem(playerInteractor.currentTrackPosition());
                    }
                });
    }


    private Disposable observeTrackItemViewUpdates() {
        return viewSettingsInteractor.observeTrackItemViewUpdates()
                .subscribe(getViewState()::setItemViewSettings);
    }

    private Disposable observeIsItemDividerShowed() {
        return viewSettingsInteractor.observeIsItemDividerShowed()
                .subscribe(getViewState()::setItemDividerShowing);
    }

    private Disposable observeCurrentTrackUpdates() {
        return playerInteractor.onChangeCurrentTrackId()
                .map(integer -> playerInteractor.currentTrackPosition())
                .subscribe(getViewState()::setCurrentTrackPosition);
    }


    public void onClickBack() {
        router.goBack();
    }


    private volatile boolean enterSlideAnimationEnded = false;

    public void onEnterSlideAnimationEnded() {
        enterSlideAnimationEnded = true;
    }

    public void onClickActionBar() {
        int positionToGo = playerInteractor.currentTrackPosition();
        getViewState().goToItem(positionToGo);
    }

    public void onClickTrackItem(List<Track> tracks, int selectedPosition) {
        playerInteractor.start(tracks, selectedPosition);
    }

    public void onRemoveItem(int position) {
        if (position >= 0 && position < playerInteractor.tracksCount()) {
            playerInteractor.remove(position)
                    .subscribeOn(schedulers.io()).subscribe();
        }
    }

    public void onMoveItem(int oldPosition, int newPosition) {
        if (oldPosition != newPosition) {
            playerInteractor.moveTrack(oldPosition, newPosition)
                    .subscribeOn(schedulers.io()).subscribe();
        }
    }
}
