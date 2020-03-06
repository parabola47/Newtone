package com.parabola.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.parabola.domain.repository.PermissionHandler.Type;

public class PermissionChangeReceiver extends BroadcastReceiver {

    public static final String ACTION_FILE_STORAGE_PERMISSION_UPDATE = "com.parabola.data.FILE_STORAGE_PERMISSION";

    private PermissionChangeListener listener;

    public void setListener(PermissionChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null)
            return;

        if (ACTION_FILE_STORAGE_PERMISSION_UPDATE.equals(intent.getAction())) {
            if (listener != null)
                listener.onPermissionUpdate(Type.FILE_STORAGE);
        }
    }

    public interface PermissionChangeListener {
        void onPermissionUpdate(Type changedAccessType);
    }
}
