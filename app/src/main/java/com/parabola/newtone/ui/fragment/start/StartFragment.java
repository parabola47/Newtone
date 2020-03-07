package com.parabola.newtone.ui.fragment.start;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.google.android.material.tabs.TabLayout;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.StartFragmentPagerAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.StartPresenter;
import com.parabola.newtone.mvp.view.StartView;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static java.util.Objects.requireNonNull;

public final class StartFragment extends MvpAppCompatFragment
        implements StartView {
    private static final String TAG = StartFragment.class.getSimpleName();

    @BindView(R.id.fragment_pager) ViewPager fragmentPager;
    @BindView(R.id.tabs) TabLayout tabLayout;
    @BindView(R.id.requestPermissionPanel) ViewGroup requestPermissionPanel;

    @InjectPresenter StartPresenter presenter;

    private FragmentPagerAdapter fragmentPagerAdapter;


    public StartFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_start, container, false);
        ButterKnife.bind(this, layout);

        fragmentPagerAdapter = new StartFragmentPagerAdapter(requireContext(), getChildFragmentManager());
        fragmentPager.setAdapter(fragmentPagerAdapter);
        setupTabLayout();
        fragmentPager.setOffscreenPageLimit(tabLayout.getTabCount());

        return layout;
    }

    @Override
    public void setPermissionPanelVisibility(boolean visible) {
        requestPermissionPanel.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.requestPermissionBtn)
    public void onClickRequestPermission() {
        presenter.onClickRequestPermission();
    }

    public void goToTab(int tabNumber, boolean smoothScroll) {
        fragmentPager.setCurrentItem(tabNumber, smoothScroll);
    }

    public void scrollOnTabTrackToCurrentTrack() {
        TabTrackFragment tabTrackFragment = (TabTrackFragment) ((StartFragmentPagerAdapter) fragmentPagerAdapter).selectItem(2);
        tabTrackFragment.scrollToCurrentTrack();
    }

    @BindColor(R.color.colorTabIconTintSelected) int selectedTabIconTint;
    @BindColor(R.color.colorTabIconTintDefault) int defaultTabIconTint;

    private void setupTabLayout() {
        tabLayout.setupWithViewPager(fragmentPager);

        buildTabItem(tabLayout, 0, R.string.tab_artists, R.drawable.ic_artist);
        buildTabItem(tabLayout, 1, R.string.tab_albums, R.drawable.ic_album);
        buildTabItem(tabLayout, 2, R.string.tab_tracks, R.drawable.ic_clef);
        buildTabItem(tabLayout, 3, R.string.tab_playlists, R.drawable.ic_playlist);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View tabView = requireNonNull(tab.getCustomView());
                ((ImageView) tabView.findViewById(R.id.icon)).setColorFilter(selectedTabIconTint);
                ((TextView) tabView.findViewById(R.id.title)).setTextColor(selectedTabIconTint);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View tabView = requireNonNull(tab.getCustomView());
                ((ImageView) tabView.findViewById(R.id.icon)).setColorFilter(defaultTabIconTint);
                ((TextView) tabView.findViewById(R.id.title)).setTextColor(defaultTabIconTint);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void buildTabItem(TabLayout tabLayout, int tabIndex, int tabTitleResId, int tabIconResId) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(requireContext())
                .inflate(R.layout.tab_item_view, tabLayout, false);
        TabLayout.Tab tab = requireNonNull(tabLayout.getTabAt(tabIndex));

        ((TextView) layout.findViewById(R.id.title))
                .setText(tabTitleResId);
        ((TextView) layout.findViewById(R.id.title))
                .setTextColor(tab.isSelected() ? selectedTabIconTint : defaultTabIconTint);
        ((ImageView) layout.findViewById(R.id.icon))
                .setImageDrawable(ContextCompat.getDrawable(requireContext(), tabIconResId));
        ((ImageView) layout.findViewById(R.id.icon))
                .setColorFilter(tab.isSelected() ? selectedTabIconTint : defaultTabIconTint);

        tab.setCustomView(layout);
    }

    public Fragment getCurrentSelectedFragment() {
        return fragmentPagerAdapter.getItem(fragmentPager.getCurrentItem());
    }

    @ProvidePresenter
    public StartPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new StartPresenter(appComponent);
    }

    public void scrollToArtistInTab(int artistId) {
        ((TabArtistFragment) fragmentPagerAdapter.getItem(0))
                .scrollTo(artistId);
    }

    public void scrollToAlbumInTab(int albumId) {
        ((TabAlbumFragment) fragmentPagerAdapter.getItem(1))
                .scrollTo(albumId);
    }
}
