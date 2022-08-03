package com.parabola.newtone.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.mvp.presenter.TimeToSleepInfoPresenter
import com.parabola.newtone.mvp.view.TimeToSleepInfoView
import moxy.MvpAppCompatDialogFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter


class TimeToSleepInfoDialog : MvpAppCompatDialogFragment(),
    TimeToSleepInfoView {

    @InjectPresenter
    lateinit var presenter: TimeToSleepInfoPresenter

    private lateinit var timeToEndTxt: TextView


    companion object {
        fun newInstance() = TimeToSleepInfoDialog()
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        timeToEndTxt = AppCompatTextView(requireContext())
        val horizontalPadding =
            resources.getDimension(R.dimen.alert_dialog_view_horizontal_padding).toInt()
        val verticalPadding = resources.getDimension(R.dimen.alert_dialog_top_title_padding).toInt()
        timeToEndTxt.setPadding(horizontalPadding, verticalPadding, horizontalPadding, 0)

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_sleep_info_dialog)
            .setView(timeToEndTxt)
            .setPositiveButton(R.string.dialog_reset) { _, _ -> presenter.onClickReset() }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
    }


    @ProvidePresenter
    fun providePresenter(): TimeToSleepInfoPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        return TimeToSleepInfoPresenter(appComponent)
    }


    override fun updateTimeToEndText(timeToEndText: String) {
        timeToEndTxt.text = timeToEndText
    }

    override fun closeScreen() {
        dismiss()
    }

}
