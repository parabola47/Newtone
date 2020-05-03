package com.parabola.domain.utils;

import com.parabola.domain.model.Track;

import java.util.List;

public final class TracklistTool {

    private TracklistTool() {
        throw new IllegalAccessError();
    }


    public static boolean isTracklistsIdentical(List<Track> firstList, List<Track> secondList) {
        if (firstList.size() != secondList.size()) {
            return false;
        }

        for (int i = 0; i < firstList.size(); i++) {
            if (firstList.get(i).getId() != secondList.get(i).getId())
                return false;
        }

        return true;
    }

}
