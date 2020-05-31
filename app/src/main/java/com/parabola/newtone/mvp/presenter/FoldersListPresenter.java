package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.repository.FolderRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.FoldersListView;
import com.parabola.newtone.ui.router.MainRouter;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class FoldersListPresenter extends MvpPresenter<FoldersListView> {

    @Inject MainRouter router;
    @Inject FolderRepository folderRepo;
    @Inject TrackRepository trackRepo;
    @Inject ViewSettingsInteractor viewSettingsInteractor;
    @Inject SchedulerProvider schedulers;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public FoldersListPresenter(AppComponent component) {
        component.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        disposables.addAll(
                refreshList(),
                observeIsItemDividerShowed(),
                observeTrackDeleting());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable refreshList() {
        return folderRepo.getAll()
                // ожидаем пока прогрузится анимация входа
                .doOnSuccess(folders -> {while (!enterSlideAnimationEnded) ;})
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::refreshFolders);
    }

    private Disposable observeIsItemDividerShowed() {
        return viewSettingsInteractor.observeIsItemDividerShowed()
                .subscribe(getViewState()::setItemDividerShowing);
    }

    private Disposable observeTrackDeleting() {
        return trackRepo.observeTrackDeleting()
                .flatMapSingle(removedTrackId -> folderRepo.getAll())
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(getViewState()::refreshFolders, error -> router.backToRoot());
    }


    public void onClickBack() {
        router.goBack();
    }


    private volatile boolean enterSlideAnimationEnded = false;

    public void onEnterSlideAnimationEnded() {
        enterSlideAnimationEnded = true;
    }

    public void onClickFolderItem(String folderPath) {
        router.openFolder(folderPath);
    }
}
