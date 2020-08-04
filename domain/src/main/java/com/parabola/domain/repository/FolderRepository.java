package com.parabola.domain.repository;

import com.parabola.domain.model.Folder;

import java.util.List;

import io.reactivex.Single;

public interface FolderRepository {

    Single<List<Folder>> getAll();

}
