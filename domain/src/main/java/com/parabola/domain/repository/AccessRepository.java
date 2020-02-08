package com.parabola.domain.repository;

import io.reactivex.Observable;

public interface AccessRepository {

    boolean hasAccess(AccessType accessType);
    Observable<Boolean> observeAccessUpdates(AccessType accessType);

    enum AccessType {
        FILE_STORAGE
    }
}
