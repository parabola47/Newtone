package com.parabola.newtone.presentation.base

import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

class DialogDismissLifecycleObserver(dialog: AlertDialog) : LifecycleObserver {

    private var dialog: AlertDialog?


    init {
        this.dialog = dialog
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
        if (dialog != null) {
            dialog!!.dismiss()
            dialog = null
        }
    }

}
