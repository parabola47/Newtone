package com.parabola.newtone;

import android.app.Application;
import android.content.Context;

import com.parabola.domain.interactors.SleepTimerInteractor;
import com.parabola.domain.interactors.player.PlayerInteractor;
import com.parabola.domain.interactors.type.Irrelevant;
import com.parabola.newtone.di.ComponentFactory;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.ui.HomeScreenWidget;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;

import io.reactivex.Observable;

@AcraCore(
        buildConfigClass = BuildConfig.class,
        deleteUnapprovedReportsOnApplicationStart = false,
        sendReportsInDevMode = false)
@AcraMailSender(mailTo = "zizik.zizik@gmail.com")
@AcraDialog(
        resTitle = R.string.dialog_on_crash_title,
        resText = R.string.dialog_on_crash_text,
        resTheme = R.style.AppTheme,
        resCommentPrompt = R.string.dialog_on_crash_comment_prompt)
public final class MainApplication extends Application {
    private static final String LOG_TAG = "Newtone Application";

    private static AppComponent appComponent;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //инициализация отправки сообщений об ошибке
        ACRA.init(this);
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
                .subscribe(irrelevant -> HomeScreenWidget.updateHomeScreenWidget(this));
        //если приходит оповещение от таймера об окончании, то останавливаем плеер
        SleepTimerInteractor sleepTimerInteractor = appComponent.provideSleepTimerInteractor();
        sleepTimerInteractor.onTimerFinished()
                .subscribe(irrelevant -> playerInteractor.pause());
    }


    public static AppComponent getComponent() {
        return appComponent;
    }
}
