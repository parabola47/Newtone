package com.parabola.newtone.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.ui.router.MainRouter;

import javax.inject.Inject;

import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.observers.ConsumerSingleObserver;
import moxy.MvpAppCompatDialogFragment;

public final class DeleteTrackDialog extends MvpAppCompatDialogFragment {

    private static final String TRACK_ID_ARG_KEY = "trackId";
    private int deletedTrackId;


    @Inject TrackRepository trackRepo;
    @Inject SchedulerProvider schedulers;
    @Inject Context appContext;
    @Inject MainRouter router;


    public DeleteTrackDialog() {
        setRetainInstance(true);
    }


    public static DeleteTrackDialog newInstance(int trackId) {
        Bundle args = new Bundle();
        args.putInt(TRACK_ID_ARG_KEY, trackId);

        DeleteTrackDialog fragment = new DeleteTrackDialog();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deletedTrackId = requireArguments().getInt(TRACK_ID_ARG_KEY);

        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        appComponent.inject(this);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.track_menu_delete_dialog_title)
                .setMessage(R.string.track_menu_delete_dialog_message)
                .setPositiveButton(R.string.dialog_delete, (d, w) -> onClickDeleteTrack())
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();
    }

    private void onClickDeleteTrack() {
        trackRepo.deleteTrack(deletedTrackId)
                .map(isDeleted -> isDeleted ? R.string.file_deleted_successfully_toast : R.string.file_not_deleted_toast)
                .map(appContext::getString)
                .observeOn(schedulers.ui())
                .subscribe(new ConsumerSingleObserver<>(
                        s -> router.showToast(s, true),
                        Functions.ERROR_CONSUMER));
    }

}
