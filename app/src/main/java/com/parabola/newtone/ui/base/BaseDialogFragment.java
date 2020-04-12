package com.parabola.newtone.ui.base;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;

import com.parabola.newtone.R;

import moxy.MvpAppCompatDialogFragment;

import static java.util.Objects.requireNonNull;

public abstract class BaseDialogFragment extends MvpAppCompatDialogFragment {
    private static final String LOG_TAG = BaseDialogFragment.class.getSimpleName();

    @Override
    public void onStart() {
        super.onStart();
        //убираем фон по умолчанию
        Window window = requireNonNull(requireDialog().getWindow());
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //в портретной ориентации устанавливаем фиксированную длину диалогового окна
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            int pxWidth = (int) requireContext().getResources().getDimension(R.dimen.alert_dialog_min_width);
            window.setLayout(pxWidth, window.getAttributes().height);
        }
    }
}
