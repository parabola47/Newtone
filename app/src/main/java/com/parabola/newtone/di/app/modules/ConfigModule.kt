package com.parabola.newtone.di.app.modules

import android.content.SharedPreferences
import com.parabola.data.repository.SortingRepositoryImpl
import com.parabola.data.settings.ViewSettingsInteractorImpl
import com.parabola.domain.repository.PermissionHandler
import com.parabola.domain.repository.SortingRepository
import com.parabola.domain.settings.ViewSettingsInteractor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ConfigModule {

    @Singleton
    @Provides
    fun provideTrackSortingRepository(
        preferences: SharedPreferences,
        accessRepo: PermissionHandler,
    ): SortingRepository = SortingRepositoryImpl(preferences, accessRepo)

    @Singleton
    @Provides
    fun provideViewSettingsInteractor(preferences: SharedPreferences): ViewSettingsInteractor =
        ViewSettingsInteractorImpl(preferences)
}
