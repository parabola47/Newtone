package com.parabola.newtone.di.app.modules;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;

import com.parabola.data.executor.SchedulerProviderImpl;
import com.parabola.data.repository.PermissionHandlerImpl;
import com.parabola.data.repository.ResourceRepositoryImpl;
import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.repository.PermissionHandler;
import com.parabola.domain.repository.ResourceRepository;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;

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
    PermissionHandler permissionService(Context context) {
        return new PermissionHandlerImpl(context);
    }

    @Singleton
    @Provides
    ResourceRepository provideResourcesProvider(Context context) {
        return new ResourceRepositoryImpl(context);
    }

    private static final int DEFAULT_NOTIFICATION_ALBUM_COVER_SIZE_PX = 192;

    @Singleton
    @Provides
    public Bitmap getDefaultNotificationAlbumCover(Context context) {
        Drawable drawable = AppCompatResources.getDrawable(context, R.drawable.album_default);
        Bitmap bitmap = Bitmap.createBitmap(DEFAULT_NOTIFICATION_ALBUM_COVER_SIZE_PX, DEFAULT_NOTIFICATION_ALBUM_COVER_SIZE_PX, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, DEFAULT_NOTIFICATION_ALBUM_COVER_SIZE_PX, DEFAULT_NOTIFICATION_ALBUM_COVER_SIZE_PX);
        drawable.draw(canvas);

        return bitmap;
    }
}
