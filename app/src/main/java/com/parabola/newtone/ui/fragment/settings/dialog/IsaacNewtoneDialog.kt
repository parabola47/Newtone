package com.parabola.newtone.ui.fragment.settings.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parabola.newtone.R;

import moxy.MvpAppCompatDialogFragment;

import static androidx.core.content.ContextCompat.getDrawable;

public final class IsaacNewtoneDialog extends MvpAppCompatDialogFragment {

    private ImageView newtoneImage;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        newtoneImage = new AppCompatImageView(requireContext());
        newtoneImage.setImageDrawable(getDrawable(requireContext(), R.drawable.isaac_newtone));

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.app_name)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setView(newtoneImage)
                .create();
    }

}
