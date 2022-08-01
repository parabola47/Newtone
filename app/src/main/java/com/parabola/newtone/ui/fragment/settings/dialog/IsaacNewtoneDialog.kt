package com.parabola.newtone.ui.fragment.settings.dialog

import android.app.Dialog
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat.getDrawable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.parabola.newtone.R
import moxy.MvpAppCompatDialogFragment

class IsaacNewtoneDialog : MvpAppCompatDialogFragment() {

    private lateinit var newtoneImage: ImageView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        newtoneImage = AppCompatImageView(requireContext())
        newtoneImage.setImageDrawable(
            getDrawable(requireContext(), R.drawable.isaac_newtone)
        )

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.app_name)
            .setNegativeButton(R.string.dialog_cancel, null)
            .setView(newtoneImage)
            .create()
    }

}
