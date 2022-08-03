package com.parabola.newtone.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.repository.TrackRepository
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.ui.router.MainRouter
import io.reactivex.internal.functions.Functions
import io.reactivex.internal.observers.ConsumerSingleObserver
import moxy.MvpAppCompatDialogFragment
import javax.inject.Inject


private const val TRACK_ID_ARG_KEY = "trackId"


class DeleteTrackDialog : MvpAppCompatDialogFragment() {
    private var deletedTrackId = 0

    @Inject
    lateinit var trackRepo: TrackRepository

    @Inject
    lateinit var schedulers: SchedulerProvider

    @Inject
    lateinit var appContext: Context

    @Inject
    lateinit var router: MainRouter


    companion object {
        fun newInstance(trackId: Int) = DeleteTrackDialog().apply {
            arguments = bundleOf(TRACK_ID_ARG_KEY to trackId)
        }
    }


    init {
        retainInstance = true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deletedTrackId = requireArguments().getInt(TRACK_ID_ARG_KEY)

        val appComponent = (requireActivity().application as MainApplication).appComponent
        appComponent.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.track_menu_delete_dialog_title)
            .setMessage(R.string.track_menu_delete_dialog_message)
            .setPositiveButton(R.string.dialog_delete) { _, _ -> onClickDeleteTrack() }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
    }


    private fun onClickDeleteTrack() {
        trackRepo.deleteTrack(deletedTrackId)
            .map { isDeleted ->
                if (isDeleted) R.string.file_deleted_successfully_toast
                else R.string.file_not_deleted_toast
            }
            .map(appContext::getString)
            .observeOn(schedulers.ui())
            .subscribe(
                ConsumerSingleObserver(
                    { message -> router.showToast(message, true) },
                    Functions.ERROR_CONSUMER
                )
            )
    }

}
