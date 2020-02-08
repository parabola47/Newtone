package com.parabola.newtone.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.parabola.newtone.R;
import com.parabola.newtone.ui.fragment.start.TabAlbumFragment;
import com.parabola.newtone.ui.fragment.start.TabArtistFragment;
import com.parabola.newtone.ui.fragment.start.TabPlaylistFragment;
import com.parabola.newtone.ui.fragment.start.TabTrackFragment;

public final class StartFragmentPagerAdapter extends FragmentPagerAdapter {

    private static final int TABS_COUNT = 4;

    private final Fragment[] tabFragments = new Fragment[4];
    private final Context context;


    public StartFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.context = context;
    }

    public Fragment selectItem(int position) {
        return tabFragments[position];
    }

    @Override
    @NonNull
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (tabFragments[0] == null) tabFragments[0] = new TabArtistFragment();
                return tabFragments[0];
            case 1:
                if (tabFragments[1] == null) tabFragments[1] = new TabAlbumFragment();
                return tabFragments[1];
            case 2:
                if (tabFragments[2] == null) tabFragments[2] = new TabTrackFragment();
                return tabFragments[2];
            case 3:
                if (tabFragments[3] == null) tabFragments[3] = new TabPlaylistFragment();
                return tabFragments[3];
            default:
                throw new IllegalArgumentException("Fragment on position " + position + " not exists");
        }
    }

    @Override
    public int getCount() {
        return TABS_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: return context.getString(R.string.tab_artists);
            case 1: return context.getString(R.string.tab_albums);
            case 2: return context.getString(R.string.tab_tracks);
            case 3: return context.getString(R.string.tab_playlists);
            default: throw new IllegalArgumentException("Position " + position + " is not correct");
        }
    }

}
