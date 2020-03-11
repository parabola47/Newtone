package com.parabola.newtone;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.parabola.domain.interactor.SleepTimerInteractor;
import com.parabola.domain.interactor.observer.ConsumerObserver;
import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.interactor.type.Irrelevant;
import com.parabola.newtone.di.ComponentFactory;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.ui.HomeScreenWidget;

import io.reactivex.Observable;

public final class MainApplication extends Application {
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
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
