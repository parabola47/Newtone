package com.parabola.data.repository;

import android.content.SharedPreferences;

import com.parabola.domain.interactor.type.Irrelevant;
import com.parabola.domain.repository.ExcludedFolderRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static java.util.Objects.requireNonNull;

public final class ExcludedFolderRepositoryImpl implements ExcludedFolderRepository {


    private final SharedPreferences prefs;
    private final PublishSubject<Irrelevant> onExcludeFoldersObserver = PublishSubject.create();


    private static final String EXCLUDED_FOLDERS_SAVE_KEY = "com.parabola.data.repository.ExcludedFolderRepositoryImpl.EXCLUDED_FOLDERS";


    public ExcludedFolderRepositoryImpl(SharedPreferences prefs) {
        this.prefs = prefs;
    }


    @Override
    public Observable<Irrelevant> onExcludeFoldersUpdatesObserver() {
        return onExcludeFoldersObserver;
    }

    @Override
    public Completable addExcludedFolder(String folder) {
        return Completable.fromAction(() -> {
            Set<String> folders = requireNonNull(prefs.getStringSet(EXCLUDED_FOLDERS_SAVE_KEY, new HashSet<>()));
            folders.add(folder);

            prefs.edit()
                    .putStringSet(EXCLUDED_FOLDERS_SAVE_KEY, new HashSet<>(folders))
                    .apply();

            onExcludeFoldersObserver.onNext(Irrelevant.INSTANCE);
        });
    }

    @Override
    public Completable refreshExcludedFolders(List<String> folders) {
        return Completable.fromAction(() -> {
            if (!isEqualToOldFolders(folders)) {
                prefs.edit()
                        .putStringSet(EXCLUDED_FOLDERS_SAVE_KEY, new HashSet<>(folders))
                        .apply();

                onExcludeFoldersObserver.onNext(Irrelevant.INSTANCE);
            }
        });
    }

    private boolean isEqualToOldFolders(List<String> newFolders) {
        Set<String> oldFolders = requireNonNull(prefs.getStringSet(EXCLUDED_FOLDERS_SAVE_KEY, new HashSet<>()));

        if (oldFolders.size() != newFolders.size()) {
            return false;
        }

        return oldFolders.containsAll(newFolders);
    }

    @Override
    public List<String> getExcludedFolders() {
        Set<String> savedSet = prefs.getStringSet(EXCLUDED_FOLDERS_SAVE_KEY, new HashSet<>());
        return Collections.unmodifiableList(new ArrayList<>(requireNonNull(savedSet)));
    }

}
