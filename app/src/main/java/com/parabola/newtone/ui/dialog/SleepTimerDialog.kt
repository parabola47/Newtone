package com.parabola.newtone.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.SleepTimerPresenter;
import com.parabola.newtone.mvp.view.SleepTimerView;

import java.util.concurrent.atomic.AtomicInteger;

import moxy.MvpAppCompatDialogFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class SleepTimerDialog extends MvpAppCompatDialogFragment
        implements SleepTimerView {
    private static final String LOG_TAG = SleepTimerDialog.class.getSimpleName();

    @InjectPresenter SleepTimerPresenter presenter;

    public SleepTimerDialog() {
    }

    public static SleepTimerDialog newInstance() {
        return new SleepTimerDialog();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AtomicInteger selectedIndex = new AtomicInteger(2);

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.sleep_timer_dialog_title)
                .setSingleChoiceItems(R.array.sleep_timer_values, selectedIndex.get(), (d, index) ->
                        selectedIndex.set(index))
                .setPositiveButton(R.string.dialog_ok, (d, which) -> {
                    long timeToSleepMs = getTimeMsByIndex(selectedIndex.get());
                    presenter.startTimer(timeToSleepMs);
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();
    }

    private long getTimeMsByIndex(int index) {
        switch (index) {
            case 0: return 5 * 60 * 1000;
            case 1: return 10 * 60 * 1000;
            case 2: return 15 * 60 * 1000;
            case 3: return 20 * 60 * 1000;
            case 4: return 30 * 60 * 1000;
            case 5: return 45 * 60 * 1000;
            case 6: return 60 * 60 * 1000;
            default: return -1;
        }
    }

    @ProvidePresenter
    SleepTimerPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new SleepTimerPresenter(appComponent);
    }

}
