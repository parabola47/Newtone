package com.parabola.newtone.mvp.presenter

import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.interactor.TrackInteractor
import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.model.Track
import com.parabola.domain.repository.ResourceRepository
import com.parabola.domain.repository.SortingRepository
import com.parabola.domain.repository.TrackRepository
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.domain.utils.EmptyItems
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.mvp.view.FolderView
import com.parabola.newtone.ui.router.MainRouter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import moxy.InjectViewState
import moxy.MvpPresenter
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@InjectViewState
class FolderPresenter(component: AppComponent, private val folderPath: String) :
    MvpPresenter<FolderView>() {

    @Volatile
    private var currentTrackId = EmptyItems.NO_TRACK.id

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var playerInteractor: PlayerInteractor

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    @Inject
    lateinit var sortingRepo: SortingRepository

    @Inject
    lateinit var trackRepo: TrackRepository

    @Inject
    lateinit var trackInteractor: TrackInteractor

    @Inject
    lateinit var resourceRepo: ResourceRepository

    @Inject
    lateinit var schedulers: SchedulerProvider

    private val disposables = CompositeDisposable()


    init {
        component.inject(this)
    }


    override fun onFirstViewAttach() {
        viewState.setFolderPath(folderPath)

        disposables.addAll(
            observeCurrentTrack(),
            observeFolderTracksSorting(),
            observeTrackItemViewUpdates(),
            observeIsItemDividerShowed(),
            observeTrackDeleting()
        )
    }

    override fun onDestroy() {
        disposables.dispose()
    }


    private fun observeCurrentTrack(): Disposable {
        return playerInteractor.onChangeCurrentTrackId()
            .doOnNext { currentTrackId = it }
            .subscribe(viewState::setCurrentTrack)
    }

    private fun observeFolderTracksSorting(): Disposable {
        val needToShowSection = AtomicBoolean(false)

        return sortingRepo.observeFolderTracksSorting() //включаем/отключаем показ секции в списке, если отсортирован по названию
            .doOnNext { sorting -> needToShowSection.set(sorting == TrackRepository.Sorting.BY_TITLE) }
            .flatMapSingle { trackInteractor.getByFolder(folderPath) }
            // ожидаем пока прогрузится анимация входа
            .doOnNext {
                @Suppress("ControlFlowWithEmptyBody")
                while (!enterSlideAnimationEnded);
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe { tracks ->
                viewState.setSectionShowing(needToShowSection.get())
                viewState.refreshTracks(tracks)
                viewState.setCurrentTrack(currentTrackId)
            }
    }

    private fun observeTrackItemViewUpdates(): Disposable {
        return viewSettingsInteractor.observeTrackItemViewUpdates()
            .subscribe(viewState::setItemViewSettings)
    }

    private fun observeIsItemDividerShowed(): Disposable {
        return viewSettingsInteractor.observeIsItemDividerShowed()
            .subscribe(viewState::setItemDividerShowing)
    }

    private fun observeTrackDeleting(): Disposable {
        return trackRepo.observeTrackDeleting()
            .doOnNext { removedTrackId ->
                if (removedTrackId == currentTrackId)
                    currentTrackId = EmptyItems.NO_TRACK.id
            }
            .flatMapSingle { trackInteractor.getByFolder(folderPath) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe { tracks ->
                if (tracks.isNotEmpty()) {
                    viewState.refreshTracks(tracks)
                    viewState.setCurrentTrack(currentTrackId)
                } else {
                    router.backToRoot()
                }
            }
    }


    fun onTrackClick(tracks: List<Track>, selectedPosition: Int) {
        playerInteractor.start(tracks, selectedPosition)
    }

    fun onClickBack() {
        router.goBack()
    }

    @Volatile
    private var enterSlideAnimationEnded = false

    fun onEnterSlideAnimationEnded() {
        enterSlideAnimationEnded = true
    }


    fun onClickMenuPlay(tracks: List<Track>, selectedPosition: Int) {
        playerInteractor.start(tracks, selectedPosition)
    }

    fun onClickMenuAddToPlaylist(trackId: Int) {
        router.openAddToPlaylistDialog(trackId)
    }

    fun onClickMenuAddToFavourites(trackId: Int) {
        trackRepo.addToFavourites(trackId)
    }

    fun onClickMenuRemoveFromFavourites(trackId: Int) {
        trackRepo.removeFromFavourites(trackId)
    }

    fun onClickMenuShareTrack(selectedTrack: Track) {
        router.openShareTrack(selectedTrack.filePath)
    }

    fun onClickMenuDeleteTrack(trackId: Int) {
        router.openDeleteTrackDialog(trackId)
    }

    fun onClickMenuAdditionalInfo(trackId: Int) {
        router.openTrackAdditionInfo(trackId)
    }

}
