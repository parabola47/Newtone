package com.parabola.newtone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.parabola.domain.interactor.SleepTimerInteractor;
import com.parabola.domain.interactor.observer.ConsumerObserver;
import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.interactor.type.Irrelevant;
import com.parabola.domain.settings.ViewSettingsInteractor.ColorTheme;
import com.parabola.newtone.di.ComponentFactory;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.ui.HomeScreenWidget;
import com.parabola.player_feature.PlayerInteractorImpl;

import io.reactivex.Observable;

import static android.content.res.Configuration.UI_MODE_NIGHT_NO;
import static android.content.res.Configuration.UI_MODE_NIGHT_YES;
import static com.parabola.newtone.util.AndroidTool.getBitmapFromVectorDrawable;

public final class MainApplication extends MultiDexApplication {
    private static final String LOG_TAG = "Newtone Application";

    private static AppComponent appComponent;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //устанавливает флаг для отображения xml-ресурсов с типом <selector>
        //на android версиях ниже LOLLIPOP
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        appComponent = ComponentFactory.createApplicationComponent(this);

        //инициализация обновлений для виджетов
        PlayerInteractor playerInteractor = appComponent.providePlayerInteractor();
        Observable.combineLatest(
                playerInteractor.onChangeCurrentTrackId(),
                playerInteractor.onChangePlayingState(),
                playerInteractor.onRepeatModeChange(),
                playerInteractor.onShuffleModeChange(), (i, b1, b2, b3) -> Irrelevant.INSTANCE)
                .subscribe(ConsumerObserver.fromConsumer(
                        irrelevant -> HomeScreenWidget.updateHomeScreenWidget(MainApplication.this)));
        //если приходит оповещение от таймера об окончании, то останавливаем плеер
        SleepTimerInteractor sleepTimerInteractor = appComponent.provideSleepTimerInteractor();
        sleepTimerInteractor.onTimerFinished()
                .subscribe(ConsumerObserver.fromConsumer(irrelevant -> playerInteractor.pause()));

        //меням альбом по умолчанию в уведомлении при изменении цветовой схемы приложения
        appComponent.provideViewSettingsInteractor()
                .observeColorTheme()
                .subscribe(ConsumerObserver.fromConsumer(
                        this::updateNotificationDefaultAlbumCover));
    }


    private static final int DEFAULT_NOTIFICATION_ALBUM_COVER_SIZE_PX = 192;

    private void updateNotificationDefaultAlbumCover(ColorTheme colorTheme) {
        Configuration config = new Configuration(getResources().getConfiguration());

        config.uiMode = colorTheme == ColorTheme.DARK ? UI_MODE_NIGHT_YES : UI_MODE_NIGHT_NO;

        Resources resources = new Resources(getAssets(), null, config);
        Bitmap bitmap = getBitmapFromVectorDrawable(resources, R.drawable.album_default,
                DEFAULT_NOTIFICATION_ALBUM_COVER_SIZE_PX, DEFAULT_NOTIFICATION_ALBUM_COVER_SIZE_PX);


        ((PlayerInteractorImpl) getAppComponent().providePlayerInteractor()).setDefaultNotificationAlbumArt(bitmap);
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
