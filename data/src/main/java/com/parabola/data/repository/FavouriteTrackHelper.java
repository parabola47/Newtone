package com.parabola.data.repository;

import android.content.SharedPreferences;

import com.parabola.domain.model.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

final class FavouriteTrackHelper {

    private static final String PREFS_FAVOURITES_KEY = "com.parabola.data.FavouriteTrackHelper.FAVOURITES";
    private static final String SAVED_ITEMS_SEPARATOR = ";";


    private final SharedPreferences prefs;

    private final List<Integer> favourites;


    FavouriteTrackHelper(SharedPreferences prefs) {
        this.prefs = prefs;
        this.favourites = getArrayPref();
    }

    //первое значение(int) - id трека, второе(long) - время добавления в избранное,
    //полученное с помошью System.currentTimeMillis()
    List<Integer> getFavourites() {
        return favourites;
    }


    boolean isFavourite(Integer trackId) {
        return favourites.contains(trackId);
    }


    void makeFavourite(Integer trackId) {
        favourites.add(0, trackId);
        saveArrayPref();
    }

    void makeNotFavourite(Integer trackId) {
        favourites.remove(trackId);
        saveArrayPref();
    }


    void moveFavouriteTrack(int positionFrom, int positionTo) {
        if (positionFrom <= positionTo)
            Collections.rotate(favourites.subList(positionFrom, positionTo + 1), -1);
        else
            Collections.rotate(favourites.subList(positionTo, positionFrom + 1), 1);
        saveArrayPref();
    }


    void invalidateFavourites(List<Track> extractedTracks) {
        if (favourites.size() == extractedTracks.size())
            return;

        for (int i = 0; i < favourites.size(); i++) {
            if (!favourites.get(i).equals(extractedTracks.get(i).getId())) {
                favourites.remove(favourites.get(i));
            }
            saveArrayPref();
        }
    }

    private void saveArrayPref() {
        String data = favourites.stream()
                .map(Object::toString)
                .collect(Collectors.joining(SAVED_ITEMS_SEPARATOR));

        prefs.edit()
                .putString(PREFS_FAVOURITES_KEY, data)
                .apply();
    }

    private List<Integer> getArrayPref() {
        String data = prefs.getString(PREFS_FAVOURITES_KEY, "");
        List<Integer> favourites = new ArrayList<>();

        if (!data.isEmpty()) {
            for (String item : data.split(SAVED_ITEMS_SEPARATOR)) {
                favourites.add(Integer.valueOf(item));
            }
        }

        return favourites;
    }

}
