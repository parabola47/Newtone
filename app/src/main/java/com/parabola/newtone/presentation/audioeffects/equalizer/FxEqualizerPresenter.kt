package com.parabola.newtone.presentation.audioeffects.equalizer

import com.parabola.domain.interactor.player.AudioEffectsInteractor
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.ui.router.MainRouter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class FxEqualizerPresenter(appComponent: AppComponent) : MvpPresenter<FxEqualizerView>() {

    @Inject
    lateinit var fxInteractor: AudioEffectsInteractor

    @Inject
    lateinit var router: MainRouter

    private val disposables = CompositeDisposable()


    init {
        appComponent.inject(this)
    }


    public override fun onFirstViewAttach() {
        viewState.setMaxEqLevel(fxInteractor.maxEqBandLevel)
        viewState.setMinEqLevel(fxInteractor.minEqBandLevel)
        viewState.refreshBands(fxInteractor.bands)
        disposables.addAll(observeEqEnabling())
    }

    override fun onDestroy() {
        disposables.dispose()
    }


    private fun observeEqEnabling(): Disposable {
        return fxInteractor.observeEqEnabling()
            .subscribe { enabled ->
                viewState.setEqChecked(enabled)
                viewState.refreshBands(fxInteractor.bands)
            }
    }


    fun onClickEqSwitcher(enabled: Boolean) {
        fxInteractor.setEqEnable(enabled)
    }

    fun onChangeBandLevel(bandId: Int, newLevel: Int) {
        fxInteractor.setBandLevel(bandId, newLevel)
    }

    fun onClickShowPresets() {
        router.openEqPresetsSelectorDialog()
    }

}
