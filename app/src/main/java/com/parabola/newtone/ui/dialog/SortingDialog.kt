package com.parabola.newtone.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.radiobutton.MaterialRadioButton
import com.parabola.domain.repository.AlbumRepository
import com.parabola.domain.repository.ArtistRepository
import com.parabola.domain.repository.SortingRepository
import com.parabola.domain.repository.TrackRepository
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.databinding.DialogSortingBinding
import javax.inject.Inject


//    КОНСТАНТЫ СКОПИРОВАНЫ ИЗ файла sorting_id.xml
private const val ALL_TRACKS_BY_TITLE_CB_ID = 10
private const val ALL_TRACKS_BY_ARTIST_CB_ID = 20
private const val ALL_TRACKS_BY_DURATION_CB_ID = 30
private const val ALL_TRACKS_BY_DATE_ADDING_CB_ID = 40
private const val ALL_TRACKS_BY_ALBUM_POS_CB_ID = 50
private const val ALL_ALBUMS_BY_TITLE_CB = 10
private const val ALL_ALBUMS_BY_ARTIST_CB = 20
private const val ALL_ALBUMS_BY_YEAR_CB = 30
private const val ALL_ARTISTS_BY_NAME_CB = 10
private const val ALL_ARTISTS_BY_TRACKS_COUNT_CB = 20


private const val SORTING_LIST_TYPE_PARAM = "SORTING_LIST_TYPE_PARAM"


class SortingDialog : DialogFragment() {

    @Inject
    lateinit var sortingRepo: SortingRepository

    private lateinit var sortingRadioGroup: RadioGroup
    private lateinit var reverseCheckBox: CheckBox


    private lateinit var sortingListType: String


    companion object {
        const val ALL_TRACKS_SORTING = "ALL_TRACKS_SORTING"
        const val ALBUM_TRACKS_SORTING = "ALBUM_TRACKS_SORTING"
        const val ARTIST_TRACKS_SORTING = "ARTIST_TRACKS_SORTING"
        const val FOLDER_TRACKS_SORTING = "FOLDER_TRACKS_SORTING"
        const val ALL_ALBUMS_SORTING = "ALL_ALBUMS_SORTING"
        const val ARTIST_ALBUMS_SORTING = "ARTIST_ALBUMS_SORTING"
        const val ALL_ARTISTS_SORTING = "ALL_ARTISTS_SORTING"


        fun newInstance(sortingList: String) = SortingDialog().apply {
            arguments = bundleOf(SORTING_LIST_TYPE_PARAM to sortingList)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sortingListType = requireArguments().getString(SORTING_LIST_TYPE_PARAM)!!

        val appComponent = (requireActivity().application as MainApplication).appComponent
        appComponent.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogSortingBinding
            .inflate(LayoutInflater.from(requireContext()))

        sortingRadioGroup = binding.sortingRadio
        reverseCheckBox = binding.reverseCheckBox

        showRadioButtons(sortingListType)

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.sort_title)
            .setView(binding.root)
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.dialog_ok) { _, _ -> onClickOk() }
            .create()
    }

    private fun onClickOk() {
        val checkedRadioButtonId = sortingRadioGroup.checkedRadioButtonId
        val isReverse = reverseCheckBox.isChecked
        when (sortingListType) {
            ALL_TRACKS_SORTING, ALBUM_TRACKS_SORTING, ARTIST_TRACKS_SORTING, FOLDER_TRACKS_SORTING ->
                saveNewSortingForTracks(checkedRadioButtonId, isReverse)
            ALL_ALBUMS_SORTING, ARTIST_ALBUMS_SORTING ->
                saveNewSortingForAlbums(checkedRadioButtonId, isReverse)
            ALL_ARTISTS_SORTING ->
                saveNewSortingForAllArtists(checkedRadioButtonId, isReverse)
            else -> throw IllegalStateException()
        }
        dismiss()
    }

    private fun saveNewSortingForAllArtists(checkedRadioButtonId: Int, isReverse: Boolean) {
        val sorting = when (checkedRadioButtonId) {
            ALL_ARTISTS_BY_NAME_CB -> if (!isReverse) ArtistRepository.Sorting.BY_NAME else ArtistRepository.Sorting.BY_NAME_DESC
            ALL_ARTISTS_BY_TRACKS_COUNT_CB -> if (!isReverse) ArtistRepository.Sorting.BY_TRACKS_COUNT else ArtistRepository.Sorting.BY_TRACKS_COUNT_DESC
            else -> throw IllegalStateException()
        }
        sortingRepo.setAllArtistsSorting(sorting)
    }


    private fun saveNewSortingForTracks(selectedRadioButtonId: Int, isReverse: Boolean) {
        val sorting = when (selectedRadioButtonId) {
            ALL_TRACKS_BY_TITLE_CB_ID -> if (!isReverse) TrackRepository.Sorting.BY_TITLE else TrackRepository.Sorting.BY_TITLE_DESC
            ALL_TRACKS_BY_ARTIST_CB_ID -> if (!isReverse) TrackRepository.Sorting.BY_ARTIST else TrackRepository.Sorting.BY_ARTIST_DESC
            ALL_TRACKS_BY_DURATION_CB_ID -> if (!isReverse) TrackRepository.Sorting.BY_DURATION else TrackRepository.Sorting.BY_DURATION_DESC
            ALL_TRACKS_BY_DATE_ADDING_CB_ID -> if (!isReverse) TrackRepository.Sorting.BY_DATE_ADDING else TrackRepository.Sorting.BY_DATE_ADDING_DESC
            ALL_TRACKS_BY_ALBUM_POS_CB_ID -> if (!isReverse) TrackRepository.Sorting.BY_ALBUM_POSITION else TrackRepository.Sorting.BY_ALBUM_POSITION_DESC
            else -> throw IllegalStateException()
        }
        when (sortingListType) {
            ALL_TRACKS_SORTING -> sortingRepo.setAllTracksSorting(sorting)
            ALBUM_TRACKS_SORTING -> sortingRepo.setAlbumTracksSorting(sorting)
            ARTIST_TRACKS_SORTING -> sortingRepo.setArtistTracksSorting(sorting)
            FOLDER_TRACKS_SORTING -> sortingRepo.setFolderTracksSorting(sorting)
            else -> throw IllegalStateException()
        }
    }


    private fun saveNewSortingForAlbums(selectedRadioButtonId: Int, isReverse: Boolean) {
        val sorting = when (selectedRadioButtonId) {
            ALL_ALBUMS_BY_TITLE_CB -> if (!isReverse) AlbumRepository.Sorting.BY_TITLE else AlbumRepository.Sorting.BY_TITLE_DESC
            ALL_ALBUMS_BY_ARTIST_CB -> if (!isReverse) AlbumRepository.Sorting.BY_ARTIST else AlbumRepository.Sorting.BY_ARTIST_DESC
            ALL_ALBUMS_BY_YEAR_CB -> if (!isReverse) AlbumRepository.Sorting.BY_YEAR else AlbumRepository.Sorting.BY_YEAR_DESC
            else -> throw IllegalStateException()
        }
        when (sortingListType) {
            ALL_ALBUMS_SORTING -> sortingRepo.setAllAlbumsSorting(sorting)
            ARTIST_ALBUMS_SORTING -> sortingRepo.setArtistAlbumsSorting(sorting)
            else -> throw IllegalStateException()
        }
    }

    private val radioButtonLayoutParams = RadioGroup.LayoutParams(
        RadioGroup.LayoutParams.MATCH_PARENT,
        RadioGroup.LayoutParams.WRAP_CONTENT
    )

    private fun showRadioButtons(sortingListType: String) {
        val sortStrings = getSortStrings(sortingListType)
        val sortIds = getSortIds(sortingListType)

        val rb = Array<RadioButton>(sortIds.size) { index ->
            MaterialRadioButton(requireContext()).apply {
                layoutParams = radioButtonLayoutParams
                text = sortStrings[index]
                id = sortIds[index]
            }
        }
        rb.forEach { sortingRadioGroup.addView(it) }

        when (sortingListType) {
            ALL_TRACKS_SORTING, ALBUM_TRACKS_SORTING, ARTIST_TRACKS_SORTING, FOLDER_TRACKS_SORTING ->
                setCheckedForTracks(rb)
            ALL_ALBUMS_SORTING, ARTIST_ALBUMS_SORTING ->
                setCheckedForAlbums(rb)
            ALL_ARTISTS_SORTING ->
                setCheckedForArtists(rb)
            else -> throw IllegalStateException()
        }
    }

    private fun getSortStrings(sortingListType: String): Array<String> {
        return when (sortingListType) {
            ALL_TRACKS_SORTING, ALBUM_TRACKS_SORTING, ARTIST_TRACKS_SORTING, FOLDER_TRACKS_SORTING ->
                resources.getStringArray(R.array.sort_tracks)
            ALL_ALBUMS_SORTING, ARTIST_ALBUMS_SORTING ->
                resources.getStringArray(R.array.sort_albums)
            ALL_ARTISTS_SORTING ->
                resources.getStringArray(R.array.sort_artists)
            else ->
                throw IllegalStateException()
        }
    }

    private fun getSortIds(sortingListType: String): IntArray {
        return when (sortingListType) {
            ALL_TRACKS_SORTING, ALBUM_TRACKS_SORTING, ARTIST_TRACKS_SORTING, FOLDER_TRACKS_SORTING ->
                resources.getIntArray(R.array.sort_tracks_id)
            ALL_ALBUMS_SORTING, ARTIST_ALBUMS_SORTING ->
                resources.getIntArray(R.array.sort_albums_id)
            ALL_ARTISTS_SORTING ->
                resources.getIntArray(R.array.sort_artists_id)
            else ->
                throw IllegalStateException()
        }
    }

    private fun setCheckedForTracks(rb: Array<RadioButton>) {
        val sorting: TrackRepository.Sorting
        when (sortingListType) {
            ALL_TRACKS_SORTING -> {
                sorting = sortingRepo.allTracksSorting()
                //для списка всех треков убираем сортировку по номеру в альбоме
                rb[4].visibility = View.GONE
            }
            ALBUM_TRACKS_SORTING -> {
                sorting = sortingRepo.albumTracksSorting()
                //для списка треков альбома убираем сортировку по артисту
                rb[1].visibility = View.GONE
            }
            ARTIST_TRACKS_SORTING -> {
                sorting = sortingRepo.artistTracksSorting()
                //для списка треков артиста убираем сортировку по артисту, и номеру в альбоме
                rb[1].visibility = View.GONE
                rb[4].visibility = View.GONE
            }
            FOLDER_TRACKS_SORTING -> sorting = sortingRepo.folderTracksSorting()
            else -> throw IllegalStateException()
        }

        when (sorting) {
            TrackRepository.Sorting.BY_TITLE -> {
                rb[0].isChecked = true
                reverseCheckBox.isChecked = false
            }
            TrackRepository.Sorting.BY_TITLE_DESC -> {
                rb[0].isChecked = true
                reverseCheckBox.isChecked = true
            }
            TrackRepository.Sorting.BY_ARTIST -> {
                rb[1].isChecked = true
                reverseCheckBox.isChecked = false
            }
            TrackRepository.Sorting.BY_ARTIST_DESC -> {
                rb[1].isChecked = true
                reverseCheckBox.isChecked = true
            }
            TrackRepository.Sorting.BY_DURATION -> {
                rb[2].isChecked = true
                reverseCheckBox.isChecked = false
            }
            TrackRepository.Sorting.BY_DURATION_DESC -> {
                rb[2].isChecked = true
                reverseCheckBox.isChecked = true
            }
            TrackRepository.Sorting.BY_DATE_ADDING -> {
                rb[3].isChecked = true
                reverseCheckBox.isChecked = false
            }
            TrackRepository.Sorting.BY_DATE_ADDING_DESC -> {
                rb[3].isChecked = true
                reverseCheckBox.isChecked = true
            }
            TrackRepository.Sorting.BY_ALBUM_POSITION -> {
                rb[4].isChecked = true
                reverseCheckBox.isChecked = false
            }
            TrackRepository.Sorting.BY_ALBUM_POSITION_DESC -> {
                rb[4].isChecked = true
                reverseCheckBox.isChecked = true
            }
            else -> throw IllegalStateException()
        }
    }

    private fun setCheckedForAlbums(rb: Array<RadioButton>) {
        val sorting: AlbumRepository.Sorting
        when (sortingListType) {
            ALL_ALBUMS_SORTING ->
                sorting = sortingRepo.allAlbumsSorting()
            ARTIST_ALBUMS_SORTING -> {
                sorting = sortingRepo.artistAlbumsSorting()
                rb[1].visibility = View.GONE //Скрываем сортировку по артисту
            }
            else ->
                throw IllegalStateException()
        }

        when (sorting) {
            AlbumRepository.Sorting.BY_TITLE -> {
                rb[0].isChecked = true
                reverseCheckBox.isChecked = false
            }
            AlbumRepository.Sorting.BY_TITLE_DESC -> {
                rb[0].isChecked = true
                reverseCheckBox.isChecked = true
            }
            AlbumRepository.Sorting.BY_ARTIST -> {
                rb[1].isChecked = true
                reverseCheckBox.isChecked = false
            }
            AlbumRepository.Sorting.BY_ARTIST_DESC -> {
                rb[1].isChecked = true
                reverseCheckBox.isChecked = true
            }
            AlbumRepository.Sorting.BY_YEAR -> {
                rb[2].isChecked = true
                reverseCheckBox.isChecked = false
            }
            AlbumRepository.Sorting.BY_YEAR_DESC -> {
                rb[2].isChecked = true
                reverseCheckBox.isChecked = true
            }
            else -> throw IllegalStateException()
        }
    }

    private fun setCheckedForArtists(rb: Array<RadioButton>) {
        when (sortingRepo.allArtistsSorting()) {
            ArtistRepository.Sorting.BY_NAME -> {
                rb[0].isChecked = true
                reverseCheckBox.isChecked = false
            }
            ArtistRepository.Sorting.BY_NAME_DESC -> {
                rb[0].isChecked = true
                reverseCheckBox.isChecked = true
            }
            ArtistRepository.Sorting.BY_TRACKS_COUNT -> {
                rb[1].isChecked = true
                reverseCheckBox.isChecked = false
            }
            ArtistRepository.Sorting.BY_TRACKS_COUNT_DESC -> {
                rb[1].isChecked = true
                reverseCheckBox.isChecked = true
            }
            else -> throw IllegalStateException()
        }
    }

}
