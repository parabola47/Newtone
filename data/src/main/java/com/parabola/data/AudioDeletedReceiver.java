package com.parabola.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AudioDeletedReceiver extends BroadcastReceiver {

    public static final String ACTION_AUDIO_REMOVED_FROM_STORAGE = "com.parabola.data.AUDIO_REMOVED_FROM_STORAGE";
    public static final String TRACK_ID_ARG = "track_id";


    private DeleteListener listener;

    public void setListener(DeleteListener listener) {
        this.listener = listener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null)
            return;

        if (ACTION_AUDIO_REMOVED_FROM_STORAGE.equals(intent.getAction())) {
            if (listener != null)
                listener.onDeleteTrack(intent.getIntExtra(TRACK_ID_ARG, -1));
        }
    }

    public interface DeleteListener {
        void onDeleteTrack(int trackId);
    }

}
