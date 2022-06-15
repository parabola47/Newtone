package com.parabola.newtone.di.app.modules

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import com.parabola.data.executor.SchedulerProviderImpl
import com.parabola.data.repository.PermissionHandlerImpl
import com.parabola.data.repository.ResourceRepositoryImpl
import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.repository.PermissionHandler
import com.parabola.domain.repository.ResourceRepository
import com.parabola.newtone.MainApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AndroidAppModule(private val newtoneApp: MainApplication) {

    @Singleton
    @Provides
    fun provideContext(): Context = newtoneApp

    @Singleton
    @Provides
    fun schedulerProvider(): SchedulerProvider = SchedulerProviderImpl()

    @Singleton
    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideContentResolver(): ContentResolver = newtoneApp.contentResolver

    @Singleton
    @Provides
    fun permissionService(context: Context): PermissionHandler = PermissionHandlerImpl(context)

    @Singleton
    @Provides
    fun provideResourcesProvider(context: Context): ResourceRepository =
        ResourceRepositoryImpl(context)

    companion object {
        private const val SHARED_PREFERENCES_NAME = "com.parabola.newtone.SHARED_PREFERENCES"
    }
}
