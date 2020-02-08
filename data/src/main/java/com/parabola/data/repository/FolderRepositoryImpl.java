package com.parabola.data.repository;

import android.content.ContentResolver;
import android.database.Cursor;

import com.parabola.data.model.FolderData;
import com.parabola.domain.model.Folder;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.FolderRepository;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Audio.AudioColumns.IS_MUSIC;
import static android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
import static android.provider.MediaStore.MediaColumns.DATA;

public final class FolderRepositoryImpl implements FolderRepository {

    private final ContentResolver contentResolver;
    private final TrackRepository trackRepo;
    private final SortingRepository sortingRepo;


    public FolderRepositoryImpl(ContentResolver contentResolver, TrackRepository trackRepo, SortingRepository sortingRepo) {
        this.contentResolver = contentResolver;
        this.trackRepo = trackRepo;
        this.sortingRepo = sortingRepo;
    }

    @Override
    public Single<List<Folder>> getAll() {
        return Single.fromCallable(() -> contentResolver.query(
                EXTERNAL_CONTENT_URI,
                new String[]{DATA},
                IS_MUSIC + "=1", null, null))
                .doAfterSuccess(Cursor::close)
                .map(this::extractFolders)
                .map(folders -> { //сортируем папки по имени
                    Collections.sort(folders, (o1, o2) -> o1.getFolderName().compareToIgnoreCase(o2.getFolderName()));
                    return folders;
                });
    }

    private List<Folder> extractFolders(Cursor cursor) {
        if (cursor.getCount() == 0) {
            return Collections.emptyList();
        }
        cursor.moveToFirst();

        List<String> filePaths = new ArrayList<>(cursor.getCount());
        do {
            filePaths.add(cursor.getString(0));
        } while (cursor.moveToNext());

        Map<String, Long> foldersTrackCount = StreamSupport.stream(filePaths)
                .map(filePath -> filePath.substring(0, filePath.lastIndexOf(File.separator)))
                .collect(Collectors.groupingBy(folderPath -> folderPath, Collectors.counting()));

        List<Folder> result = new ArrayList<>(foldersTrackCount.size());
        for (Map.Entry<String, Long> entry : foldersTrackCount.entrySet()) {
            FolderData folderData = new FolderData();

            folderData.folderPath = entry.getKey();
            folderData.tracksCount = entry.getValue().intValue();
            result.add(folderData);
        }

        return result;
    }

    @Override
    public Single<List<Track>> getTracksByFolder(String folderPath) {
        return Single.fromCallable(() -> getIds(folderPath))
                .flatMap(ids -> trackRepo.getByIds(ids, sortingRepo.folderTracksSorting()));
    }

    private List<Integer> getIds(String folderPath) {
        try (Cursor cursor = contentResolver.query(
                EXTERNAL_CONTENT_URI,
                new String[]{_ID, DATA},
                IS_MUSIC + "=1",
                null, null)) {
            if (cursor == null || cursor.getCount() == 0)
                return Collections.emptyList();

            cursor.moveToFirst();
            List<Integer> ids = new ArrayList<>();

            for (int i = 0; i < cursor.getCount(); i++) {
                if (Track.getFolderPath(cursor.getString(1)).equals(folderPath)) {
                    ids.add(cursor.getInt(0));
                }
                cursor.moveToNext();
            }
            return ids;
        }
    }


}
