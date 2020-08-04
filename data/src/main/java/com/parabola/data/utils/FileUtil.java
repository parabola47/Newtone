package com.parabola.data.utils;

import java.io.File;

public final class FileUtil {

    private FileUtil() {
        throw new IllegalAccessError();
    }


    //true если файл по данному пути поличилось удалить или если файл отсутсвовал изначально,
    // false, если не удалось удалить файл
    public static boolean deleteFile(String path) {
        File deleteFile = new File(path);
        if (deleteFile.exists()) {
            return deleteFile.delete();
        }
        return true;
    }

}
