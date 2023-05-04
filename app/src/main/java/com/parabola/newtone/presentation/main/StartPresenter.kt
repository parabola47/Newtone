package com.parabola.newtone.presentation.main

import com.parabola.domain.interactor.TrackInteractor
import com.parabola.domain.repository.PermissionHandler
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.ui.router.MainRouter
import io.reactivex.disposables.Disposable
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class StartPresenter(appComponent: AppComponent) : MvpPresenter<StartView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var accessRepo: PermissionHandler

    @Inject
    lateinit var trackInteractor: TrackInteractor

    private lateinit var storagePermissionObserver: Disposable


    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        storagePermissionObserver =
            accessRepo.observePermissionUpdates(PermissionHandler.Type.FILE_STORAGE)
                .subscribe { viewState.setPermissionPanelVisibility(!it) }
    }

    override fun onDestroy() {
        storagePermissionObserver.dispose()
    }


    fun onClickRequestPermission() {
        router.openRequestStoragePermissionScreen()
    }

    fun onClickMenuShuffleAll() {
        trackInteractor.shuffleAll()
    }

    fun onClickMenuExcludedFolders() {
        router.openExcludedFolders()
    }

}
