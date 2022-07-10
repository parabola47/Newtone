package com.parabola.newtone.mvp.presenter

import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.model.Track
import com.parabola.domain.repository.TrackRepository
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.mvp.view.QueueView
import com.parabola.newtone.ui.router.MainRouter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class QueuePresenter(appComponent: AppComponent) : MvpPresenter<QueueView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var playerInteractor: PlayerInteractor

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    @Inject
    lateinit var trackRepo: TrackRepository

    @Inject
    lateinit var schedulers: SchedulerProvider

    private var isFirstTracklistUpdate = true

    private val disposables = CompositeDisposable()


    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        disposables.addAll(
            observeTracklistUpdates(),
            observeTrackItemViewUpdates(),
            observeIsItemDividerShowed(),
            observeCurrentTrackUpdates()
        )
        viewState.goToItem(playerInteractor.currentTrackPosition())
    }

    override fun onDestroy() {
        disposables.dispose()
    }


    private fun observeTracklistUpdates(): Disposable {
        return playerInteractor.onTracklistChanged()
            .flatMapSingle(trackRepo::getByIds)
            // ожидаем пока прогрузится анимация входа
            .doOnNext {
                @Suppress("ControlFlowWithEmptyBody")
                while (!enterSlideAnimationEnded);
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe { tracks ->
                viewState.refreshTracks(tracks)
                viewState.setTrackCount(tracks.size)
                viewState.setCurrentTrackPosition(playerInteractor.currentTrackPosition())
                if (isFirstTracklistUpdate) {
                    isFirstTracklistUpdate = false
                    viewState.goToItem(playerInteractor.currentTrackPosition())
                }
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

    private fun observeCurrentTrackUpdates(): Disposable {
        return playerInteractor.onChangeCurrentTrackId()
            .map { playerInteractor.currentTrackPosition() }
            .subscribe(viewState::setCurrentTrackPosition)
    }


    fun onClickBack() {
        router.goBack()
    }

    @Volatile
    private var enterSlideAnimationEnded = false

    fun onEnterSlideAnimationEnded() {
        enterSlideAnimationEnded = true
    }

    fun onClickActionBar() {
        val positionToGo = playerInteractor.currentTrackPosition()
        viewState.goToItem(positionToGo)
    }

    fun onClickTrackItem(tracks: List<Track>, selectedPosition: Int) {
        playerInteractor.start(tracks, selectedPosition)
    }

    fun onRemoveItem(position: Int) {
        if (position >= 0 && position < playerInteractor.tracksCount()) {
            playerInteractor.remove(position)
                .subscribeOn(schedulers.io())
                .subscribe()
        }
    }

    fun onMoveItem(oldPosition: Int, newPosition: Int) {
        if (oldPosition != newPosition) {
            playerInteractor.moveTrack(oldPosition, newPosition)
                .subscribeOn(schedulers.io())
                .subscribe()
        }
    }

}
