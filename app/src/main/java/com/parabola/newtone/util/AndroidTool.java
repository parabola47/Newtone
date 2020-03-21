package com.parabola.newtone.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public final class AndroidTool {

    private AndroidTool() {
        throw new AssertionError();
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
