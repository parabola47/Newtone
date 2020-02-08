package com.parabola.player_feature;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.exoplayer2.source.ConcatenatingMediaSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

class PlayerSettingSaver {
    private static final String LOG_TAG = PlayerSettingSaver.class.getSimpleName();

    private SharedPreferences prefs;

    private static final String SHARED_PREFERENCES_KEY = "com.parabola.player_feature.PlayerSettingSaver";


    private static final String NOTIFICATION_BACKGROUND_COLORIZED_KEY = "com.parabola.player_feature.NOTIFICATION_BACKGROUND_COLORIZED";
    private static final String NOTIFICATION_ARTWORK_SHOW_KEY = "com.parabola.player_feature.NOTIFICATION_ARTWORK_SHOW_KEY";


    private static final String SHUFFLE_MODE_KEY = "com.parabola.player_feature.SHUFFLE_MODE_KEY";
    private static final String REPEAT_MODE_KEY = "com.parabola.player_feature.REPEAT_MODE_KEY";


    private static final String SAVED_PLAYLIST_KEY = "com.parabola.player_feature.SAVED_PLAYLIST_KEY";
    private static final String SAVED_PLAYLIST_POSITION_KEY = "com.parabola.player_feature.SAVED_PLAYLIST_POSITION_KEY";

    private static final String SAVED_PLAYBACK_POSITION_KEY = "com.parabola.player_feature.SAVED_PLAYBACK_POSITION_KEY";


    private static final String SAVED_PLAYBACK_SPEED_KEY = "com.parabola.player_feature.SAVED_PLAYBACK_SPEED_KEY";
    private static final String SAVED_PLAYBACK_PITCH_KEY = "com.parabola.player_feature.SAVED_PLAYBACK_PITCH_KEY";
    private static final String SAVED_BASS_BOOST_ENABLED_KEY = "com.parabola.player_feature.SAVED_BASS_BOOST_ENABLED_KEY";
    private static final String SAVED_BASS_BOOST_STRENGTH_KEY = "com.parabola.player_feature.SAVED_BASS_BOOST_STRENGTH_KEY";
    private static final String SAVED_VIRTUALIZER_ENABLED_KEY = "com.parabola.player_feature.SAVED_VIRTUALIZER_ENABLED_KEY";
    private static final String SAVED_VIRTUALIZER_STRENGTH_KEY = "com.parabola.player_feature.SAVED_VIRTUALIZER_STRENGTH_KEY";
    private static final String SAVED_EQ_ENABLED_KEY = "com.parabola.player_feature.SAVED_EQ_ENABLED_KEY";
    private static final String SAVED_EQ_BAND_ENABLED_KEY_FORMAT = "com.parabola.player_feature.SAVED_EQ_BAND_ENABLED_KEY_FORMAT%d";


    PlayerSettingSaver(Context context) {
        this.prefs = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
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

    void setRepeatMode(boolean enable) {
        prefs.edit()
                .putBoolean(REPEAT_MODE_KEY, enable)
                .apply();
    }

    boolean isRepeatModeEnabled() {
        return prefs.getBoolean(REPEAT_MODE_KEY, false);
    }


    private static final String SAVED_PLAYLIST_DELIMITER = ";";

    //TODO посмотреть время выполнения, возможно стоит оптимизировать
    void setSavedPlaylist(ConcatenatingMediaSource mediaSource, int currentWindowIndex) {
        long startTime = System.currentTimeMillis();
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < mediaSource.getSize(); i++) {
            str.append((int) mediaSource.getMediaSource(i).getTag());

            if (i != mediaSource.getSize() - 1) {
                str.append(SAVED_PLAYLIST_DELIMITER);
            }
        }

        prefs.edit()
                .putString(SAVED_PLAYLIST_KEY, str.toString())
                .putInt(SAVED_PLAYLIST_POSITION_KEY, currentWindowIndex)
                .apply();

        long endTime = System.currentTimeMillis();
        Log.d(LOG_TAG, "SAVE PLAYLIST WITH SIZE " + mediaSource.getSize() + " AT " + (endTime - startTime) + " MS");
    }

    void setCurrentWindowIndex(int currentWindowIndex) {
        prefs.edit()
                .putInt(SAVED_PLAYLIST_POSITION_KEY, currentWindowIndex)
                .apply();
    }

    List<Integer> getSavedPlaylist() {
        long startTime = System.currentTimeMillis();

        String savedString = prefs.getString(SAVED_PLAYLIST_KEY, "");
        if (savedString.isEmpty()) {
            return Collections.emptyList();
        }

        String[] idsStr = savedString.split(SAVED_PLAYLIST_DELIMITER);
        List<Integer> ids = new ArrayList<>();

        for (int i = 0; i < idsStr.length; i++) {
            ids.add(Integer.parseInt(idsStr[i]));
        }

        long endTime = System.currentTimeMillis();
        Log.d(LOG_TAG, "GET SAVED PLAYLIST WITH SIZE " + ids.size() + " AT " + (endTime - startTime) + " MS");

        return ids;
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


    float getSavedPlaybackSpeed() {
        return prefs.getFloat(SAVED_PLAYBACK_SPEED_KEY, 1.0f);
    }

    void setPlaybackSpeed(float speed) {
        prefs.edit()
                .putFloat(SAVED_PLAYBACK_SPEED_KEY, speed)
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
