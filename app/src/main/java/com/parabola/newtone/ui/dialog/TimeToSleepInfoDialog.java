package com.parabola.newtone.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.TimeToSleepInfoPresenter;
import com.parabola.newtone.mvp.view.TimeToSleepInfoView;

import moxy.MvpAppCompatDialogFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class TimeToSleepInfoDialog extends MvpAppCompatDialogFragment
        implements TimeToSleepInfoView {

    @InjectPresenter TimeToSleepInfoPresenter presenter;

    private TextView timeToEndTxt;


    public static TimeToSleepInfoDialog newInstance() {
        return new TimeToSleepInfoDialog();
    }


    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        timeToEndTxt = new AppCompatTextView(requireContext());
        int horizontalPadding = (int) getResources().getDimension(R.dimen.alert_dialog_view_horizontal_padding);
        int verticalPadding = (int) getResources().getDimension(R.dimen.alert_dialog_top_title_padding);
        timeToEndTxt.setPadding(horizontalPadding, verticalPadding, horizontalPadding, 0);

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.title_sleep_info_dialog)
                .setView(timeToEndTxt)
                .setPositiveButton(R.string.dialog_reset, (d, w) ->
                        presenter.onClickReset())
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();
    }

    @ProvidePresenter
    TimeToSleepInfoPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new TimeToSleepInfoPresenter(appComponent);
    }


    @Override
    public void updateTimeToEndText(String timeToEndText) {
        timeToEndTxt.setText(timeToEndText);
    }

    @Override
    public void closeScreen() {
        dismiss();
    }

}
