package com.parabola.domain.utils;

public final class StringTool {
    private StringTool() {
        throw new AssertionError();
    }


    private static final String EMPTY = "";
    private static final char COMMA = ',';
    private static final char QUESTION_MARK = '?';

    public static String makeQueryPlaceholders(int len) {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            return EMPTY;
        }

        StringBuilder sb = new StringBuilder(len * 2 - 1);
        sb.append(QUESTION_MARK);
        for (int i = 1; i < len; i++) {
            sb.append(COMMA).append(QUESTION_MARK);
        }
        return sb.toString();
    }
}
