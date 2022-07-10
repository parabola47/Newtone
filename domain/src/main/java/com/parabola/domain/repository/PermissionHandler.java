package com.parabola.domain.repository;

import io.reactivex.Observable;

public interface PermissionHandler {

    // оповещение об обновлении в указанном разрешении.
    // Разрешение может быть не выдано, данный метод призывается лишь для перепроверки после
    // возможной выдачи разрешения
    void invalidatePermission(Type type);


    boolean hasPermission(Type type);

    Observable<Boolean> observePermissionUpdates(Type type);

    enum Type {
        FILE_STORAGE
    }
}
