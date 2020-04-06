package com.parabola.newtone.util;

import android.app.Activity;
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

import static java.util.Objects.requireNonNull;

public final class AndroidTool {

    private AndroidTool() {
        throw new AssertionError();
    }


    public static AlertDialog createDeleteTrackDialog(Context context,
                                                      DialogInterface.OnClickListener onClickDeleteListener) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.track_menu_delete_dialog_title)
                .setMessage(R.string.track_menu_delete_dialog_message)
                .setPositiveButton(R.string.dialog_delete, onClickDeleteListener)
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();

        Window window = requireNonNull(dialog.getWindow());
        window.getDecorView().setBackground(ContextCompat.getDrawable(context, R.drawable.dialog_bg));
        int pxWidth = (int) context.getResources().getDimension(R.dimen.alert_dialog_min_width);
        window.setLayout(pxWidth, ViewGroup.LayoutParams.WRAP_CONTENT);

        return dialog;
    }


    public static final int MIN_GRID_ALBUM_VIEW_COLUMN_COUNT = 2;

    /**
     * Вычисление количества альбомов, которые должны быть помещены в одну строку на текущем экране.<p>
     * Если ширина экрана меньше или равна 500, то возвращается {@link #MIN_GRID_ALBUM_VIEW_COLUMN_COUNT}.<p>
     * Если же ширина экрана больше 500, то отображаемое количество будет численно равно отношению ширины экрана к 200
     * с округлением в меньшую сторону
     *
     * @return количество альбомов, которые должны быть показаны в одной строке
     */
    public static int calculateAlbumColumnCount(Activity activity) {
        float screenWidthDp = getScreenWidthDp(activity, activity.getWindowManager());

        int columnsCount = MIN_GRID_ALBUM_VIEW_COLUMN_COUNT;
        if (screenWidthDp > 500) columnsCount = ((int) screenWidthDp / 200);

        return columnsCount;
    }


    public static float getScreenWidthPx(WindowManager windowManager) {
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);

        return point.x;
    }

    public static float getScreenWidthDp(Context context, WindowManager windowManager) {
        return convertPixelsToDp(getScreenWidthPx(windowManager), context);
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
