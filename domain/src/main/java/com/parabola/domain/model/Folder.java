package com.parabola.domain.model;

import java.io.File;

public interface Folder {
    default String getFolderName() {
        String absolutePath = getAbsolutePath();

        if (absolutePath.endsWith(File.separator)) {
            absolutePath = absolutePath.substring(0, absolutePath.length() - 1);
        }

        int index = absolutePath.lastIndexOf(File.separator);

        return absolutePath.substring(index + 1);
    }
    default String getPathToParent() {
        String absolutePath = getAbsolutePath();

        if (absolutePath.endsWith(File.separator)) {
            absolutePath = absolutePath.substring(0, absolutePath.length() - 1);
        }

        int index = absolutePath.lastIndexOf(File.separator);

        return absolutePath.substring(0, index);
    }

    String getAbsolutePath();

    int getTracksCount();

    default boolean equals(Folder o) {
        return getAbsolutePath().equals(o.getAbsolutePath());
    }
}
