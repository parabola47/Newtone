package com.parabola.data.repository;

import com.parabola.domain.model.Folder;
import com.parabola.domain.repository.FolderRepository;

import java.util.List;

import io.reactivex.Single;

public final class FolderRepositoryImpl implements FolderRepository {

    private final DataExtractor dataExtractor;


    public FolderRepositoryImpl(DataExtractor dataExtractor) {
        this.dataExtractor = dataExtractor;
    }


    @Override
    public Single<List<Folder>> getAll() {
        return Single.fromCallable(() -> dataExtractor.folders);
    }

}
