package com.parabola.domain.utils;

import com.parabola.domain.model.Track;

public final class EmptyItems {

    private EmptyItems() {
        throw new IllegalAccessError();
    }


    public static final Track NO_TRACK = new Track() {
        public int getId() {
            return -1;
        }

        public String getTitle() {
            return "";
        }

        public int getAlbumId() {
            return -1;
        }

        public String getAlbumTitle() {
            return "";
        }

        public Object getArtImage() {
            return null;
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

        @Override
        public long getDateAdded() {
            return 0;
        }

        public int getPositionInCd() {
            return 0;
        }

        public int getGenreId() {
            return -1;
        }

        public String getGenreName() {
            return "<unknown>";
        }

        public int getYear() {
            return 1970;
        }

        public int getFileSize() {
            return 0;
        }

        public int getBitrate() {
            return 0;
        }

        public int getSampleRate() {
            return 0;
        }

        public String getFilePath() {
            return "";
        }

        public boolean isFavourite() {
            return false;
        }
    };

}
