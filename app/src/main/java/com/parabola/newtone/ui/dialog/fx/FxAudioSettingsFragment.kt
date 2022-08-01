package com.parabola.newtone.ui.dialog.fx

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.databinding.TabFxAudioSettingsBinding
import com.parabola.newtone.mvp.presenter.fx.FxAudioSettingsPresenter
import com.parabola.newtone.mvp.view.fx.FxAudioSettingsView
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter


private const val BASS_BOOST_PROGRESS_STEP = 20
private const val VIRTUALIZER_PROGRESS_STEP = 20


class FxAudioSettingsFragment : MvpAppCompatFragment(),
    FxAudioSettingsView {

    @InjectPresenter
    lateinit var presenter: FxAudioSettingsPresenter

    private var _binding: TabFxAudioSettingsBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TabFxAudioSettingsBinding.inflate(inflater, container, false)
        binding.apply {
            playbackSpeedCroller.setOnProgressChangedListener(presenter::onPlaybackSpeedProgressChanged)
            playbackPitchCroller.setOnProgressChangedListener(presenter::onPlaybackPitchProgressChanged)
            bassBoostCroller.setOnProgressChangedListener { progress ->
                presenter.onBassBoostProgressChange(progress * BASS_BOOST_PROGRESS_STEP)
            }
            virtualizerCroller.setOnProgressChangedListener { progress ->
                presenter.onVirtualizerProgressChange(progress * VIRTUALIZER_PROGRESS_STEP)
            }

            playbackSpeedCroller.setOnDoubleTapListener { playbackSpeedCroller.progress = 50 }
            playbackPitchCroller.setOnDoubleTapListener { playbackPitchCroller.progress = 50 }
            bassBoostCroller.setOnDoubleTapListener { bassBoostCroller.progress = 0 }
            virtualizerCroller.setOnDoubleTapListener { virtualizerCroller.progress = 0 }

            playbackSpeedSwitch.setOnCheckedChangeListener { _, isChecked ->
                presenter.onPlaybackSpeedSwitchClick(isChecked)
            }
            playbackPitchSwitch.setOnCheckedChangeListener { _, isChecked ->
                presenter.onPlaybackPitchSwitchClick(isChecked)
            }
            bassBoostSwitchButton.setOnCheckedChangeListener { _, isChecked ->
                presenter.onBassBoostSwitchClick(isChecked)
            }
            virtualizerSwitchButton.setOnCheckedChangeListener { _, isChecked ->
                presenter.onVirtualizerSwitchClick(isChecked)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    @ProvidePresenter
    fun providePresenter(): FxAudioSettingsPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        return FxAudioSettingsPresenter(appComponent)
    }


    override fun setPlaybackSpeedSwitch(enabled: Boolean) {
        binding.playbackSpeedSwitch.isChecked = enabled
        binding.playbackSpeedCroller.isEnabled = enabled
    }

    override fun setPlaybackPitchSwitch(enabled: Boolean) {
        binding.playbackPitchSwitch.isChecked = enabled
        binding.playbackPitchCroller.isEnabled = enabled
    }

    override fun setPlaybackSpeedSeekbar(progress: Int) {
        binding.playbackSpeedCroller.progress = progress
    }

    override fun setPlaybackSpeedText(speed: Float) {
        binding.playbackSpeedCroller.label = getString(R.string.fx_tempo, speed)
    }

    override fun setPlaybackPitchSeekbar(progress: Int) {
        binding.playbackPitchCroller.progress = progress
    }

    override fun setPlaybackPitchText(pitch: Float) {
        binding.playbackPitchCroller.label = getString(R.string.fx_pitch, pitch)
    }

    override fun hideBassBoostPanel() {
        binding.bassBoostSwitchButton.visibility = View.GONE
        binding.bassBoostCroller.visibility = View.GONE
    }

    override fun setBassBoostSeekbar(currentLevel: Int) {
        binding.bassBoostCroller.progress = currentLevel / VIRTUALIZER_PROGRESS_STEP
    }

    override fun setBassBoostSwitch(bassBoostEnabled: Boolean) {
        binding.bassBoostSwitchButton.isChecked = bassBoostEnabled
        binding.bassBoostCroller.isEnabled = bassBoostEnabled
    }

    override fun hideVirtualizerPanel() {
        binding.virtualizerSwitchButton.visibility = View.GONE
        binding.virtualizerCroller.visibility = View.GONE
    }

    override fun setVirtualizerSeekbar(currentLevel: Int) {
        binding.virtualizerCroller.progress = currentLevel / VIRTUALIZER_PROGRESS_STEP
    }

    override fun setVirtualizerSwitch(virtualizerEnabled: Boolean) {
        binding.virtualizerSwitchButton.isChecked = virtualizerEnabled
        binding.virtualizerCroller.isEnabled = virtualizerEnabled
    }

}
