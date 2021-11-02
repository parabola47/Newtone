package com.parabola.newtone.ui.fragment.start;


import static com.parabola.newtone.util.AndroidTool.getStyledColor;
import static java.util.Objects.requireNonNull;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.ListPopupWindowAdapter;
import com.parabola.newtone.adapter.StartFragmentPagerAdapter;
import com.parabola.newtone.databinding.FragmentStartBinding;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.StartPresenter;
import com.parabola.newtone.mvp.view.StartView;
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver;
import com.parabola.newtone.ui.fragment.Scrollable;
import com.parabola.newtone.util.OnTabSelectedAdapter;

import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class StartFragment extends MvpAppCompatFragment
        implements StartView {
    private static final String LOG_TAG = StartFragment.class.getSimpleName();

    @InjectPresenter StartPresenter presenter;

    private FragmentStartBinding binding;

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
        binding = FragmentStartBinding.inflate(inflater, container, false);

        fragmentPagerAdapter = new StartFragmentPagerAdapter(requireContext(), getChildFragmentManager());
        binding.fragmentPager.setAdapter(fragmentPagerAdapter);
        setupTabLayout();
        binding.fragmentPager.setOffscreenPageLimit(binding.tabLayout.getTabCount());
        defaultTabIconTint = ContextCompat.getColor(requireContext(), R.color.colorTabIconTintDefault);

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

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.requestPermissionPanel.requestPermissionBtn
                .setOnClickListener(v -> presenter.onClickRequestPermission());
    }

    @Override
    public void setPermissionPanelVisibility(boolean visible) {
        binding.requestPermissionPanel.getRoot().setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void goToTab(int tabNumber, boolean smoothScroll) {
        binding.fragmentPager.setCurrentItem(tabNumber, smoothScroll);
    }

    public void scrollOnTabTrackToCurrentTrack() {
        TabTrackFragment tabTrackFragment = (TabTrackFragment) fragmentPagerAdapter.getItem(2);
        tabTrackFragment.scrollToCurrentTrack();
    }

    private int selectedTabIconTint;
    private int defaultTabIconTint;

    private void setupTabLayout() {
        binding.tabLayout.setupWithViewPager(binding.fragmentPager);

        buildTabItem(binding.tabLayout, 0, R.string.tab_artists, R.drawable.ic_artist);
        buildTabItem(binding.tabLayout, 1, R.string.tab_albums, R.drawable.ic_album);
        buildTabItem(binding.tabLayout, 2, R.string.tab_tracks, R.drawable.ic_clef);
        buildTabItem(binding.tabLayout, 3, R.string.tab_playlists, R.drawable.ic_playlist);

        binding.tabLayout.addOnTabSelectedListener(new OnTabSelectedAdapter() {
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                ((Scrollable) fragmentPagerAdapter.getItem(tab.getPosition())).smoothScrollToTop();
            }
        });

        binding.fragmentPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float offset, int positionOffsetPixels) {
                for (int i = 0; i < binding.tabLayout.getTabCount(); i++) {
                    float tabOffset = 0f;
                    if (i == position)
                        tabOffset = 1 - offset;
                    else if (i == position + 1 && position + 1 < binding.tabLayout.getTabCount())
                        tabOffset = offset;

                    refreshTabColor(requireNonNull(binding.tabLayout.getTabAt(i)), tabOffset);
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

        //при долгом удержании на табе треков открывать контекстное меню
        if (tabIndex == 2) {
            tab.view.setOnLongClickListener(view -> {
                showTabTrackContextMenu();
                return true;
            });
        }

        ((TextView) layout.findViewById(R.id.title)).setText(tabTitleResId);
        ((ImageView) layout.findViewById(R.id.icon)).setImageResource(tabIconResId);

        tab.setCustomView(layout);
    }

    private void showTabTrackContextMenu() {
        ListPopupWindowAdapter menuAdapter = new ListPopupWindowAdapter(requireContext(), R.menu.tab_track_menu);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.tab_track_menu_title)
                .setAdapter(menuAdapter, (d, which) ->
                        handleSelectedMenu(menuAdapter.getItem(which)))
                .create();
        getLifecycle().addObserver(new DialogDismissLifecycleObserver(dialog));
        dialog.show();
    }

    private void handleSelectedMenu(MenuItem selectedMenuItem) {
        switch (selectedMenuItem.getItemId()) {
            case R.id.shuffle_all:
                presenter.onClickMenuShuffleAll();
                break;
            case R.id.excluded_folders:
                presenter.onClickMenuExcludedFolders();
                break;
        }
    }

    public Fragment getCurrentSelectedFragment() {
        return fragmentPagerAdapter.getItem(binding.fragmentPager.getCurrentItem());
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
