package com.parabola.newtone.di.app.modules;

import android.content.Context;

import com.parabola.data.repository.SortingRepositoryImpl;
import com.parabola.domain.repository.AccessRepository;
import com.parabola.domain.repository.SortingRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class ConfigModule {

    @Singleton
    @Provides
    SortingRepository provideTrackSortingRepository(Context context, AccessRepository accessRepo) {
        return new SortingRepositoryImpl(context, accessRepo);
    }

}
