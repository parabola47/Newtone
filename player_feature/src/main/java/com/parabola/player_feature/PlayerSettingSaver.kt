package com.parabola.player_feature

import android.content.SharedPreferences
import com.parabola.domain.interactor.player.PlayerInteractor

class PlayerSettingSaver(private val prefs: SharedPreferences) {

    var isNotificationBackgroundColorized: Boolean
        get() = prefs.getBoolean(NOTIFICATION_BACKGROUND_COLORIZED_KEY, true)
        set(colorized) {
            prefs.edit()
                .putBoolean(NOTIFICATION_BACKGROUND_COLORIZED_KEY, colorized)
                .apply()
        }


    var isNotificationArtworkShow: Boolean
        get() = prefs.getBoolean(NOTIFICATION_ARTWORK_SHOW_KEY, true)
        set(show) {
            prefs.edit()
                .putBoolean(NOTIFICATION_ARTWORK_SHOW_KEY, show)
                .apply()
        }


    var isShuffleModeEnabled: Boolean
        get() = prefs.getBoolean(SHUFFLE_MODE_KEY, false)
        set(enable) {
            prefs.edit()
                .putBoolean(SHUFFLE_MODE_KEY, enable)
                .apply()
        }


    var repeatMode: PlayerInteractor.RepeatMode
        get() {
            val savedRepeatMode = prefs
                .getString(REPEAT_MODE_KEY, PlayerInteractor.DEFAULT_REPEAT_MODE.name)
            return PlayerInteractor.RepeatMode.valueOf(savedRepeatMode!!)
        }
        set(repeatMode) {
            prefs.edit()
                .putString(REPEAT_MODE_KEY, repeatMode.name)
                .apply()
        }


    val savedPlaylist: List<Int>
        get() {
            val savedString = prefs.getString(SAVED_PLAYLIST_KEY, "")
            if (savedString == null || savedString.isEmpty()) {
                return emptyList()
            }

            return savedString.split(SAVED_PLAYLIST_DELIMITER)
                .map { id: String -> id.toInt() }
        }

    // сохраняет треки в формате чисел, разеделённых символом ;
    // например 345;4502;5431;33;895
    fun savePlaylist(trackIds: List<Int>, currentWindowIndex: Int) {
        val savedPlaylist = trackIds.joinToString(SAVED_PLAYLIST_DELIMITER)

        prefs.edit()
            .putString(SAVED_PLAYLIST_KEY, savedPlaylist)
            .putInt(SAVED_PLAYLIST_POSITION_KEY, currentWindowIndex)
            .apply()
    }

    var savedWindowIndex: Int
        get() = prefs.getInt(SAVED_PLAYLIST_POSITION_KEY, -1)
        set(currentWindowIndex) {
            prefs.edit()
                .putInt(SAVED_PLAYLIST_POSITION_KEY, currentWindowIndex)
                .apply()
        }


    // миллисекунды
    var playbackPosition: Long
        get() = prefs.getLong(SAVED_PLAYBACK_POSITION_KEY, 0)
        set(playbackPosition) {
            prefs.edit()
                .putLong(SAVED_PLAYBACK_POSITION_KEY, playbackPosition)
                .apply()
        }


    var isPlaybackSpeedEnabled: Boolean
        get() = prefs.getBoolean(SAVED_PLAYBACK_SPEED_ENABLED_KEY, false)
        set(enable) {
            prefs.edit()
                .putBoolean(SAVED_PLAYBACK_SPEED_ENABLED_KEY, enable)
                .apply()
        }

    var playbackSpeed: Float
        get() = prefs.getFloat(SAVED_PLAYBACK_SPEED_KEY, 1.0f)
        set(speed) {
            prefs.edit()
                .putFloat(SAVED_PLAYBACK_SPEED_KEY, speed)
                .apply()
        }


    var isPlaybackPitchEnabled: Boolean
        get() = prefs.getBoolean(SAVED_PLAYBACK_PITCH_ENABLED_KEY, false)
        set(enable) {
            prefs.edit()
                .putBoolean(SAVED_PLAYBACK_PITCH_ENABLED_KEY, enable)
                .apply()
        }

    var playbackPitch: Float
        get() = prefs.getFloat(SAVED_PLAYBACK_PITCH_KEY, 1.0f)
        set(pitch) {
            prefs.edit()
                .putFloat(SAVED_PLAYBACK_PITCH_KEY, pitch)
                .apply()
        }


    var isBassBoostEnabled: Boolean
        get() = prefs.getBoolean(SAVED_BASS_BOOST_ENABLED_KEY, false)
        set(enable) {
            prefs.edit()
                .putBoolean(SAVED_BASS_BOOST_ENABLED_KEY, enable)
                .apply()
        }


    var bassBoostStrength: Short
        get() = prefs.getInt(SAVED_BASS_BOOST_STRENGTH_KEY, 0).toShort()
        set(strength) {
            prefs.edit()
                .putInt(SAVED_BASS_BOOST_STRENGTH_KEY, strength.toInt())
                .apply()
        }


    var isVirtualizerEnabled: Boolean
        get() = prefs.getBoolean(SAVED_VIRTUALIZER_ENABLED_KEY, false)
        set(enable) {
            prefs.edit()
                .putBoolean(SAVED_VIRTUALIZER_ENABLED_KEY, enable)
                .apply()
        }


    var virtualizerStrength: Short
        get() = prefs.getInt(SAVED_VIRTUALIZER_STRENGTH_KEY, 0).toShort()
        set(strength) {
            prefs.edit()
                .putInt(SAVED_VIRTUALIZER_STRENGTH_KEY, strength.toInt())
                .apply()
        }


    var isEqEnabled: Boolean
        get() = prefs.getBoolean(SAVED_EQ_ENABLED_KEY, false)
        set(enable) {
            prefs.edit()
                .putBoolean(SAVED_EQ_ENABLED_KEY, enable)
                .apply()
        }

    val savedBandLevels: List<Int>
        get() {
            val bandLevels = prefs.getString(SAVED_EQ_BAND_LEVELS_KEY, "")
            if (bandLevels == null || bandLevels.isEmpty()) {
                return emptyList()
            }

            return bandLevels.split(SAVED_EQ_BAND_LEVELS_DELIMITER)
                .map { it.toInt() }
        }

    fun saveBandLevels(bandLevels: List<Int>) {
        val savedPlaylist = bandLevels.joinToString(SAVED_EQ_BAND_LEVELS_DELIMITER)

        prefs.edit()
            .putString(SAVED_EQ_BAND_LEVELS_KEY, savedPlaylist)
            .apply()
    }

}


private const val NOTIFICATION_BACKGROUND_COLORIZED_KEY =
    "com.parabola.player_feature.NOTIFICATION_BACKGROUND_COLORIZED"
private const val NOTIFICATION_ARTWORK_SHOW_KEY =
    "com.parabola.player_feature.NOTIFICATION_ARTWORK_SHOW_KEY"


private const val SHUFFLE_MODE_KEY = "com.parabola.player_feature.SHUFFLE_MODE_KEY"
private const val REPEAT_MODE_KEY = "com.parabola.player_feature.REPEAT_MODE"


private const val SAVED_PLAYLIST_KEY = "com.parabola.player_feature.SAVED_PLAYLIST_KEY"
private const val SAVED_PLAYLIST_POSITION_KEY =
    "com.parabola.player_feature.SAVED_PLAYLIST_POSITION_KEY"
private const val SAVED_PLAYLIST_DELIMITER = ";"

private const val SAVED_PLAYBACK_POSITION_KEY =
    "com.parabola.player_feature.SAVED_PLAYBACK_POSITION_KEY"

private const val SAVED_PLAYBACK_SPEED_ENABLED_KEY =
    "com.parabola.player_feature.SAVED_PLAYBACK_SPEED_ENABLED_KEY"
private const val SAVED_PLAYBACK_SPEED_KEY =
    "com.parabola.player_feature.SAVED_PLAYBACK_SPEED_KEY"

private const val SAVED_PLAYBACK_PITCH_ENABLED_KEY =
    "com.parabola.player_feature.SAVED_PLAYBACK_PITCH_ENABLED_KEY"
private const val SAVED_PLAYBACK_PITCH_KEY =
    "com.parabola.player_feature.SAVED_PLAYBACK_PITCH_KEY"

private const val SAVED_BASS_BOOST_ENABLED_KEY =
    "com.parabola.player_feature.SAVED_BASS_BOOST_ENABLED_KEY"
private const val SAVED_BASS_BOOST_STRENGTH_KEY =
    "com.parabola.player_feature.SAVED_BASS_BOOST_STRENGTH_KEY"

private const val SAVED_VIRTUALIZER_ENABLED_KEY =
    "com.parabola.player_feature.SAVED_VIRTUALIZER_ENABLED_KEY"
private const val SAVED_VIRTUALIZER_STRENGTH_KEY =
    "com.parabola.player_feature.SAVED_VIRTUALIZER_STRENGTH_KEY"

private const val SAVED_EQ_ENABLED_KEY = "com.parabola.player_feature.SAVED_EQ_ENABLED_KEY"
private const val SAVED_EQ_BAND_LEVELS_KEY =
    "com.parabola.player_feature.SAVED_EQ_BAND_LEVELS_KEY"
private const val SAVED_EQ_BAND_LEVELS_DELIMITER = ";"
