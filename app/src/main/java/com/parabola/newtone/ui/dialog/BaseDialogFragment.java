package com.parabola.newtone.ui.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.arellomobile.mvp.MvpAppCompatDialogFragment;

import static com.parabola.newtone.util.AndroidTool.convertDpToPixel;

public abstract class BaseDialogFragment extends MvpAppCompatDialogFragment {

    @Override
    public void onStart() {
        super.onStart();
        //убираем фон по умолчанию
        requireDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        //стандартная длина диалогового окна
        int pxWidth = (int) convertDpToPixel(300, requireContext());
        requireDialog().getWindow().setLayout(pxWidth, requireDialog().getWindow().getAttributes().height);
    }
}
