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
import com.parabola.newtone.ui.dialog.BaseDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class AudioEffectsDialog extends BaseDialogFragment {

    private FragmentPagerAdapter audioEffectsPagerAdapter;

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


        tabLayout.getTabAt(0).setIcon(R.drawable.fx_eq_icon);
        tabLayout.getTabAt(1).setIcon(R.drawable.fx_ic_tune);

        return layout;
    }

    @OnClick(R.id.cancel)
    public void onClickCancel() {
        dismiss();
    }

    public static class AudioEffectsPagerAdapter extends FragmentPagerAdapter {
        private Fragment[] fragments;

        public AudioEffectsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            fragments = new Fragment[2];
            fragments[0] = new FxEqualizerFragment();
            fragments[1] = new FxAudioSettingsFragment();
        }

        @Override
        @NonNull
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }
    }

}
