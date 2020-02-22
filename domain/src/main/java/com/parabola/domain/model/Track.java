package com.parabola.domain.model;

import java.io.File;

public interface Track {
    int getId();
    String getTitle();

    long getDateAddingTimestamp();

    int getAlbumId();
    String getAlbumTitle();
    <ArtImage> ArtImage getArtImage();

    int getArtistId();
    String getArtistName();

    long getDurationMs();

    int getPositionInCd();

    String getFilePath();

    boolean isFavourite();
    Long getFavouriteTimestamp();


    default boolean equals(Track o) {
        return getId() == o.getId();
    }

    default String getFolderPath() {
        return getFolderPath(getFilePath());
    }

    static String getFolderPath(String trackPath) {
        int lastSlashIndex = trackPath.lastIndexOf(File.separator);

        return trackPath.substring(0, lastSlashIndex);
    }

    default String getFileName() {
        int lastSlashIndex = getFilePath().lastIndexOf(File.separator);

        return getFilePath().substring(lastSlashIndex + 1);
    }

    String FILE_TYPE_DIVIDER = ".";

    default String getFileNameWithoutExtension() {
        int lastDotIndex = getFileName().lastIndexOf(FILE_TYPE_DIVIDER);

        //если в расширении нет точки, то возвращаем полное имя файла
        if (lastDotIndex == -1)
            return getFileName();

        return getFileName().substring(0, lastDotIndex);
    }

}
