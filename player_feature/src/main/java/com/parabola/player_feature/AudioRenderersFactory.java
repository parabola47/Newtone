package com.parabola.player_feature;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

class AudioRenderersFactory implements RenderersFactory {
    private final Context context;

    public AudioRenderersFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Renderer[] createRenderers(
            @NonNull Handler eventHandler,
            @NonNull VideoRendererEventListener videoRendererEventListener,
            @NonNull AudioRendererEventListener audioRendererEventListener,
            @NonNull TextOutput textRendererOutput,
            @NonNull MetadataOutput metadataRendererOutput,
            @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        MediaCodecAudioRenderer audioRenderer = new MediaCodecAudioRenderer(context,
                MediaCodecSelector.DEFAULT, eventHandler, audioRendererEventListener);

        return new Renderer[]{audioRenderer};
    }
}
