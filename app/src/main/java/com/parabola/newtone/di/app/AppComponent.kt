package com.parabola.newtone.di.app

import android.content.Intent
import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.repository.TrackRepository
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.newtone.MainApplication
import com.parabola.newtone.di.app.modules.*
import com.parabola.sleep_timer_feature.SleepTimerInteractor
import dagger.Component
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidAppModule::class,
        IntentModule::class,
        InteractorModule::class,
        DataModule::class,
        NavigationModule::class,
        ConfigModule::class,
        PresentationUseCasesModule::class,
    ]
)
interface AppComponent : AppComponentInjects {

    object Initializer {
        fun init(app: MainApplication): AppComponent {
            return DaggerAppComponent.builder()
                .androidAppModule(AndroidAppModule(app))
                .build()
        }
    }

    fun providePlayerInteractor(): PlayerInteractor
    fun provideSleepTimerInteractor(): SleepTimerInteractor
    fun provideTrackRepo(): TrackRepository
    fun provideViewSettingsInteractor(): ViewSettingsInteractor

    @Named(IntentModule.TOGGLE_PLAYER_STATE_INTENT)
    fun provideTogglePlayerIntent(): Intent

    @Named(IntentModule.GO_NEXT_TRACK_INTENT)
    fun provideGoNextIntent(): Intent

    @Named(IntentModule.GO_PREVIOUS_TRACK_INTENT)
    fun provideGoPreviousIntent(): Intent

    @Named(IntentModule.TOGGLE_REPEAT_MODE_INTENT)
    fun provideToggleRepeatModeIntent(): Intent

    @Named(IntentModule.TOGGLE_SHUFFLE_MODE_INTENT)
    fun provideToggleShuffleModeIntent(): Intent

    @Named(IntentModule.OPEN_ACTIVITY_INTENT)
    fun provideOpenActivityIntent(): Intent

}
