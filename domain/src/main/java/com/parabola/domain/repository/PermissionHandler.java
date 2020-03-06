package com.parabola.domain.repository;

import io.reactivex.Observable;

public interface PermissionHandler {

    boolean hasPermission(Type type);
    Observable<Boolean> observePermissionUpdates(Type type);

    enum Type {
        FILE_STORAGE
    }
}
