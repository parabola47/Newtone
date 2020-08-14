package com.parabola.domain.repository;

import com.parabola.domain.model.Folder;

import java.util.List;

import io.reactivex.Single;

public interface FolderRepository {

    //Проверяет количество треков в указанной папке, а также во всех подпапках
    long tracksCountInFolderRecursively(String folderPath);

    Single<List<Folder>> getAll();

}
