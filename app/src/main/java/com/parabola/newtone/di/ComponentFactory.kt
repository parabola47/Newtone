package com.parabola.newtone.di

import com.parabola.newtone.MainApplication
import com.parabola.newtone.di.app.AppComponent

object ComponentFactory {
    // TODO убрать аннотацию после того как MainApplication будет переведён на котлин
    @JvmStatic
    fun createApplicationComponent(app: MainApplication): AppComponent {
        return AppComponent.Initializer.init(app)
    }
}
