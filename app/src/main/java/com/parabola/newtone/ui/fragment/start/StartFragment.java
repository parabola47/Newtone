package com.parabola.newtone.ui.fragment.start;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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
import com.parabola.newtone.mvp.presenter.StartPresenter;
import com.parabola.newtone.mvp.view.StartView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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


    private void setupTabLayout() {
        tabLayout.setupWithViewPager(fragmentPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.tab_ic_mic);
        tabLayout.getTabAt(1).setIcon(R.drawable.tab_ic_album);
        tabLayout.getTabAt(2).setIcon(R.drawable.tab_ic_clef);
        tabLayout.getTabAt(3).setIcon(R.drawable.tab_ic_playlist);
    }

    public Fragment getCurrentSelectedFragment() {
        return fragmentPagerAdapter.getItem(fragmentPager.getCurrentItem());
    }

    @ProvidePresenter
    public StartPresenter providePresenter() {
        return new StartPresenter(MainApplication.getComponent());
    }

}
