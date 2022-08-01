package com.parabola.newtone.ui.fragment.settings

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.CornerFamily
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.databinding.FragmentTrackItemDisplaySettingBinding
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver
import com.parabola.newtone.ui.router.MainRouter
import com.parabola.newtone.util.AndroidTool.convertDpToPixel
import com.parabola.newtone.util.SeekBarChangeAdapter
import javax.inject.Inject


private const val COVER_SIZE_MIN = 32
private const val TEXT_SIZE_MIN = 12
private const val BORDER_PADDING_MIN = 8


class TrackItemDisplaySettingFragment : BaseSwipeToBackFragment() {

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    @Inject
    lateinit var router: MainRouter

    private var _binding: FragmentTrackItemDisplaySettingBinding? = null
    private val binding get() = _binding!!


    companion object {
        fun newInstance() = TrackItemDisplaySettingFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentTrackItemDisplaySettingBinding.inflate(inflater, container, false)
        rootBinding.container.addView(binding.root)

        val appComponent = (requireActivity().application as MainApplication).appComponent
        appComponent.inject(this)

        rootBinding.main.setText(R.string.track_item_display_setting_screen_title)
        rootBinding.additionalInfo.visibility = View.GONE
        rootBinding.otherInfo.visibility = View.GONE

        binding.apply {
            trackHolder.cover.setImageResource(R.drawable.album_default)
            trackHolder.trackTitle.setText(R.string.default_track_title)
            trackHolder.durationTxt.setText(R.string.default_duration)
            trackHolder.root.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorListItemDefaultBackground
                )
            )

            coverSizeSeekBar.setOnSeekBarChangeListener(object : SeekBarChangeAdapter() {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    refreshCoverSize(progress)
                }
            })
            coverCornersSeekBar.setOnSeekBarChangeListener(object : SeekBarChangeAdapter() {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    refreshCoverCorners(progress)
                }
            })
            textSizeSeekBar.setOnSeekBarChangeListener(object : SeekBarChangeAdapter() {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    refreshTextSize(progress)
                }
            })
            borderPaddingSeekBar.setOnSeekBarChangeListener(object : SeekBarChangeAdapter() {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    refreshBorderPadding(progress)
                }
            })
            showCoverSwitch.setOnCheckedChangeListener { _, isChecked ->
                refreshCoverShow(isChecked)
                onChangeCoverShow(isChecked)
            }
            showAlbumTitleSwitch.setOnCheckedChangeListener { _, isChecked ->
                refreshAlbumTitleShow(isChecked)
            }

            val trackItemView = viewSettingsInteractor.trackItemViewSettings
            textSizeSeekBar.progress = trackItemView.textSize - TEXT_SIZE_MIN
            borderPaddingSeekBar.progress =
                trackItemView.borderPadding - BORDER_PADDING_MIN
            showAlbumTitleSwitch.isChecked = trackItemView.isAlbumTitleShows
            showCoverSwitch.isChecked = trackItemView.isCoverShows
            coverSizeSeekBar.progress = trackItemView.coverSize - COVER_SIZE_MIN
            coverCornersSeekBar.progress = trackItemView.coverCornersRadius

            setDefault.setOnClickListener { onClickSetDefault() }
            showCoverBar.setOnClickListener { showCoverSwitch.toggle() }
            showAlbumTitleBar.setOnClickListener { showAlbumTitleSwitch.toggle() }

            refreshTextSize(textSizeSeekBar.progress)
            refreshBorderPadding(borderPaddingSeekBar.progress)
            refreshAlbumTitleShow(showAlbumTitleSwitch.isChecked)
            val isShowCoverChecked = showCoverSwitch.isChecked
            refreshCoverShow(isShowCoverChecked)
            onChangeCoverShow(isShowCoverChecked)
            refreshCoverSize(coverSizeSeekBar.progress)
            refreshCoverCorners(coverCornersSeekBar.progress)
        }

        return root
    }

    override fun onDestroy() {
        if (isRemoving)
            onFinishing()
        _binding = null
        super.onDestroy()
    }

    private fun onFinishing() {
        //сохраняем состояние
        val trackItemView = TrackItemView(
            binding.textSizeSeekBar.progress + TEXT_SIZE_MIN,
            binding.borderPaddingSeekBar.progress + BORDER_PADDING_MIN,
            binding.showAlbumTitleSwitch.isChecked,
            binding.showCoverSwitch.isChecked,
            binding.coverSizeSeekBar.progress + COVER_SIZE_MIN,
            binding.coverCornersSeekBar.progress
        )
        viewSettingsInteractor.setTrackItemView(trackItemView)
    }


    private fun onClickSetDefault() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.reset_settings_dialog_title)
            .setMessage(R.string.track_item_reset_settings_dialog_message)
            .setPositiveButton(R.string.dialog_reset) { _, _ ->
                binding.apply {
                    coverSizeSeekBar.progress = 40 - COVER_SIZE_MIN
                    coverCornersSeekBar.progress = 4
                    showAlbumTitleSwitch.isChecked = false
                    textSizeSeekBar.progress = 16 - TEXT_SIZE_MIN
                    borderPaddingSeekBar.progress = 16 - BORDER_PADDING_MIN
                    showCoverSwitch.isChecked = true
                }
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
        lifecycle.addObserver(DialogDismissLifecycleObserver(dialog))
    }

    override fun onClickBackButton() {
        router.goBack()
    }

    private fun refreshCoverSize(progress: Int) {
        val coverSizeDp = progress + COVER_SIZE_MIN
        binding.coverSizeValue.text = coverSizeDp.toString()
        val coverSizePx =
            convertDpToPixel(coverSizeDp.toFloat(), requireContext()).toInt()
        val params = binding.trackHolder.cover.layoutParams
        params.width = coverSizePx
        params.height = coverSizePx
        binding.trackHolder.cover.layoutParams = params
    }

    private fun refreshCoverCorners(progress: Int) {
        val cornerSizePx = convertDpToPixel(progress.toFloat(), requireContext())

        binding.trackHolder.cover.shapeAppearanceModel =
            binding.trackHolder.cover.shapeAppearanceModel.toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, cornerSizePx)
                .build()

        binding.coverCornersValue.text = progress.toString()
    }

    private fun refreshTextSize(progress: Int) {
        val textSizeSp = progress + TEXT_SIZE_MIN
        binding.textSizeValue.text = textSizeSp.toString()
        binding.trackHolder.trackTitle.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            textSizeSp.toFloat()
        )
        binding.trackHolder.additionalTrackInfo.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            (textSizeSp - 2).toFloat()
        )
        binding.trackHolder.durationTxt.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            (textSizeSp - 4).toFloat()
        )
    }

    private fun refreshCoverShow(show: Boolean) {
        binding.trackHolder.cover.visibility =
            if (show) View.VISIBLE else View.GONE
    }

    private fun refreshAlbumTitleShow(show: Boolean) {
        val defaultArtist = getString(R.string.default_artist)
        val defaultAlbum = getString(R.string.default_album)
        val text = if (show) getString(
            R.string.track_item_artist_with_album,
            defaultArtist,
            defaultAlbum
        ) else defaultArtist
        binding.trackHolder.additionalTrackInfo.text = text
    }

    private fun onChangeCoverShow(show: Boolean) {
        binding.apply {
            coverSizeBar.isEnabled = show
            coverSizeSeekBar.isEnabled = show
            coverCornersBar.isEnabled = show
            coverCornersSeekBar.isEnabled = show
        }
    }

    private fun refreshBorderPadding(progress: Int) {
        val paddingDp = progress + BORDER_PADDING_MIN
        val paddingPx = convertDpToPixel(paddingDp.toFloat(), requireContext()).toInt()
        binding.borderPaddingValue.text = paddingDp.toString()
        binding.trackHolder.trackContent.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
    }

}
