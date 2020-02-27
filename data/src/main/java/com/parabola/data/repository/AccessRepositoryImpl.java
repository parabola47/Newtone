package com.parabola.data.repository;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;

import com.parabola.data.PermissionChangeReceiver;
import com.parabola.domain.repository.AccessRepository;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public final class AccessRepositoryImpl implements AccessRepository {

    private final Context context;

    private final BehaviorSubject<Boolean> fileStorageAccessObservable;


    public AccessRepositoryImpl(Context context) {
        this.context = context;

        fileStorageAccessObservable = BehaviorSubject.createDefault(hasExternalStorageAccess());

        IntentFilter filter = new IntentFilter();
        filter.addAction(PermissionChangeReceiver.ACTION_FILE_STORAGE_PERMISSION_UPDATE);
        PermissionChangeReceiver receiver = new PermissionChangeReceiver();

        receiver.setListener(changedAccessType -> {
            if (changedAccessType != AccessType.FILE_STORAGE)
                return;

            boolean hasExternalStorageAccess = hasExternalStorageAccess();
            if (fileStorageAccessObservable.getValue() != hasExternalStorageAccess) {
                fileStorageAccessObservable.onNext(hasExternalStorageAccess);
            }
        });
        context.registerReceiver(receiver, filter);
    }

    @Override
    public boolean hasAccess(AccessType accessType) {
        switch (accessType) {
            case FILE_STORAGE:
                return hasExternalStorageAccess();
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public Observable<Boolean> observeAccessUpdates(AccessType accessType) {
        switch (accessType) {
            case FILE_STORAGE: return fileStorageAccessObservable;
            default: throw new IllegalArgumentException();
        }
    }

    private boolean hasExternalStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

}
