package com.parabola.newtone.ui.dialog.fx

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.parabola.domain.interactor.player.AudioEffectsInteractor
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import moxy.MvpAppCompatDialogFragment
import javax.inject.Inject

class EqPresetsSelectorDialog : MvpAppCompatDialogFragment() {

    @Inject
    lateinit var fxInteractor: AudioEffectsInteractor

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        (requireActivity().application as MainApplication).appComponent.inject(this)
        val presets = fxInteractor.presets

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.preset_selector_dialog_title)
            .setItems(presets.toTypedArray()) { _, presetIndex ->
                fxInteractor.usePreset(presetIndex)
                fxInteractor.setEqEnable(true)
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
    }

}
