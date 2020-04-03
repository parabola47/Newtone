package com.parabola.newtone.ui.dialog;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;

import java.util.Objects;

import moxy.MvpAppCompatDialogFragment;

import static com.parabola.newtone.util.AndroidTool.convertDpToPixel;

public abstract class BaseDialogFragment extends MvpAppCompatDialogFragment {
    private static final String LOG_TAG = BaseDialogFragment.class.getSimpleName();

    public static final int DEFAULT_DIALOG_WIDTH_DP = 300;

    @Override
    public void onStart() {
        super.onStart();
        //убираем фон по умолчанию
        Window window = Objects.requireNonNull(requireDialog().getWindow());
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //в портретной ориентации устанавливаем фиксированную длину диалогового окна
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            int pxWidth = (int) convertDpToPixel(DEFAULT_DIALOG_WIDTH_DP, requireContext());
            window.setLayout(pxWidth, window.getAttributes().height);
        }
    }
}
