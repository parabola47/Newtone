package com.parabola.data.repository;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.parabola.domain.repository.PermissionHandler;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public final class PermissionHandlerImpl implements PermissionHandler {

    private final Context context;

    private final BehaviorSubject<Boolean> fileStorageAccessObservable;


    public PermissionHandlerImpl(Context context) {
        this.context = context;

        fileStorageAccessObservable = BehaviorSubject.createDefault(hasExternalStorageAccess());
    }

    @Override
    public void invalidatePermission(Type type) {
        if (type != Type.FILE_STORAGE)
            return;

        boolean hasExternalStorageAccess = hasExternalStorageAccess();
        if (fileStorageAccessObservable.getValue() != hasExternalStorageAccess) {
            fileStorageAccessObservable.onNext(hasExternalStorageAccess);
        }
    }

    @Override
    public boolean hasPermission(Type type) {
        switch (type) {
            case FILE_STORAGE:
                return hasExternalStorageAccess();
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public Observable<Boolean> observePermissionUpdates(Type type) {
        switch (type) {
            case FILE_STORAGE:
                return fileStorageAccessObservable;
            default:
                throw new IllegalArgumentException();
        }
    }

    private boolean hasExternalStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

}
