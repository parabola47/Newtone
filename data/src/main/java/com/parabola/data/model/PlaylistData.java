package com.parabola.data.model;

import androidx.annotation.Nullable;

import com.parabola.domain.model.Playlist;

import java.util.List;

public final class PlaylistData implements Playlist {
    public int id;
    public String title;
    public long dateAddingTimestamp;
    public List<TrackItem> playlistTracks;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public long getDateAddingTimestamp() {
        return dateAddingTimestamp;
    }

    @Override
    public List<TrackItem> getPlaylistTracks() {
        return playlistTracks;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Playlist)) {
            return false;
        }
        return equals((Playlist) obj);
    }

    public static class TrackItemData implements TrackItem {
        public int trackId;
        public long additionDate;

        @Override
        public int getTrackId() {
            return trackId;
        }

        @Override
        public long getAdditionDate() {
            return additionDate;
        }
    }
}
