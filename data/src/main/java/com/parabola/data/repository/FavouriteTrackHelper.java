package com.parabola.data.repository;

import android.content.SharedPreferences;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class FavouriteTrackHelper {

    private static final String PREFS_FAVOURITES_SET_KEY = "FAVOURITES_SET";

    private final SharedPreferences prefs;

    private final SparseArray<Long> favourites;

    FavouriteTrackHelper(SharedPreferences prefs) {
        this.prefs = prefs;
        this.favourites = getArrayPref();
    }

    //первое значение(int) - id трека, второе(long) - время добавления в избранное,
    //полученное с помошью System.currentTimeMillis()
    List<Map.Entry<Integer, Long>> getFavourites() {
        List<Map.Entry<Integer, Long>> favourites = new ArrayList<>(this.favourites.size());

        for (int i = 0; i < this.favourites.size(); i++) {
            favourites.add(new AbstractMap.SimpleEntry<>(this.favourites.keyAt(i), this.favourites.valueAt(i)));
        }

        return favourites;
    }

    boolean isFavourite(int trackId) {
        return favourites.get(trackId) != null;
    }

    Long getFavouriteTimeStamp(int trackId) {
        return favourites.get(trackId);
    }

    void makeFavourite(int trackId) {
        favourites.put(trackId, System.currentTimeMillis());
        saveArrayPref();
    }

    void makeNotFavourite(int trackId) {
        favourites.remove(trackId);
        saveArrayPref();
    }

    private void saveArrayPref() {
        JSONArray json = new JSONArray();
        StringBuffer data = new StringBuffer().append("[");
        for (int i = 0; i < favourites.size(); i++) {
            data.append("{")
                    .append("\"key\": ")
                    .append(favourites.keyAt(i)).append(",")
                    .append("\"order\": ")
                    .append(favourites.valueAt(i))
                    .append("},");
            json.put(data);
        }
        data.append("]");

        prefs.edit()
                .putString(PREFS_FAVOURITES_SET_KEY, favourites.size() == 0 ? null : data.toString())
                .apply();
    }

    private SparseArray<Long> getArrayPref() {
        String json = prefs.getString(PREFS_FAVOURITES_SET_KEY, null);

        SparseArray<Long> favourites = new SparseArray<>();
        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    favourites.put(item.getInt("key"), item.getLong("order"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return favourites;
    }

}
