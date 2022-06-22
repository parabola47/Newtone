package com.parabola.player_feature;

import android.content.SharedPreferences;

import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.interactor.player.PlayerInteractor.RepeatMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

class PlayerSettingSaver {
    private static final String LOG_TAG = PlayerSettingSaver.class.getSimpleName();

    private SharedPreferences prefs;

    private static final String NOTIFICATION_BACKGROUND_COLORIZED_KEY = "com.parabola.player_feature.NOTIFICATION_BACKGROUND_COLORIZED";
    private static final String NOTIFICATION_ARTWORK_SHOW_KEY = "com.parabola.player_feature.NOTIFICATION_ARTWORK_SHOW_KEY";


    private static final String SHUFFLE_MODE_KEY = "com.parabola.player_feature.SHUFFLE_MODE_KEY";
    private static final String REPEAT_MODE_KEY = "com.parabola.player_feature.REPEAT_MODE";


    private static final String SAVED_PLAYLIST_KEY = "com.parabola.player_feature.SAVED_PLAYLIST_KEY";
    private static final String SAVED_PLAYLIST_POSITION_KEY = "com.parabola.player_feature.SAVED_PLAYLIST_POSITION_KEY";

    private static final String SAVED_PLAYBACK_POSITION_KEY = "com.parabola.player_feature.SAVED_PLAYBACK_POSITION_KEY";


    private static final String SAVED_PLAYBACK_SPEED_ENABLED_KEY = "com.parabola.player_feature.SAVED_PLAYBACK_SPEED_ENABLED_KEY";
    private static final String SAVED_PLAYBACK_SPEED_KEY = "com.parabola.player_feature.SAVED_PLAYBACK_SPEED_KEY";
    private static final String SAVED_PLAYBACK_PITCH_ENABLED_KEY = "com.parabola.player_feature.SAVED_PLAYBACK_PITCH_ENABLED_KEY";
    private static final String SAVED_PLAYBACK_PITCH_KEY = "com.parabola.player_feature.SAVED_PLAYBACK_PITCH_KEY";
    private static final String SAVED_BASS_BOOST_ENABLED_KEY = "com.parabola.player_feature.SAVED_BASS_BOOST_ENABLED_KEY";
    private static final String SAVED_BASS_BOOST_STRENGTH_KEY = "com.parabola.player_feature.SAVED_BASS_BOOST_STRENGTH_KEY";
    private static final String SAVED_VIRTUALIZER_ENABLED_KEY = "com.parabola.player_feature.SAVED_VIRTUALIZER_ENABLED_KEY";
    private static final String SAVED_VIRTUALIZER_STRENGTH_KEY = "com.parabola.player_feature.SAVED_VIRTUALIZER_STRENGTH_KEY";
    private static final String SAVED_EQ_ENABLED_KEY = "com.parabola.player_feature.SAVED_EQ_ENABLED_KEY";
    private static final String SAVED_EQ_BAND_ENABLED_KEY_FORMAT = "com.parabola.player_feature.SAVED_EQ_BAND_ENABLED_KEY_FORMAT%d";


    PlayerSettingSaver(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    void setNotificationBackgroundColorized(boolean colorized) {
        prefs.edit()
                .putBoolean(NOTIFICATION_BACKGROUND_COLORIZED_KEY, colorized)
                .apply();
    }

    boolean isNotificationBackgroundColorized() {
        return prefs.getBoolean(NOTIFICATION_BACKGROUND_COLORIZED_KEY, true);
    }

    void setNotificationArtworkShow(boolean show) {
        prefs.edit()
                .putBoolean(NOTIFICATION_ARTWORK_SHOW_KEY, show)
                .apply();
    }

    boolean isNotificationArtworkShow() {
        return prefs.getBoolean(NOTIFICATION_ARTWORK_SHOW_KEY, true);
    }

    void setShuffleMode(boolean enable) {
        prefs.edit()
                .putBoolean(SHUFFLE_MODE_KEY, enable)
                .apply();
    }

    boolean isShuffleModeEnabled() {
        return prefs.getBoolean(SHUFFLE_MODE_KEY, false);
    }

    void setRepeatMode(RepeatMode repeatMode) {
        prefs.edit()
                .putString(REPEAT_MODE_KEY, repeatMode.name())
                .apply();
    }

    RepeatMode getSavedRepeatMode() {
        String savedRepeatMode = prefs
                .getString(REPEAT_MODE_KEY, PlayerInteractor.DEFAULT_REPEAT_MODE.name());
        return RepeatMode.valueOf(savedRepeatMode);
    }


    private static final String SAVED_PLAYLIST_DELIMITER = ";";

    void setSavedPlaylist(List<Integer> trackIds, int currentWindowIndex) {
        String savedPlaylist = trackIds.stream()
                .map(id -> Integer.toString(id))
                .collect(Collectors.joining(SAVED_PLAYLIST_DELIMITER));

        prefs.edit()
                .putString(SAVED_PLAYLIST_KEY, savedPlaylist)
                .putInt(SAVED_PLAYLIST_POSITION_KEY, currentWindowIndex)
                .apply();
    }

    void setCurrentWindowIndex(int currentWindowIndex) {
        prefs.edit()
                .putInt(SAVED_PLAYLIST_POSITION_KEY, currentWindowIndex)
                .apply();
    }

    List<Integer> getSavedPlaylist() {
        String savedString = prefs.getString(SAVED_PLAYLIST_KEY, "");
        if (savedString == null || savedString.isEmpty()) {
            return Collections.emptyList();
        }

        String[] idsStr = savedString.split(SAVED_PLAYLIST_DELIMITER);

        return Arrays.stream(idsStr)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    int getSavedWindowIndex() {
        return prefs.getInt(SAVED_PLAYLIST_POSITION_KEY, -1);
    }


    void setPlaybackPosition(long playbackPositionMs) {
        prefs.edit()
                .putLong(SAVED_PLAYBACK_POSITION_KEY, playbackPositionMs)
                .apply();
    }

    long getSavedPlaybackPosition() {
        return prefs.getLong(SAVED_PLAYBACK_POSITION_KEY, 0);
    }


    boolean getSavedPlaybackSpeedEnabled() {
        return prefs.getBoolean(SAVED_PLAYBACK_SPEED_ENABLED_KEY, false);
    }

    void setSavedPlaybackSpeedEnabled(boolean enable) {
        prefs.edit()
                .putBoolean(SAVED_PLAYBACK_SPEED_ENABLED_KEY, enable)
                .apply();
    }

    float getSavedPlaybackSpeed() {
        return prefs.getFloat(SAVED_PLAYBACK_SPEED_KEY, 1.0f);
    }

    void setPlaybackSpeed(float speed) {
        prefs.edit()
                .putFloat(SAVED_PLAYBACK_SPEED_KEY, speed)
                .apply();
    }

    boolean getSavedPlaybackPitchEnabled() {
        return prefs.getBoolean(SAVED_PLAYBACK_PITCH_ENABLED_KEY, false);
    }

    void setSavedPlaybackPitchEnabled(boolean enable) {
        prefs.edit()
                .putBoolean(SAVED_PLAYBACK_PITCH_ENABLED_KEY, enable)
                .apply();
    }

    float getSavedPlaybackPitch() {
        return prefs.getFloat(SAVED_PLAYBACK_PITCH_KEY, 1.0f);
    }

    void setPlaybackPitch(float pitch) {
        prefs.edit()
                .putFloat(SAVED_PLAYBACK_PITCH_KEY, pitch)
                .apply();
    }


    boolean getSavedBassBoostEnabled() {
        return prefs.getBoolean(SAVED_BASS_BOOST_ENABLED_KEY, false);
    }

    void setBassBoostEnabled(boolean enabled) {
        prefs.edit()
                .putBoolean(SAVED_BASS_BOOST_ENABLED_KEY, enabled)
                .apply();
    }


    short getSavedBassBoostStrength() {
        return (short) prefs.getInt(SAVED_BASS_BOOST_STRENGTH_KEY, 0);
    }

    void setBassBoostStrength(short strength) {
        prefs.edit()
                .putInt(SAVED_BASS_BOOST_STRENGTH_KEY, strength)
                .apply();
    }


    boolean getSavedVirtualizerEnabled() {
        return prefs.getBoolean(SAVED_VIRTUALIZER_ENABLED_KEY, false);
    }

    void setVirtualizerEnabled(boolean enabled) {
        prefs.edit()
                .putBoolean(SAVED_VIRTUALIZER_ENABLED_KEY, enabled)
                .apply();
    }


    short getSavedVirtualizerStrength() {
        return (short) prefs.getInt(SAVED_VIRTUALIZER_STRENGTH_KEY, 0);
    }

    void setVirtualizerStrength(short strength) {
        prefs.edit()
                .putInt(SAVED_VIRTUALIZER_STRENGTH_KEY, strength)
                .apply();
    }


    boolean getSavedIsEqEnabled() {
        return prefs.getBoolean(SAVED_EQ_ENABLED_KEY, false);
    }

    void setEqEnabled(boolean enable) {
        prefs.edit()
                .putBoolean(SAVED_EQ_ENABLED_KEY, enable)
                .apply();
    }

    short getSavedBandLevel(short bandId) {
        return (short) prefs.getInt(String.format(Locale.getDefault(), SAVED_EQ_BAND_ENABLED_KEY_FORMAT, bandId), 0);
    }

    void setBandLevel(short bandId, short bandLevel) {
        prefs.edit()
                .putInt(String.format(Locale.getDefault(), SAVED_EQ_BAND_ENABLED_KEY_FORMAT, bandId), bandLevel)
                .apply();
    }
}