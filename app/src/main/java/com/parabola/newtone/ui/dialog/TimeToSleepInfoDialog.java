package com.parabola.newtone.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.mvp.presenter.TimeToSleepInfoPresenter;
import com.parabola.newtone.mvp.view.TimeToSleepInfoView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class TimeToSleepInfoDialog extends BaseDialogFragment
        implements TimeToSleepInfoView {

    @InjectPresenter TimeToSleepInfoPresenter presenter;


    @BindView(R.id.time_to_end_txt) TextView timeToEndTxt;


    public static TimeToSleepInfoDialog newInstance() {
        return new TimeToSleepInfoDialog();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.dialog_time_to_sleep, container, false);
        ButterKnife.bind(this, layout);

        return layout;
    }

    @OnClick(R.id.reset)
    public void onClickReset() {
        presenter.onClickReset();
    }

    @OnClick(R.id.cancel)
    public void onClickCancel() {
        presenter.onClickCancel();
    }


    @ProvidePresenter
    TimeToSleepInfoPresenter providePresenter() {
        return new TimeToSleepInfoPresenter(MainApplication.getComponent());
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
