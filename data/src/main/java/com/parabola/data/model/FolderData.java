package com.parabola.data.model;

import androidx.annotation.Nullable;

import com.parabola.domain.model.Folder;

public final class FolderData implements Folder {
    public String folderPath;
    public int tracksCount;

    @Override
    public String getAbsolutePath() {
        return folderPath;
    }

    @Override
    public int getTracksCount() {
        return tracksCount;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Folder)) {
            return false;
        }
        return equals((Folder) obj);
    }
}
