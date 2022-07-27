package com.parabola.newtone.mvp.presenter

import com.parabola.domain.interactor.player.PlayerSetting
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.mvp.view.SettingView
import com.parabola.newtone.ui.router.MainRouter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class SettingPresenter(appComponent: AppComponent) : MvpPresenter<SettingView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var playerSetting: PlayerSetting

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    private val disposables = CompositeDisposable()


    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        disposables.addAll(
            observeIsNotificationBackgroundColorized(),
            observeNotificationArtworkShow(),
            observeIsItemDividerShowed()
        )
    }

    override fun onDestroy() {
        disposables.dispose()
    }


    private fun observeIsNotificationBackgroundColorized(): Disposable {
        return playerSetting.observeIsNotificationBackgroundColorized()
            .subscribe(viewState::setNotificationColorSwitchChecked)
    }

    private fun observeNotificationArtworkShow(): Disposable {
        return playerSetting.observeNotificationArtworkShow()
            .subscribe(viewState::setNotificationArtworkSwitchChecked)
    }

    private fun observeIsItemDividerShowed(): Disposable {
        return viewSettingsInteractor.observeIsItemDividerShowed()
            .subscribe(viewState::setShowListItemDividerSwitchChecked)
    }


    fun onClickBack() {
        router.goBack()
    }

    fun onClickColorThemeSettings() {
        router.openColorThemeSelectorSettings()
    }

    fun onClickNotificationArtworkShowSetting() {
        playerSetting.isNotificationArtworkShow = !playerSetting.isNotificationArtworkShow
    }

    fun onClickNotificationColorSetting() {
        playerSetting.isNotificationBackgroundColorized =
            !playerSetting.isNotificationBackgroundColorized
    }

    fun onClickShowItemDivider() {
        viewSettingsInteractor.setIsItemDividerShowed(!viewSettingsInteractor.isItemDividerShowed)
    }

    fun onClickExcludedFolders() {
        router.openExcludedFolders()
    }

    fun onClickTrackItemViewSettings() {
        router.openTrackItemDisplaySettings()
    }

    fun onClickAlbumItemViewSettings() {
        router.openAlbumItemDisplaySettings()
    }

    fun onClickArtistItemViewSettings() {
        router.openArtistItemDisplaySettings()
    }

    fun onClickPrivacyPolicy() {
        router.openPrivacyPolicyWebPage()
    }

    fun onClickContactDevelopers() {
        router.openContactDevelopersViaEmail()
    }

    private var appInfoBarClickCount = 0

    fun onClickAppInfoBar() {
        if (++appInfoBarClickCount % 10 == 0) {
            router.openNewtoneDialog()
        }
    }

}
