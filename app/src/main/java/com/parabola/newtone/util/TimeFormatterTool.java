package com.parabola.newtone.util;

import java.util.Locale;

public final class TimeFormatterTool {

    private TimeFormatterTool() {
    }

    public static String formatMillisecondsToMinutes(long ms) {
        return formatSecondsToMinutes(ms / 1000);
    }

    /**
     * @param s количество секунд, которое необходимо преобразовать
     * @return строка в формате MM:SS, если количество минут меньше 60.
     * Если количество минут больше 60, то в формате H:MM:SS
     */
    public static String formatSecondsToMinutes(long s) {
        if (s < 0) {
            throw new IllegalArgumentException("Parameter s must be larger than or equal to 0. Value: " + s);
        }
        long hours = s / (3600);
        long minutes = (s - (hours * 3600)) / 60;
        long seconds = s - ((hours * 3600) + (minutes * 60));

        if (hours < 1) {
            return String.format(Locale.getDefault(), "%1d:%02d", minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%1d:%02d:%02d", hours, minutes, seconds);
        }
    }

}
