package com.parabola.player_feature;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

class VolumeSettingsObserver extends ContentObserver {
    private static final String LOG_TAG = VolumeSettingsObserver.class.getSimpleName();

    private final Context context;

    private float previousVolume;
    private OnVolumeChangeListener listener;

    public VolumeSettingsObserver(Context context, Handler handler) {
        super(handler);
        this.context = context;

        AudioManager audio = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }


    public int getMaxVolume() {
        return getAudio().getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public void setCurrentVolume(int volume) {
        getAudio().setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    public int getCurrentVolume() {
        return getAudio().getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onChange(boolean selfChange) {
        int currentVolume = getCurrentVolume();

        if (previousVolume != currentVolume) {
            previousVolume = currentVolume;
            if (listener != null) {
                listener.onVolumeChange(currentVolume);
            }
        }
    }

    public void setOnVolumeChangeLister(OnVolumeChangeListener listener) {
        this.listener = listener;
    }

    private AudioManager getAudio() {
        return (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    interface OnVolumeChangeListener {
        void onVolumeChange(int volume);
    }
}
