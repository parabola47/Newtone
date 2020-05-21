package com.parabola.newtone.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.parabola.domain.repository.AlbumRepository;
import com.parabola.domain.repository.ArtistRepository;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class SortingDialog extends DialogFragment {

    public static final String ALL_TRACKS_SORTING = "ALL_TRACKS_SORTING";
    public static final String ALBUM_TRACKS_SORTING = "ALBUM_TRACKS_SORTING";
    public static final String ARTIST_TRACKS_SORTING = "ARTIST_TRACKS_SORTING";
    public static final String FOLDER_TRACKS_SORTING = "FOLDER_TRACKS_SORTING";

    public static final String ALL_ALBUMS_SORTING = "ALL_ALBUMS_SORTING";
    public static final String ARTIST_ALBUMS_SORTING = "ARTIST_ALBUMS_SORTING";

    public static final String ALL_ARTISTS_SORTING = "ALL_ARTISTS_SORTING";


    @Inject SortingRepository sortingRepo;


    @BindView(R.id.sorting_radio) RadioGroup sortingRadioGroup;
    @BindView(R.id.reverseCheckBox) CheckBox reverseCheckBox;


    private static final String SORTING_LIST_TYPE_PARAM = "SORTING_LIST_TYPE_PARAM";
    private String sortingListType;

    public static SortingDialog newInstance(String sortingList) {
        Bundle args = new Bundle();
        args.putString(SORTING_LIST_TYPE_PARAM, sortingList);

        SortingDialog fragment = new SortingDialog();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sortingListType = requireArguments().getString(SORTING_LIST_TYPE_PARAM);

        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        appComponent.inject(this);
    }


    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View customView = View.inflate(requireContext(), R.layout.dialog_sorting, null);
        ButterKnife.bind(this, customView);
        showRadioButtons(sortingListType);

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.sort_title)
                .setView(customView)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.dialog_ok, (d, w) ->
                        onClickOk())
                .create();
    }


    private void onClickOk() {
        int checkedRadioButtonId = sortingRadioGroup.getCheckedRadioButtonId();
        boolean isReverse = reverseCheckBox.isChecked();
        switch (sortingListType) {
            case ALL_TRACKS_SORTING:
            case ALBUM_TRACKS_SORTING:
            case ARTIST_TRACKS_SORTING:
            case FOLDER_TRACKS_SORTING:
                saveNewSortingForTracks(checkedRadioButtonId, isReverse);
                break;
            case ALL_ALBUMS_SORTING:
            case ARTIST_ALBUMS_SORTING:
                saveNewSortingForAlbums(checkedRadioButtonId, isReverse);
                break;
            case ALL_ARTISTS_SORTING:
                saveNewSortingForAllArtists(checkedRadioButtonId, isReverse);
                break;
            default: throw new IllegalStateException();
        }
        dismiss();
    }

    private void saveNewSortingForAllArtists(int checkedRadioButtonId, boolean isReverse) {
        ArtistRepository.Sorting sorting;
        switch (checkedRadioButtonId) {
            case ALL_ARTISTS_BY_NAME_CB:
                if (!isReverse) sorting = ArtistRepository.Sorting.BY_NAME;
                else sorting = ArtistRepository.Sorting.BY_NAME_DESC;
                break;
            case ALL_ARTISTS_BY_TRACKS_COUNT_CB:
                if (!isReverse) sorting = ArtistRepository.Sorting.BY_TRACKS_COUNT;
                else sorting = ArtistRepository.Sorting.BY_TRACKS_COUNT_DESC;
                break;
            default: throw new IllegalStateException();
        }
        sortingRepo.setAllArtistsSorting(sorting);
    }

    private void saveNewSortingForTracks(int selectedRadioButtonId, boolean isReverse) {
        TrackRepository.Sorting sorting;
        switch (selectedRadioButtonId) {
            case ALL_TRACKS_BY_TITLE_CB_ID:
                if (!isReverse) sorting = TrackRepository.Sorting.BY_TITLE;
                else sorting = TrackRepository.Sorting.BY_TITLE_DESC;
                break;
            case ALL_TRACKS_BY_ARTIST_CB_ID:
                if (!isReverse) sorting = TrackRepository.Sorting.BY_ARTIST;
                else sorting = TrackRepository.Sorting.BY_ARTIST_DESC;
                break;
            case ALL_TRACKS_BY_DURATION_CB_ID:
                if (!isReverse) sorting = TrackRepository.Sorting.BY_DURATION;
                else sorting = TrackRepository.Sorting.BY_DURATION_DESC;
                break;
            case ALL_TRACKS_BY_DATE_ADDING_CB_ID:
                if (!isReverse) sorting = TrackRepository.Sorting.BY_DATE_ADDING;
                else sorting = TrackRepository.Sorting.BY_DATE_ADDING_DESC;
                break;
            case ALL_TRACKS_BY_ALBUM_POS_CB_ID:
                if (!isReverse) sorting = TrackRepository.Sorting.BY_ALBUM_POSITION;
                else sorting = TrackRepository.Sorting.BY_ALBUM_POSITION_DESC;
                break;
            default: throw new IllegalStateException();
        }
        switch (sortingListType) {
            case ALL_TRACKS_SORTING:
                sortingRepo.setAllTracksSorting(sorting);
                break;
            case ALBUM_TRACKS_SORTING:
                sortingRepo.setAlbumTracksSorting(sorting);
                break;
            case ARTIST_TRACKS_SORTING:
                sortingRepo.setArtistTracksSorting(sorting);
                break;
            case FOLDER_TRACKS_SORTING:
                sortingRepo.setFolderTracksSorting(sorting);
                break;
            default: throw new IllegalStateException();
        }
    }

    private void saveNewSortingForAlbums(int selectedRadioButtonId, boolean isReverse) {
        AlbumRepository.Sorting sorting;
        switch (selectedRadioButtonId) {
            case ALL_ALBUMS_BY_TITLE_CB:
                if (!isReverse) sorting = AlbumRepository.Sorting.BY_TITLE;
                else sorting = AlbumRepository.Sorting.BY_TITLE_DESC;
                break;
            case ALL_ALBUMS_BY_ARTIST_CB:
                if (!isReverse) sorting = AlbumRepository.Sorting.BY_ARTIST;
                else sorting = AlbumRepository.Sorting.BY_ARTIST_DESC;
                break;
            case ALL_ALBUMS_BY_YEAR_CB:
                if (!isReverse) sorting = AlbumRepository.Sorting.BY_YEAR;
                else sorting = AlbumRepository.Sorting.BY_YEAR_DESC;
                break;
            default: throw new IllegalStateException();
        }

        switch (sortingListType) {
            case ALL_ALBUMS_SORTING:
                sortingRepo.setAllAlbumsSorting(sorting);
                break;
            case ARTIST_ALBUMS_SORTING:
                sortingRepo.setArtistAlbumsSorting(sorting);
                break;
            default: throw new IllegalStateException();
        }
    }


    private final RadioGroup.LayoutParams radioButtonLayoutParams
            = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);

    private void showRadioButtons(String sortingListType) {
        String[] sortStrings = getSortStrings(sortingListType);
        int[] sortIds = getSortIds(sortingListType);
        RadioButton[] rb = new RadioButton[sortIds.length];

        for (int i = 0; i < rb.length; i++) {
            rb[i] = new MaterialRadioButton(requireContext());
            rb[i].setLayoutParams(radioButtonLayoutParams);
            rb[i].setText(sortStrings[i]);
            rb[i].setId(sortIds[i]);

            sortingRadioGroup.addView(rb[i]);
        }
        switch (sortingListType) {
            case ALL_TRACKS_SORTING:
            case ALBUM_TRACKS_SORTING:
            case ARTIST_TRACKS_SORTING:
            case FOLDER_TRACKS_SORTING:
                setCheckedForTracks(rb);
                break;
            case ALL_ALBUMS_SORTING:
            case ARTIST_ALBUMS_SORTING:
                setCheckedForAlbums(rb);
                break;
            case ALL_ARTISTS_SORTING: setCheckedForArtists(rb);
                break;
            default: throw new IllegalStateException();
        }
    }

    private String[] getSortStrings(String sortingListType) {
        switch (sortingListType) {
            case ALL_TRACKS_SORTING:
            case ALBUM_TRACKS_SORTING:
            case ARTIST_TRACKS_SORTING:
            case FOLDER_TRACKS_SORTING:
                return getResources().getStringArray(R.array.sort_tracks);
            case ALL_ALBUMS_SORTING:
            case ARTIST_ALBUMS_SORTING:
                return getResources().getStringArray(R.array.sort_albums);
            case ALL_ARTISTS_SORTING:
                return getResources().getStringArray(R.array.sort_artists);
            default: throw new IllegalStateException();
        }
    }

    private int[] getSortIds(String sortingListType) {
        switch (sortingListType) {
            case ALL_TRACKS_SORTING:
            case ALBUM_TRACKS_SORTING:
            case ARTIST_TRACKS_SORTING:
            case FOLDER_TRACKS_SORTING:
                return getResources().getIntArray(R.array.sort_tracks_id);
            case ALL_ALBUMS_SORTING:
            case ARTIST_ALBUMS_SORTING:
                return getResources().getIntArray(R.array.sort_albums_id);
            case ALL_ARTISTS_SORTING:
                return getResources().getIntArray(R.array.sort_artists_id);
            default: throw new IllegalStateException();
        }
    }

    //    КОНСТАНТЫ СКОПИРОВАНЫ ИЗ файла sorting_id.xml
    private static final int ALL_TRACKS_BY_TITLE_CB_ID = 10;
    private static final int ALL_TRACKS_BY_ARTIST_CB_ID = 20;
    private static final int ALL_TRACKS_BY_DURATION_CB_ID = 30;
    private static final int ALL_TRACKS_BY_DATE_ADDING_CB_ID = 40;
    private static final int ALL_TRACKS_BY_ALBUM_POS_CB_ID = 50;

    private static final int ALL_ALBUMS_BY_TITLE_CB = 10;
    private static final int ALL_ALBUMS_BY_ARTIST_CB = 20;
    private static final int ALL_ALBUMS_BY_YEAR_CB = 30;

    private static final int ALL_ARTISTS_BY_NAME_CB = 10;
    private static final int ALL_ARTISTS_BY_TRACKS_COUNT_CB = 20;


    private void setCheckedForTracks(RadioButton[] rb) {
        TrackRepository.Sorting sorting;
        switch (sortingListType) {
            case ALL_TRACKS_SORTING:
                sorting = sortingRepo.allTracksSorting();
                rb[4].setVisibility(View.GONE);     //для списка всех треков убираем сортировку по номеру в альбоме
                break;
            case ALBUM_TRACKS_SORTING:
                sorting = sortingRepo.albumTracksSorting();
                rb[1].setVisibility(View.GONE);     //для списка треков альбома убираем сортировку по артисту,
                break;
            case ARTIST_TRACKS_SORTING:
                sorting = sortingRepo.artistTracksSorting();
                rb[1].setVisibility(View.GONE);     //для списка треков артиста убираем сортировку по артисту,
                rb[4].setVisibility(View.GONE);     //и номеру в альбоме
                break;
            case FOLDER_TRACKS_SORTING:
                sorting = sortingRepo.folderTracksSorting();
                break;
            default:
                throw new IllegalStateException();
        }

        switch (sorting) {
            case BY_TITLE: rb[0].setChecked(true); reverseCheckBox.setChecked(false); return;
            case BY_TITLE_DESC: rb[0].setChecked(true); reverseCheckBox.setChecked(true); return;
            case BY_ARTIST: rb[1].setChecked(true); reverseCheckBox.setChecked(false); return;
            case BY_ARTIST_DESC: rb[1].setChecked(true); reverseCheckBox.setChecked(true); return;
            case BY_DURATION: rb[2].setChecked(true); reverseCheckBox.setChecked(false); return;
            case BY_DURATION_DESC: rb[2].setChecked(true); reverseCheckBox.setChecked(true); return;
            case BY_DATE_ADDING: rb[3].setChecked(true); reverseCheckBox.setChecked(false); return;
            case BY_DATE_ADDING_DESC: rb[3].setChecked(true); reverseCheckBox.setChecked(true); return;
            case BY_ALBUM_POSITION: rb[4].setChecked(true); reverseCheckBox.setChecked(false); return;
            case BY_ALBUM_POSITION_DESC: rb[4].setChecked(true); reverseCheckBox.setChecked(true); return;
            default: throw new IllegalStateException();
        }
    }

    private void setCheckedForAlbums(RadioButton[] rb) {
        AlbumRepository.Sorting sorting;
        if (sortingListType.equals(ALL_ALBUMS_SORTING)) {
            sorting = sortingRepo.allAlbumsSorting();
        } else if (sortingListType.equals(ARTIST_ALBUMS_SORTING)) {
            sorting = sortingRepo.artistAlbumsSorting();
            rb[1].setVisibility(View.GONE);     //Скрываем сортировку по артисту
        } else {
            throw new IllegalStateException();
        }

        switch (sorting) {
            case BY_TITLE: rb[0].setChecked(true); reverseCheckBox.setChecked(false); break;
            case BY_TITLE_DESC: rb[0].setChecked(true); reverseCheckBox.setChecked(true); break;
            case BY_ARTIST: rb[1].setChecked(true); reverseCheckBox.setChecked(false); break;
            case BY_ARTIST_DESC: rb[1].setChecked(true); reverseCheckBox.setChecked(true); break;
            case BY_YEAR: rb[2].setChecked(true); reverseCheckBox.setChecked(false); break;
            case BY_YEAR_DESC: rb[2].setChecked(true); reverseCheckBox.setChecked(true); break;
            default: throw new IllegalStateException();
        }
    }

    private void setCheckedForArtists(RadioButton[] rb) {
        switch (sortingRepo.allArtistsSorting()) {
            case BY_NAME: rb[0].setChecked(true); reverseCheckBox.setChecked(false); return;
            case BY_NAME_DESC: rb[0].setChecked(true); reverseCheckBox.setChecked(true); return;
            case BY_TRACKS_COUNT: rb[1].setChecked(true); reverseCheckBox.setChecked(false); return;
            case BY_TRACKS_COUNT_DESC: rb[1].setChecked(true); reverseCheckBox.setChecked(true); return;
            default: throw new IllegalStateException();
        }
    }

}
