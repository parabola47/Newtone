package com.parabola.newtone.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.SleepTimerPresenter;
import com.parabola.newtone.mvp.view.SleepTimerView;
import com.parabola.newtone.ui.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class SleepTimerDialog extends BaseDialogFragment
        implements SleepTimerView {

    @InjectPresenter SleepTimerPresenter presenter;

    @BindView(R.id.time_radio) RadioGroup timeGroup;

    public SleepTimerDialog() {
    }

    public static SleepTimerDialog newInstance() {
        return new SleepTimerDialog();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.dialog_sleep_timer, container, false);
        ButterKnife.bind(this, layout);

        return layout;
    }

    @OnClick(R.id.ok)
    public void onClickOk() {
        int id = timeGroup.getCheckedRadioButtonId();
        long timeToSleepMs = getTimeById(id);

        presenter.startTimer(timeToSleepMs);
    }

    @OnClick(R.id.cancel)
    public void onClickCancel() {
        presenter.onClickCancel();
    }

    private long getTimeById(int id) {
        switch (id) {
            case R.id.min5: return 5 * 60 * 1000;
            case R.id.min10: return 10 * 60 * 1000;
            case R.id.min15: return 15 * 60 * 1000;
            case R.id.min20: return 20 * 60 * 1000;
            case R.id.min30: return 30 * 60 * 1000;
            case R.id.min45: return 45 * 60 * 1000;
            case R.id.h1: return 60 * 60 * 1000;
            default: return -1;
        }
    }

    @ProvidePresenter
    SleepTimerPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new SleepTimerPresenter(appComponent);
    }

    @Override
    public void showToast(String toastText) {
        Toast.makeText(requireContext(), toastText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void closeScreen() {
        dismiss();
    }
}
