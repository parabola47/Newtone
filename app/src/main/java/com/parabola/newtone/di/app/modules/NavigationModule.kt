package com.parabola.newtone.di.app.modules

import com.parabola.newtone.ui.router.MainRouter
import com.parabola.newtone.ui.router.MainRouterImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NavigationModule {
    @Singleton
    @Provides
    fun provideMainRouter(): MainRouter = MainRouterImpl()
}
