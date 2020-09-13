package com.parabola.newtone.ui.dialog.fx;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parabola.domain.interactor.player.AudioEffectsInteractor;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;

import java.util.List;

import javax.inject.Inject;

import moxy.MvpAppCompatDialogFragment;

public final class EqPresetsSelectorDialog extends MvpAppCompatDialogFragment {

    @Inject AudioEffectsInteractor fxInteractor;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ((MainApplication) requireActivity().getApplication()).getAppComponent().inject(this);
        List<String> presets = fxInteractor.getPresets();
        String[] items = new String[presets.size()];
        items = presets.toArray(items);

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.preset_selector_dialog_title)
                .setItems(items, (d, presetIndex) -> {
                    fxInteractor.usePreset((short) presetIndex);
                    fxInteractor.setEqEnable(true);
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();
    }
}
