package com.parabola.newtone.presentation.settings

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.domain.settings.ViewSettingsInteractor.ArtistItemView
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.databinding.FragmentArtistItemDisplaySettingBinding
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver
import com.parabola.newtone.ui.router.MainRouter
import com.parabola.newtone.util.AndroidTool
import com.parabola.newtone.util.SeekBarChangeAdapter
import javax.inject.Inject


private const val TEXT_SIZE_MIN = 12
private const val BORDER_PADDING_MIN = 8


class ArtistItemDisplaySettingFragment : BaseSwipeToBackFragment() {

    private var _binding: FragmentArtistItemDisplaySettingBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    @Inject
    lateinit var router: MainRouter


    companion object {
        fun newInstance() = ArtistItemDisplaySettingFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentArtistItemDisplaySettingBinding.inflate(inflater, container, false)
        rootBinding.container.addView(binding.root)

        val appComponent = (requireActivity().application as MainApplication).appComponent
        appComponent.inject(this)

        rootBinding.main.setText(R.string.artist_item_display_setting_screen_title)
        rootBinding.additionalInfo.visibility = View.GONE
        rootBinding.otherInfo.visibility = View.GONE

        binding.apply {
            artistHolder.artist.setText(R.string.default_artist)
            artistHolder.artistInfo.setText(R.string.default_artist_info)

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
            setDefault.setOnClickListener { onClickSetDefault() }

            val artistItemView = viewSettingsInteractor.artistItemViewSettings

            textSizeSeekBar.progress = artistItemView.textSize - TEXT_SIZE_MIN
            borderPaddingSeekBar.progress = artistItemView.borderPadding - BORDER_PADDING_MIN

            refreshTextSize(textSizeSeekBar.progress)
            refreshBorderPadding(borderPaddingSeekBar.progress)
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
        val artistItemView = ArtistItemView(
            binding.textSizeSeekBar.progress + TEXT_SIZE_MIN,
            binding.borderPaddingSeekBar.progress + BORDER_PADDING_MIN
        )
        viewSettingsInteractor.artistItemViewSettings = artistItemView
    }


    private fun onClickSetDefault() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.reset_settings_dialog_title)
            .setMessage(R.string.artist_item_reset_settings_dialog_message)
            .setPositiveButton(R.string.dialog_reset) { _, _ ->
                binding.textSizeSeekBar.progress = 16 - TEXT_SIZE_MIN
                binding.borderPaddingSeekBar.progress = 16 - BORDER_PADDING_MIN
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
        lifecycle.addObserver(DialogDismissLifecycleObserver(dialog))
    }

    override fun onClickBackButton() {
        router.goBack()
    }

    private fun refreshTextSize(progress: Int) {
        binding.apply {
            val textSizeSp = progress + TEXT_SIZE_MIN
            textSizeValue.text = textSizeSp.toString()
            artistHolder.artist.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp.toFloat())
            artistHolder.artistInfo.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                (textSizeSp - 2).toFloat()
            )
        }
    }

    private fun refreshBorderPadding(progress: Int) {
        val paddingDp = progress + BORDER_PADDING_MIN
        val paddingPx = AndroidTool.convertDpToPixel(paddingDp.toFloat(), requireContext()).toInt()
        binding.borderPaddingValue.text = paddingDp.toString()
        binding.artistHolder.root.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
    }

}
