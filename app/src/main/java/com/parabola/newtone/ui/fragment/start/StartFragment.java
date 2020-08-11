package com.parabola.newtone.ui.fragment.start;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.StartFragmentPagerAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.StartPresenter;
import com.parabola.newtone.mvp.view.StartView;
import com.parabola.newtone.ui.fragment.Scrollable;
import com.parabola.newtone.util.OnTabSelectedAdapter;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.parabola.newtone.util.AndroidTool.getStyledColor;
import static java.util.Objects.requireNonNull;

public final class StartFragment extends MvpAppCompatFragment
        implements StartView {
    private static final String LOG_TAG = StartFragment.class.getSimpleName();

    @BindView(R.id.fragment_pager) ViewPager fragmentPager;
    @BindView(R.id.tabs) TabLayout tabLayout;
    @BindView(R.id.requestPermissionPanel) ViewGroup requestPermissionPanel;

    @InjectPresenter StartPresenter presenter;

    private StartFragmentPagerAdapter fragmentPagerAdapter;


    public StartFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        selectedTabIconTint = getStyledColor(context, R.attr.colorPrimary);
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

        //берём старые фрагменты, если экран не создаётся с нуля
        if (savedInstanceState != null) {
            Fragment[] tabFragments = new Fragment[4];
            for (Fragment fragment : getChildFragmentManager().getFragments()) {
                if (fragment instanceof TabArtistFragment) tabFragments[0] = fragment;
                if (fragment instanceof TabAlbumFragment) tabFragments[1] = fragment;
                if (fragment instanceof TabTrackFragment) tabFragments[2] = fragment;
                if (fragment instanceof TabPlaylistFragment) tabFragments[3] = fragment;
            }
            fragmentPagerAdapter.initTabsFragments(tabFragments);
        }

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
        TabTrackFragment tabTrackFragment = (TabTrackFragment) fragmentPagerAdapter.getItem(2);
        tabTrackFragment.scrollToCurrentTrack();
    }

    private int selectedTabIconTint;
    @BindColor(R.color.colorTabIconTintDefault) int defaultTabIconTint;

    private void setupTabLayout() {
        tabLayout.setupWithViewPager(fragmentPager);

        buildTabItem(tabLayout, 0, R.string.tab_artists, R.drawable.ic_artist);
        buildTabItem(tabLayout, 1, R.string.tab_albums, R.drawable.ic_album);
        buildTabItem(tabLayout, 2, R.string.tab_tracks, R.drawable.ic_clef);
        buildTabItem(tabLayout, 3, R.string.tab_playlists, R.drawable.ic_playlist);

        tabLayout.addOnTabSelectedListener(new OnTabSelectedAdapter() {
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                ((Scrollable) fragmentPagerAdapter.getItem(tab.getPosition())).smoothScrollToTop();
            }
        });

        fragmentPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float offset, int positionOffsetPixels) {
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    float tabOffset = 0f;
                    if (i == position)
                        tabOffset = 1 - offset;
                    else if (i == position + 1 && position + 1 < tabLayout.getTabCount())
                        tabOffset = offset;

                    refreshTabColor(requireNonNull(tabLayout.getTabAt(i)), tabOffset);
                }
            }

            private void refreshTabColor(TabLayout.Tab tab, float offset) {
                int color = ColorUtils.blendARGB(defaultTabIconTint, selectedTabIconTint, offset);

                View tabView = requireNonNull(tab.getCustomView());
                ((ImageView) tabView.findViewById(R.id.icon)).setColorFilter(color);
                ((TextView) tabView.findViewById(R.id.title)).setTextColor(color);
            }
        });
    }

    private void buildTabItem(TabLayout tabLayout, int tabIndex, int tabTitleResId, int tabIconResId) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(requireContext())
                .inflate(R.layout.tab_item_view, tabLayout, false);
        TabLayout.Tab tab = requireNonNull(tabLayout.getTabAt(tabIndex));

        ((TextView) layout.findViewById(R.id.title)).setText(tabTitleResId);
        ((ImageView) layout.findViewById(R.id.icon)).setImageResource(tabIconResId);

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
