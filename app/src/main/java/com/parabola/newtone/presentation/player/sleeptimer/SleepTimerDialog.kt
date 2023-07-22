package com.parabola.newtone.presentation.player.sleeptimer

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import moxy.MvpAppCompatDialogFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import java.util.concurrent.atomic.AtomicInteger

class SleepTimerDialog : MvpAppCompatDialogFragment(), SleepTimerView {

    @InjectPresenter
    lateinit var presenter: SleepTimerPresenter


    companion object {
        fun newInstance() = SleepTimerDialog()
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val selectedIndex = AtomicInteger(2)

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.sleep_timer_dialog_title)
            .setSingleChoiceItems(
                R.array.sleep_timer_values,
                selectedIndex.get(),
            ) { _, index -> selectedIndex.set(index) }
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                val timeToSleepMs = getTimeMsByIndex(selectedIndex.get())
                presenter.startTimer(timeToSleepMs)
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
    }


    private fun getTimeMsByIndex(index: Int): Long {
        return when (index) {
            0 -> 5 * 60 * 1000
            1 -> 10 * 60 * 1000
            2 -> 15 * 60 * 1000
            3 -> 20 * 60 * 1000
            4 -> 30 * 60 * 1000
            5 -> 45 * 60 * 1000
            6 -> 60 * 60 * 1000
            else -> -1
        }
    }

    @ProvidePresenter
    fun providePresenter(): SleepTimerPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        return SleepTimerPresenter(appComponent)
    }

}
