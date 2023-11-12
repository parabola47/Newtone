package com.parabola.newtone.presentation.playlist.folderslist

import com.parabola.domain.model.Track
import com.parabola.domain.repository.ExcludedFolderRepository
import com.parabola.domain.repository.FolderRepository
import com.parabola.domain.repository.TrackRepository
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.presentation.base.BasePresenter
import com.parabola.newtone.presentation.router.MainRouter
import io.reactivex.Observable
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class FoldersListPresenter(
    component: AppComponent,
) : BasePresenter<FoldersListView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var folderRepo: FolderRepository

    @Inject
    lateinit var trackRepo: TrackRepository

    @Inject
    lateinit var excludedFolderRepo: ExcludedFolderRepository

    @Inject
    lateinit var useCases: FoldersListScreenUseCases

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    init {
        component.inject(this)
    }


    override fun onFirstViewAttach() {
        refreshList()
        observeExcludedFoldersUpdates()
        observeIsItemDividerShowed()
        observeTrackDeleting()
    }

    private fun refreshList() {
        folderRepo.all
            // ожидаем пока прогрузится анимация входа
            .doOnSuccess {
                @Suppress("ControlFlowWithEmptyBody")
                while (!enterSlideAnimationEnded);
            }
            .schedule(onSuccess = viewState::refreshFolders)
    }

    private fun observeExcludedFoldersUpdates() {
        excludedFolderRepo.onExcludeFoldersUpdatesObserver()
            .flatMapSingle { folderRepo.all }
            .schedule(onNext = viewState::refreshFolders)
    }

    private fun observeIsItemDividerShowed() {
        viewSettingsInteractor.observeIsItemDividerShowed()
            .schedule(onNext = viewState::setItemDividerShowing)
    }

    private fun observeTrackDeleting() {
        trackRepo.observeTrackDeleting()
            .flatMapSingle { folderRepo.all }
            .schedule(
                onNext = viewState::refreshFolders,
                onError = { router.backToRoot() },
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
        useCases.shuffleFolder(folderPath)
            .schedule()
    }

    fun onClickMenuAddToPlaylist(folderPath: String) {
        trackRepo.getByFolder(folderPath)
            .flatMapObservable { folderTracks -> Observable.fromIterable(folderTracks) }
            .map(Track::getId)
            .toList()
            .map { trackIds -> trackIds.toIntArray() }
            .schedule(onSuccess = router::openAddToPlaylistDialog)
    }

    fun onClickMenuExcludeFolder(excludeFolderPath: String) {
        excludedFolderRepo.addExcludedFolder(excludeFolderPath)
            .schedule()
    }

}
