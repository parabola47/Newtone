package com.parabola.domain.repository;

import com.parabola.domain.interactor.type.Irrelevant;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

public interface ExcludedFolderRepository {


    Observable<Irrelevant> onExcludeFoldersUpdatesObserver();
    Completable addExcludedFolder(String folder);
    Completable refreshExcludedFolders(List<String> folders);
    List<String> getExcludedFolders();

}
