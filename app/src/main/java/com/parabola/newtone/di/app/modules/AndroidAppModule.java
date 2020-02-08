package com.parabola.newtone.di.app.modules;

import android.content.ContentResolver;
import android.content.Context;

import com.parabola.data.executor.SchedulerProviderImpl;
import com.parabola.data.repository.AccessRepositoryImpl;
import com.parabola.data.repository.ResourceRepositoryImpl;
import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.repository.AccessRepository;
import com.parabola.domain.repository.ResourceRepository;
import com.parabola.newtone.MainApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class AndroidAppModule {

    private final MainApplication newtoneApp;

    public AndroidAppModule(MainApplication newtoneApp) {
        this.newtoneApp = newtoneApp;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return newtoneApp;
    }

    @Singleton
    @Provides
    SchedulerProvider schedulerProvider() {
        return new SchedulerProviderImpl();
    }

    @Singleton
    @Provides
    ContentResolver provideContentResolver() {
        return newtoneApp.getContentResolver();
    }

    @Singleton
    @Provides
    AccessRepository permissionService(Context context) {
        return new AccessRepositoryImpl(context);
    }

    @Singleton
    @Provides
    ResourceRepository provideResourcesProvider(Context context) {
        return new ResourceRepositoryImpl(context);
    }
}
