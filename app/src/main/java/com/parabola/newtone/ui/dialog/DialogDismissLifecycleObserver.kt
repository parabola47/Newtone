package com.parabola.newtone.ui.dialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

public final class DialogDismissLifecycleObserver implements LifecycleObserver {

    private AlertDialog dialog;

    public DialogDismissLifecycleObserver(@NonNull AlertDialog dialog) {
        this.dialog = dialog;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy(LifecycleOwner owner) {
        owner.getLifecycle().removeObserver(this);
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

}
