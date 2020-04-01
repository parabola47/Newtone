package com.parabola.newtone.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.parabola.newtone.R;

import java.util.Objects;

public final class AndroidTool {

    private AndroidTool() {
        throw new AssertionError();
    }

    private static final int DELETE_TRACK_DIALOG_WIDTH_DP = 300;

    public static AlertDialog createDeleteTrackDialog(Context context,
                                                      DialogInterface.OnClickListener onClickDeleteListener) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.track_menu_delete_dialog_title)
                .setMessage(R.string.track_menu_delete_dialog_message)
                .setPositiveButton(R.string.dialog_delete, onClickDeleteListener)
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();

        Window window = Objects.requireNonNull(dialog.getWindow());
        window.getDecorView().setBackground(ContextCompat.getDrawable(context, R.drawable.dialog_bg));
        int widthPx = (int) convertDpToPixel(DELETE_TRACK_DIALOG_WIDTH_DP, context);
        window.setLayout(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT);

        return dialog;
    }

    public static float getScreenWidthDp(Context context, WindowManager windowManager) {
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);

        return convertPixelsToDp(point.x, context);
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

}
