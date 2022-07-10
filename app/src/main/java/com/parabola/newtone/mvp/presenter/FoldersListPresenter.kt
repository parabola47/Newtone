package com.parabola.newtone.mvp.presenter

import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.interactor.FolderInteractor
import com.parabola.domain.model.Track
import com.parabola.domain.repository.ExcludedFolderRepository
import com.parabola.domain.repository.FolderRepository
import com.parabola.domain.repository.TrackRepository
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.mvp.view.FoldersListView
import com.parabola.newtone.ui.router.MainRouter
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.functions.Functions
import io.reactivex.internal.observers.ConsumerSingleObserver
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class FoldersListPresenter(component: AppComponent) : MvpPresenter<FoldersListView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var folderRepo: FolderRepository

    @Inject
    lateinit var trackRepo: TrackRepository

    @Inject
    lateinit var excludedFolderRepo: ExcludedFolderRepository

    @Inject
    lateinit var folderInteractor: FolderInteractor

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    @Inject
    lateinit var schedulers: SchedulerProvider

    private val disposables = CompositeDisposable()


    init {
        component.inject(this)
    }


    override fun onFirstViewAttach() {
        disposables.addAll(
            refreshList(),
            observeExcludedFoldersUpdates(),
            observeIsItemDividerShowed(),
            observeTrackDeleting()
        )
    }

    override fun onDestroy() {
        disposables.dispose()
    }


    private fun refreshList(): Disposable {
        return folderRepo.all // ожидаем пока прогрузится анимация входа
            .doOnSuccess { while (!enterSlideAnimationEnded); }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe(viewState::refreshFolders)
    }

    private fun observeExcludedFoldersUpdates(): Disposable {
        return excludedFolderRepo.onExcludeFoldersUpdatesObserver()
            .flatMapSingle { folderRepo.all }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe(viewState::refreshFolders)
    }

    private fun observeIsItemDividerShowed(): Disposable {
        return viewSettingsInteractor.observeIsItemDividerShowed()
            .subscribe(viewState::setItemDividerShowing)
    }

    private fun observeTrackDeleting(): Disposable {
        return trackRepo.observeTrackDeleting()
            .flatMapSingle { folderRepo.all }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe(
                viewState::refreshFolders,
                { router.backToRoot() },
            )
    }


    fun onClickBack() {
        router.goBack()
    }

    @Volatile
    private var enterSlideAnimationEnded = false

    fun onEnterSlideAnimationEnded() {
        enterSlideAnimationEnded = true
    }

    fun onClickFolderItem(folderPath: String) {
        router.openFolder(folderPath)
    }

    fun onClickMenuShuffle(folderPath: String) {
        folderInteractor.shuffleFolder(folderPath)
    }

    fun onClickMenuAddToPlaylist(folderPath: String) {
        trackRepo.getByFolder(folderPath)
            .flatMapObservable { folderTracks -> Observable.fromIterable(folderTracks) }
            .map(Track::getId)
            .toList()
            .map { trackIds -> trackIds.toIntArray() }
            .subscribe(
                ConsumerSingleObserver(
                    router::openAddToPlaylistDialog,
                    Functions.ERROR_CONSUMER
                )
            )
    }

    fun onClickMenuExcludeFolder(excludeFolderPath: String) {
        excludedFolderRepo.addExcludedFolder(excludeFolderPath)
            .subscribe()
    }

}
