package com.parabola.newtone.mvp.presenter

import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.domain.settings.ViewSettingsInteractor.ColorTheme
import com.parabola.domain.settings.ViewSettingsInteractor.PrimaryColor
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.mvp.view.ColorThemeSelectorView
import com.parabola.newtone.ui.router.MainRouter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class ColorThemeSelectorPresenter(appComponent: AppComponent) :
    MvpPresenter<ColorThemeSelectorView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    private val disposables = CompositeDisposable()


    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        disposables.addAll(
            observeColorTheme(),
            observePrimaryColor()
        )
    }

    override fun onDestroy() {
        disposables.dispose()
    }

    private fun observeColorTheme(): Disposable {
        return viewSettingsInteractor.observeColorTheme()
            .subscribe(viewState::setDarkLightTheme)
    }

    private fun observePrimaryColor(): Disposable {
        return viewSettingsInteractor.observePrimaryColor()
            .subscribe(viewState::setPrimaryColor)
    }

    fun onDarkLightSelection(colorTheme: ColorTheme) {
        viewSettingsInteractor.colorTheme = colorTheme
    }

    fun onPrimaryColorSelection(primaryColor: PrimaryColor) {
        viewSettingsInteractor.primaryColor = primaryColor
    }

    fun onClickBackButton() {
        router.goBack()
    }

}
