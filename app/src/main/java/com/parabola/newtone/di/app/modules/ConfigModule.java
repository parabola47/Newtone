package com.parabola.newtone.di.app.modules;

import android.content.Context;

import com.parabola.data.repository.SortingRepositoryImpl;
import com.parabola.data.settings.ViewSettingsInteractorImpl;
import com.parabola.domain.repository.PermissionHandler;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.settings.ViewSettingsInteractor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class ConfigModule {

    @Singleton
    @Provides
    SortingRepository provideTrackSortingRepository(Context context, PermissionHandler accessRepo) {
        return new SortingRepositoryImpl(context, accessRepo);
    }

    @Singleton
    @Provides
    ViewSettingsInteractor provideViewSettingsInteractor(Context context) {
        return new ViewSettingsInteractorImpl(context);
    }

}
