package com.parabola.newtone.ui.dialog.fx;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.parabola.newtone.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moxy.MvpAppCompatDialogFragment;

import static java.util.Objects.requireNonNull;

public final class AudioEffectsDialog extends MvpAppCompatDialogFragment {

    private AudioEffectsPagerAdapter audioEffectsPagerAdapter;

    @BindView(R.id.tabs) TabLayout tabLayout;
    @BindView(R.id.audio_effects_pager) ViewPager audioEffectsPager;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.dialog_audio_effects, container, false);
        ButterKnife.bind(this, layout);

        audioEffectsPagerAdapter = new AudioEffectsPagerAdapter(getChildFragmentManager());
        audioEffectsPager.setAdapter(audioEffectsPagerAdapter);
        audioEffectsPager.setOffscreenPageLimit(audioEffectsPagerAdapter.getCount());
        tabLayout.setupWithViewPager(audioEffectsPager);

        requireNonNull(tabLayout.getTabAt(0)).setIcon(R.drawable.fx_eq_icon);
        requireNonNull(tabLayout.getTabAt(1)).setIcon(R.drawable.fx_ic_tune);

        //берём старые фрагменты, если экран не создаётся с нуля
        if (savedInstanceState != null) {
            Fragment[] tabFragments = new Fragment[2];
            for (Fragment fragment : getChildFragmentManager().getFragments()) {
                if (fragment instanceof FxEqualizerFragment) tabFragments[0] = fragment;
                if (fragment instanceof FxAudioSettingsFragment) tabFragments[1] = fragment;
            }
            audioEffectsPagerAdapter.initTabsFragments(tabFragments);
        }

        return layout;
    }

    @OnClick(R.id.cancel)
    public void onClickCancel() {
        dismiss();
    }

    public static class AudioEffectsPagerAdapter extends FragmentPagerAdapter {
        private final Fragment[] fragments = new Fragment[TABS_COUNT];
        private static final int TABS_COUNT = 2;


        public AudioEffectsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }


        void initTabsFragments(Fragment[] tabFragments) {
            if (tabFragments.length != TABS_COUNT)
                throw new IllegalArgumentException("Size of array tabFragments is " + tabFragments.length + ". It must be " + TABS_COUNT);

            System.arraycopy(tabFragments, 0, this.fragments, 0, this.fragments.length);
        }

        @Override
        @NonNull
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (fragments[0] == null) fragments[0] = new FxEqualizerFragment();
                    return fragments[0];
                case 1:
                    if (fragments[1] == null) fragments[1] = new FxAudioSettingsFragment();
                    return fragments[1];
                default:
                    throw new IllegalArgumentException("Fragment on position " + position + " not exists");
            }
        }

        @Override
        public int getCount() {
            return fragments.length;
        }
    }

}
