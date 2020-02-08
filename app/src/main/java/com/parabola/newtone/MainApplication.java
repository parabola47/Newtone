package com.parabola.newtone;

import android.app.Application;
import android.content.Intent;
import android.os.Build;

import com.parabola.newtone.di.ComponentFactory;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.ui.AudioPlayerService;

public final class MainApplication extends Application {


    private static AppComponent appComponent;


    @Override
    public void onCreate() {
        super.onCreate();

        appComponent = ComponentFactory.createApplicationComponent(this);

        Intent intent = new Intent(this, AudioPlayerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }


    public static AppComponent getComponent() {
        return appComponent;
    }
}
