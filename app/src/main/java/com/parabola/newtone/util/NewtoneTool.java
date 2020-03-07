package com.parabola.newtone.util;

import android.content.Context;
import android.graphics.Typeface;

import com.parabola.newtone.R;
import com.skydoves.powermenu.PowerMenu;

public final class NewtoneTool {

    private NewtoneTool() {
        throw new AssertionError();
    }

    public static PowerMenu.Builder constructDefaultContextMenu(Context context) {
        return new PowerMenu.Builder(context)
                .setMenuRadius(16)
                .setTextColorResource(R.color.colorNewtoneWhite)
                .setTextSize(15)
                .setTextTypeface(Typeface.DEFAULT)
                .setMenuColorResource(R.color.colorMenuItemBackground)
                .setBackgroundAlpha(0f)
                .setAutoDismiss(true);
    }

}
