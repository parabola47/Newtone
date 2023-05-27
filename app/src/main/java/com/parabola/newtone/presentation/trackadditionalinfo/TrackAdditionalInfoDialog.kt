package com.parabola.newtone.presentation.trackadditionalinfo

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.os.bundleOf
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.CornerFamily
import com.parabola.domain.model.Track
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.databinding.DialogTrackAdditionInfoBinding
import com.parabola.newtone.util.TimeFormatterTool.formatMillisecondsToMinutes
import moxy.MvpAppCompatDialogFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter


private const val TRACK_ID_BUNDLE_KEY = "track id"


class TrackAdditionalInfoDialog : MvpAppCompatDialogFragment(),
    TrackAdditionalInfoView {

    @InjectPresenter
    lateinit var presenter: TrackAdditionalInfoPresenter

    private var _binding: DialogTrackAdditionInfoBinding? = null
    private val binding get() = _binding!!


    companion object {
        fun newInstance(trackId: Int) = TrackAdditionalInfoDialog().apply {
            arguments = bundleOf(TRACK_ID_BUNDLE_KEY to trackId)
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogTrackAdditionInfoBinding
            .inflate(LayoutInflater.from(requireContext()))

        binding.albumCover.shapeAppearanceModel =
            binding.albumCover.shapeAppearanceModel.toBuilder()
                .setAllCorners(
                    CornerFamily.ROUNDED,
                    resources.getDimension(R.dimen.track_add_info_dialog_cover_corner_size)
                )
                .build()

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    @ProvidePresenter
    fun providePresenter(): TrackAdditionalInfoPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        val trackId = requireArguments().getInt(TRACK_ID_BUNDLE_KEY)

        return TrackAdditionalInfoPresenter(appComponent, trackId)
    }


    override fun setTrack(track: Track) {
        binding.apply {
            artist.text = track.artistName
            album.text = track.albumTitle
            albumPosition.text = track.positionInCd.toString()
            title.text = track.title

            duration.text = formatMillisecondsToMinutes(track.durationMs)

            if (track.genreName.isEmpty()) genreWrapper.visibility = View.GONE
            else genre.text = track.genreName

            year.text = track.year.toString()
            filepath.text = track.filePath

            fileSize.text =
                getString(R.string.track_add_info_file_size_format, track.fileSizeKilobytes)
            bitrate.text =
                getString(R.string.track_add_info_bitrate_format, track.bitrate)
            sampleRate.text =
                getString(R.string.track_add_info_sample_rate_format, track.sampleRate)

            val artImage = track.getArtImage<Bitmap>()
            if (artImage != null) albumCover.setImageBitmap(artImage)
            else albumCover.setImageResource(R.drawable.album_default)
        }
    }

    override fun closeScreen() {
        dismiss()
    }

}
