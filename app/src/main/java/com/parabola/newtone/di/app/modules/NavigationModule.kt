package com.parabola.newtone.di.app.modules;

import com.parabola.newtone.ui.router.MainRouter;
import com.parabola.newtone.ui.router.MainRouterImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class NavigationModule {

    @Singleton
    @Provides
    MainRouter provideMainRouter() {
        return new MainRouterImpl();
    }
}
