package com.parabola.newtone.presentation.main.start

import com.parabola.domain.repository.PermissionHandler
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.presentation.base.BasePresenter
import com.parabola.newtone.presentation.router.MainRouter
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class StartPresenter(
    appComponent: AppComponent,
) : BasePresenter<StartView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var accessRepo: PermissionHandler

    @Inject
    lateinit var useCases: StartScreenUseCases


    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        accessRepo.observePermissionUpdates(PermissionHandler.Type.FILE_STORAGE)
            .schedule(
                onNext = { isFileStoragePermissionGranted ->
                    viewState.setPermissionPanelVisibility(!isFileStoragePermissionGranted)
                },
            )
    }


    fun onClickRequestPermission() {
        router.openRequestStoragePermissionScreen()
    }

    fun onClickMenuShuffleAll() {
        useCases.shuffleAll()
            .schedule()
    }

    fun onClickMenuExcludedFolders() {
        router.openExcludedFolders()
    }

}
