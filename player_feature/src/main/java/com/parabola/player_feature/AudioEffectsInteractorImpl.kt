package com.parabola.player_feature

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioListener
import com.parabola.domain.interactor.player.AudioEffectsInteractor
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.*


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

        val playbackParameters = PlaybackParameters(speed, pitch)
        exoPlayer.setPlaybackParameters(playbackParameters)

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
                if (savedPlaybackSpeed.value != playbackParameters.speed && settings.isPlaybackSpeedEnabled) {
                    savedPlaybackSpeed.onNext(playbackParameters.speed)
                }
                if (savedPlaybackPitch.value != playbackParameters.pitch && settings.isPlaybackPitchEnabled) {
                    savedPlaybackPitch.onNext(playbackParameters.pitch)
                }
            }
        })

        exoPlayer.addAudioListener(object : AudioListener {
            override fun onAudioSessionId(audioSessionId: Int) {
                try {
                    bassBoost = BassBoost(0, audioSessionId)
                } catch (ignored: RuntimeException) {
                }
                if (isBassBoostAvailable) {
                    bassBoost!!.enabled = settings.isBassBoostEnabled
                    bassBoost!!.setStrength(settings.bassBoostStrength)
                }
                try {
                    virtualizer = Virtualizer(0, audioSessionId)
                } catch (ignored: RuntimeException) {
                }
                if (isVirtualizerAvailable) {
                    virtualizer!!.enabled = settings.isVirtualizerEnabled
                    virtualizer!!.setStrength(settings.virtualizerStrength)
                }
                try {
                    equalizer = Equalizer(0, audioSessionId)
                } catch (ignored: RuntimeException) {
                }
                if (isEqAvailable) {
                    equalizer!!.enabled = settings.isEqEnabled
                    eqEnablingUpdates.onNext(equalizer!!.enabled)
                    for (i in 0 until equalizer!!.numberOfBands) {
                        equalizer!!.setBandLevel(
                            i.toShort(),
                            (settings.getSavedBandLevel(i.toShort()) * 100).toShort()
                        )
                    }
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
    override fun isEqAvailable(): Boolean = (equalizer?.numberOfBands ?: 0) > 0

    override fun setEqEnable(enable: Boolean) {
        equalizer?.enabled = enable

        settings.isEqEnabled = enable
        eqEnablingUpdates.onNext(enable)
    }

    override fun getMaxEqBandLevel(): Short {
        return if (equalizer != null) (equalizer!!.bandLevelRange[1] / 100).toShort() else 0
    }

    override fun getMinEqBandLevel(): Short {
        return if (equalizer != null) (equalizer!!.bandLevelRange[0] / 100).toShort() else 0
    }

    override fun observeEqEnabling(): Observable<Boolean> = eqEnablingUpdates

    override fun setBandLevel(bandId: Int, bandLevel: Short) {
        if (isEqAvailable)
            equalizer!!.setBandLevel(bandId.toShort(), (bandLevel * 100).toShort())

        settings.setBandLevel(bandId.toShort(), bandLevel)
    }

    override fun getBands(): List<AudioEffectsInteractor.EqBand> {
        if (equalizer == null) return emptyList()

        val bands = mutableListOf<AudioEffectsInteractor.EqBand>()

        for (i in 0 until equalizer!!.numberOfBands) {
            val band = AudioEffectsInteractor.EqBand()
            band.id = i
            band.frequency = equalizer!!.getCenterFreq(i.toShort()) / 1000
            band.currentLevel = (equalizer!!.getBandLevel(i.toShort()) / 100).toShort()
            bands.add(band)
        }

        return bands
    }


    override fun usePreset(presetIndex: Short) {
        equalizer!!.usePreset(presetIndex)
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
