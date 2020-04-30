package com.parabola.newtone.ui.base;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class BaseDialogFragment extends DialogFragment {

    private AlertDialog dialog;

    public static BaseDialogFragment build(@NonNull AlertDialog dialog) {
        BaseDialogFragment dialogFragment = new BaseDialogFragment();
        dialogFragment.dialog = dialog;

        return dialogFragment;
    }

    public BaseDialogFragment() {
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return dialog;
    }

}
