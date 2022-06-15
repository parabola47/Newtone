package com.parabola.newtone.di.app.modules;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;

import com.parabola.data.executor.SchedulerProviderImpl;
import com.parabola.data.repository.PermissionHandlerImpl;
import com.parabola.data.repository.ResourceRepositoryImpl;
import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.repository.PermissionHandler;
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


    private static final String SHARED_PREFERENCES_NAME = "com.parabola.newtone.SHARED_PREFERENCES";

    @Singleton
    @Provides
    SharedPreferences provideSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Singleton
    @Provides
    ContentResolver provideContentResolver() {
        return newtoneApp.getContentResolver();
    }

    @Singleton
    @Provides
    PermissionHandler permissionService(Context context) {
        return new PermissionHandlerImpl(context);
    }

    @Singleton
    @Provides
    ResourceRepository provideResourcesProvider(Context context) {
        return new ResourceRepositoryImpl(context);
    }

}
