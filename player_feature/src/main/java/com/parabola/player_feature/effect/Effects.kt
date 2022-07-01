package com.parabola.player_feature.effect

import android.media.audiofx.Equalizer
import com.parabola.domain.interactor.player.AudioEffectsInteractor.EqBand
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.*


private const val BAND_LEVEL_MULTIPLIER = 100
private const val FREQUENCY_MULTIPLIER = 1000


sealed interface Effect {
    sealed interface EqualizerEffect : Effect {
        val isAvailable: Boolean

        var isEnabled: Boolean
        val enablingUpdates: Observable<Boolean>

        val minBandLevel: Int
        val maxBandLevel: Int

        fun setBandLevel(bandId: Int, bandLevel: Int)
        val bands: List<EqBand>

        val presets: List<String>
        fun usePreset(presetIndex: Int)
    }
}

internal class EqualizerEffectImpl(
    audioSessionId: Int,
    enableOnStart: Boolean,
    bandLevels: List<Int>,
) :
    Effect.EqualizerEffect {

    private val equalizer: Equalizer
    override val enablingUpdates = BehaviorSubject.createDefault(false)

    init {
        equalizer = Equalizer(0, audioSessionId).apply {
            enabled = enableOnStart
            enablingUpdates.onNext(enabled)
            bandLevels.forEachIndexed { index, bandLevel ->
                this.setBandLevel(
                    index.toShort(),
                    (bandLevel * BAND_LEVEL_MULTIPLIER).toShort()
                )
            }
        }
    }

    override val isAvailable: Boolean = equalizer.numberOfBands > 0
    override var isEnabled: Boolean
        get() = equalizer.enabled
        set(enable) {
            equalizer.enabled = enable

            enablingUpdates.onNext(enable)
        }

    private val bandLevelRange = equalizer.bandLevelRange ?: shortArrayOf(0, 0)

    override val minBandLevel: Int = bandLevelRange[0] / BAND_LEVEL_MULTIPLIER
    override val maxBandLevel: Int = bandLevelRange[1] / BAND_LEVEL_MULTIPLIER

    override fun setBandLevel(bandId: Int, bandLevel: Int) {
        equalizer.setBandLevel(bandId.toShort(), (bandLevel * BAND_LEVEL_MULTIPLIER).toShort())
    }

    override val bands: List<EqBand>
        get() {
            val resultBands = mutableListOf<EqBand>()

            for (bandId in 0 until equalizer.numberOfBands) {
                val band = EqBand().apply {
                    id = bandId
                    frequency = equalizer.getCenterFreq(bandId.toShort()) / FREQUENCY_MULTIPLIER
                    currentLevel = equalizer.getBandLevel(bandId.toShort()) / BAND_LEVEL_MULTIPLIER
                }

                resultBands.add(band)
            }

            return Collections.unmodifiableList(resultBands)
        }

    override val presets: List<String> by lazy {
        val presetsCache = mutableListOf<String>()
        for (i in 0 until equalizer.numberOfPresets) {
            presetsCache.add(equalizer.getPresetName(i.toShort()))
        }

        return@lazy Collections.unmodifiableList(presetsCache)
    }

    override fun usePreset(presetIndex: Int) {
        equalizer.usePreset(presetIndex.toShort())
    }

}

internal class EqualizerEffectUnavailable : Effect.EqualizerEffect {
    override val isAvailable: Boolean = false

    override var isEnabled: Boolean = false
    override val enablingUpdates = BehaviorSubject.createDefault(false)

    override val minBandLevel: Int = 0
    override val maxBandLevel: Int = 0

    override fun setBandLevel(bandId: Int, bandLevel: Int) {}
    override val bands: List<EqBand> = emptyList()

    override val presets: List<String> = emptyList()
    override fun usePreset(presetIndex: Int) {}
}
