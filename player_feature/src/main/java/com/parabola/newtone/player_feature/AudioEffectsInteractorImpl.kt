package com.parabola.newtone.player_feature

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.parabola.domain.interactor.player.AudioEffectsInteractor
import com.parabola.domain.interactor.player.AudioEffectsInteractor.EqBand
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.Collections


private const val DEFAULT_PLAYBACK_SPEED = 1.0f
private const val DEFAULT_PLAYBACK_PITCH = 1.0f

internal class AudioEffectsInteractorImpl(
    private val exoPlayer: ExoPlayer,
    private val settings: PlayerSettingSaver,
) : AudioEffectsInteractor {

    private val playbackSpeedEnabledUpdates: BehaviorSubject<Boolean>
    private val savedPlaybackSpeed: BehaviorSubject<Float>
    private val playbackPitchEnabledUpdates: BehaviorSubject<Boolean>
    private val savedPlaybackPitch: BehaviorSubject<Float>
    private val eqEnablingUpdates = BehaviorSubject.createDefault(false)
    private val savedVirtualizerLevelUpdates: BehaviorSubject<Short>
    private val savedBassBoostLevelUpdates: BehaviorSubject<Short>


    //FXs
    private var equalizer: Equalizer? = null
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

        exoPlayer.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
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
                    Equalizer(0, audioSessionId).apply {
                        enabled = settings.isEqEnabled
                        eqEnablingUpdates.onNext(enabled)
                        val bandLevels = settings.savedBandLevels
                        for (bandNumber in bandLevels.indices) {
                            setBandLevel(
                                bandNumber.toShort(),
                                (bandLevels[bandNumber] * 100).toShort(),
                            )
                        }
                    }
                } catch (e: RuntimeException) {
                    null
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
    override fun isEqAvailable() = (equalizer?.numberOfBands ?: 0) > 0

    override fun setEqEnable(enable: Boolean) {
        equalizer?.enabled = enable

        settings.isEqEnabled = enable
        eqEnablingUpdates.onNext(enable)
    }

    override fun getMinEqBandLevel() =
        if (equalizer != null) (equalizer!!.bandLevelRange[0] / 100)
        else 0

    override fun getMaxEqBandLevel() =
        if (equalizer != null) (equalizer!!.bandLevelRange[1] / 100)
        else 0

    override fun observeEqEnabling() = eqEnablingUpdates

    override fun setBandLevel(bandId: Int, bandLevel: Int) {
        if (isEqAvailable)
            equalizer!!.setBandLevel(bandId.toShort(), (bandLevel * 100).toShort())

        val bandLevels = bands.map { it.currentLevel }
        settings.saveBandLevels(bandLevels)
    }

    override fun getBands(): List<EqBand> {
        if (equalizer == null)
            return emptyList()

        val bands = mutableListOf<EqBand>()

        for (i in 0 until equalizer!!.numberOfBands) {
            val band = EqBand().apply {
                id = i
                frequency = equalizer!!.getCenterFreq(i.toShort()) / 1000
                currentLevel = (equalizer!!.getBandLevel(i.toShort()) / 100)
            }
            bands.add(band)
        }

        return bands
    }

    override fun usePreset(presetIndex: Int) {
        if (equalizer != null)
            equalizer!!.usePreset(presetIndex.toShort())

        val bandLevels = bands.map { it.currentLevel }
        settings.saveBandLevels(bandLevels)
    }

    private var presetsCache: MutableList<String>? = null

    override fun getPresets(): List<String> {
        if (presetsCache != null)
            return Collections.unmodifiableList(presetsCache!!)

        if (equalizer == null) {
            presetsCache = mutableListOf()

            return Collections.unmodifiableList(presetsCache!!)
        }

        presetsCache = mutableListOf()
        for (i in 0 until equalizer!!.numberOfPresets) {
            presetsCache!!.add(equalizer!!.getPresetName(i.toShort()))
        }

        return Collections.unmodifiableList(presetsCache!!)
    }

}
