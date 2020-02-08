package com.parabola.domain.utils;

import com.parabola.domain.model.Track;

public final class EmptyItems {

    public static final Track NO_TRACK = new Track() {
        public int getId() {
            return -1;
        }

        public String getTitle() {
            return "";
        }

        public long getDateAddingTimestamp() {
            return 0;
        }

        public int getAlbumId() {
            return -1;
        }

        public String getAlbumTitle() {
            return "";
        }

        @Override
        public String getArtLink() {
            return "";
        }

        public int getArtistId() {
            return -1;
        }

        public String getArtistName() {
            return "";
        }

        public long getDurationMs() {
            return 0;
        }

        public int getPositionInCd() {
            return 0;
        }

        public String getFilePath() {
            return "";
        }

        public boolean isFavourite() {
            return false;
        }

        public Long getFavouriteTimestamp() {
            return null;
        }
    };

}
