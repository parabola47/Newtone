package com.parabola.newtone.di;

import com.parabola.newtone.MainApplication;
import com.parabola.newtone.di.app.AppComponent;

public final class ComponentFactory {

    private ComponentFactory() {
    }

    public static AppComponent createApplicationComponent(final MainApplication app) {
        return AppComponent.Initializer.init(app);
    }
}
