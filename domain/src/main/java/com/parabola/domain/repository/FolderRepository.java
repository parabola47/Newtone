package com.parabola.domain.repository;

import com.parabola.domain.model.Folder;
import com.parabola.domain.model.Track;

import java.util.List;

import io.reactivex.Single;

public interface FolderRepository {

    Single<List<Folder>> getAll();
    Single<List<Track>> getTracksByFolder(String folderPath);
}
