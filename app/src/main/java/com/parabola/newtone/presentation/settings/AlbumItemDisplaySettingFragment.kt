package com.parabola.newtone.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.CornerFamily
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView.AlbumViewType
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.databinding.FragmentAlbumItemDisplaySettingBinding
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver
import com.parabola.newtone.presentation.router.MainRouter
import com.parabola.newtone.util.AndroidTool.*
import com.parabola.newtone.util.SeekBarChangeAdapter
import javax.inject.Inject


private const val VIEW_TYPE_BUNDLE_CHECK_POSITION_KEY = "VIEW_TYPE_BUNDLE_CHECK_POSITION"

private const val COVER_SIZE_MIN = 32
private const val TEXT_SIZE_MIN = 12
private const val BORDER_PADDING_MIN = 8


class AlbumItemDisplaySettingFragment : BaseSwipeToBackFragment() {

    private var _binding: FragmentAlbumItemDisplaySettingBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    @Inject
    lateinit var router: MainRouter


    companion object {
        fun newInstance() = AlbumItemDisplaySettingFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentAlbumItemDisplaySettingBinding.inflate(inflater, container, false)
        rootBinding.container.addView(binding.root)

        val appComponent = (requireActivity().application as MainApplication).appComponent
        appComponent.inject(this)

        rootBinding.main.setText(R.string.album_item_display_setting_screen_title)
        rootBinding.additionalInfo.visibility = View.GONE
        rootBinding.otherInfo.visibility = View.GONE

        binding.apply {
            albumListHolder.albumTitle.setText(R.string.default_album)
            albumListHolder.author.setText(R.string.default_artist)
            albumListHolder.tracksCount.setText(R.string.default_album_tracks_count)
            albumListHolder.albumCover.setImageResource(R.drawable.album_default)

            albumGridHolder.albumTitle.setText(R.string.default_album)
            albumGridHolder.author.setText(R.string.default_artist)
            albumGridHolder.albumCover.setImageResource(R.drawable.album_default)
            val layoutParams = albumGridHolder.albumCover.layoutParams
            layoutParams.width = gridAlbumWidth
            albumGridHolder.albumCover.layoutParams = layoutParams

            viewTypeToggle.addOnButtonCheckedListener { _, checkedButtonId, isChecked ->
                if (isChecked) refreshViewType(checkedButtonId)
            }
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
            setDefault.setOnClickListener { onClickSetDefault() }

            val albumItemView = viewSettingsInteractor.albumItemViewSettings

            val checkedButtonId = savedInstanceState?.getInt(VIEW_TYPE_BUNDLE_CHECK_POSITION_KEY)
                ?: if (albumItemView.viewType == AlbumViewType.GRID) R.id.gridButton
                else R.id.listButton

            viewTypeToggle.check(checkedButtonId)
            textSizeSeekBar.progress = albumItemView.textSize - TEXT_SIZE_MIN
            borderPaddingSeekBar.progress = albumItemView.borderPadding - BORDER_PADDING_MIN
            coverSizeSeekBar.progress = albumItemView.coverSize - COVER_SIZE_MIN
            coverCornersSeekBar.progress = albumItemView.coverCornersRadius

            refreshViewType(checkedButtonId)
            refreshTextSize(textSizeSeekBar.progress)
            refreshBorderPadding(borderPaddingSeekBar.progress)
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
        val albumViewType =
            if (binding.viewTypeToggle.checkedButtonId == R.id.gridButton) AlbumViewType.GRID
            else AlbumViewType.LIST

        val albumItemView = AlbumItemView(
            albumViewType,
            binding.textSizeSeekBar.progress + TEXT_SIZE_MIN,
            binding.borderPaddingSeekBar.progress + BORDER_PADDING_MIN,
            binding.coverSizeSeekBar.progress + COVER_SIZE_MIN,
            binding.coverCornersSeekBar.progress
        )
        viewSettingsInteractor.albumItemViewSettings = albumItemView
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(
            VIEW_TYPE_BUNDLE_CHECK_POSITION_KEY,
            binding.viewTypeToggle.checkedButtonId
        )
    }


    private fun onClickSetDefault() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.reset_settings_dialog_title)
            .setMessage(R.string.album_item_reset_settings_dialog_message)
            .setPositiveButton(R.string.dialog_reset) { _, _ ->
                binding.viewTypeToggle.check(R.id.gridButton)
                binding.textSizeSeekBar.progress = 16 - TEXT_SIZE_MIN
                binding.borderPaddingSeekBar.progress = 16 - BORDER_PADDING_MIN
                binding.coverSizeSeekBar.progress = 64 - COVER_SIZE_MIN
                binding.coverCornersSeekBar.progress = 4
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
        lifecycle.addObserver(DialogDismissLifecycleObserver(dialog))
    }

    override fun onClickBackButton() {
        router.goBack()
    }

    private fun refreshViewType(checkedButtonId: Int) {
        if (checkedButtonId == R.id.gridButton) {
            binding.apply {
                albumGridHolder.root.visibility = View.VISIBLE
                albumListHolder.root.visibility = View.INVISIBLE
                borderPaddingBar.isEnabled = false
                borderPaddingSeekBar.isEnabled = false
                coverSizeBar.isEnabled = false
                coverSizeSeekBar.isEnabled = false
            }
        } else if (checkedButtonId == R.id.listButton) {
            binding.apply {
                albumGridHolder.root.visibility = View.INVISIBLE
                albumListHolder.root.visibility = View.VISIBLE
                borderPaddingBar.isEnabled = true
                borderPaddingSeekBar.isEnabled = true
                coverSizeBar.isEnabled = true
                coverSizeSeekBar.isEnabled = true
            }
        } else {
            throw IllegalArgumentException("checkedButtonId equals to $checkedButtonId")
        }
    }

    private fun refreshTextSize(progress: Int) {
        binding.apply {
            val textSizeSp = progress + TEXT_SIZE_MIN
            textSizeValue.text = textSizeSp.toString()

            albumListHolder.albumTitle.textSize = textSizeSp.toFloat()
            albumListHolder.author.textSize = (textSizeSp - 2).toFloat()
            albumListHolder.tracksCount.textSize = (textSizeSp - 4).toFloat()

            albumGridHolder.albumTitle.textSize = (textSizeSp + 2).toFloat()
            albumGridHolder.author.textSize = (textSizeSp - 4).toFloat()
        }
    }

    private fun refreshBorderPadding(progress: Int) {
        val paddingDp = progress + BORDER_PADDING_MIN
        val paddingPx = convertDpToPixel(paddingDp.toFloat(), requireContext()).toInt()
        binding.borderPaddingValue.text = paddingDp.toString()

        binding.albumListHolder.root.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
    }

    private fun refreshCoverSize(progress: Int) {
        val coverSizeDp = progress + COVER_SIZE_MIN
        binding.coverSizeValue.text = coverSizeDp.toString()
        val coverSizePx = convertDpToPixel(coverSizeDp.toFloat(), requireContext()).toInt()

        val params = binding.albumListHolder.albumCover.layoutParams
        params.width = coverSizePx
        params.height = coverSizePx
        binding.albumListHolder.albumCover.layoutParams = params
    }

    private fun refreshCoverCorners(progress: Int) {
        val cornerSizePx = convertDpToPixel(progress.toFloat(), requireContext())

        val gridCover = binding.albumGridHolder.albumCover
        gridCover.shapeAppearanceModel = gridCover.shapeAppearanceModel.toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, cornerSizePx)
            .build()

        val listCover = binding.albumListHolder.albumCover
        listCover.shapeAppearanceModel = gridCover.shapeAppearanceModel.toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, cornerSizePx)
            .build()

        binding.coverCornersValue.text = progress.toString()
    }

    private val gridAlbumWidth: Int
        get() = (getScreenWidthPx(requireActivity().windowManager)
                / calculateAlbumColumnCount(requireActivity())
                ).toInt()

}
