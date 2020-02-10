package com.parabola.newtone;

import android.app.Application;

import com.parabola.domain.interactors.player.PlayerInteractor;
import com.parabola.domain.interactors.type.Irrelevant;
import com.parabola.newtone.di.ComponentFactory;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.ui.HomeScreenWidget;

import io.reactivex.Observable;

public final class MainApplication extends Application {
    private static final String LOG_TAG = "Newtone Application";

    private static AppComponent appComponent;


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
                .subscribe(irrelevant -> HomeScreenWidget.updateHomeScreenWidget(this));
    }


    public static AppComponent getComponent() {
        return appComponent;
    }
}
