package com.parabola.player_feature

import android.media.audiofx.BassBoost
import android.media.audiofx.Virtualizer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioListener
import com.parabola.domain.interactor.player.AudioEffectsInteractor
import com.parabola.domain.interactor.player.AudioEffectsInteractor.EqBand
import com.parabola.player_feature.effect.Effect
import com.parabola.player_feature.effect.EqualizerEffectImpl
import com.parabola.player_feature.effect.EqualizerEffectUnavailable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


private const val DEFAULT_PLAYBACK_SPEED = 1.0f
private const val DEFAULT_PLAYBACK_PITCH = 1.0f

internal class AudioEffectsInteractorImpl(
    private val exoPlayer: SimpleExoPlayer,
    private val settings: PlayerSettingSaver,
) : AudioEffectsInteractor {

    private val playbackSpeedEnabledUpdates: BehaviorSubject<Boolean>
    private val savedPlaybackSpeed: BehaviorSubject<Float>
    private val playbackPitchEnabledUpdates: BehaviorSubject<Boolean>
    private val savedPlaybackPitch: BehaviorSubject<Float>
    private val savedVirtualizerLevelUpdates: BehaviorSubject<Short>
    private val savedBassBoostLevelUpdates: BehaviorSubject<Short>


    // TODO [23.06.2022] убрать nullable у переменных, узнать как это выделяется в отдельную абстракцию
    //FXs
    private lateinit var equalizer: Effect.EqualizerEffect
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null


    init {
        val speed =
            if (settings.isPlaybackSpeedEnabled) settings.playbackSpeed
            else DEFAULT_PLAYBACK_SPEED
        val pitch =
            if (settings.isPlaybackPitchEnabled) settings.playbackPitch
            else DEFAULT_PLAYBACK_PITCH

        exoPlayer.setPlaybackParameters(PlaybackParameters(speed, pitch))

        savedPlaybackSpeed = BehaviorSubject.createDefault(settings.playbackSpeed)
        savedPlaybackPitch = BehaviorSubject.createDefault(settings.playbackPitch)

        playbackSpeedEnabledUpdates =
            BehaviorSubject.createDefault(settings.isPlaybackSpeedEnabled)
        playbackPitchEnabledUpdates =
            BehaviorSubject.createDefault(settings.isPlaybackPitchEnabled)

        savedVirtualizerLevelUpdates =
            BehaviorSubject.createDefault(settings.virtualizerStrength)
        savedBassBoostLevelUpdates = BehaviorSubject.createDefault(settings.bassBoostStrength)

        exoPlayer.addListener(object : Player.EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                if (savedPlaybackSpeed.value != playbackParameters.speed
                    && settings.isPlaybackSpeedEnabled
                ) {
                    savedPlaybackSpeed.onNext(playbackParameters.speed)
                }
                if (savedPlaybackPitch.value != playbackParameters.pitch
                    && settings.isPlaybackPitchEnabled
                ) {
                    savedPlaybackPitch.onNext(playbackParameters.pitch)
                }
            }
        })

        exoPlayer.addAudioListener(object : AudioListener {
            override fun onAudioSessionId(audioSessionId: Int) {
                bassBoost = try {
                    BassBoost(0, audioSessionId).apply {
                        enabled = settings.isBassBoostEnabled
                        setStrength(settings.bassBoostStrength)
                    }
                } catch (e: RuntimeException) {
                    null
                }

                virtualizer = try {
                    Virtualizer(0, audioSessionId).apply {
                        enabled = settings.isVirtualizerEnabled
                        setStrength(settings.virtualizerStrength)
                    }
                } catch (e: RuntimeException) {
                    null
                }

                equalizer = try {
                    EqualizerEffectImpl(
                        audioSessionId,
                        enableOnStart = settings.isEqEnabled,
                        settings.savedBandLevels,
                    )
                } catch (e: RuntimeException) {
                    EqualizerEffectUnavailable()
                }
            }
        })
    }


    //    SPEED
    override fun observeIsPlaybackSpeedEnabled(): Observable<Boolean> = playbackSpeedEnabledUpdates

    override fun setPlaybackSpeedEnabled(enable: Boolean) {
        settings.isPlaybackSpeedEnabled = enable
        playbackSpeedEnabledUpdates.onNext(enable)

        val speed = if (enable) settings.playbackSpeed else DEFAULT_PLAYBACK_SPEED

        val playbackParameters = PlaybackParameters(speed, exoPlayer.playbackParameters.pitch)
        exoPlayer.setPlaybackParameters(playbackParameters)
    }

    override fun getSavedPlaybackSpeed(): Float = savedPlaybackSpeed.value!!

    override fun observePlaybackSpeed(): Observable<Float> = savedPlaybackSpeed

    override fun setSavedPlaybackSpeed(speed: Float) {
        settings.playbackSpeed = speed

        if (settings.isPlaybackSpeedEnabled) {
            val playbackParameters = PlaybackParameters(speed, exoPlayer.playbackParameters.pitch)
            exoPlayer.setPlaybackParameters(playbackParameters)
        }
    }


    //    PITCH
    override fun observeIsPlaybackPitchEnabled(): Observable<Boolean> = playbackPitchEnabledUpdates

    override fun setPlaybackPitchEnabled(enabled: Boolean) {
        settings.isPlaybackPitchEnabled = enabled
        playbackPitchEnabledUpdates.onNext(enabled)

        val pitch = if (enabled) settings.playbackPitch else DEFAULT_PLAYBACK_PITCH

        val playbackParameters = PlaybackParameters(exoPlayer.playbackParameters.speed, pitch)
        exoPlayer.setPlaybackParameters(playbackParameters)
    }

    override fun getSavedPlaybackPitch(): Float {
        return savedPlaybackPitch.value!!
    }

    override fun observePlaybackPitch(): Observable<Float> {
        return savedPlaybackPitch
    }

    override fun setSavedPlaybackPitch(pitch: Float) {
        settings.playbackPitch = pitch
        if (settings.isPlaybackPitchEnabled) {
            val playbackParameters = PlaybackParameters(
                exoPlayer.playbackParameters.speed, pitch
            )
            exoPlayer.setPlaybackParameters(playbackParameters)
        }
    }


    //    BASS BOOST
    override fun isBassBoostAvailable(): Boolean = bassBoost?.strengthSupported ?: false

    override fun isBassBoostEnabled(): Boolean = bassBoost?.enabled ?: false

    override fun setBassBoostEnable(enable: Boolean) {
        bassBoost?.enabled = enable
        settings.isBassBoostEnabled = enable
    }

    override fun observeBassBoostLevel(): Observable<Short> = savedBassBoostLevelUpdates

    override fun setBassBoostLevel(strength: Short) {
        if (isBassBoostAvailable) bassBoost!!.setStrength(strength)

        settings.bassBoostStrength = strength
        savedBassBoostLevelUpdates.onNext(strength)
    }


    //    VIRTUALIZER
    override fun isVirtualizerAvailable(): Boolean = virtualizer?.strengthSupported ?: false

    override fun isVirtualizerEnabled(): Boolean = virtualizer?.enabled ?: false

    override fun setVirtualizerEnable(enable: Boolean) {
        virtualizer?.enabled = enable
        settings.isVirtualizerEnabled = enable
    }

    override fun observeVirtualizerLevel(): Observable<Short> = savedVirtualizerLevelUpdates

    override fun setVirtualizerLevel(strength: Short) {
        if (isVirtualizerAvailable) virtualizer!!.setStrength(strength)

        settings.virtualizerStrength = strength
        savedVirtualizerLevelUpdates.onNext(strength)
    }


    //    EQ
    override fun isEqAvailable() = equalizer.isAvailable

    override fun setEqEnable(enable: Boolean) {
        equalizer.isEnabled = enable
        settings.isEqEnabled = enable
    }

    override fun getMinEqBandLevel() = equalizer.minBandLevel

    override fun getMaxEqBandLevel() = equalizer.maxBandLevel

    override fun observeEqEnabling() = equalizer.enablingUpdates

    override fun setBandLevel(bandId: Int, bandLevel: Int) {
        equalizer.setBandLevel(bandId, bandLevel)

        val bandLevels = equalizer.bands.map { it.currentLevel }
        settings.saveBandLevels(bandLevels)
    }

    override fun getBands(): List<EqBand> = equalizer.bands

    override fun usePreset(presetIndex: Int) {
        equalizer.usePreset(presetIndex)

        val bandLevels = equalizer.bands.map { it.currentLevel }
        settings.saveBandLevels(bandLevels)
    }

    override fun getPresets(): List<String> = equalizer.presets

}
